package com.pehrs.vespa.yql.plugin;


import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static com.pehrs.vespa.yql.plugin.YqlElementTypes.*;


%%

%{
  public YqlLexer() {
    this((java.io.Reader)null);
  }
%}

%{
  private static String zzToPrintable(CharSequence cs) {
    return zzToPrintable(cs.toString());
  }
%}

%public
%class YqlLexer
%implements FlexLexer
%function advance
%type IElementType

%debug

%unicode
%eof{  return;
%eof}

EOL=\R
WHITE_SPACE=\s+

// LINE_COMMENT="//".*
// BLOCK_COMMENT="/"\*([^*]|\*+[^*/])*(\*+"/")?
NUMBER=(-?(0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]*)?)|Infinity|-Infinity|NaN
STRING=([^\\\" ])+
IDENTIFIER=[[:jletterdigit:]~!()*\-."/"@\^<>=]+

%state IN_STRING

%%
<YYINITIAL> {
  {WHITE_SPACE}               { return WHITE_SPACE; }

  "\""                        { yybegin(IN_STRING); return DOUBLE_QUOTE; }

  "{"                         { return L_CURLY; }
  "}"                         { return R_CURLY; }
  "["                         { return L_BRACKET; }
  "]"                         { return R_BRACKET; }
  ","                         { return COMMA; }
  ":"                         { return COLON; }
  "true"                      { return TRUE; }
  "false"                     { return FALSE; }
  "null"                      { return NULL; }

  // {LINE_COMMENT}              { return LINE_COMMENT; }
  // {BLOCK_COMMENT}             { return BLOCK_COMMENT; }
  {NUMBER}                    { return NUMBER; }
  {IDENTIFIER}                { return IDENTIFIER; }
  // {STRING}                    { return STRING; }

}
<IN_STRING> {
  {WHITE_SPACE}               { return WHITE_SPACE; }
  {STRING}                    { return STRING; }
  "\""                        { yybegin(YYINITIAL); return DOUBLE_QUOTE; }
}

[^] { return BAD_CHARACTER; }

//CRLF=\R
//WHITE_SPACE=[\ \n\t\f]
//// FIRST_VALUE_CHARACTER=[^\n\f\\ ] | "\\"{CRLF} | "\\".
//// VALUE_CHARACTER=[^\n\f\\] | "\\"{CRLF} | "\\".
//VALUE_CHARACTER=[^\"\n\f\\ ] | "\\"{CRLF} | "\\".
//// END_OF_LINE_COMMENT=("#"|"!")[^\r\n]*
//// SEPARATOR=[:]
//EMPTY_STRING="\"\""
//DOUBLE_QUOTE="\""
//// KEY_CHARACTER=[^:=\ \n\t\f\\] | "\\ "
//
//FLOAT=(-?(0|[1-9][0-9]*)(\.[0-9]+)?([eE][+-]?[0-9]*)?)|Infinity|-Infinity|NaN
//INTEGER=(-?(0|[1-9][0-9]*))|Infinity|-Infinity|NaN
//
//%state WAITING_VALUE
//
//%%
//
//  "{"                         { return L_CURLY; }
//  "}"                         { return R_CURLY; }
//  "["                         { return L_BRACKET; }
//  "]"                         { return R_BRACKET; }
//  ","                         { return COMMA; }
//  ":"                         { return COLON; }
//  "true"                      { return TRUE; }
//  "false"                     { return FALSE; }
//  "null"                      { return NULL; }
//
//
//
//// <YYINITIAL> {END_OF_LINE_COMMENT}                           { yybegin(YYINITIAL); return COMMENT; }
//
//// <YYINITIAL> {KEY_CHARACTER}+                                { yybegin(YYINITIAL); return KEY; }
//
//// <YYINITIAL> {SEPARATOR}                                     { yybegin(WAITING_VALUE); return SEPARATOR; }
//
//{EMPTY_STRING}                                  { yybegin(YYINITIAL); return EMPTY_STRING; }
//
//{DOUBLE_QUOTE}                                  { yybegin(YYINITIAL); return DOUBLE_QUOTE; }
//
//{CRLF}({CRLF}|{WHITE_SPACE})+               { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
//
//{WHITE_SPACE}+                              { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
//
//
//// FIXME: These will NEVER MATCH as the VALUE_CHARACTER* will match before this
//
//{FLOAT}                                         { return FLOAT; }
//{INTEGER}                                       { return INTEGER; }
//
//
//// <YYINITIAL> {FIRST_VALUE_CHARACTER}{VALUE_CHARACTER}*       { yybegin(YYINITIAL); return VALUE; }
//{VALUE_CHARACTER}*                              { yybegin(YYINITIAL); return VALUE; }
//
//
//({CRLF}|{WHITE_SPACE})+                                     { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }
//
//[^]                                                         { return TokenType.BAD_CHARACTER; }
