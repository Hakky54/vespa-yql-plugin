package com.pehrs.vespa.yql.plugin.formatter;

import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.formatting.Indent;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.json.JsonLanguage;
import com.intellij.json.formatter.JsonCodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.pehrs.vespa.yql.plugin.YqlElementTypes;
import com.pehrs.vespa.yql.plugin.YqlLanguage;
import org.jetbrains.annotations.NotNull;

public class YqlFormattingBuilderModel implements FormattingModelBuilder {

  @Override
  public @NotNull FormattingModel createModel(@NotNull FormattingContext formattingContext) {
    CodeStyleSettings settings = formattingContext.getCodeStyleSettings();
    JsonCodeStyleSettings customSettings = settings.getCustomSettings(JsonCodeStyleSettings.class);
    SpacingBuilder spacingBuilder = createSpacingBuilder(settings);
    final YqlBlock block =
        new YqlBlock(null, formattingContext.getNode(), customSettings, null, Indent.getSmartIndent(Indent.Type.CONTINUATION), null,
            spacingBuilder);
    return FormattingModelProvider.createFormattingModelForPsiFile(formattingContext.getContainingFile(), block, settings);
  }

  static @NotNull SpacingBuilder createSpacingBuilder(CodeStyleSettings settings) {
    final JsonCodeStyleSettings jsonSettings = settings.getCustomSettings(JsonCodeStyleSettings.class);
    final CommonCodeStyleSettings commonSettings = settings.getCommonSettings(JsonLanguage.INSTANCE);

    final int spacesBeforeComma = commonSettings.SPACE_BEFORE_COMMA ? 1 : 0;
    final int spacesBeforeColon = jsonSettings.SPACE_BEFORE_COLON ? 1 : 0;
    final int spacesAfterColon = jsonSettings.SPACE_AFTER_COLON ? 1 : 0;

    return new SpacingBuilder(settings, YqlLanguage.INSTANCE)
        .before(YqlElementTypes.COLON).spacing(spacesBeforeColon, spacesBeforeColon, 0, false, 0)
        .after(YqlElementTypes.COLON).spacing(spacesAfterColon, spacesAfterColon, 0, false, 0)
        .withinPair(YqlElementTypes.L_BRACKET, YqlElementTypes.R_BRACKET).spaceIf(commonSettings.SPACE_WITHIN_BRACKETS, true)
        .withinPair(YqlElementTypes.L_CURLY, YqlElementTypes.R_CURLY).spaceIf(commonSettings.SPACE_WITHIN_BRACES, true)
        .before(YqlElementTypes.COMMA).spacing(spacesBeforeComma, spacesBeforeComma, 0, false, 0)
        .after(YqlElementTypes.COMMA).spaceIf(commonSettings.SPACE_AFTER_COMMA);
  }
}
