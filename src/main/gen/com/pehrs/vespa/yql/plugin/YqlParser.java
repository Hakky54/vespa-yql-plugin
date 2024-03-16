// This is a generated file. Not intended for manual editing.
package com.pehrs.vespa.yql.plugin;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.pehrs.vespa.yql.plugin.YqlElementTypes.*;
import static com.pehrs.vespa.yql.plugin.psi.YqlParserUtil.*;
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
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
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

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(ARRAY, BOOLEAN_LITERAL, LITERAL, NULL_LITERAL,
      NUMBER_LITERAL, OBJECT, REFERENCE_EXPRESSION, STRING_LITERAL,
      VALUE),
  };

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
  // value (','|&']')
  static boolean array_element(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_element")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = value(b, l + 1);
    p = r; // pin = 1
    r = r && array_element_1(b, l + 1);
    exit_section_(b, l, m, r, p, YqlParser::not_bracket_or_next_value);
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
  // 'select'| 'from'| 'where'| 'order by'| 'limit'| 'offset'| 'timeout'
  public static boolean basic_keyword(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "basic_keyword")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BASIC_KEYWORD, "<basic keyword>");
    r = consumeToken(b, "select");
    if (!r) r = consumeToken(b, "from");
    if (!r) r = consumeToken(b, "where");
    if (!r) r = consumeToken(b, "order by");
    if (!r) r = consumeToken(b, "limit");
    if (!r) r = consumeToken(b, "offset");
    if (!r) r = consumeToken(b, "timeout");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TRUE | FALSE
  public static boolean boolean_literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "boolean_literal")) return false;
    if (!nextTokenIs(b, "<boolean literal>", FALSE, TRUE)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BOOLEAN_LITERAL, "<boolean literal>");
    r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, FALSE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // string_literal | number_literal | boolean_literal | null_literal
  public static boolean literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, LITERAL, "<literal>");
    r = string_literal(b, l + 1);
    if (!r) r = number_literal(b, l + 1);
    if (!r) r = boolean_literal(b, l + 1);
    if (!r) r = null_literal(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '{' string_literal ':' NUMBER '}nearestNeighbor(' string_literal COMMA string_literal ')'
  public static boolean nearest_neighbor_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nearest_neighbor_statement")) return false;
    if (!nextTokenIs(b, L_CURLY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, L_CURLY);
    r = r && string_literal(b, l + 1);
    r = r && consumeTokens(b, 0, COLON, NUMBER);
    r = r && consumeToken(b, "}nearestNeighbor(");
    r = r && string_literal(b, l + 1);
    r = r && consumeToken(b, COMMA);
    r = r && string_literal(b, l + 1);
    r = r && consumeToken(b, ")");
    exit_section_(b, m, NEAREST_NEIGHBOR_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // !('}'|value)
  static boolean not_brace_or_next_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_brace_or_next_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !not_brace_or_next_value_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // '}'|value
  private static boolean not_brace_or_next_value_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_brace_or_next_value_0")) return false;
    boolean r;
    r = consumeToken(b, R_CURLY);
    if (!r) r = value(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // !(']'|value)
  static boolean not_bracket_or_next_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_bracket_or_next_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !not_bracket_or_next_value_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ']'|value
  private static boolean not_bracket_or_next_value_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "not_bracket_or_next_value_0")) return false;
    boolean r;
    r = consumeToken(b, R_BRACKET);
    if (!r) r = value(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // NULL
  public static boolean null_literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "null_literal")) return false;
    if (!nextTokenIs(b, NULL)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NULL);
    exit_section_(b, m, NULL_LITERAL, r);
    return r;
  }

  /* ********************************************************** */
  // NUMBER
  public static boolean number_literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "number_literal")) return false;
    if (!nextTokenIs(b, NUMBER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NUMBER);
    exit_section_(b, m, NUMBER_LITERAL, r);
    return r;
  }

  /* ********************************************************** */
  // '{' object_element* '}'
  public static boolean object(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object")) return false;
    if (!nextTokenIs(b, L_CURLY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OBJECT, null);
    r = consumeToken(b, L_CURLY);
    p = r; // pin = 1
    r = r && report_error_(b, object_1(b, l + 1));
    r = p && consumeToken(b, R_CURLY) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // object_element*
  private static boolean object_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!object_element(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "object_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // (query_property | property ) (','|&'}')
  static boolean object_element(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_element")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = object_element_0(b, l + 1);
    p = r; // pin = 1
    r = r && object_element_1(b, l + 1);
    exit_section_(b, l, m, r, p, YqlParser::not_brace_or_next_value);
    return r || p;
  }

  // query_property | property
  private static boolean object_element_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_element_0")) return false;
    boolean r;
    r = query_property(b, l + 1);
    if (!r) r = property(b, l + 1);
    return r;
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
  // property_name (':' property_value)
  public static boolean property(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY, "<property>");
    r = property_name(b, l + 1);
    p = r; // pin = 1
    r = r && property_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ':' property_value
  private static boolean property_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COLON);
    p = r; // pin = 1
    r = r && property_value(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // literal | reference_expression
  static boolean property_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_name")) return false;
    boolean r;
    r = literal(b, l + 1);
    if (!r) r = reference_expression(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // value
  static boolean property_value(PsiBuilder b, int l) {
    return value(b, l + 1);
  }

  /* ********************************************************** */
  // '"yql"' (':' DOUBLE_QUOTED_STRING)
  public static boolean query_property(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query_property")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, QUERY_PROPERTY, "<query property>");
    r = consumeToken(b, "\"yql\"");
    p = r; // pin = 1
    r = r && query_property_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ':' DOUBLE_QUOTED_STRING
  private static boolean query_property_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query_property_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeTokens(b, 1, COLON, DOUBLE_QUOTED_STRING);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '"' (basic_keyword)* '"'
  public static boolean query_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query_statement")) return false;
    if (!nextTokenIs(b, DOUBLE_QUOTE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, QUERY_STATEMENT, null);
    r = consumeToken(b, DOUBLE_QUOTE);
    p = r; // pin = 1
    r = r && report_error_(b, query_statement_1(b, l + 1));
    r = p && consumeToken(b, DOUBLE_QUOTE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (basic_keyword)*
  private static boolean query_statement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query_statement_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!query_statement_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "query_statement_1", c)) break;
    }
    return true;
  }

  // (basic_keyword)
  private static boolean query_statement_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query_statement_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = basic_keyword(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean reference_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "reference_expression")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IDENTIFIER);
    exit_section_(b, m, REFERENCE_EXPRESSION, r);
    return r;
  }

  /* ********************************************************** */
  // SINGLE_QUOTED_STRING | DOUBLE_QUOTED_STRING
  public static boolean string_literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_literal")) return false;
    if (!nextTokenIs(b, "<string literal>", DOUBLE_QUOTED_STRING, SINGLE_QUOTED_STRING)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING_LITERAL, "<string literal>");
    r = consumeToken(b, SINGLE_QUOTED_STRING);
    if (!r) r = consumeToken(b, DOUBLE_QUOTED_STRING);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // object | array | literal | reference_expression
  public static boolean value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, VALUE, "<value>");
    r = object(b, l + 1);
    if (!r) r = array(b, l + 1);
    if (!r) r = literal(b, l + 1);
    if (!r) r = reference_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // value*
  static boolean yql(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "yql")) return false;
    while (true) {
      int c = current_position_(b);
      if (!value(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "yql", c)) break;
    }
    return true;
  }

}
