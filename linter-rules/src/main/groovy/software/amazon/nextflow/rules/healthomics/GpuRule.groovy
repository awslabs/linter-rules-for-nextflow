/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules.healthomics

import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.util.AstUtil

class GpuRule extends AbstractAstVisitorRule {
    String name = 'GpuRule'
    int priority = 2
    Class astVisitorClass = GpuAstVisitor
}


class GpuAstVisitor extends AbstractAstVisitor {
    def MIN_GPU=1
    def MAX_GPU=4
    def GPU_TYPES = [
            'nvidia-tesla-t4',
            'nvidia-tesla-t4-a10g',
            'nvidia-tesla-a10g',
            'nvidia-l4-a10g',
            'nvidia-l4',
            'nvidia-l40s'
    ]

    @Override
    void visitMethodCallExpression(MethodCallExpression expression) {
        if(expression.getMethodAsString() == 'accelerator'){
            checkArguments(expression)
        }
        super.visitMethodCallExpression(expression)
    }

    private checkArguments(final MethodCallExpression expression){
        def methodArguments = AstUtil.getMethodArguments(expression)
        if(methodArguments.size() != 2) {
            addViolation(expression, 'the accelerator directive requires 2 arguments')
        }
        else{
            // Find parameters by type rather than position
            def typeParam = null
            def countParam = null
            
            methodArguments.each { arg ->
                if (arg instanceof MapExpression) {
                    typeParam = arg
                } else if (arg instanceof ConstantExpression || arg instanceof PropertyExpression) {
                    countParam = arg
                }
            }
            
            // Validate that we found both required parameters
            if (countParam == null) {
                addViolation(expression, 'the accelerator directive requires a count parameter')
            } else {
                // Validate count parameter
                switch (countParam) {
                    case ConstantExpression:
                        checkGpuCounts(countParam as ConstantExpression)
                        break
                    case PropertyExpression:
                        checkGpuCountsParam(countParam as PropertyExpression)
                        break
                    default:
                        addViolation(expression, 'Invalid count parameter type')
                }
            }
            
            if (typeParam == null) {
                addViolation(expression, 'the accelerator directive requires a type parameter')
            } else {
                // Validate type parameter
                checkGpuType(typeParam as MapExpression)
            }
        }
    }

    private void checkGpuType(MapExpression expression){
        def gpuMap = [:]
        expression.getMapEntryExpressions().collect { MapEntryExpression entry ->
            def key = entry.getKeyExpression().getText()
            def value = entry.getValueExpression().getText()
            return gpuMap[key] = value
        }.first()

        if(!gpuMap.containsKey('type')){
            addViolation(expression, 'the type parameter for accelerator must be specified')
        }

        if(!GPU_TYPES.contains(gpuMap['type'])){
            def gpuString = GPU_TYPES.toString()
            if(!(gpuMap['type'] as String).contains('params.')){
                addViolation(expression,
                        "the GPU type specified: ${gpuMap['type']} is not a supported type ${gpuString}")
            }
        }
    }

    private void checkGpuCounts(ConstantExpression expression){
        def gpuCount = expression.value
        if (gpuCount > 4){
            addViolation(expression, "The GPU count: ${gpuCount} exceeds the maximum: ${MAX_GPU}")
        }
        if (gpuCount < 1){
            addViolation(expression, "The GPU count: ${gpuCount} is below the minimum: ${MIN_GPU}")
        }
    }

    private void checkGpuCountsParam(PropertyExpression expression){
        def arg = expression.getObjectExpression().getText()
        if (!arg.contains('params')){
            addViolation(expression, "The GPU count ${gpuCount} must be either an integer or params")
        }
    }

}