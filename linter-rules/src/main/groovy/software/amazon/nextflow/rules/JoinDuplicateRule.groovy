/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules


import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule

/**
 * Checks if a module has been included twice, with or without an alias
 */
class JoinDuplicateRule extends AbstractAstVisitorRule{
    String name = 'JoinDuplicateRule'
    int priority = 3
    Class astVisitorClass = JoinDuplicateAstVisitor
}

class JoinDuplicateAstVisitor extends AbstractAstVisitor {
    var inJoinMethod = false
    var failOnDuplicateFound = false

    @Override
    void visitMethodCallExpression(MethodCallExpression call) {
        String methodName = call.methodAsString
        if (methodName == "join") {
            inJoinMethod = true
        }

        super.visitMethodCallExpression(call)

        if (inJoinMethod && !failOnDuplicateFound) {
            addViolation(call, "The join doesn't explicitly define a strategy for duplicates. Recommend adding a 'failOnDuplicate' argument." )
        }

        inJoinMethod = false
        failOnDuplicateFound = false
    }

    @Override
    void visitMapEntryExpression(MapEntryExpression expression) {

        if (inJoinMethod &&  expression.keyExpression.text == "failOnDuplicate") {
            failOnDuplicateFound = true
        }

        super.visitMapEntryExpression(expression)
    }
}
