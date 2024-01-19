package software.amazon.nextflow.rules.healthomics

import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule
import software.amazon.nextflow.rules.utils.HealthOmicsNFUtils
import software.amazon.nextflow.rules.utils.NFUtils

class AllowedHealthOmicsDirectivesRule extends AbstractAstVisitorRule{
    String name = 'AllowedHealthOmicsDirectivesRule'
    int priority = 1
    Class astVisitorClass = AllowedHealthOmicsDirectivesAstVisitor
}

class AllowedHealthOmicsDirectivesAstVisitor extends AbstractAstVisitor {
    int nestedProcessMethodDepth = 0

    @Override
    void visitMethodCallExpression(MethodCallExpression callExpression) {
        String methodName = callExpression.getMethodAsString()
        if (methodName == 'process') {
            nestedProcessMethodDepth = 1
        } else if (nestedProcessMethodDepth > 0) {
            nestedProcessMethodDepth++

            // at depth 3 method calls are process directives
            if (nestedProcessMethodDepth == 3 &&
                    !(methodName in NFUtils.ALLOWED_NF_INPUTS_DIRECTIVES) &&  // ignore these
                    !(methodName in NFUtils.ALLOWED_NF_OUTPUTS_DIRECTIVES) && // ignore these
                    methodName in HealthOmicsNFUtils.UNSUPPORTED_NF_PROCESS_DIRECTIVES  // reject these
            ) {
                addViolation(callExpression, "The process directive $methodName is not supported by AWS HealthOmics at this time. " +
                        "It may be ignored or it may cause a workflow run to fail")
            }
        }

        super.visitMethodCallExpression(callExpression)
        nestedProcessMethodDepth --
    }
}
