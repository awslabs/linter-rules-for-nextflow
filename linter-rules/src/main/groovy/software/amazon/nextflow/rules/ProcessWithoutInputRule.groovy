/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules

import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule

class ProcessWithoutInputRule extends AbstractAstVisitorRule{
    String name = "ProcessWithoutInputRule"
    int priority = 2
    Class astVisitorClass = ProcessWithoutInputAstVisitor
}

class ProcessWithoutInputAstVisitor extends AbstractAstVisitor {
    boolean inProcess = false
    boolean inputLabeledExpressionFound = false
    int depth = 0

    @Override
    void visitMethodCallExpression(MethodCallExpression call) {
        depth++

        String methodName = call.methodAsString

        if (methodName == "process") {
            inProcess = true
        }


        super.visitMethodCallExpression(call)
        depth--

        if(depth == 0 && inProcess && !(inputLabeledExpressionFound)) {
            addViolation(call, "No 'input:' expression for process. This will limit composition and flexibility.")
        }
        if(depth == 0) {
            inProcess = false
            inputLabeledExpressionFound = false
        }
    }

    @Override
    void visitExpressionStatement(ExpressionStatement statement) {
        if (inProcess && statement.statementLabels != null && statement.statementLabels.contains("input")){
            inputLabeledExpressionFound = true
        }
        super.visitExpressionStatement(statement)
    }
}
