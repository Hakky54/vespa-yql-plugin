package com.pehrs.vespa.yql.plugin.completions;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.util.ProcessingContext;
import com.pehrs.vespa.yql.plugin.YQL;
import com.pehrs.vespa.yql.plugin.YqlElementTypes;
import com.pehrs.vespa.yql.plugin.YqlFileType;
import com.pehrs.vespa.yql.plugin.psi.YqlFile;
import com.pehrs.vespa.yql.plugin.psi.YqlQueryProperty;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

final public class YqlCompletionContributor extends CompletionContributor {

  static List<LookupElementBuilder> YQL_LOOKUP_ELEMENTS =
      YQL.YQL_KEYWORDS.stream().map(kw -> LookupElementBuilder.create(kw))
          .collect(Collectors.toList());

  static List<String> OTHER_INSERTS = List.of(
      "\"yql\": \"select * from ...\"",
      "\"input\": {}",
      "\"ranking\": \"\"",
      "\"trace\": {\n"
          + "        \"level\": 5,\n"
          + "        \"timestamps\": true\n"
          + "    },"
  );
  static List<LookupElementBuilder> YQL_LOOKUP_OTHER =
      OTHER_INSERTS.stream().map(kw -> LookupElementBuilder.create(kw))
          .collect(Collectors.toList());

  private static List<LookupElementBuilder> getMatchingCompletions(String textBefore,
      String prefix) {

    if (prefix.length() == 0 || prefix.equals("\"")) {
      return YQL_LOOKUP_ELEMENTS;
    }
    String textBeforePrefix = textBefore.substring(1, textBefore.length() - prefix.length());
    return YQL.YQL_KEYWORDS.stream()
        .filter(kw -> kw.startsWith(prefix))
        .map(kw -> LookupElementBuilder.create(textBeforePrefix + kw))
        .collect(Collectors.toList());
  }

  private static String getPrefix(String textBefore) {
    if (textBefore.endsWith(" ")) {
      return "";
    }
    String[] parts = textBefore.split(" ");
    if (parts == null || parts.length == 0) {
      return "";
    }
    return parts[parts.length - 1];
  }

  public YqlCompletionContributor() {
//    extend(CompletionType.BASIC,
//        PlatformPatterns.psiElement(YqlElementTypes.L_CURLY),
//        new CompletionProvider<CompletionParameters>() {
//          public void addCompletions(@NotNull CompletionParameters parameters,
//              @NotNull ProcessingContext context,
//              @NotNull CompletionResultSet resultSet) {
//            resultSet.addAllElements(YQL_LOOKUP_OTHER);
//          }
//        }
//    );
//    extend(CompletionType.BASIC,
//        PlatformPatterns.psiElement(YqlElementTypes.DOUBLE_QUOTED_STRING),
//        new CompletionProvider<CompletionParameters>() {
//          public void addCompletions(@NotNull CompletionParameters parameters,
//              @NotNull ProcessingContext context,
//              @NotNull CompletionResultSet resultSet) {
//
//            // YQL syntax completions
//            if (isInYqlValue(parameters.getPosition())) {
//              @NotNull PsiElement position = parameters.getPosition();
//              @NlsSafe String text = position.getText();
//              // CompletionUtil.DUMMY_IDENTIFIER
//              String realText = text.replace(CompletionUtil.DUMMY_IDENTIFIER, "");
//              int offset = text.indexOf(CompletionUtil.DUMMY_IDENTIFIER);
//              String textBefore = text.substring(0, offset);
//              String prefix = getPrefix(textBefore);
//              System.out.println("TEXT: " + realText);
//              System.out.println("text before: " + textBefore);
//              // resultSet.addElement(LookupElementBuilder.create("select"));
//              resultSet.addAllElements(YQL_LOOKUP_ELEMENTS);
//              // resultSet.addAllElements(getMatchingCompletions(textBefore, prefix));
//            }
//          }
//        }
//    );

    extend(CompletionType.BASIC,
        PlatformPatterns.psiElement(YqlElementTypes.STRING),
        new CompletionProvider<CompletionParameters>() {
          public void addCompletions(@NotNull CompletionParameters parameters,
              @NotNull ProcessingContext context,
              @NotNull CompletionResultSet resultSet) {
            if (isInYqlPropertyValue(parameters.getPosition())) {
              resultSet.addAllElements(YQL_LOOKUP_ELEMENTS);
            }
          }
        });

    extend(CompletionType.BASIC,
        PlatformPatterns.psiElement(),
        new CompletionProvider<CompletionParameters>() {
          public void addCompletions(@NotNull CompletionParameters parameters,
              @NotNull ProcessingContext context,
              @NotNull CompletionResultSet resultSet) {
            if (isInYqlRoot(parameters.getPosition())) {
              System.out.println("IN YQL ROOT");
              resultSet.addAllElements(YQL_LOOKUP_OTHER);
            }
          }
        });
  }

  private static boolean isInYqlRoot(PsiElement position) {
    PsiElement p1 = position.getParent();
    if(p1 == null) {
      return false;
    }
    PsiElement p2 = p1.getParent();
    if(p2 == null) {
      return false;
    }
    return p2 instanceof YqlFile;
  }

  private static boolean isInYqlPropertyValue(PsiElement position) {
    PsiElement p1 = position.getParent();
    if (p1 == null) {
      return false;
    }
    PsiElement p2 = p1.getParent();
    if (p2 == null) {
      return false;
    }
    PsiElement p3 = p2.getParent();
    if (p3 == null) {
      return false;
    }
    return p3 instanceof YqlQueryProperty;
  }

}