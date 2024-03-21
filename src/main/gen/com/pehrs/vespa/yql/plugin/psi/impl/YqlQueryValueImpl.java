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

public class YqlQueryValueImpl extends YqlElementImpl implements YqlQueryValue {

  public YqlQueryValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull YqlVisitor visitor) {
    visitor.visitQueryValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof YqlVisitor) accept((YqlVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<YqlBasicKeyword> getBasicKeywordList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, YqlBasicKeyword.class);
  }

  @Override
  @NotNull
  public List<YqlStringValue> getStringValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, YqlStringValue.class);
  }

}
