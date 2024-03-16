package com.pehrs.vespa.yql.plugin;


import com.intellij.lang.Language;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class YqlTokenType extends IElementType {
  public YqlTokenType(@NotNull @NonNls String debugName) {
    super(debugName, YqlLanguage.INSTANCE);
  }

  public static IElementType DOUBLE_QUOTE = new IElementType("DOUBLE_QUOTE", YqlLanguage.INSTANCE);


}
