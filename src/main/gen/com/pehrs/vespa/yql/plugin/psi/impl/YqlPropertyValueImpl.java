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

public class YqlPropertyValueImpl extends YqlElementImpl implements YqlPropertyValue {

  public YqlPropertyValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull YqlVisitor visitor) {
    visitor.visitPropertyValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof YqlVisitor) accept((YqlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public YqlArray getArray() {
    return findChildByClass(YqlArray.class);
  }

  @Override
  @Nullable
  public YqlObject getObject() {
    return findChildByClass(YqlObject.class);
  }

  @Override
  @Nullable
  public YqlStringLiteral getStringLiteral() {
    return findChildByClass(YqlStringLiteral.class);
  }

}
