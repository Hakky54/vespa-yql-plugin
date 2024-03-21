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
  // 'select' | 'from' | 'where' | 'order by' | 'limit' | 'offset' | 'timeout' | 'nearestNeighbor' | 'weightedSet' | 'predicate' | 'dotProduct' | 'userQuery' | 'nonEmpty' | 'userInput' | 'geoLocation' | 'sameElement' | 'matches' | 'range' | 'contains' | 'weakAnd' | 'phrase' | 'fuzzy' | 'equiv' | 'onear' | 'wand' | 'true' | 'false' | 'rank' | 'near' | 'and' | 'not' | 'uri' | 'or'
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
    if (!r) r = consumeToken(b, "nearestNeighbor");
    if (!r) r = consumeToken(b, "weightedSet");
    if (!r) r = consumeToken(b, "predicate");
    if (!r) r = consumeToken(b, "dotProduct");
    if (!r) r = consumeToken(b, "userQuery");
    if (!r) r = consumeToken(b, "nonEmpty");
    if (!r) r = consumeToken(b, "userInput");
    if (!r) r = consumeToken(b, "geoLocation");
    if (!r) r = consumeToken(b, "sameElement");
    if (!r) r = consumeToken(b, "matches");
    if (!r) r = consumeToken(b, "range");
    if (!r) r = consumeToken(b, "contains");
    if (!r) r = consumeToken(b, "weakAnd");
    if (!r) r = consumeToken(b, "phrase");
    if (!r) r = consumeToken(b, "fuzzy");
    if (!r) r = consumeToken(b, "equiv");
    if (!r) r = consumeToken(b, "onear");
    if (!r) r = consumeToken(b, "wand");
    if (!r) r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, FALSE);
    if (!r) r = consumeToken(b, "rank");
    if (!r) r = consumeToken(b, "near");
    if (!r) r = consumeToken(b, "and");
    if (!r) r = consumeToken(b, "not");
    if (!r) r = consumeToken(b, "uri");
    if (!r) r = consumeToken(b, "or");
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
  // (query_property | property) (','|&'}')
  static boolean object_element(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "object_element")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = object_element_0(b, l + 1);
    p = r; // pin = 1
    r = r && object_element_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
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
  // object | array | string_literal | NUMBER | TRUE | FALSE
  public static boolean property_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY_VALUE, "<property value>");
    r = object(b, l + 1);
    if (!r) r = array(b, l + 1);
    if (!r) r = string_literal(b, l + 1);
    if (!r) r = consumeToken(b, NUMBER);
    if (!r) r = consumeToken(b, TRUE);
    if (!r) r = consumeToken(b, FALSE);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // '"yql"' ':' query_value
  public static boolean query_property(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query_property")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, QUERY_PROPERTY, "<query property>");
    r = consumeToken(b, "\"yql\"");
    r = r && consumeToken(b, COLON);
    r = r && query_value(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // DOUBLE_QUOTE (basic_keyword | string_value)* DOUBLE_QUOTE
  public static boolean query_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query_value")) return false;
    if (!nextTokenIs(b, DOUBLE_QUOTE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOUBLE_QUOTE);
    r = r && query_value_1(b, l + 1);
    r = r && consumeToken(b, DOUBLE_QUOTE);
    exit_section_(b, m, QUERY_VALUE, r);
    return r;
  }

  // (basic_keyword | string_value)*
  private static boolean query_value_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query_value_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!query_value_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "query_value_1", c)) break;
    }
    return true;
  }

  // basic_keyword | string_value
  private static boolean query_value_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "query_value_1_0")) return false;
    boolean r;
    r = basic_keyword(b, l + 1);
    if (!r) r = string_value(b, l + 1);
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
