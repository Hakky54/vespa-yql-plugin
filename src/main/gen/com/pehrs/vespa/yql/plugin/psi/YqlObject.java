// This is a generated file. Not intended for manual editing.
package com.pehrs.vespa.yql.plugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;

public interface YqlObject extends YqlContainer {

  @NotNull
  List<YqlProperty> getPropertyList();

  @Nullable YqlProperty findProperty(@NotNull String name);

  @Nullable ItemPresentation getPresentation();

}
