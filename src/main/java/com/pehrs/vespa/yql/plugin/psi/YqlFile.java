package com.pehrs.vespa.yql.plugin.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.ide.scratch.RootType;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.ide.scratch.ScratchUtil;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.pehrs.vespa.yql.plugin.YqlFileType;
import com.pehrs.vespa.yql.plugin.YqlLanguage;
import org.jetbrains.annotations.NotNull;

public class YqlFile extends PsiFileBase {

  public YqlFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, YqlLanguage.INSTANCE);
  }

  public static boolean isYqlFile(VirtualFile file, Project project) {
      FileType type = file.getFileType();
      if (type instanceof LanguageFileType
          && ((LanguageFileType) type).getLanguage() instanceof YqlLanguage) {
        return true;
      }
      if (project == null || !ScratchUtil.isScratch(file)) {
        return false;
      }
      RootType rootType = ScratchFileService.findRootType(file);
      return rootType != null && rootType.substituteLanguage(project, file) instanceof YqlLanguage;
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