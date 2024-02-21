/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules.healthomics

import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.util.AstUtil

class PublishDirRule extends AbstractAstVisitorRule {
    String name ='PublishDirRule'
    int priority = 1
    Class astVisitorClass = PublishDirAstVisitor
}

class PublishDirAstVisitor extends AbstractAstVisitor{

    @Override
    void visitMethodCallExpression(MethodCallExpression expression) {
        if(expression.methodAsString == 'publishDir') {
            checkArgs(expression)
        }

        super.visitMethodCallExpression(expression)
    }

    private checkArgs(MethodCallExpression expression) {
        // todo check other named args for this directive
        //def argumentNames = AstUtil.getArgumentNames(expression)
        def methodArguments = AstUtil.getMethodArguments(expression)
        if( methodArguments.size() > 0 ) {
            for (arg in methodArguments) {
                if(arg instanceof ConstantExpression){
                    checkPublishDirConstant(arg as ConstantExpression)
                }
            }
        } else {
            addViolation(expression, "The publishDir directive must have at least 1 argument")
        }
    }

    private void checkPublishDirConstant(ConstantExpression expression){
        if ( !(expression.getText() =~ "/mnt/workflow/pubdir(/.*)?")){
            addViolation(expression,
                    "AWS HealthOmics requires the value of publishDir to be '/mnt/workflow/pubdir'. " +
                            "Please replace the current value")
        }
    }
}
