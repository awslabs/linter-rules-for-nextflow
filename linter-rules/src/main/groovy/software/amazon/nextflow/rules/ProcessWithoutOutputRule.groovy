/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules

import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule

class ProcessWithoutOutputRule extends AbstractAstVisitorRule{
    String name = "ProcessWithoutOutputRule"
    int priority = 2
    Class astVisitorClass = ProcessWithoutOutputAstVisitor
}

class ProcessWithoutOutputAstVisitor extends AbstractAstVisitor {
    boolean inProcess = false
    boolean outputLabeledExpressionFound = false
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

        if(depth == 0 && inProcess && !(outputLabeledExpressionFound)) {
            addViolation(call, "No 'output:' expression for process. This will limit composition.")
        }
        if(depth == 0) {
            inProcess = false
            outputLabeledExpressionFound = false
        }
    }

    @Override
    void visitExpressionStatement(ExpressionStatement statement) {
        if (inProcess && statement.statementLabels != null && statement.statementLabels.contains("output")){
            outputLabeledExpressionFound = true
        }
        super.visitExpressionStatement(statement)
    }
}
