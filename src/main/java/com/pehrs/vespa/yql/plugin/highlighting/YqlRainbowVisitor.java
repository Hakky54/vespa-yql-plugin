// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.pehrs.vespa.yql.plugin.highlighting;

import com.intellij.codeInsight.daemon.RainbowVisitor;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.pehrs.vespa.yql.plugin.psi.YqlFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public final class YqlRainbowVisitor extends RainbowVisitor {
  private static final class Holder {
    private static final Map<String, Set<String>> blacklist = createBlacklist();

    private static Map<String, Set<String>> createBlacklist() {
      Map<String, Set<String>> blacklist = new HashMap<>();
      blacklist.put("package.yql", Set.of("/dependencies",
                                                      "/devDependencies",
                                                      "/peerDependencies",
                                                      "/scripts",
                                                      "/directories",
                                                      "/optionalDependencies"));
      return blacklist;
    }
  }

  @Override
  public boolean suitableForFile(@NotNull PsiFile file) {
    return file instanceof YqlFile;
  }

  @Override
  public void visit(@NotNull PsiElement element) {
//    if (element instanceof YqlProperty) {
//      PsiFile file = element.getContainingFile();
//      String fileName = file.getName();
//      if (Holder.blacklist.containsKey(fileName)) {
//        JsonPointerPosition position = JsonOriginalPsiWalker.INSTANCE.findPosition(element, false);
//        if (position != null && Holder.blacklist.get(fileName).contains(position.toJsonPointer())) return;
//      }
//      String name = ((YqlProperty)element).getName();
//      addInfo(getInfo(file, ((YqlProperty)element).getNameElement(), name, YqlSyntaxHighlighterFactory.YQL_PROPERTY_KEY));
//      YqlValue value = ((YqlProperty)element).getValue();
//      if (value instanceof YqlObject) {
//        addInfo(getInfo(file, value.getFirstChild(), name, YqlSyntaxHighlighterFactory.YQL_BRACES));
//        addInfo(getInfo(file, value.getLastChild(), name, YqlSyntaxHighlighterFactory.YQL_BRACES));
//      }
//      else if (value instanceof YqlArray) {
//        addInfo(getInfo(file, value.getFirstChild(), name, YqlSyntaxHighlighterFactory.YQL_BRACKETS));
//        addInfo(getInfo(file, value.getLastChild(), name, YqlSyntaxHighlighterFactory.YQL_BRACKETS));
//        for (YqlValue array : ((YqlArray)value).getValueList()) {
//          addSimpleValueInfo(name, file, array);
//        }
//      }
//      else {
//        addSimpleValueInfo(name, file, value);
//      }
//    }
  }

//  private void addSimpleValueInfo(String name, PsiFile file, YqlValue value) {
//    if (value instanceof YqlStringLiteral) {
//      addInfo(getInfo(file, value, name, YqlSyntaxHighlighterFactory.YQL_STRING));
//    }
//    else if (value instanceof YqlNumberLiteral) {
//      addInfo(getInfo(file, value, name, YqlSyntaxHighlighterFactory.YQL_NUMBER));
//    }
//    else if (value instanceof YqlLiteral) {
//      addInfo(getInfo(file, value, name, YqlSyntaxHighlighterFactory.YQL_KEYWORD));
//    }
//  }

  @Override
  public @NotNull HighlightVisitor clone() {
    return new YqlRainbowVisitor();
  }
}
