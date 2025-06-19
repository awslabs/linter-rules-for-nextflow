#!/bin/bash

# Nextflow Linter wrapper script
# Usage: nextflow-lint [--fail-on-violations] [directory]
# If no directory is provided, uses current working directory

# Parse command line arguments
FAIL_ON_VIOLATIONS=false
LINT_DIR=""
RULESET=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --fail-on-violations)
      FAIL_ON_VIOLATIONS=true
      shift
      ;;
    --ruleset)
      RULESET="$2"
      shift 2
      ;;
    --help|-h)
      echo "Usage: $0 [--fail-on-violations] [--ruleset RULESET] [directory]"
      echo "  --fail-on-violations: Exit with code 1 if violations are found"
      echo "  --ruleset RULESET: Specify ruleset (healthomics or general)"
      echo "  directory: Directory to lint (default: current directory)"
      exit 0
      ;;
    -*)
      echo "Unknown option $1"
      echo "Usage: $0 [--fail-on-violations] [--ruleset RULESET] [directory]"
      exit 1
      ;;
    *)
      LINT_DIR="$1"
      shift
      ;;
  esac
done

# Determine the directory to lint
if [ -z "$LINT_DIR" ]; then
    # No directory provided, use current working directory
    LINT_DIR="$PWD"
    echo "Linting current directory: $LINT_DIR"
else
    # Convert to absolute path if it's relative
    if [[ "$LINT_DIR" != /* ]]; then
        LINT_DIR="$PWD/$LINT_DIR"
    fi
    
    # Check if directory exists
    if [ ! -d "$LINT_DIR" ]; then
        echo "Error: Directory '$LINT_DIR' not found"
        exit 1
    fi
    
    echo "Linting directory: $LINT_DIR"
fi

# Find available container runtime
CONTAINER_CMD=""
for cmd in docker finch podman; do
    if command -v "$cmd" &> /dev/null; then
        CONTAINER_CMD="$cmd"
        break
    fi
done

# Check if any container runtime was found
if [ -z "$CONTAINER_CMD" ]; then
    echo "Error: No container runtime found. Please install docker, finch, or podman."
    exit 1
fi

echo "Using container runtime: $CONTAINER_CMD"

# Build container command arguments
CONTAINER_ARGS="-v $LINT_DIR:/data"

# Add environment variables for ruleset if specified
if [ -n "$RULESET" ]; then
    CONTAINER_ARGS="$CONTAINER_ARGS -e ruleset=$RULESET"
fi

# Run the linter container and capture output
if [ "$FAIL_ON_VIOLATIONS" = true ]; then
    # Capture output when we need to check for violations
    OUTPUT=$("$CONTAINER_CMD" run $CONTAINER_ARGS linter-rules-for-nextflow 2>&1)
    CONTAINER_EXIT_CODE=$?
    
    # Print the output
    echo "$OUTPUT"
    
    # Check for violations in the output
    if echo "$OUTPUT" | grep -q "FilesWithViolations=[1-9]"; then
        echo "Violations detected. Exiting with code 1."
        exit 1
    fi
    
    # Exit with container's exit code if no violations detected
    exit $CONTAINER_EXIT_CODE
else
    # Run normally without violation checking
    "$CONTAINER_CMD" run $CONTAINER_ARGS linter-rules-for-nextflow
    exit $?
fi
