package software.amazon.nextflow.rules.healthomics

import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.EmptyExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.util.AstUtil

class CpuRule extends AbstractAstVisitorRule {
    String name = 'CpuRule'
    int priority = 2
    Class astVisitorClass = CpuAstVisitor
}

class CpuAstVisitor extends AbstractAstVisitor {
    def MIN_CPU = 2
    def MAX_CPU = 96

    @Override
    void visitMethodCallExpression(MethodCallExpression expression) {
        if(expression.getMethodAsString() == 'cpus'){
            checkOneArgument(expression)
        }

        super.visitMethodCallExpression(expression)
    }


    private checkOneArgument(final MethodCallExpression expression){
        def methodArguments = AstUtil.getMethodArguments(expression)
        if (methodArguments.size() == 0) {
            addViolation(expression, 'the cpus directive must have one argument')
            return new EmptyExpression()
        } else if (methodArguments.size() > 1) {
            addViolation(expression, 'the cpus directive must have only one argument')
        }

        if( methodArguments.first() instanceof ConstantExpression){
           checkNumeric((ConstantExpression)methodArguments.first())
        }
    }

    private checkNumeric(ConstantExpression expression){
        try {
            def val = Integer.parseInt(expression.value.toString())
            checkMinMax(expression, val)
        } catch (NumberFormatException ignored){
            addViolation(expression,
                    "'${expression.value}' is not a valid number. Replace with the desired CPU count.")
        }

    }
    private void checkMinMax(Expression exp, final int val) {
        if (val < MIN_CPU) {
            addViolation(exp,
                    "The minimum CPU count is '$MIN_CPU', the supplied value was '$val', please replace with a larger value.")
        } else if (val > MAX_CPU) {
            addViolation(exp,
                    "The maximum CPU count is '$MAX_CPU', the supplied value was '$val', please replace with a smaller value.")
        }
    }
}
