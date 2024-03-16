// This is a generated file. Not intended for manual editing.
package com.pehrs.vespa.yql.plugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.navigation.ItemPresentation;

public interface YqlProperty extends YqlElement, PsiNamedElement {

  @NotNull String getName();

  @NotNull YqlValue getNameElement();

  @Nullable YqlValue getValue();

  @Nullable ItemPresentation getPresentation();

}
