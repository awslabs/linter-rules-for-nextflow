/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.groovy;

import java.util.Collections;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.classgen.BytecodeExpression;

public class EchoCodeVisitor extends CodeVisitorSupport {
    private int depth = -1;

    @Override
    public void visitBlockStatement(BlockStatement block) {
        this.depth++;
        echoStatement(block);
        super.visitBlockStatement(block);
        this.depth--;
    }

    @Override
    public void visitForLoop(ForStatement forLoop) {
        this.depth++;
        echoStatement(forLoop);
        super.visitForLoop(forLoop);
        this.depth--;
    }

    @Override
    public void visitWhileLoop(WhileStatement loop) {
        this.depth++;
        echoStatement(loop);
        super.visitWhileLoop(loop);
        this.depth--;
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement loop) {
        this.depth++;
        echoStatement(loop);
        super.visitDoWhileLoop(loop);
        this.depth--;
    }

    @Override
    public void visitIfElse(IfStatement ifElse) {
        this.depth++;
        echoStatement(ifElse);
        super.visitIfElse(ifElse);
        this.depth--;
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement statement) {
        this.depth++;
        echoStatement(statement);
        super.visitExpressionStatement(statement);
        this.depth--;
    }

    @Override
    public void visitReturnStatement(ReturnStatement statement) {
        this.depth++;
        echoStatement(statement);
        super.visitReturnStatement(statement);
        this.depth--;
    }

    @Override
    public void visitAssertStatement(AssertStatement statement) {
        this.depth++;
        echoStatement(statement);
        super.visitAssertStatement(statement);
        this.depth--;
    }

    @Override
    public void visitTryCatchFinally(TryCatchStatement statement) {
        this.depth++;
        echoStatement(statement);
        super.visitTryCatchFinally(statement);
        this.depth--;
    }

    @Override
    public void visitEmptyStatement(EmptyStatement statement) {
        this.depth++;
        echoStatement(statement);
        super.visitEmptyStatement(statement);
        this.depth--;
    }

    @Override
    public void visitSwitch(SwitchStatement statement) {
        this.depth++;
        echoStatement(statement);
        super.visitSwitch(statement);
    }

    @Override
    protected void afterSwitchConditionExpressionVisited(SwitchStatement statement) {
        this.depth--;
        super.afterSwitchConditionExpressionVisited(statement);
    }

    @Override
    public void visitCaseStatement(CaseStatement statement) {
        this.depth++;
        echoStatement(statement);
        super.visitCaseStatement(statement);
        this.depth--;
    }

    @Override
    public void visitBreakStatement(BreakStatement statement) {
        this.depth++;
        echoStatement(statement);
        super.visitBreakStatement(statement);
        this.depth--;
    }

    @Override
    public void visitContinueStatement(ContinueStatement statement) {
        this.depth++;
        echoStatement(statement);
        super.visitContinueStatement(statement);
        this.depth--;
    }

