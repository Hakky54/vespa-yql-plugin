// This is a generated file. Not intended for manual editing.
package com.pehrs.vespa.yql.plugin;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.pehrs.vespa.yql.plugin.psi.impl.*;

public interface YqlElementTypes {

  IElementType ARRAY = new YqlElementType("ARRAY");
  IElementType BASIC_KEYWORD = new YqlElementType("BASIC_KEYWORD");
  IElementType BOOLEAN_LITERAL = new YqlElementType("BOOLEAN_LITERAL");
  IElementType LITERAL = new YqlElementType("LITERAL");
  IElementType NEAREST_NEIGHBOR_STATEMENT = new YqlElementType("NEAREST_NEIGHBOR_STATEMENT");
  IElementType NULL_LITERAL = new YqlElementType("NULL_LITERAL");
  IElementType NUMBER_LITERAL = new YqlElementType("NUMBER_LITERAL");
  IElementType OBJECT = new YqlElementType("OBJECT");
  IElementType PROPERTY = new YqlElementType("PROPERTY");
  IElementType QUERY_PROPERTY = new YqlElementType("QUERY_PROPERTY");
  IElementType QUERY_STATEMENT = new YqlElementType("QUERY_STATEMENT");
  IElementType REFERENCE_EXPRESSION = new YqlElementType("REFERENCE_EXPRESSION");
  IElementType STRING_LITERAL = new YqlElementType("STRING_LITERAL");
  IElementType VALUE = new YqlElementType("VALUE");

  IElementType BLOCK_COMMENT = new YqlTokenType("BLOCK_COMMENT");
  IElementType COLON = new YqlTokenType(":");
  IElementType COMMA = new YqlTokenType(",");
  IElementType DOUBLE_QUOTE = new YqlTokenType("\"");
  IElementType DOUBLE_QUOTED_STRING = new YqlTokenType("DOUBLE_QUOTED_STRING");
  IElementType FALSE = new YqlTokenType("false");
  IElementType IDENTIFIER = new YqlTokenType("IDENTIFIER");
  IElementType LINE_COMMENT = new YqlTokenType("LINE_COMMENT");
  IElementType L_BRACKET = new YqlTokenType("[");
  IElementType L_CURLY = new YqlTokenType("{");
  IElementType NULL = new YqlTokenType("null");
  IElementType NUMBER = new YqlTokenType("NUMBER");
  IElementType R_BRACKET = new YqlTokenType("]");
  IElementType R_CURLY = new YqlTokenType("}");
  IElementType SINGLE_QUOTED_STRING = new YqlTokenType("SINGLE_QUOTED_STRING");
  IElementType STRING = new YqlTokenType("STRING");
  IElementType TRUE = new YqlTokenType("true");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ARRAY) {
        return new YqlArrayImpl(node);
      }
      else if (type == BASIC_KEYWORD) {
        return new YqlBasicKeywordImpl(node);
      }
      else if (type == BOOLEAN_LITERAL) {
        return new YqlBooleanLiteralImpl(node);
      }
      else if (type == NEAREST_NEIGHBOR_STATEMENT) {
        return new YqlNearestNeighborStatementImpl(node);
      }
      else if (type == NULL_LITERAL) {
        return new YqlNullLiteralImpl(node);
      }
      else if (type == NUMBER_LITERAL) {
        return new YqlNumberLiteralImpl(node);
      }
      else if (type == OBJECT) {
        return new YqlObjectImpl(node);
      }
      else if (type == PROPERTY) {
        return new YqlPropertyImpl(node);
      }
      else if (type == QUERY_PROPERTY) {
        return new YqlQueryPropertyImpl(node);
      }
      else if (type == QUERY_STATEMENT) {
        return new YqlQueryStatementImpl(node);
      }
      else if (type == REFERENCE_EXPRESSION) {
        return new YqlReferenceExpressionImpl(node);
      }
      else if (type == STRING_LITERAL) {
        return new YqlStringLiteralImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
