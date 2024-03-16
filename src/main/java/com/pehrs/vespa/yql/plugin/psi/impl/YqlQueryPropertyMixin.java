package com.pehrs.vespa.yql.plugin.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.pehrs.vespa.yql.plugin.psi.YqlElementGenerator;
import com.pehrs.vespa.yql.plugin.psi.YqlProperty;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class YqlQueryPropertyMixin extends YqlElementImpl implements YqlProperty {
  public YqlQueryPropertyMixin(@NotNull ASTNode node) {
    super(node);
  }

  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    YqlElementGenerator generator = new YqlElementGenerator(this.getProject());
    this.getNameElement().replace(generator.createStringLiteral(StringUtil.unquoteString(name)));
    return this;
  }

  public PsiReference getReference() {
    return new YqlPropertyNameReference(this);
  }

  public PsiReference @NotNull [] getReferences() {
    PsiReference[] fromProviders = ReferenceProvidersRegistry.getReferencesFromProviders(this);
    PsiReference[] refs = (PsiReference[]) ArrayUtil.prepend(new YqlPropertyNameReference(this), fromProviders);
    if (refs == null) {
      throw new RuntimeException("Could not get references for " + this);
    }
    return refs;
  }
}