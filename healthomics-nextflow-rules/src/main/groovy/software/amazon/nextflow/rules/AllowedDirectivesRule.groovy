package software.amazon.nextflow.rules

import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule

/**
 * Checks if a process directive is one of the recognized Nextflow Directives
 * (see https://www.nextflow.io/docs/latest/process.html#directives)
 */
class AllowedDirectivesRule extends AbstractAstVisitorRule{
    String name = 'AllowedDirectivesRule'
    int priority = 1
    Class astVisitorClass = AllowedDirectivesAstVisitor
}

class AllowedDirectivesAstVisitor extends AbstractAstVisitor {
    int nestedProcessMethodDepth = 0;

    @Override
    void visitMethodCallExpression(MethodCallExpression node) {
        String methodName = node.getMethodAsString()
        if (methodName == 'process') {
            nestedProcessMethodDepth = 1
        } else if (nestedProcessMethodDepth > 0) {
            nestedProcessMethodDepth++

            // at depth 3 method calls are process directives
            if (nestedProcessMethodDepth == 3 &&
                    !(methodName in NFUtils.ALLOWED_NF_INPUTS_DIRECTIVES) &&  // these are allowed method calls
                    !(methodName in NFUtils.ALLOWED_NF_OUTPUTS_DIRECTIVES) && // also allowed method calls
                    !(methodName in NFUtils.ALLOWED_NF_PROCESS_DIRECTIVES)    // process directives (method calls)
            ) {
                addViolation(node, "The process directive $methodName is not recognized. " +
                        "Check the value and correct if needed")
            }
        }

        super.visitMethodCallExpression(node)
        nestedProcessMethodDepth --
    }
}
