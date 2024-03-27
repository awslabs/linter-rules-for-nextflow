/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.util.AstUtil

/**
 * Checks if a module has been included twice, with or without an alias
 */
class JoinMismatchRule extends AbstractAstVisitorRule{
    String name = 'JoinMismatchRule'
    int priority = 3
    Class astVisitorClass = JoinMismatchAstVisitor
}

class JoinMismatchAstVisitor extends AbstractAstVisitor {
    var inJoinMethod = false
    var failOnMismatchFound = false

    @Override
    void visitMethodCallExpression(MethodCallExpression call) {
        String methodName = call.methodAsString
        if (methodName == "join") {
            inJoinMethod = true
        }

        super.visitMethodCallExpression(call)

        if (inJoinMethod && !failOnMismatchFound) {
            addViolation(call, "A join will silently discard mismatched items. Use 'failOnMismatch' argument to make your intention explicit." )
        }

        inJoinMethod = false
        failOnMismatchFound = false
    }

    @Override
    void visitMapEntryExpression(MapEntryExpression expression) {

        if (inJoinMethod &&  expression.keyExpression.text == "failOnMismatch") {
            failOnMismatchFound = true
        }

        super.visitMapEntryExpression(expression)
    }
}
