package com.pehrs.vespa.yql.plugin.psi;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.pehrs.vespa.yql.plugin.YqlFileType;
import org.jetbrains.annotations.NotNull;

public class YqlElementGenerator {
  private final Project myProject;

  public YqlElementGenerator(@NotNull Project project) {
    myProject = project;
  }

  /**
   * Create lightweight in-memory {@link YqlFileType} filled with {@code content}.
   *
   * @param content content of the file to be created
   * @return created file
   */
  public @NotNull PsiFile createDummyFile(@NotNull String content) {
    final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(myProject);
    return psiFileFactory.createFileFromText("dummy." + YqlFileType.INSTANCE.getDefaultExtension(), YqlFileType.INSTANCE, content);
  }

  /**
   * Create YQL value from supplied content.
   *
   * @param content properly escaped text of YQL value, e.g. Java literal {@code "\"new\\nline\""} if you want to create string literal
   * @param <T>     type of the YQL value desired
   * @return element created from given text
   *
   * @see #createStringLiteral(String)
   */
  public @NotNull <T extends YqlValue> T createValue(@NotNull String content) {
    final PsiFile file = createDummyFile("{\"foo\": " + content + "}");
    //noinspection unchecked,ConstantConditions
    return (T)((YqlObject)file.getFirstChild()).getPropertyList().get(0).getValue();
  }

  public @NotNull YqlObject createObject(@NotNull String content) {
    final PsiFile file = createDummyFile("{" + content + "}");
    return (YqlObject) file.getFirstChild();
  }

  /**
   * Create YQL string literal from supplied <em>unescaped</em> content.
   *
   * @param unescapedContent unescaped content of string literal, e.g. Java literal {@code "new\nline"} (compare with {@link #createValue(String)}).
   * @return YQL string literal created from given text
   */
  public @NotNull YqlStringLiteral createStringLiteral(@NotNull String unescapedContent) {
    return createValue('"' + StringUtil.escapeStringCharacters(unescapedContent) + '"');
  }

  public @NotNull YqlProperty createProperty(final @NotNull String name, final @NotNull String value) {
    final PsiFile file = createDummyFile("{\"" + name + "\": " + value + "}");
    return ((YqlObject) file.getFirstChild()).getPropertyList().get(0);
  }

  public @NotNull PsiElement createComma() {
    final YqlArray array = createValue("[1, 2]");
    return array.getValueList().get(0).getNextSibling();
  }
}
