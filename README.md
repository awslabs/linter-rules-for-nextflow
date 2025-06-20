[![Java CI with Gradle](https://github.com/awslabs/linter-rules-for-nextflow/actions/workflows/gradle.yml/badge.svg)](https://github.com/linter-rules-for-nextflow/actions/workflows/gradle.yml)
[![Github All Releases](https://img.shields.io/github/downloads/awslabs/linter-rules-for-nextflow/total.svg)]()
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

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
  -classpath ./linter-rules/build/libs/linter-rules-0.1.1.jar:CodeNarc-3.3.0-all.jar:slf4j-api-1.7.36.jar:slf4j-simple-1.7.36.jar \
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
  -classpath ./linter-rules/build/libs/linter-rules-0.1.1.jar:CodeNarc-3.3.0-all.jar:slf4j-api-1.7.36.jar:slf4j-simple-1.7.36.jar \
  org.codenarc.CodeNarc \
  -report=text:stdout \
  -rulesetfiles=rulesets/general.xml \
  -includes=**/**.nf
```

### Docker

A `Dockerfile` is provided for this project which will build an image that contains the scripts in `scripts/` and the
required JAR files. Recent copies of the container image are publically available from [ECR Public Gallery](https://gallery.ecr.aws/aws-genomics/linter-rules-for-nextflow).

If you prefer to build the container from source:

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

#### Fail on Violations

You can configure the container to exit with a non-zero code when violations are detected by using the `check.sh` script with the `--fail-on-violations` option:

```shell
cd examples
docker run --entrypoint="" -v $PWD:/data linter-rules-for-nextflow ./check.sh --fail-on-violations
```

This is particularly useful in CI/CD pipelines where you want the build to fail if code quality issues are detected. You can also combine this with ruleset selection:

```shell
cd examples
docker run --entrypoint="" -v $PWD:/data linter-rules-for-nextflow ./check.sh --fail-on-violations general
```

Note: The `--entrypoint=""` flag is required to override the container's default entrypoint and run the script with custom arguments.

#### AST Echo

The container also contains the `ast-echo` application along with a script to run it (`echo-tree.sh`). For example:

```shell
docker run --entrypoint="" -v $PWD/examples:/data linter-rules-for-nextflow ./echo-tree.sh /data/example.nf
```

Note: The `--entrypoint=""` flag is required to override the container's default entrypoint and run the AST echo script directly.

### Helper scripts

This project contains helper scripts that you can install on your `$PATH` by copying them to `$HOME/.local/bin` or similar.

- *`nextflow-lint.sh`* will run the linter against the container built from the `Dockerfile`.
- *`nextflow-echo.sh`* will run the `ast-echo` application against the latest JAR build.

#### nextflow-lint.sh Usage

The `nextflow-lint.sh` script supports several options:

```bash
nextflow-lint.sh [--fail-on-violations] [--ruleset RULESET] [directory]
```

**Options:**
- `--fail-on-violations`: Exit with code 1 if lint violations are found (useful for CI/CD pipelines)
- `--ruleset RULESET`: Specify which ruleset to use (`healthomics` or `general`)
- `directory`: Directory to lint (defaults to current directory)
- `--help`: Display usage information

**Examples:**
```bash
# Lint current directory with default ruleset
nextflow-lint.sh

# Lint specific directory
nextflow-lint.sh /path/to/nextflow/project

# Lint with general rules only
nextflow-lint.sh --ruleset general

# Lint and fail if violations found (for CI/CD)
nextflow-lint.sh --fail-on-violations

# Combine options
nextflow-lint.sh --fail-on-violations --ruleset general /path/to/project
```

The `--fail-on-violations` option is particularly useful in CI/CD pipelines where you want the build to fail if code quality issues are detected.

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

## Release Management

This project includes automated release management through Gradle tasks. The release automation handles version updates, testing, building, git operations, and artifact generation.

### Quick Start

```bash
# Check what would happen in a release (dry run)
./gradlew preRelease -PreleaseVersion=0.2.0

# Perform a complete release
./gradlew release -PreleaseVersion=0.2.0
```

### Release Process

The automated release process performs the following steps:

1. **Validation**
   - Checks git working directory is clean
   - Verifies you're on the `main` branch
   - Validates version format (semantic versioning)

2. **Version Updates**
   - Updates version in all `build.gradle` files
   - Updates version in `AstEchoCli.java`
   - Updates JAR references in `README.md`

3. **Testing & Building**
   - Runs all tests across subprojects
   - Builds all JAR artifacts

4. **Git Operations**
   - Commits version changes
   - Creates and pushes git tag
   - Pushes changes to remote

5. **Documentation & Security**
   - Generates release notes from commit history
   - Creates SHA256 checksums for all artifacts

### Available Commands

#### Core Release Commands

```bash
# Dry run - validate release without making changes
./gradlew preRelease -PreleaseVersion=x.y.z

# Full automated release
./gradlew release -PreleaseVersion=x.y.z
```

#### Utility Commands

```bash
# Show current version
./gradlew showVersion

# List all releases (git tags)
./gradlew listReleases

# Generate checksums for existing builds
./gradlew generateChecksums

# Clean up release artifacts (notes, checksums)
./gradlew cleanReleaseArtifacts

# Show detailed help
./gradlew releaseHelp
```

### Release Artifacts

After a successful release, the following artifacts are created:

- **JAR Files**: `linter-rules-x.y.z.jar` and `ast-echo-x.y.z.jar`
- **Release Notes**: `release-notes-vx.y.z.md` with commit history and installation instructions
- **Checksums**: `checksums-vx.y.z.txt` with SHA256 hashes for security verification
- **Git Tag**: `vx.y.z` pushed to the remote repository

### GitHub Release

After running the automated release, you'll need to manually create the GitHub release:

1. Go to the [releases page](https://github.com/awslabs/linter-rules-for-nextflow/releases)
2. Click "Create a new release"
3. Select the tag that was just created (e.g., `v0.2.0`)
4. Copy the content from the generated `release-notes-vx.y.z.md` file
5. Upload the JAR files and checksums file as release assets

### Version Guidelines

This project follows [Semantic Versioning](https://semver.org/):

- **MAJOR** (x.0.0): Breaking changes to rules or APIs
- **MINOR** (0.x.0): New rules, features, or non-breaking enhancements
- **PATCH** (0.0.x): Bug fixes, documentation updates, or minor improvements

### Examples

```bash
# Patch release (bug fixes)
./gradlew release -PreleaseVersion=0.1.2

# Minor release (new features)
./gradlew release -PreleaseVersion=0.2.0

# Major release (breaking changes)
./gradlew release -PreleaseVersion=1.0.0

# Check what a release would do first
./gradlew preRelease -PreleaseVersion=0.1.2
```

### Troubleshooting

**Common Issues:**

- **"Git working directory is not clean"**: Commit or stash your changes before releasing
- **"Not on main branch"**: Switch to the main branch before releasing
- **"Tag already exists"**: The version you're trying to release already exists
- **"Version format invalid"**: Use semantic versioning format (x.y.z)

**Recovery:**

If a release fails partway through:

1. Check the git status and recent commits
2. If version files were updated but not tagged, you may need to reset:
   ```bash
   git reset --hard HEAD~1  # Only if the commit was made but tag failed
   ```
3. Clean up any generated artifacts:
   ```bash
   ./gradlew cleanReleaseArtifacts
   ```
4. Fix the underlying issue and retry the release

### CI/CD Integration

The release automation is designed to work well in CI/CD environments:

```bash
# In your CI pipeline
./gradlew preRelease -PreleaseVersion=${VERSION}
./gradlew release -PreleaseVersion=${VERSION}
```

Ensure your CI environment has:
- Git configured with appropriate credentials
- Push access to the repository
- Java 17+ and Gradle available
