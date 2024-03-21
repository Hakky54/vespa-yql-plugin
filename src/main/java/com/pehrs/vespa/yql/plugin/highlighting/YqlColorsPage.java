package com.pehrs.vespa.yql.plugin.highlighting;


import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_KEYWORD;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_NUMBER;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_PROPERTY_KEY;
import static com.pehrs.vespa.yql.plugin.highlighting.YqlSyntaxHighlighterFactory.YQL_STRING;

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

  private static final Map<String, TextAttributesKey> ourAdditionalHighlighting = Map.of(
      "prop", YQL_PROPERTY_KEY,
      "str", YQL_STRING,
      "num", YQL_NUMBER,
      "key", YQL_KEYWORD
  );

  private static final AttributesDescriptor[] ourAttributeDescriptors = new AttributesDescriptor[]{
      new AttributesDescriptor("Property", YQL_PROPERTY_KEY),
      new AttributesDescriptor("String", YQL_STRING),
      new AttributesDescriptor("Number", YQL_NUMBER),
      new AttributesDescriptor("Keyword", YQL_KEYWORD),

//    new AttributesDescriptor("Braces", YQL_BRACES),
//    new AttributesDescriptor("Brackets", YQL_BRACKETS),
//    new AttributesDescriptor("Comma", YQL_COMMA),
//    new AttributesDescriptor("Colon", YQL_COLON),
//    new AttributesDescriptor("Number", YQL_NUMBER),
//    new AttributesDescriptor("String", YQL_STRING),
//    new AttributesDescriptor("Keyword", YQL_KEYWORD),
//    new AttributesDescriptor("Line Comment", YQL_LINE_COMMENT),
//    new AttributesDescriptor("Block Comment", YQL_BLOCK_COMMENT),
//    new AttributesDescriptor("Valid Escape Sequence", YQL_VALID_ESCAPE),
//    new AttributesDescriptor("Invalid Escape Sequence", YQL_INVALID_ESCAPE),
//    new AttributesDescriptor("Parameter", YQL_PARAMETER)
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
          "<prop>yql</prop>": "<key>select</key> <str>embedding</str>, <str>title</str>, <str>segment_index</str> <key>from</key> books <key>where</key> <str>{targetHits:5}</str><key>nearestNeighbor</key><str>(embedding,q_embedding)</str>",
          "<prop>input</prop>": {
            "<prop>query(threshold)</prop>": <num>0.88</num>,
            "<prop>query(q_embedding)</prop>": [
              <num>0.010332104</num>,
              <num>0.14517991</num>,
              <num>0.019870816</num>
            ]
          },
          "<prop>ranking</prop>": "<str>recommendation</str>"
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
        || YQL_STRING.equals(type)
        || YQL_KEYWORD.equals(type)
        || YQL_NUMBER.equals(type)
        ;
  }

  @Override
  public @NotNull Language getLanguage() {
    return YqlLanguage.INSTANCE;
  }
}
