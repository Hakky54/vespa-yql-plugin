// This is a generated file. Not intended for manual editing.
package com.pehrs.vespa.yql.plugin.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class YqlVisitor extends PsiElementVisitor {

  public void visitArray(@NotNull YqlArray o) {
    visitPsiElement(o);
  }

  public void visitBasicKeyword(@NotNull YqlBasicKeyword o) {
    visitPsiElement(o);
  }

  public void visitObject(@NotNull YqlObject o) {
    visitPsiElement(o);
  }

  public void visitProperty(@NotNull YqlProperty o) {
    visitPsiElement(o);
  }

  public void visitPropertyKey(@NotNull YqlPropertyKey o) {
    visitPsiElement(o);
  }

  public void visitPropertyValue(@NotNull YqlPropertyValue o) {
    visitPsiElement(o);
  }

  public void visitQueryProperty(@NotNull YqlQueryProperty o) {
    visitPsiElement(o);
  }

  public void visitQueryValue(@NotNull YqlQueryValue o) {
    visitPsiElement(o);
  }

  public void visitStringLiteral(@NotNull YqlStringLiteral o) {
    visitPsiElement(o);
  }

  public void visitStringValue(@NotNull YqlStringValue o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
