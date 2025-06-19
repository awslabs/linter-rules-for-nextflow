#! /bin/bash

# Parse command line arguments
FAIL_ON_VIOLATIONS=false
RULESET=""

while [[ $# -gt 0 ]]; do
  case $1 in
    --fail-on-violations)
      FAIL_ON_VIOLATIONS=true
      shift
      ;;
    -*)
      echo "Unknown option $1"
      echo "Usage: $0 [--fail-on-violations] [ruleset]"
      echo "  --fail-on-violations: Exit with code 1 if violations are found"
      echo "  ruleset: Ruleset to use (default: healthomics)"
      exit 1
      ;;
    *)
      RULESET="$1"
      shift
      ;;
  esac
done

# Set default ruleset if not provided
RULESET="${RULESET:-healthomics}"
echo "Checking with ruleset: ${RULESET}"

# Run CodeNarc and capture output
OUTPUT=$(java -Dorg.slf4j.simpleLogger.defaultLogLevel=error \
  -classpath linter-rules.jar:CodeNarc-3.3.0-all.jar:slf4j-api-1.7.36.jar:slf4j-simple-1.7.36.jar \
  org.codenarc.CodeNarc \
  -report=text:stdout \
  -rulesetfiles=rulesets/"${RULESET}".xml \
  -basedir=/data \
  -includes=**/*.nf 2>&1)

# Print the output
echo "$OUTPUT"

# Check for violations if --fail-on-violations flag is set
if [ "$FAIL_ON_VIOLATIONS" = true ]; then
  if echo "$OUTPUT" | grep -q "FilesWithViolations=[1-9]"; then
    echo "Violations detected. Exiting with code 1."
    exit 1
  fi
fi