package com.pehrs.vespa.yql.plugin;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class YqlElementType extends IElementType {
  public YqlElementType(@NotNull @NonNls String debugName) {
    super(debugName, YqlLanguage.INSTANCE);
  }
}
