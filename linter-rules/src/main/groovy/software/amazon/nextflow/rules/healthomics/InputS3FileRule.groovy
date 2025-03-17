package software.amazon.nextflow.rules.healthomics

import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.ConstantExpression                                                                               
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
    void visitMethodCallExpression(MethodCallExpression expression) {
        if (expression.getMethodAsString() == "path") {
            checkArguments(expression)
        }
        super .visitMethodCallExpression(expression)
    }
    private checkArguments(final MethodCallExpression expression) {
        def methodArguments = AstUtil.getMethodArguments(expression)
        if (methodArguments.size() != 1) {
            addViolation(expression, 'The path directive must have exactly one argument.')
        }
        def fpath = methodArguments.first()
        switch (fpath) {
            case ConstantExpression:
            checkFilePath(fpath as ConstantExpression)
            break case PropertyExpression:
            checkFilePathParam(fpath as PropertyExpression)
            break default :
            addViolation(expression, 'Invalid fpath')
        }
    }
    private void checkFilePath(ConstantExpression expression) {
        def arg = expression.value
        if (!arg.matches(FILE_PATTERN)) {
            addViolation(expression, "The file path '$arg' does not match the pattern '$FILE_PATTERN'. Replace with an S3 object URI.")
        }
    }
    private void checkFilePathParam(PropertyExpression expression) {
        def arg = expression.getObjectExpression().getText()
        if (!arg.contains('params')) {
            addViolation(expression, "The file path must be either a string or params.")
        }
    }
}
