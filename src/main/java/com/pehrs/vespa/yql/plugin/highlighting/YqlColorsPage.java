// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.pehrs.vespa.yql.plugin.highlighting;


import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_BLOCK_COMMENT;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_BRACES;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_BRACKETS;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_COLON;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_COMMA;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_INVALID_ESCAPE;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_KEYWORD;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_LINE_COMMENT;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_NUMBER;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_PARAMETER;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_PROPERTY_KEY;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_STRING;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_VALID_ESCAPE;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.RainbowColorSettingsPage;
import com.intellij.psi.codeStyle.DisplayPriority;
import com.intellij.psi.codeStyle.DisplayPrioritySortable;
import com.pehrs.vespa.yql.plugin.YqlIcons;
import com.pehrs.vespa.yql.plugin.YqlLanguage;
import java.util.Map;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

public final class YqlColorsPage implements RainbowColorSettingsPage, DisplayPrioritySortable {
  private static final Map<String, TextAttributesKey> ourAdditionalHighlighting = Map.of("propertyKey", YQL_PROPERTY_KEY);

  private static final AttributesDescriptor[] ourAttributeDescriptors = new AttributesDescriptor[]{
    new AttributesDescriptor("Property", YQL_PROPERTY_KEY),

    new AttributesDescriptor("Braces", YQL_BRACES),
    new AttributesDescriptor("Brackets", YQL_BRACKETS),
    new AttributesDescriptor("Comma", YQL_COMMA),
    new AttributesDescriptor("Colon", YQL_COLON),
    new AttributesDescriptor("Number", YQL_NUMBER),
    new AttributesDescriptor("String", YQL_STRING),
    new AttributesDescriptor("Keyword", YQL_KEYWORD),
    new AttributesDescriptor("Line Comment", YQL_LINE_COMMENT),
    new AttributesDescriptor("Block Comment", YQL_BLOCK_COMMENT),
    new AttributesDescriptor("Valid Escape Sequence", YQL_VALID_ESCAPE),
    new AttributesDescriptor("Invalid Escape Sequence", YQL_INVALID_ESCAPE),
    new AttributesDescriptor("Parameter", YQL_PARAMETER)
  };

  @Override
  public @NotNull Icon getIcon() {
    return YqlIcons.FILE;
  }

  @Override
  public @NotNull SyntaxHighlighter getHighlighter() {
    return SyntaxHighlighterFactory.getSyntaxHighlighter(YqlLanguage.INSTANCE, null, null);
  }

  @Override
  public @NotNull String getDemoText() {
    return """
      {
        // Line comments are not included in standard but nonetheless allowed.
        /* As well as block comments. */
        <propertyKey>"the only keywords are"</propertyKey>: [true, false, null],
        <propertyKey>"strings with"</propertyKey>: {
          <propertyKey>"no escapes"</propertyKey>: "pseudopolinomiality"
          <propertyKey>"valid escapes"</propertyKey>: "C-style\\r\\n and unicode\\u0021",
          <propertyKey>"illegal escapes"</propertyKey>: "\\0377\\x\\"
        },
        <propertyKey>"some numbers"</propertyKey>: [
          42,
          -0.0e-0,
          6.626e-34
        ]\s
      }""";
  }

  @Override
  public @NotNull Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return ourAdditionalHighlighting;
  }

  @Override
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return ourAttributeDescriptors;
  }

  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override
  public @NotNull String getDisplayName() {
    return "YQL";
  }

  @Override
  public DisplayPriority getPriority() {
    return DisplayPriority.LANGUAGE_SETTINGS;
  }

  @Override
  public boolean isRainbowType(TextAttributesKey type) {
    return YQL_PROPERTY_KEY.equals(type)
      || YQL_BRACES.equals(type)
      || YQL_BRACKETS.equals(type)
      || YQL_STRING.equals(type)
      || YQL_NUMBER.equals(type)
      || YQL_KEYWORD.equals(type);
  }

  @Override
  public @NotNull Language getLanguage() {
    return YqlLanguage.INSTANCE;
  }
}
