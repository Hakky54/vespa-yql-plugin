package com.pehrs.vespa.yql.plugin.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.pehrs.vespa.yql.plugin.YqlFileType;
import com.pehrs.vespa.yql.plugin.YqlLanguage;
import org.jetbrains.annotations.NotNull;

public class YqlFile extends PsiFileBase {

  public YqlFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, YqlLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return YqlFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "YQL";
  }

}