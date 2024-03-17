// This is a generated file. Not intended for manual editing.
package com.pehrs.vespa.yql.plugin;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.pehrs.vespa.yql.plugin.psi.impl.*;

public interface YqlElementTypes {

  IElementType ARRAY = new YqlElementType("ARRAY");
  IElementType OBJECT = new YqlElementType("OBJECT");
  IElementType PROPERTY = new YqlElementType("PROPERTY");
  IElementType PROPERTY_KEY = new YqlElementType("PROPERTY_KEY");
  IElementType PROPERTY_VALUE = new YqlElementType("PROPERTY_VALUE");
  IElementType STRING_LITERAL = new YqlElementType("STRING_LITERAL");
  IElementType STRING_VALUE = new YqlElementType("STRING_VALUE");

  IElementType COLON = new YqlTokenType(":");
  IElementType COMMA = new YqlTokenType(",");
  IElementType DOUBLE_QUOTE = new YqlTokenType("DOUBLE_QUOTE");
  IElementType FALSE = new YqlTokenType("false");
  IElementType IDENTIFIER = new YqlTokenType("IDENTIFIER");
  IElementType L_BRACKET = new YqlTokenType("[");
  IElementType L_CURLY = new YqlTokenType("{");
  IElementType NULL = new YqlTokenType("null");
  IElementType NUMBER = new YqlTokenType("NUMBER");
  IElementType R_BRACKET = new YqlTokenType("]");
  IElementType R_CURLY = new YqlTokenType("}");
  IElementType STRING = new YqlTokenType("STRING");
  IElementType TRUE = new YqlTokenType("true");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ARRAY) {
        return new YqlArrayImpl(node);
      }
      else if (type == OBJECT) {
        return new YqlObjectImpl(node);
      }
      else if (type == PROPERTY) {
        return new YqlPropertyImpl(node);
      }
      else if (type == PROPERTY_KEY) {
        return new YqlPropertyKeyImpl(node);
      }
      else if (type == PROPERTY_VALUE) {
        return new YqlPropertyValueImpl(node);
      }
      else if (type == STRING_LITERAL) {
        return new YqlStringLiteralImpl(node);
      }
      else if (type == STRING_VALUE) {
        return new YqlStringValueImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
