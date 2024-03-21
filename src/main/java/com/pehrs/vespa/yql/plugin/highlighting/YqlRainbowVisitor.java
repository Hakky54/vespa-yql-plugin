package com.pehrs.vespa.yql.plugin.highlighting;

import com.intellij.codeInsight.daemon.RainbowVisitor;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.pehrs.vespa.yql.plugin.psi.YqlFile;
import com.pehrs.vespa.yql.plugin.psi.YqlQueryProperty;
import com.pehrs.vespa.yql.plugin.psi.YqlQueryValue;
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

  public YqlRainbowVisitor() {
    super();
  }

  @Override
  public boolean analyze(@NotNull PsiFile file, boolean updateWholeFile,
      @NotNull HighlightInfoHolder holder, @NotNull Runnable action) {
    return super.analyze(file, updateWholeFile, holder, action);
  }

  @Override
  public boolean suitableForFile(@NotNull PsiFile file) {
    return file instanceof YqlFile;
  }

  @Override
  public void visit(@NotNull PsiElement element) {
    PsiFile file = element.getContainingFile();

//    if (element instanceof YqlQueryProperty) {
//      YqlQueryProperty property = (YqlQueryProperty) element;
//      @NotNull YqlQueryValue queryValue = property.getQueryValue();
//      queryValue.getBasicKeywordList().stream()
//          .forEach(yqlBasicKeyword -> {
//            addInfo(getInfo(file, yqlBasicKeyword.getFirstChild(), "???", YqlSyntaxHighlighterFactory.YQL_KEYWORD));
//          });
//    }

//     if (element instanceof YqlProperty) {
//      PsiFile file = element.getContainingFile();
//      String fileName = file.getName();
//      if (Holder.blacklist.containsKey(fileName)) {
//        JsonPointerPosition position = JsonOriginalPsiWalker.INSTANCE.findPosition(element, false);
//        if (position != null && Holder.blacklist.get(fileName).contains(position.toJsonPointer())) return;
//      }
//      String name = ((YqlProperty)element).getName();
//      addInfo(getInfo(file, ((YqlProperty)element).getNameElement(), name, YqlSyntaxHighlighterFactory.YQL_PROPERTY_KEY));
//       YqlValue value = ((YqlProperty)element).getValue();
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
// }
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
