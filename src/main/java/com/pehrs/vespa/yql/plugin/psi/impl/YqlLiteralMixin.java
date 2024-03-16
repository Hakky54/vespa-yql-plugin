package com.pehrs.vespa.yql.plugin.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.pehrs.vespa.yql.plugin.psi.YqlLiteral;
import org.jetbrains.annotations.NotNull;

public abstract class YqlLiteralMixin extends YqlElementImpl implements YqlLiteral {
  protected YqlLiteralMixin(@NotNull ASTNode node) {
    super(node);
  }

  public PsiReference @NotNull [] getReferences() {
    PsiReference[] refs = ReferenceProvidersRegistry.getReferencesFromProviders(this);
    if (refs == null) {
      throw new RuntimeException("Could not get any references for " + this);
    }
    return refs;
  }
}
