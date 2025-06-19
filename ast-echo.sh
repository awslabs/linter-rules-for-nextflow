#!/bin/bash

# AST Echo wrapper script
# Usage: ast-echo <groovy-file>

# Check if input file is provided
if [ $# -eq 0 ]; then
    echo "Usage: ast-echo <groovy-file>"
    echo "Example: ast-echo main.nf"
    exit 1
fi

# Input file from command line argument
INPUT_FILE="$1"

# Check if input file exists
if [ ! -f "$INPUT_FILE" ]; then
    echo "Error: File '$INPUT_FILE' not found"
    exit 1
fi

# Find the AST Echo project directory (assuming script is run from project or subdirectory)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$HOME/devel/linter-rules-for-nextflow/ast-echo"

# Find the latest ast-echo jar file in the libs directory
JAR_FILE=$(find "$PROJECT_DIR/build/libs" -name "ast-echo-*.jar" -type f | sort -V | tail -n 1)

# Check if jar file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: AST Echo jar file not found in $PROJECT_DIR/build/libs/"
    echo "Please build the project first:"
    echo "  cd $PROJECT_DIR"
    echo "  ./gradlew clean jar"
    exit 1
fi

# Run the AST Echo utility
java -jar "$JAR_FILE" "$INPUT_FILE"