    @Override
    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        this.depth++;
        echoStatement(statement);
        super.visitSynchronizedStatement(statement);
        this.depth--;
    }

    @Override
    public void visitThrowStatement(ThrowStatement statement) {
        this.depth++;
        echoStatement(statement);
        super.visitThrowStatement(statement);
        this.depth--;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        this.depth++;
        echoExpression(call);
        super.visitMethodCallExpression(call);
        this.depth--;
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        this.depth++;
        echoExpression(call);
        super.visitStaticMethodCallExpression(call);
        this.depth--;
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        this.depth++;
        echoExpression(call);
        super.visitConstructorCallExpression(call);
        this.depth--;
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitBinaryExpression(expression);
        this.depth--;
    }

    @Override
    public void visitTernaryExpression(TernaryExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitTernaryExpression(expression);
        this.depth--;
    }

    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitShortTernaryExpression(expression);
        this.depth--;
    }

    @Override
    public void visitPostfixExpression(PostfixExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitPostfixExpression(expression);
        this.depth--;
    }

    @Override
    public void visitPrefixExpression(PrefixExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitPrefixExpression(expression);
        this.depth--;
    }

    @Override
    public void visitBooleanExpression(BooleanExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitBooleanExpression(expression);
        this.depth--;
    }

    @Override
    public void visitNotExpression(NotExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitNotExpression(expression);
        this.depth--;
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitClosureExpression(expression);
        this.depth--;
    }

    @Override
    public void visitLambdaExpression(LambdaExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitLambdaExpression(expression);
        this.depth--;
    }

    @Override
    public void visitTupleExpression(TupleExpression expression) {
        // Depth is not incremented and these expressions are not echoed as they are always another expression type like BinaryExpression
        super.visitTupleExpression(expression);
    }

    @Override
    public void visitListExpression(ListExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitListExpression(expression);
        this.depth--;
    }

    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitArrayExpression(expression);
        this.depth--;
    }

    @Override
    public void visitMapExpression(MapExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitMapExpression(expression);
        this.depth--;
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitMapEntryExpression(expression);
        this.depth--;
    }

    @Override
    public void visitRangeExpression(RangeExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitRangeExpression(expression);
        this.depth--;
    }

    @Override
    public void visitSpreadExpression(SpreadExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitSpreadExpression(expression);
        this.depth--;
    }

    @Override
    public void visitSpreadMapExpression(SpreadMapExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitSpreadMapExpression(expression);
        this.depth--;
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitMethodPointerExpression(expression);
        this.depth--;
    }

    @Override
    public void visitMethodReferenceExpression(MethodReferenceExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitMethodReferenceExpression(expression);
        this.depth--;
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitUnaryMinusExpression(expression);
        this.depth--;
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitUnaryPlusExpression(expression);
        this.depth--;
    }

    @Override
    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitBitwiseNegationExpression(expression);
        this.depth--;
    }

    @Override
    public void visitCastExpression(CastExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitCastExpression(expression);
        this.depth--;
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitConstantExpression(expression);
        this.depth--;
    }

    @Override
    public void visitClassExpression(ClassExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitClassExpression(expression);
        this.depth--;
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitVariableExpression(expression);
        this.depth--;
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitDeclarationExpression(expression);
        this.depth--;
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitPropertyExpression(expression);
        this.depth--;
    }

    @Override
    public void visitAttributeExpression(AttributeExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitAttributeExpression(expression);
        this.depth--;
    }

    @Override
    public void visitFieldExpression(FieldExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitFieldExpression(expression);
        this.depth--;
    }

    @Override
    public void visitGStringExpression(GStringExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitGStringExpression(expression);
        this.depth--;
    }

    @Override
    public void visitCatchStatement(CatchStatement statement) {
        this.depth++;
        echoStatement(statement);
        super.visitCatchStatement(statement);
        this.depth--;
    }

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitArgumentlistExpression(expression);
        this.depth--;
    }

    @Override
    public void visitClosureListExpression(ClosureListExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitClosureListExpression(expression);
        this.depth--;
    }

    @Override
    public void visitBytecodeExpression(BytecodeExpression expression) {
        this.depth++;
        echoExpression(expression);
        super.visitBytecodeExpression(expression);
        this.depth--;
    }

    @Override
    public void visitEmptyExpression(EmptyExpression expression) {
        // noop
        super.visitEmptyExpression(expression);
    }

    private void echoStatement(Statement statement){
        String labels = "";
        if (statement.getStatementLabels() != null){
            labels = String.join(",", statement.getStatementLabels());
        }

        System.err.printf("%s%s <%s>: %s %s%n",
                depthString(depth),
                Position.fromAstNode(statement),
                statement.getClass().getSimpleName(), labels, statement.getText());

    }

    private void echoExpression(Expression expression){
        System.err.printf("%s%s <%s>: %s%n",
                depthString(depth),
                Position.fromAstNode(expression),
                expression.getClass().getSimpleName(), expression.getText());

    }

    private String depthString(final int depth){
        var padding = String.join("", Collections.nCopies(depth, "  "));
        if(depth > 0) {
            return padding + "-> ";
        }
        return padding;
    }
}


