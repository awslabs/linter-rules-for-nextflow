package software.amazon.groovy;

import java.util.Objects;
import org.codehaus.groovy.ast.ASTNode;

/**
 * Holds the source position information for a node in the Abstract Syntax Tree after parsing. Positions may vary
 * depending on the parsing/ compilation phase.
 *
 * @param line line number of the start of the code represented by a node
 * @param column column number of the start of the code represented by a node
 * @param endLine line number of the end of the code represented by a node
 * @param endColumn column number of the end of the code represented by a node
 */
public record Position(int line, int column, int endLine, int endColumn) {

    public static Position fromAstNode(ASTNode node){
        Objects.requireNonNull(node, "node cannot be null");

        return new Position(node.getLineNumber(), node.getColumnNumber(), node.getLastLineNumber(), node.getLastColumnNumber());
    }

    /**
     * Formats a string showing (line:column)-(endLine:endColumn)
     * If either line or column is -1, it will be represented as "∅". This can happen when the Abstract Syntax Tree
     * has inferred a value that doesn't explicitly exist in the source Concrete Syntax Tree or where it has been
     * flattened.
     *
     * @return the formatted String representation
     */
    @Override
    public String toString(){
        var lineStr = (line == -1 ? "∅" : String.valueOf(line));
        var endLineStr = (endLine == -1 ? lineStr : String.valueOf(endLine));
        var columnStr = (column == -1 ? "∅" : String.valueOf(column));
        var endColumnStr = (endColumn == -1 ? columnStr : String.valueOf(endColumn));
        return String.format("(%s:%s)-(%s:%s)", lineStr, columnStr, endLineStr, endColumnStr);
    }
}

