package com.pehrs.vespa.yql.plugin;

import com.intellij.lang.Language;

public class YqlLanguage extends Language {

  public static final YqlLanguage INSTANCE = new YqlLanguage();

  private YqlLanguage() {
    super("YQL");
  }

  public boolean isCaseSensitive() {
    return true;
  }

  public boolean hasPermissiveStrings() {
    return false;
  }

}
