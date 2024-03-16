package com.pehrs.vespa.yql.plugin;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.NlsContexts.Label;
import com.intellij.openapi.util.NlsSafe;
import javax.swing.Icon;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class YqlFileType extends LanguageFileType {
  public static YqlFileType INSTANCE = new YqlFileType();

  public YqlFileType() {
    super(YqlLanguage.INSTANCE);
  }

  @Override
  public @NonNls @NotNull String getName() {
    return "YQL";
  }

  @Override
  public @Label @NotNull String getDescription() {
    return "Vespa YQL Request";
  }

  @Override
  public @NlsSafe @NotNull String getDefaultExtension() {
    return "yql";
  }

  @Override
  public Icon getIcon() {
    return YqlIcons.FILE;
  }
}
