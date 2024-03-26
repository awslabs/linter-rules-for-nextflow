#! /bin/bash

RULESET="$1"
echo  "Checking with ruleset: ${RULESET:=healthomics}"

java  -Dorg.slf4j.simpleLogger.defaultLogLevel=error \
  -classpath linter-rules.jar:CodeNarc-3.3.0-all.jar:slf4j-api-1.7.36.jar:slf4j-simple-1.7.36.jar \
  org.codenarc.CodeNarc \
  -report=text:stdout \
  -rulesetfiles=rulesets/"${RULESET}".xml \
  -basedir=/data \
  -includes=**/*.nf