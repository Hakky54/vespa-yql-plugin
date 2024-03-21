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

// %debug

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
