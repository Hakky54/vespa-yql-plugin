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

public class YqlArrayImpl extends YqlContainerImpl implements YqlArray {

  public YqlArrayImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull YqlElementVisitor visitor) {
    visitor.visitArray(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof YqlElementVisitor) accept((YqlElementVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<YqlValue> getValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, YqlValue.class);
  }

  @Override
  public @Nullable ItemPresentation getPresentation() {
    return YqlPsiImplUtils.getPresentation(this);
  }

}
