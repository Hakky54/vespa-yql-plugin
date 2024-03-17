package com.pehrs.vespa.yql.plugin.psi.impl;


public abstract class YqlObjectMixin{ // extends YqlContainerImpl implements YqlObject {
//  private final CachedValueProvider<Map<String, YqlProperty>> myPropertyCache;
//
//  public YqlObjectMixin(@NotNull ASTNode node) {
//    super(node);
//    this.myPropertyCache = () -> {
//      Map<String, YqlProperty> cache = new HashMap();
//      Iterator var2 = this.getPropertyList().iterator();
//
//      while(var2.hasNext()) {
//        YqlProperty property = (YqlProperty)var2.next();
//        String propertyName = property.getName();
//        if (!cache.containsKey(propertyName)) {
//          cache.put(propertyName, property);
//        }
//      }
//
//      return Result.createSingleDependency(cache, this);
//    };
//  }
//
//  public @Nullable YqlProperty findProperty(@NotNull String name) {
//    return (YqlProperty)((Map)CachedValuesManager.getCachedValue(this, this.myPropertyCache)).get(name);
//  }
}
