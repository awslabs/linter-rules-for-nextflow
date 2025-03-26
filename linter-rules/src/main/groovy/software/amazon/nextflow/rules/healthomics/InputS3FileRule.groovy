package software.amazon.nextflow.rules.healthomics

import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
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
        if (methodArguments.size() == 1) {
            def fpath = methodArguments.first()
            switch (fpath) {
                case ConstantExpression:
                    checkFilePath(fpath as ConstantExpression) // check literals
                    break
                case PropertyExpression:
                    break // skip property expressions
                case VariableExpression:
                    break // skip variable expressions
                default:
                    addViolation(expression, 'Invalid path')
            }
        }
    }
    private void checkFilePath(ConstantExpression expression) {
        def arg = expression.value
        // fail if not an s3 path AND literal doesn't contain a variable
        if (!arg.matches(FILE_PATTERN) && !arg.matches('.*\\$\\{[^}]+\\}.*')) {
            addViolation(expression, "The file path '$arg' does not match the pattern '$FILE_PATTERN'. Replace with an S3 object URI.")
        }
    }

}
