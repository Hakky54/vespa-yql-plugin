// This is a generated file. Not intended for manual editing.
package com.pehrs.vespa.yql.plugin.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.pehrs.vespa.yql.plugin.YqlElementTypes.*;
import com.pehrs.vespa.yql.plugin.psi.*;
import com.intellij.navigation.ItemPresentation;

public class YqlQueryPropertyImpl extends YqlQueryPropertyMixin implements YqlQueryProperty {

  public YqlQueryPropertyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull YqlElementVisitor visitor) {
    visitor.visitQueryProperty(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof YqlElementVisitor) accept((YqlElementVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public YqlQueryStatement getQueryStatement() {
    return findChildByClass(YqlQueryStatement.class);
  }

  @Override
  public @NotNull String getName() {
    return YqlPsiImplUtils.getName(this);
  }

  @Override
  public @NotNull YqlValue getNameElement() {
    return YqlPsiImplUtils.getNameElement(this);
  }

  @Override
  public @Nullable YqlValue getValue() {
    return YqlPsiImplUtils.getValue(this);
  }

  @Override
  public @Nullable ItemPresentation getPresentation() {
    return YqlPsiImplUtils.getPresentation(this);
  }

}
