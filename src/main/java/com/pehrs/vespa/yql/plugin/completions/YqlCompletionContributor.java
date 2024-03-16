package com.pehrs.vespa.yql.plugin.completions;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResult;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.pehrs.vespa.yql.plugin.YqlElementTypes;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

final public class YqlCompletionContributor extends CompletionContributor {

  static List<String> YQL_KEYWORDS = List.of(
      // Basic keywords
      "select",
      "from",
      "where",
      "order by",
      "limit",
      "offset",
      "timeout",
      // where keywords
      "nearestNeighbor",
      "weightedSet",
      "predicate",
      "dotProduct",
      "userQuery",
      "nonEmpty",
      "userInput",
      "geoLocation",
      "sameElement",
      "matches",
      "range",
      "contains",
      "weakAnd",
      "phrase",
      "fuzzy",
      "equiv",
      "onear",
      "wand",
      "true",
      "false",
      "rank",
      "near",
      "and",
      "not",
      "uri",
      "or"
  );
  static List<LookupElementBuilder> YQL_LOOKUP_ELEMENTS =
      YQL_KEYWORDS.stream().map(kw -> LookupElementBuilder.create(kw)).collect(Collectors.toList());

  static List<String> OTHER_INSERTS = List.of(
      "\"yql\": \"select \"",
      "\"input\": {}",
      "\"ranking\": \"\""
      );
  static List<LookupElementBuilder> YQL_LOOKUP_OTHER =
      OTHER_INSERTS.stream().map(kw -> LookupElementBuilder.create(kw)).collect(Collectors.toList());

  private static List<LookupElementBuilder> getMatchingCompletions(String textBefore,
      String prefix) {

    if (prefix.length() == 0 || prefix.equals("\"")) {
      return YQL_LOOKUP_ELEMENTS;
    }
    String textBeforePrefix = textBefore.substring(1, textBefore.length() - prefix.length());
    return YQL_KEYWORDS.stream()
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
    extend(CompletionType.BASIC,
        PlatformPatterns.psiElement(YqlElementTypes.L_CURLY),
        new CompletionProvider<CompletionParameters>() {
          public void addCompletions(@NotNull CompletionParameters parameters,
              @NotNull ProcessingContext context,
              @NotNull CompletionResultSet resultSet) {
            resultSet.addAllElements(YQL_LOOKUP_OTHER);
          }
        }
    );
    extend(CompletionType.BASIC,
        PlatformPatterns.psiElement(YqlElementTypes.DOUBLE_QUOTED_STRING),
        new CompletionProvider<CompletionParameters>() {
          public void addCompletions(@NotNull CompletionParameters parameters,
              @NotNull ProcessingContext context,
              @NotNull CompletionResultSet resultSet) {

            // YQL syntax completions
            if (isInYqlValue(parameters.getPosition())) {
              @NotNull PsiElement position = parameters.getPosition();
              @NlsSafe String text = position.getText();
              // CompletionUtil.DUMMY_IDENTIFIER
              String realText = text.replace(CompletionUtil.DUMMY_IDENTIFIER, "");
              int offset = text.indexOf(CompletionUtil.DUMMY_IDENTIFIER);
              String textBefore = text.substring(0, offset);
              String prefix = getPrefix(textBefore);
              System.out.println("TEXT: " + realText);
              System.out.println("text before: " + textBefore);
              // resultSet.addElement(LookupElementBuilder.create("select"));
              resultSet.addAllElements(YQL_LOOKUP_ELEMENTS);
              // resultSet.addAllElements(getMatchingCompletions(textBefore, prefix));
            }
          }
        }
    );

  }

  private static boolean isInYqlValue(PsiElement position) {
    PsiElement p1 = position.getParent();
    if (p1 == null) {
      return false;
    }
    PsiElement p2 = p1.getParent();
    if (p2 == null) {
      return false;
    }
    PsiElement c1 = p2.getFirstChild();
    if (c1 == null) {
      return false;
    }
    return "\"yql\"".equals(c1.getText());
  }

}