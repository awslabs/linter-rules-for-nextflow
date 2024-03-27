/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules

import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule

class ProcessWithoutExecEnvironmentRule extends AbstractAstVisitorRule{
    String name = "ProcessWithoutExecEnvironmentRule"
    int priority = 2
    Class astVisitorClass = ProcessWithoutExecEnvironmentAstVisitor
    // one or more of these should be defined to declare an execution environment for portability
    static Set<String> directives = ["label", "conda", "container", "module", "spack"]
}

class ProcessWithoutExecEnvironmentAstVisitor extends AbstractAstVisitor {
    boolean inProcess = false
    boolean directiveFound = false

    int depth = 0

    @Override
    void visitMethodCallExpression(MethodCallExpression call) {
        depth++

        String methodName = call.methodAsString

        if (methodName == "process") {
            inProcess = true
        }

        if (inProcess && methodName in ProcessWithoutExecEnvironmentRule.directives) {
            directiveFound = true
        }

        super.visitMethodCallExpression(call)
        depth--

        if(depth == 0 && inProcess && !directiveFound) {
            addViolation(call,
                    "No execution environment is declared using one of " +
                            "${ProcessWithoutExecEnvironmentRule.directives.toString()}. " +
                            "This may reduce the portability of this process.")
        }
        if(depth == 0) {
            inProcess = false
            directiveFound = false
        }
    }
}
