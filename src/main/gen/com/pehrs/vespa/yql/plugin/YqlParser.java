// This is a generated file. Not intended for manual editing.
package com.pehrs.vespa.yql.plugin;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.pehrs.vespa.yql.plugin.YqlElementTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class YqlParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return yql(b, l + 1);
  }

  /* ********************************************************** */
  // '[' array_element* ']'
  public static boolean array(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array")) return false;
    if (!nextTokenIs(b, L_BRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ARRAY, null);
    r = consumeToken(b, L_BRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, array_1(b, l + 1));
    r = p && consumeToken(b, R_BRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // array_element*
  private static boolean array_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!array_element(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "array_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // property_value (','|&']')
  static boolean array_element(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_element")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = property_value(b, l + 1);
    p = r; // pin = 1
    r = r && array_element_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ','|&']'
  private static boolean array_element_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_element_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = array_element_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &']'
  private static boolean array_element_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_element_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, R_BRACKET);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (object)
  static boolean item_(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "item_")) return false;
    if (!nextTokenIs(b, L_CURLY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = object(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '{' (object_element)* '}'
  public static boolean object(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object")) return false;
    if (!nextTokenIs(b, L_CURLY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, L_CURLY);
    r = r && object_1(b, l + 1);
    r = r && consumeToken(b, R_CURLY);
    exit_section_(b, m, OBJECT, r);
    return r;
  }

  // (object_element)*
  private static boolean object_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!object_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "object_1", c)) break;
    }
    return true;
  }

  // (object_element)
  private static boolean object_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = object_element(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // property (','|&'}')
  static boolean object_element(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_element")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = property(b, l + 1);
    p = r; // pin = 1
    r = r && object_element_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ','|&'}'
  private static boolean object_element_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_element_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    if (!r) r = object_element_1_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // &'}'
  private static boolean object_element_1_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_element_1_1")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, R_CURLY);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // property_key ':' property_value
  public static boolean property(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property")) return false;
    if (!nextTokenIs(b, DOUBLE_QUOTE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = property_key(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && property_value(b, l + 1);
    exit_section_(b, m, PROPERTY, r);
    return r;
  }

  /* ********************************************************** */
  // string_literal
  public static boolean property_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key")) return false;
    if (!nextTokenIs(b, DOUBLE_QUOTE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = string_literal(b, l + 1);
    exit_section_(b, m, PROPERTY_KEY, r);
    return r;
  }

  /* ********************************************************** */
  // object | array | string_literal | NUMBER
  public static boolean property_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY_VALUE, "<property value>");
    r = object(b, l + 1);
    if (!r) r = array(b, l + 1);
    if (!r) r = string_literal(b, l + 1);
    if (!r) r = consumeToken(b, NUMBER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // DOUBLE_QUOTE string_value* DOUBLE_QUOTE
  public static boolean string_literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_literal")) return false;
    if (!nextTokenIs(b, DOUBLE_QUOTE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOUBLE_QUOTE);
    r = r && string_literal_1(b, l + 1);
    r = r && consumeToken(b, DOUBLE_QUOTE);
    exit_section_(b, m, STRING_LITERAL, r);
    return r;
  }

  // string_value*
  private static boolean string_literal_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_literal_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!string_value(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "string_literal_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // IDENTIFIER | NUMBER | STRING
  public static boolean string_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING_VALUE, "<string value>");
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, NUMBER);
    if (!r) r = consumeToken(b, STRING);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // item_*
  static boolean yql(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "yql")) return false;
    while (true) {
      int c = current_position_(b);
      if (!item_(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "yql", c)) break;
    }
    return true;
  }

}
