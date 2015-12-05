package cz.cvut.fit.ostrajava.Compiler;

import cz.cvut.fit.ostrajava.Parser.*;

/**
 * Created by tomaskohout on 11/21/15.
 */
public class CompilerTypes {


    public static boolean isConditionalExpression(Node node){
        return isEqualityExpression(node) || isRelationalExpression(node) || isOrExpression(node) || isAndExpression(node);
    }

    public static boolean isOrExpression(Node node){
        return node instanceof ASTConditionalOrExpression;
    }

    public static boolean isAndExpression(Node node){
        return node instanceof ASTConditionalAndExpression;
    }

    public static boolean isEqualityExpression(Node node){
        return node instanceof ASTEqualityExpression;
    }

    public static boolean isRelationalExpression(Node node){
        return node instanceof ASTRelationalExpression;
    }

    public static boolean isArithmeticExpression(Node node){
        return isAdditiveExpression(node) || isMultiplicativeExpression(node);
    }

    public static boolean isAdditiveExpression(Node node){
        return node instanceof ASTAdditiveExpression;
    }
    public static boolean isMultiplicativeExpression(Node node){
        return node instanceof ASTMultiplicativeExpression;
    }

    public static boolean isVariable(Node node){
        return node instanceof ASTName;
    }

    public static boolean isLiteral(Node node){
        return isNumberLiteral(node) || isBooleanLiteral(node);
    }

    public static boolean isNumberLiteral(Node node){
        return node instanceof ASTNumberLiteral;
    }

    public static boolean isFloatLiteral(Node node){
        return node instanceof ASTFloatLiteral;
    }

    public static boolean isCharLiteral(Node node){
        return node instanceof ASTCharLiteral;
    }

    public static boolean isStringLiteral(Node node){
        return node instanceof ASTStringLiteral;
    }

    public static boolean isBooleanLiteral(Node node){
        return node instanceof ASTTrue || node instanceof ASTFalse;
    }

    public static boolean isNullLiteral(Node node){ return node instanceof ASTNullLiteral;}

    public static boolean isAllocationExpression(Node node){
        return node instanceof ASTAllocationExpression;
    }

    public static boolean isUnaryExpression(Node node){
        return node instanceof ASTUnaryExpression || node instanceof  ASTUnaryExpressionNotPlusMinus;
    }

    public static boolean isCallExpression(Node node){
        if (node instanceof ASTPrimaryExpression){
            Node last = node.jjtGetChild(node.jjtGetNumChildren() -1);
            return last instanceof ASTArguments;
        }

        return false;
    }

    public static boolean isFieldExpression(Node node){
        return  (node instanceof ASTPrimaryExpression) && !isCallExpression(node);
    }

    public static boolean isThis(Node node){
        return node instanceof ASTThis;
    }

    public static boolean isSuper(Node node){
        return node instanceof ASTSuper;
    }

    public static boolean isArray(Node node){
        return node.jjtGetNumChildren() >= 2 && (node.jjtGetChild(node.jjtGetNumChildren() - 1) instanceof ASTArraySuffix);
    }
}
