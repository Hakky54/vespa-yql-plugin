package com.pehrs.vespa.yql.plugin.psi.impl;


abstract class YqlPropertyMixin { //extends YqlElementImpl implements YqlProperty {
//  public YqlPropertyMixin(@NotNull ASTNode node) {
//    super(node);
//  }
//
//  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
//    YqlElementGenerator generator = new YqlElementGenerator(this.getProject());
//    this.getNameElement().replace(generator.createStringLiteral(StringUtil.unquoteString(name)));
//    return this;
//  }
//
//  public PsiReference getReference() {
//    return new YqlPropertyNameReference(this);
//  }
//
//  public PsiReference @NotNull [] getReferences() {
//    PsiReference[] fromProviders = ReferenceProvidersRegistry.getReferencesFromProviders(this);
//    PsiReference[] refs = (PsiReference[])ArrayUtil.prepend(new YqlPropertyNameReference(this), fromProviders);
//    if (refs == null) {
//      throw new RuntimeException("Could not get references for " + this);
//    }
//    return refs;
//  }
}
