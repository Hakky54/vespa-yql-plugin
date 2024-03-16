package com.pehrs.vespa.yql.plugin.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.pehrs.vespa.yql.plugin.psi.YqlElement;
import org.jetbrains.annotations.NotNull;

public class YqlElementImpl extends ASTWrapperPsiElement implements YqlElement {
  public YqlElementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public String toString() {
    String className = this.getClass().getSimpleName();
    return StringUtil.trimEnd(className, "Impl");
  }
}
