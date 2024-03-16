// This is a generated file. Not intended for manual editing.
package com.pehrs.vespa.yql.plugin.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;

public class YqlElementVisitor extends PsiElementVisitor {

  public void visitArray(@NotNull YqlArray o) {
    visitContainer(o);
  }

  public void visitBasicKeyword(@NotNull YqlBasicKeyword o) {
    visitPsiElement(o);
  }

  public void visitBooleanLiteral(@NotNull YqlBooleanLiteral o) {
    visitLiteral(o);
  }

  public void visitContainer(@NotNull YqlContainer o) {
    visitValue(o);
  }

  public void visitLiteral(@NotNull YqlLiteral o) {
    visitValue(o);
  }

  public void visitNearestNeighborStatement(@NotNull YqlNearestNeighborStatement o) {
    visitPsiElement(o);
  }

  public void visitNullLiteral(@NotNull YqlNullLiteral o) {
    visitLiteral(o);
  }

  public void visitNumberLiteral(@NotNull YqlNumberLiteral o) {
    visitLiteral(o);
  }

  public void visitObject(@NotNull YqlObject o) {
    visitContainer(o);
  }

  public void visitProperty(@NotNull YqlProperty o) {
    visitElement(o);
    // visitPsiNamedElement(o);
  }

  public void visitQueryProperty(@NotNull YqlQueryProperty o) {
    visitPsiElement(o);
  }

  public void visitQueryStatement(@NotNull YqlQueryStatement o) {
    visitPsiElement(o);
  }

  public void visitReferenceExpression(@NotNull YqlReferenceExpression o) {
    visitValue(o);
  }

  public void visitStringLiteral(@NotNull YqlStringLiteral o) {
    visitLiteral(o);
  }

  public void visitValue(@NotNull YqlValue o) {
    visitElement(o);
  }

  public void visitElement(@NotNull YqlElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
