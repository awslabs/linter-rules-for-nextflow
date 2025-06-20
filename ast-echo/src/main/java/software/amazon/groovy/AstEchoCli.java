/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.groovy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.control.CompilePhase;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "AstEchoCli", mixinStandardHelpOptions = true, version = "AstEchoCli 0.1.1",
        description = "Echoes the AST of a Groovy script or DSL to STDOUT.")
public class AstEchoCli implements Callable<Integer> {

    @Parameters(index = "0", description = "The path to the Groovy script or DSL.")
    private Path scriptPath;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new AstEchoCli()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        final var source = readSource(scriptPath);
        final var ast = buildAst(source);
        echoAst(ast);
        return 0;
    }

    private void echoAst(List<ASTNode> ast) {
        ast.forEach(node -> {
            if (! (node instanceof ClassNode)){
                var visitor = new EchoCodeVisitor();
                node.visit(visitor);
            }
        });
    }

    private String readSource(final Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    public List<ASTNode> buildAst(String source) {
        var astBuilder = new AstBuilder();
        return astBuilder.buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, source);
    }
}