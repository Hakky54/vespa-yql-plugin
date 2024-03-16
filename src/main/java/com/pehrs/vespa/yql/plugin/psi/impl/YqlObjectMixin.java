package com.pehrs.vespa.yql.plugin.psi.impl;


import com.intellij.json.psi.impl.JsonContainerImpl;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.CachedValueProvider.Result;
import com.pehrs.vespa.yql.plugin.psi.YqlObject;
import com.pehrs.vespa.yql.plugin.psi.YqlProperty;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class YqlObjectMixin extends YqlContainerImpl implements YqlObject {
  private final CachedValueProvider<Map<String, YqlProperty>> myPropertyCache;

  public YqlObjectMixin(@NotNull ASTNode node) {
    super(node);
    this.myPropertyCache = () -> {
      Map<String, YqlProperty> cache = new HashMap();
      Iterator var2 = this.getPropertyList().iterator();

      while(var2.hasNext()) {
        YqlProperty property = (YqlProperty)var2.next();
        String propertyName = property.getName();
        if (!cache.containsKey(propertyName)) {
          cache.put(propertyName, property);
        }
      }

      return Result.createSingleDependency(cache, this);
    };
  }

  public @Nullable YqlProperty findProperty(@NotNull String name) {
    return (YqlProperty)((Map)CachedValuesManager.getCachedValue(this, this.myPropertyCache)).get(name);
  }
}
