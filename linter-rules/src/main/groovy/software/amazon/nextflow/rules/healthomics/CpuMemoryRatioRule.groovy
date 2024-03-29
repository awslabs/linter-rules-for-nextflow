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

class CpuMemoryRatioRule extends AbstractAstVisitorRule{
    String name = 'CpuMemoryRatioRule'
    int priority = 3
    Class astVisitorClass = CpuMemoryRatioAstVisitor
}

class CpuMemoryRatioAstVisitor extends AbstractAstVisitor {
    float C_RATIO = 2.0
    float M_RATIO = 4.0
    float R_RATIO = 8.0

    def okRatios = [C_RATIO, M_RATIO, R_RATIO]
    def requestedCpu = 0
    def requestedMemory = 0
    def nestedProcessMethodDepth = 0

    @Override
    void visitMethodCallExpression(MethodCallExpression call) {
        String methodName = call.getMethodAsString()
        if (methodName == 'process') {
            nestedProcessMethodDepth = 1
        } else if (nestedProcessMethodDepth > 0){
            nestedProcessMethodDepth++

            // at depth 3 method calls are process directives
            if (nestedProcessMethodDepth == 3) {
                switch (methodName) {
                    case 'cpus':
                        requestedCpu = handleCpu(call)
                        break
                    case 'memory':
                        requestedMemory = handleMemory(call)
                        break
                    default:
                        //no-op
                        break
                }
            }
        }
        super.visitMethodCallExpression(call)
        exitMethodCall(call)
    }

    private exitMethodCall(MethodCallExpression call) {
        if(nestedProcessMethodDepth == 1) {
            if (requestedMemory >= 1 && requestedCpu >= 1) {
                checkMemoryToCpuRatio(call)
            }
            requestedCpu = 0
            requestedMemory = 0
        }

        nestedProcessMethodDepth--
    }

    /**
     * Only attempts to extract a numeric value for GB of RAM. This method performs
     * no checks on the value as these are done elsewhere or in other rules.
     */
    private static int handleMemory(MethodCallExpression call){
        def methodArguments = AstUtil.getMethodArguments(call)
        if (methodArguments.size() == 1 && methodArguments[0] instanceof ConstantExpression){
            def value = (methodArguments[0] as ConstantExpression).value
            def matcher = value =~ /(\d+)\s+GB/

            if (matcher.size() == 1){
                return matcher[0][1] as int
            }
        }
        return 0
    }

    private static int handleCpu(MethodCallExpression call){
        def methodArguments = AstUtil.getMethodArguments(call)
        if (methodArguments.size() == 1 && methodArguments[0] instanceof ConstantExpression){
            def value = (methodArguments[0] as ConstantExpression).value
            try {
                return value as int
            } catch (NumberFormatException ignored){
                return 0
            }
        }
        return 0
    }

    private checkMemoryToCpuRatio(MethodCallExpression call){
        def memToCpuRatio = (requestedMemory / requestedCpu) as float

        if (! okRatios.contains(memToCpuRatio)) {
            addViolation(call, "Memory to cpu ratio may not be optimal for HealthOmics " +
                    "instance placement. Ratios of 2:1, 4:1 or 8:1 are optimal")
        }
    }
}