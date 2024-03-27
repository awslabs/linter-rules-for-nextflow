package software.amazon.nextflow.rules

import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule

class ProcessWithoutMemoryRule extends AbstractAstVisitorRule{
    String name = "ProcessWithoutMemoryRule"
    int priority = 2
    Class astVisitorClass = ProcessWithoutMemoryAstVisitor
}

class ProcessWithoutMemoryAstVisitor extends AbstractAstVisitor {
    boolean inProcess = false
    boolean memoryDirectiveFound = false
    boolean labelDirectiveFound = false
    int depth = 0

    @Override
    void visitMethodCallExpression(MethodCallExpression call) {
        depth++

        String methodName = call.methodAsString

        if (methodName == "process") {
            inProcess = true
        }

        if (inProcess && methodName == "memory") {
            memoryDirectiveFound = true
        }

        if (inProcess && methodName == "label") {
            labelDirectiveFound = true
        }

        super.visitMethodCallExpression(call)
        depth--

        if(depth == 0 && inProcess && !(memoryDirectiveFound || labelDirectiveFound)) {
            addViolation(call, "No 'label' or 'memory' directive found. This may reduce the portability of this process.")
        }
        if(depth == 0) {
            inProcess = false
            memoryDirectiveFound = false
            labelDirectiveFound = false
        }
    }
}
