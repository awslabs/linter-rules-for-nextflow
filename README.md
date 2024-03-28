[![Java CI with Gradle](https://github.com/awslabs/linter-rules-for-nextflow/actions/workflows/gradle.yml/badge.svg)](https://github.com/linter-rules-for-nextflow/actions/workflows/gradle.yml)


# Linter Rules for Nextflow

Linter Rules for Nextlfow is a collection of CodeNarc rules designed to lint [Nextflow](https://www.nextflow.io/) DSL scripts and
spot potential problems before a workflow script is run.

Because Nextflow DSL2 is a Groovy DSL it is possible to parse it syntactically with a Groovy parser and inspect it's
Abstract Syntax Tree using a Groovy static analysis tool (linter) like [CodeNarc](https://codenarc.org/).

This means that we can create custom CodeNarc rules that inspect Nextflow scripts (or config files) to detect and report
potential issues.

## License
This repository is distributed under the terms of the Apache 2 license. Full details are in the [LICENSE](./LICENSE) file.

## Build
To build this library you can use

```shell
./gradlew clean build
```

The resulting jar files will be found in `healthomics-nextflow-rules/build/libs/` and `ast-echo/build/libs`

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

You will also need an installation of Java (at least version 1.8 but 17 or higher is preferable). To test for the existence
of Java you can run
```shell
java -version
```

If there is no java installation then using [SDKMAN](https://sdkman.io/) is one of the simpler options for installation
that also supports having multiple versions if that is required.

Assuming your have built this library (see "Build" above) you can run the following command.

```shell
java  -Dorg.slf4j.simpleLogger.defaultLogLevel=error \
  -classpath ./linter-rules/build/libs/linter-rules-0.1.jar:CodeNarc-3.3.0-all.jar:slf4j-api-1.7.36.jar:slf4j-simple-1.7.36.jar \
  org.codenarc.CodeNarc \
  -report=text:stdout \
  -rulesetfiles=rulesets/healthomics.xml \
  -includes=**/**.nf
```

If your jar files are in different locations adjust the `-classpath` line as appropriate. All lines after 
`org.codenarc.CodeNarc` are CodeNarc [command line parameters](https://codenarc.org/codenarc-command-line.html#codenarc-command-line-parameters)

Here we have configured CodeNarc to use the rules in `rulesets/healthomics.xml` which is defined in this library and
contained in this libraries JAR file. The `-includes` parameter takes an Ant style pattern. This pattern will inspect
all `*.nf` files at the current location and in subdirectories.

### General NF Rules only
If you only wish to check general Nextflow lint rules and not include those specialized to AWS HealthOmics then you can
run:

```shell
java  -Dorg.slf4j.simpleLogger.defaultLogLevel=error \
  -classpath ./linter-rules/build/libs/linter-rules-0.1.jar:CodeNarc-3.3.0-all.jar:slf4j-api-1.7.36.jar:slf4j-simple-1.7.36.jar \
  org.codenarc.CodeNarc \
  -report=text:stdout \
  -rulesetfiles=rulesets/general.xml \
  -includes=**/**.nf
```

### Docker

A `Dockerfile` is provided for this project which will build an image that contains the scripts in `scripts/` and the
required JAR files. To build the container:

```shell
docker build -t linter-rules-for-nextflow .
```

The container is configured to (by default) run all rules herein against all Nextflow (`*.nf`) files found 
in the `data` volume. For example, to check the files in the `examples/` folder:

```shell
cd examples
docker run -v $PWD:/data linter-rules-for-nextflow
```

which will produce a report similar to:

```
CodeNarc Report - Jan 11, 2024, 8:29:21?PM

Summary: TotalFiles=1 FilesWithViolations=1 P1=3 P2=0 P3=0

File: example.nf
    Violation: Rule=ContainerUriIsEcrUri P=1 Line=2 Msg=[The container image URI 'ubuntu:latest' does not match the pattern '\d{12}\.dkr\.ecr\..+\.amazonaws.com/.*'. Replace with an ECR image URI.] Src=[container 'ubuntu:latest']
    Violation: Rule=ContainerUriIsEcrUri P=1 Line=15 Msg=[The container image URI 'ubuntu:latest' does not match the pattern '\d{12}\.dkr\.ecr\..+\.amazonaws.com/.*'. Replace with an ECR image URI.] Src=[container 'ubuntu:latest']
    Violation: Rule=PublishDirRule P=1 Line=17 Msg=[AWS HealthOmics requires the value of publishDir to be '/mnt/workflow/pubdir'. Please replace the current value] Src=[publishDir '/foo']

[CodeNarc (https://codenarc.org) v3.3.0]
CodeNarc completed: (p1=3; p2=0; p3=0) 1757ms
```

#### General Rules Only

If you only want to check the general rules using the container you can set the `ruleset` environment variable to `general`
as follows:

```shell
cd examples
docker run -v $PWD:/data -e ruleset=general linter-rules-for-nextflow
```

#### AST Echo

The container also contains the `ast-echo` application along with a script to run it (`echo-tree.sh`). For example:

```shell
docker run -v $PWD/examples:/data linter-rules-for-nextflow ./echo-tree.sh /data/example.nf
```

## Development

We currently use Groovy 3.0.18 and Java 17 for development.

### Add a rule

CodeNarc rules are typically implemented using the [visitor pattern](https://en.wikipedia.org/wiki/Visitor_pattern). Rules
that follow this pattern will extend the `org.codenarc.rule.AbstractAstVisitorRule` and declare a "companion" visitor
class that extends the `org.codenarc.rule.AbstractAstVisitor`.  The `AbstractAstVisitor` defines several methods beginning
with `visit` such as `visitMethodCallExpression(MethodCallExpression expression)`. The visitor class associated with the
rule will need to override the visit method (or methods) that are relevant to the code they wish to examine.

There is one visit method for each of the possible Groovy language expressions and statements. Unfortunately there are
dozens of Groovy expressions and statements which can make it difficult to know which your rule needs to override. It is
also not immediately obvious how Nextflow statements and expressions are semantically realized as Groovy. To help you decide,
this package includes a Java application (AstEchoCli) that will echo the Groovy Abstract Syntax Tree for any Nextflow script or other valid
Groovy or Groovy DSL. Consult the README.md in the `ast-echo` folder of this project for build and usage instructions.

New rules should be added to the appropriate package in `./linter-rules/src/groovy`.

A rule consists of a Rule and an ASTVisitor typically in the same groovy file. The Rule `class` should extend `AbstractAstVisitorRule`

```groovy
class MyRule extends AbstractAstVisitorRule {

    String name = 'MyRule'
    int priority = 3        // priority indicates severity of the violation. 1 = severe, 2 = moderate, 3 = advisory
    Class astVisitorClass = MyRuleAstVisitor
}
```

```groovy
class MyRuleAstVisitor extends AbstractAstVisitor {
    // override visitor methods as needed
    // for example:

    @Override
    void visitMethodCallExpression(MethodCallExpression expression){
        // code here to inspect the expression to determine if it is relevant to the check and if it violates the check
        
        // for any violations, add them
        if(somethingBad){
            addViolation(expression, "message explaining violation and potential fix")
        }
        
        // importantly, call the super method to continue traversal of the AST before you exit the method
        super.visitMethodCallExpression(expression)
    }
}
```

### Add a Test

All rules should include tests for cases that produce no violations and cases that should produce violations. Tests
should extend `AbstractRuleTestCase<T>` where `T` is the class of your new rule.

For example:
```groovy
class CpuRuleTest extends AbstractRuleTestCase<CpuRule> {

    // provides an instance of the rule to the test
    @Override
    protected CpuRule createRule() {
        new CpuRule()
    }

    // test the properties of the rule are what you think they should be
    @Test
    void ruleProperties() {
        assert rule.priority == 2
        assert rule.name == "CpuRule"
    }

    // provide one or more tests that produce no violations for valid code (and edge cases)
    @Test
    void cpuRule_NoViolationsMin() {
        final SOURCE =
                '''
                process MY_PROCESS {
                 cpus 2
                }
                '''
        assertNoViolations(SOURCE)
    }

    // provide at least one test that contains source that will trigger one or more violations
    @Test
    void cpuRule_TooManyArgsViolation() {
        final SOURCE =
                '''
                process MY_PROCESS {
                    cpus 14, 18
                }
                '''
        // assertion that only one violation occurs and that it matches the expected priority, violating code fragment
        // and violation message.
        // several other assert violation methods are available for various scenarios
        assertSingleViolation(SOURCE, 3, "cpus 14, 18",
                "the cpus directive must have only one argument")
    }
}
```

### Running tests

To run all tests in this project you should use:

```shell
./gradlew test
```

To run all tests in a specific Rule test class (in this case CpuRuleTest):

```shell
./gradlew test --tests CpuRuleTest
```

To run a specific test method in a test class:

```shell
./gradlew test --tests CpuRuleTest.cpuRule_TooManyArgsViolation
```

### Update the Ruleset

When a rule is created it should be added to the `linter-rules/src/resources/rulesets/healthomics.xml` file. If the rule is general to any
Nextflow environment then it should **also** be added to the `linter-rules/src/resources/rulesets/general.xml`
Entries use the fully qualified class name of the rule. Assuming the new Rule is called `NewRule` and the package is 
`software.amazon.nextflow.rules.healthomics`, the rule to be added will be:

```xml
<rule class="software.amazon.nextflow.rules.healthomics.NewRule"/>
```
