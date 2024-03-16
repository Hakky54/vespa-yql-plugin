// This is a generated file. Not intended for manual editing.
package com.pehrs.vespa.yql.plugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;

public interface YqlArray extends YqlContainer {

  @NotNull
  List<YqlValue> getValueList();

  @Nullable ItemPresentation getPresentation();

}
