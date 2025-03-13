package software.amazon.nextflow.rules.healthomics

import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.util.AstUtil

/*
 * Checks that the URI of the input file matches the s3 URI pattern
 *
 * @author Jesse Marks
 */
class InputS3FileRule extends AbstractAstVisitorRule {

    String name = 'InputFileIsS3'
    int priority = 1
    Class astVisitorClass = InputS3FileAstVisitor
}

class InputS3FileAstVisitor extends AbstractAstVisitor {

    final String FILE_PATTERN = "s3://.*"

    @Override
    void visitMethodCallExpression(MethodCallExpression expression){
        if (expression.getMethod().getText() == "path") {

            def methodArguments = AstUtil.getMethodArguments(expression)

            // check that it is an "s3://" URI
            if (methodArguments.size() == 1) {
                final argExpression = methodArguments.get(0)
                final arg = methodArguments.get(0).getText()

                if (AstUtil.isConstantOrLiteral(argExpression) && !arg.matches(FILE_PATTERN)) {

                    addViolation(expression, "The file path URI '$arg' does not match the pattern '$FILE_PATTERN'. Replace with a file located on s3.")
                }
            }
        }


        super.visitMethodCallExpression(expression)
    }
}
