/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules

import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule

class ProcessWithoutCpuRule extends AbstractAstVisitorRule {
    String name = "ProcessWithoutCpuRule"
    int priority = 2
    Class astVisitorClass = ProcessWithCpuAstVisitor
}

class ProcessWithCpuAstVisitor extends AbstractAstVisitor {
    boolean inProcess = false
    boolean cpuDirectiveFound = false
    boolean labelDirectiveFound = false
    int depth = 0

    @Override
    void visitMethodCallExpression(MethodCallExpression call) {
        depth++

        String methodName = call.methodAsString

        if (methodName == "process") {
            inProcess = true
        }

        if (inProcess && methodName == "cpus") {
            cpuDirectiveFound = true
        }

        if (inProcess && methodName == "label") {
            labelDirectiveFound = true
        }

        super.visitMethodCallExpression(call)
        depth--

        if(depth == 0 && inProcess && !(cpuDirectiveFound || labelDirectiveFound)) {
            addViolation(call, "No 'label' or 'cpus' directive found. This may reduce the portability of this process.")
        }
        if(depth == 0) {
            inProcess = false
            cpuDirectiveFound = false
            labelDirectiveFound = false
        }
    }


}
