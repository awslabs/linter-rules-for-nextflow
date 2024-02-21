/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules.healthomics

import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.EmptyExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.util.AstUtil

class MemoryRule extends AbstractAstVisitorRule{
    String name = 'MemoryRule'
    int priority = 2
    Class astVisitorClass = MemoryRuleAstVisitor
}

class MemoryRuleAstVisitor extends AbstractAstVisitor {
    def MIN_MEM = 2
    def MAX_MEM = 768

    @Override
    void visitMethodCallExpression(MethodCallExpression expression) {
        if(expression.getMethodAsString() == 'memory'){
            checkOneArgument(expression)
        }

        super.visitMethodCallExpression(expression)
    }

    private checkOneArgument(final MethodCallExpression expression){
        def methodArguments = AstUtil.getMethodArguments(expression)
        if (methodArguments.size() == 0) {
            addViolation(expression, 'the memory directive must have one argument')
            return new EmptyExpression()
        } else if (methodArguments.size() > 1) {
            addViolation(expression, 'the memory directive must have only one argument')
        }

        if( methodArguments.first() instanceof ConstantExpression){
            checkMemory((ConstantExpression)methodArguments.first())
        }
    }

    private checkMemory(final ConstantExpression expression){
        def value = expression.value as String
        def matcher = value =~ /(\d+)\s+GB/

        if (matcher.size() != 1){
            addViolation(expression, "Unexpected memory directive of $value. " +
                    "Recommend using a numeric value and GB units such as '2 GB'")
            return
        }

        def num = matcher[0][1] as int

        if (num < MIN_MEM) {
            addViolation(expression, "The minimum memory allowed in AWS HealthOmics is $MIN_MEM GB. " +
                    "The requested amount was $value please replace with a larger value")
        }
        if (num > MAX_MEM) {
            addViolation(expression, "The maximum memory allowed in AWS HealthOmics is $MAX_MEM GB. " +
                    "The requested amount was $value, please replace with a smaller value")
        }

    }
}
