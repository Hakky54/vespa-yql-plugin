package com.pehrs.vespa.yql.plugin.psi.impl;

public class YqlPropertyNameReference { //implements PsiReference {
//  private final YqlProperty myProperty;
//
//  public YqlPropertyNameReference(@NotNull YqlProperty property) {
//    super();
//    this.myProperty = property;
//  }
//
//  public @NotNull PsiElement getElement() {
//    return this.myProperty;
//  }
//
//  public @NotNull TextRange getRangeInElement() {
//    YqlValue nameElement = this.myProperty.getNameElement();
//    TextRange range = ElementManipulators.getValueTextRange(nameElement);
//    if (range == null) {
//      throw new RuntimeException("Could not get text range");
//    }
//    return range;
//  }
//
//  public @Nullable PsiElement resolve() {
//    return this.myProperty;
//  }
//
//  public @NotNull String getCanonicalText() {
//    String name = this.myProperty.getName();
//    if (name == null) {
//      throw new RuntimeException("Could not get name");
//    }
//    return name;
//  }
//
//  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
//    return this.myProperty.setName(newElementName);
//  }
//
//  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
//    return null;
//  }
//
//  public boolean isReferenceTo(@NotNull PsiElement element) {
//    if (element == null) {
//      throw new RuntimeException("No element provided as parameter");
//    }
//
//    if (!(element instanceof YqlProperty otherProperty)) {
//      return false;
//    } else {
//      PsiElement selfResolve = this.resolve();
//      return otherProperty.getName().equals(this.getCanonicalText()) && selfResolve != otherProperty;
//    }
//  }
//
//  public boolean isSoft() {
//    return true;
//  }
}
