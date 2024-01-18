package software.amazon.nextflow.rules.healthomics

import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule

/**
 * This rule inspects Nextflow processes to determine if recommended directives are missing. Note that if a "label"
 * directive is present then we assume that the processes required directives are declared in a config file and are
 * therefore not required here.
 */
class MissingProcessDirectivesRule extends AbstractAstVisitorRule{
    String name = 'MissingProcessDirectivesRule'
    int priority = 2
    Class astVisitorClass = MissingProcessDirectivesVisitor
}

class MissingProcessDirectivesVisitor extends AbstractAstVisitor {
    boolean hasProcessLabel = false
    boolean declaresCpus = false
    boolean declaresMemory = false
    boolean declaresContainer = false
    String processName = null
    int nestedProcessMethodDepth = 0;

    @Override
    void visitMethodCallExpression(MethodCallExpression node) {
        String methodName = node.getMethodAsString()
        if (methodName == 'process') {
            nestedProcessMethodDepth = 1
        } else if (nestedProcessMethodDepth > 0){
            nestedProcessMethodDepth++

            // at depth 3 method calls might be process directives
            if (nestedProcessMethodDepth == 3) {
                switch (methodName) {
                    case 'cpus':
                        declaresCpus = true
                        break
                    case 'memory':
                        declaresMemory = true
                        break
                    case 'container':
                        declaresContainer = true
                        break
                    case 'label':
                        hasProcessLabel = true
                        break
                    default:
                        //no-op
                        break
                }
            }
        }

        super.visitMethodCallExpression(node)
        exitMethodCall(node)
    }

    @Override
    void visitConstantExpression(ConstantExpression expression){
        if (nestedProcessMethodDepth == 2 && !processName){
            processName = expression.value
        }

        super.visitConstantExpression(expression)
    }

    private void exitMethodCall(MethodCallExpression node) {
        if (nestedProcessMethodDepth == 1) {
            if(!hasProcessLabel) {
                if (!declaresCpus) {
                    addViolation(node, "$processName does not contain a cpus directive which is recommended for reproducibility")
                }

                if (!declaresMemory) {
                    addViolation(node, "$processName does not contain a memory directive which is recommended for reproducibility")
                }
                if (!declaresContainer) {
                    addViolation(node, "$processName does not contain a container directive which is recommended for reproducibility")
                }
            }

            hasProcessLabel = false
            declaresCpus = false
            declaresMemory = false
            processName = null
        }

        nestedProcessMethodDepth--
    }
}
