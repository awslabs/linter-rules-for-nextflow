# HealthOmics CodeNarc Rules for Nextflow Scripts

Because Nextflow DSL2 is a Groovy DSL it is possible to parse it syntactically with a Groovy parser and inspect it's
Abstract Syntax Tree using a Groovy static analysis tool (linter) like [CodeNarc](https://codenarc.org/).

This means that we can create custom CodeNarc rules that inspect Nextflow scripts (or config files) to detect and report
potential issues. 

## Build
To build this library into a JAR file you can use

```shell
./gradlew jar
```

## Run
 To run lint checks against Nextflow files you need to have access to the CodeNarc jar as well as an SLF4J implementation
for logging.

To download CodeNarc along with its dependent Groovy libs
```shell
wget https://repo1.maven.org/maven2/org/codenarc/CodeNarc/3.3.0/CodeNarc-3.3.0-all.jar
```

Download SLF4J API and an implementation such as SLF4J simple.
```shell
wget https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar
wget https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar
```

You will also need an installation of Java (at least version 1.8 but 11 or higher is preferable). To test for the existence
of Java you can run
```shell
java -version
```

If there is no java installation then using [SDKMAN](https://sdkman.io/) is one of the simpler options for installation
that also supports having multiple versions if that is required.

Assuming your have built this library (see "Build" above) you can run the following command.

```shell
java  -Dorg.slf4j.simpleLogger.defaultLogLevel=error \
  -classpath ./healthomics-nextflow-rules/build/libs/healthomics-nextflow-0.1.jar:CodeNarc-3.3.0-all.jar:slf4j-api-1.7.36.jar:slf4j-simple-1.7.36.jar \
  org.codenarc.CodeNarc \
  -report=text:stdout \
  -rulesetfiles=rulesets/healthomics.xml \
  -includes=**/**.nf
```

If you jar files are in different locations adjust the `-classpath` line as appropriate. All lines after 
`org.codenarc.CodeNarc` are CodeNarc [command line parameters](https://codenarc.org/codenarc-command-line.html#codenarc-command-line-parameters)

Here we have configured CodeNarc to use the rules in `rulesets/healthomics.xml` which is defined in this library and
contained in this libraries JAR file. The `-includes` parameter takes an Ant style pattern. This pattern will inspect
all `*.nf` files at the current location and in subdirectories.

## Development

### Add a rule

CodeNarc rules are typically implemented using the [visitor pattern](https://en.wikipedia.org/wiki/Visitor_pattern. Rules
that follow this pattern will extend the `org.codenarc.rule.AbstractAstVisitorRule` and declare a "companion" visitor
class that extends the `org.codenarc.rule.AbstractAstVisitor`.  The `AbstractAstVisitor` defines several methods beginning
with `visit` such as `visitMethodCallExpression(MethodCallExpression expression)`. The visitor class associated with the
rule will need to override the visit method (or methods) that are relevant to the code they wish to examine.

There is one visit method for each of the possible Groovy language expressions and statements. Unfortunately there are
dozens of Groovy expressions and statements which can make it difficult to know which your rule needs to override. It is
also not immediately obvious how Nextflow statements and expressions are semantically realized as Groovy. To help you decide,
this package includes a Java application (AstEchoCli) that will echo the Groovy Abstract Syntax Tree for any Nextflow script or other valid
Groovy or Groovy DSL. Consult the README.md in the `ast-echo` folder of this project for build and usage instructions.

TODO - information about expected rule structure

### Add a Test
TODO

### Update the Ruleset
TODO

## Docker
TODO