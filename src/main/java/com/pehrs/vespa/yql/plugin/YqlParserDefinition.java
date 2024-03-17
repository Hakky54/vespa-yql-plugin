package com.pehrs.vespa.yql.plugin;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.pehrs.vespa.yql.plugin.psi.YqlFile;
import org.jetbrains.annotations.NotNull;

public class YqlParserDefinition implements ParserDefinition {

  public static final IFileElementType FILE = new IFileElementType(YqlLanguage.INSTANCE);

  @NotNull
  @Override
  public Lexer createLexer(Project project) {
    return new YqlLexerAdapter();
  }

  @NotNull
  @Override
  public TokenSet getCommentTokens() {
    // return YqlTokenSets.YQL_COMMENTARIES;
    // FIXME:
    return TokenSet.EMPTY;
  }

  @NotNull
  @Override
  public TokenSet getStringLiteralElements() {
    return TokenSet.EMPTY;
  }

  @NotNull
  @Override
  public PsiParser createParser(final Project project) {
    return new YqlParser();
  }

  @NotNull
  @Override
  public IFileElementType getFileNodeType() {
    return FILE;
  }

  @NotNull
  @Override
  public PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new YqlFile(viewProvider);
  }

  @NotNull
  @Override
  public PsiElement createElement(ASTNode node) {
    return YqlElementTypes.Factory.createElement(node);
  }

}
