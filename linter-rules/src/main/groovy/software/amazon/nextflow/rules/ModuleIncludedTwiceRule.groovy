/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.nextflow.rules

import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.util.AstUtil

/**
 * Checks if a module has been included twice, with or without an alias
 */
class ModuleIncludedTwiceRule extends AbstractAstVisitorRule{
    String name = 'ModuleIncludedTwiceRule'
    int priority = 2
    Class astVisitorClass = ModuleIncludedTwiceAstVisitor
}

class ModuleIncludedTwiceAstVisitor extends AbstractAstVisitor {

    Map<String, Set<String>> pathsAndModules = [:]
    boolean withinInclude
    String path
    Set<String> modules = []

    @Override
    void visitMethodCallExpression(MethodCallExpression call) {
        String methodName = call.methodAsString
        if (methodName == "include") {
            withinInclude = true
        }

        if (methodName == "from") {
            for (arg in AstUtil.getMethodArguments(call)) {
                path = arg.text
            }
        }
        super.visitMethodCallExpression(call)
        withinInclude = false
        path = null
        modules = []
    }

    @Override
    void visitVariableExpression(VariableExpression expression) {
        if (withinInclude && expression.text != 'this') {

            if (pathsAndModules.containsKey(path)) {
                if (pathsAndModules.get(path).contains(expression.text)) {
                    addViolation(expression, "include with same module and path declared twice")
                } else {
                    pathsAndModules.get(path).add(expression.text)
                }
            } else {
                modules.add(expression.text)
                pathsAndModules.put(path, modules)
            }
        }
        super.visitVariableExpression(expression)
    }

}
