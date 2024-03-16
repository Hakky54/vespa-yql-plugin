package com.pehrs.vespa.yql.plugin;

import com.intellij.lexer.FlexAdapter;

public class YqlLexerAdapter extends FlexAdapter {

  public YqlLexerAdapter() {
    super(new YqlLexer());
  }

}
