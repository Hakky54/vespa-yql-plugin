package com.pehrs.vespa.yql.plugin.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory;
import com.pehrs.vespa.yql.plugin.psi.YqlBasicKeyword;
import com.pehrs.vespa.yql.plugin.psi.YqlPropertyKey;
import com.pehrs.vespa.yql.plugin.psi.YqlQueryValue;
import com.pehrs.vespa.yql.plugin.psi.YqlStringValue;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

import static com.pehrs.vespa.yql.plugin.YQL.YQL_KEYWORDS;

public class YqlAnnotator implements Annotator {

  public YqlAnnotator() {
    super();
  }

  @Override
  public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
    // Ensure the PSI Element is an expression

    if (element.getParent() != null && element.getParent() instanceof YqlQueryValue) {
      if (element instanceof YqlBasicKeyword basicKeywordElement) {
        processBasicKeyword(basicKeywordElement, holder);
      }
      if (element instanceof YqlStringValue stringValue) {
        processString(stringValue, holder);
      }
    }


//    if (element instanceof YqlPropertyKey propKey) {
//      processPropertyKey(propKey, holder);
//    }
  }

  private void processPropertyKey(YqlPropertyKey propertyKey, AnnotationHolder holder) {

    TextRange textRange = propertyKey.getTextRange();
    @NlsSafe String text = propertyKey.getFirstChild()
        .getText();

    Optional<String> keyword = YQL_KEYWORDS.stream().filter(kw -> text.contains(kw))
        .findFirst();

    if (keyword.isPresent()) {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
          .range(textRange)
          .textAttributes(YqlSyntaxHighlighterFactory.YQL_PROPERTY_KEY)
          .create();
    } else {
      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
          .range(textRange)
          .textAttributes(YqlSyntaxHighlighterFactory.YQL_PROPERTY_KEY)
          .create();
    }
  }


  private void processString(YqlStringValue stringValue, AnnotationHolder holder) {
    @NlsSafe String text = stringValue.getFirstChild()
        .getText();

    Optional<String> keyword = YQL_KEYWORDS.stream().filter(kw -> text.contains(kw))
        .findFirst();

    if (keyword.isPresent()) {
      TextRange textOrgRange = stringValue.getTextRange();

      int index = text.indexOf(keyword.get());

      // TextRange prefixRange = TextRange.from(element.getTextRange().getStartOffset(), SIMPLE_PREFIX_STR.length() + 1);
      TextRange textRange = TextRange.from(
          textOrgRange.getStartOffset() + index,
          keyword.get().length()
      );

      holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
          .range(textRange)
          .textAttributes(YqlSyntaxHighlighterFactory.YQL_KEYWORD)
          .create();
    }
  }

  private void processBasicKeyword(YqlBasicKeyword basicKeywordElement, AnnotationHolder holder) {

    TextRange textRange = basicKeywordElement.getTextRange();

    // Highligt the text!!
    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(textRange)
        .textAttributes(YqlSyntaxHighlighterFactory.YQL_KEYWORD)
        .create();
  }

}