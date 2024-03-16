// This is a generated file. Not intended for manual editing.
package com.pehrs.vespa.yql.plugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;

public interface YqlQueryProperty extends PsiElement {

  @Nullable
  YqlQueryStatement getQueryStatement();

  @NotNull String getName();

  @NotNull YqlValue getNameElement();

  @Nullable YqlValue getValue();

  //WARNING: value(...) is skipped
  //matching value(YqlQueryProperty, ...)
  //methods are not found in YqlPsiImplUtils

  @Nullable ItemPresentation getPresentation();

}
