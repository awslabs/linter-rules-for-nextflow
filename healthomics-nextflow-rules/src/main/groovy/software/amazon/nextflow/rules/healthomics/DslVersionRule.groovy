/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules.healthomics

import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule

class DslVersionRule extends AbstractAstVisitorRule {
    String name = 'DslVersionRule'
    int priority = 1
    Class astVisitorClass = DslVersionAstVisitor
}

class DslVersionAstVisitor extends AbstractAstVisitor {

    @Override
    void visitBinaryExpression(BinaryExpression expression) {
        if (expression.leftExpression instanceof PropertyExpression) {
            def propExpression = expression.leftExpression as PropertyExpression
            if (propExpression.text == 'nextflow.enable.dsl'){
                if (expression.rightExpression instanceof ConstantExpression) {
                    def constExpression = expression.rightExpression as ConstantExpression
                    if ( constExpression.value != 2){
                        addViolation(constExpression, "HealthOmics only supports DSL version 2, found ${constExpression.value}")
                    }
                } else {
                    addViolation(expression.rightExpression, "Expected a constant with a value of 2, found ${expression.rightExpression.text}")
                }
            }
        }

        super.visitBinaryExpression(expression)
    }
}