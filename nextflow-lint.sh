#!/bin/bash

# Nextflow Linter wrapper script
# Usage: nextflow-lint [directory]
# If no directory is provided, uses current working directory

# Determine the directory to lint
if [ $# -eq 0 ]; then
    # No argument provided, use current working directory
    LINT_DIR="$PWD"
    echo "Linting current directory: $LINT_DIR"
else
    # Use provided directory argument
    LINT_DIR="$1"
    
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

# Run the linter container
"$CONTAINER_CMD" run -v "$LINT_DIR:/data" linter-rules-for-nextflow
