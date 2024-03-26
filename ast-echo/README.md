# AST Echo

AST Echo is a basic utility that will echo the Abstract Syntax Tree of any Groovy file or Groovy DSL script to STDOUT.

This can be useful when examining how the Groovy parsers and compilers "see" the script or DSL you provide.

## Build

To build this application as an executable JAR with all dependencies, run the following from the *root directory* 
of this repository.

```shell
./gradlew :ast-echo:clean
./gradlew :ast-echo:jar
```

This will produce an executable jar file in `ast-echo/build/libs/` called `ast-echo-<version>-all.jar`

## Run

To run the application you only need to provide the path of a Groovy script or DSL on the command line as follows

```shell
java -jar ast-echo-<version>-all.jar <file>
```

e.g

```shell
java -jar ast-echo/build/libs/ast-echo-<version>-all.jar example.nf
```