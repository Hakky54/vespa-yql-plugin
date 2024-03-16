package com.pehrs.vespa.yql.plugin.psi.impl;

import com.intellij.json.psi.impl.JSStringLiteralEscaper;
import com.intellij.lang.ASTNode;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.LeafElement;
import org.jetbrains.annotations.NotNull;

public abstract class YqlStringLiteralMixin extends YqlLiteralImpl implements PsiLanguageInjectionHost {
  protected YqlStringLiteralMixin(ASTNode node) {
    super(node);
  }

  public boolean isValidHost() {
    return true;
  }

  public PsiLanguageInjectionHost updateText(@NotNull String text) {
    ASTNode valueNode = this.getNode().getFirstChildNode();

    assert valueNode instanceof LeafElement;

    ((LeafElement)valueNode).replaceWithText(text);
    return this;
  }

  public @NotNull LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
    return new JSStringLiteralEscaper<PsiLanguageInjectionHost>(this) {
      protected boolean isRegExpLiteral() {
        return false;
      }
    };
  }


  public void subtreeChanged() {
    // FIXME: Below does not compile!!!!!
    // this.putUserData(YqlPsiImplUtils.STRING_FRAGMENTS, (Object)null);
  }
}
