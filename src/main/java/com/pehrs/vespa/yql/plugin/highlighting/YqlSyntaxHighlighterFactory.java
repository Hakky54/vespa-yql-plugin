package com.pehrs.vespa.yql.plugin.highlighting;

import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BLOCK_COMMENT;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACES;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.BRACKETS;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.COMMA;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.IDENTIFIER;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INSTANCE_FIELD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.KEYWORD;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.LINE_COMMENT;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.NUMBER;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.SEMICOLON;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.STRING;
import static com.intellij.openapi.editor.DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE;

import com.intellij.lang.Language;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.pehrs.vespa.yql.plugin.YqlElementTypes;
import com.pehrs.vespa.yql.plugin.YqlFileType;
import com.pehrs.vespa.yql.plugin.YqlLanguage;
import com.pehrs.vespa.yql.plugin.YqlLexerAdapter;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YqlSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
  public static final TextAttributesKey YQL_BRACKETS = TextAttributesKey.createTextAttributesKey("YQL.BRACKETS", BRACKETS);
  public static final TextAttributesKey YQL_BRACES = TextAttributesKey.createTextAttributesKey("YQL.BRACES", BRACES);
  public static final TextAttributesKey YQL_COMMA = TextAttributesKey.createTextAttributesKey("YQL.COMMA", COMMA);
  public static final TextAttributesKey YQL_COLON = TextAttributesKey.createTextAttributesKey("YQL.COLON", SEMICOLON);
  public static final TextAttributesKey YQL_NUMBER = TextAttributesKey.createTextAttributesKey("YQL.NUMBER", NUMBER);
  public static final TextAttributesKey YQL_STRING = TextAttributesKey.createTextAttributesKey("YQL.STRING", STRING);
  public static final TextAttributesKey YQL_KEYWORD = TextAttributesKey.createTextAttributesKey("YQL.KEYWORD", KEYWORD);
  public static final TextAttributesKey YQL_LINE_COMMENT = TextAttributesKey.createTextAttributesKey("YQL.LINE_COMMENT", LINE_COMMENT);
  public static final TextAttributesKey YQL_BLOCK_COMMENT = TextAttributesKey.createTextAttributesKey("YQL.BLOCK_COMMENT", BLOCK_COMMENT);

  // Artificial element type
  public static final TextAttributesKey YQL_IDENTIFIER = TextAttributesKey.createTextAttributesKey("YQL.IDENTIFIER", IDENTIFIER);

  // Added by annotators
  public static final TextAttributesKey YQL_PROPERTY_KEY = TextAttributesKey.createTextAttributesKey("YQL.PROPERTY_KEY", INSTANCE_FIELD);
  // String escapes
  public static final TextAttributesKey YQL_VALID_ESCAPE =
    TextAttributesKey.createTextAttributesKey("YQL.VALID_ESCAPE", VALID_STRING_ESCAPE);
  public static final TextAttributesKey YQL_INVALID_ESCAPE =
    TextAttributesKey.createTextAttributesKey("YQL.INVALID_ESCAPE", INVALID_STRING_ESCAPE);

  public static final TextAttributesKey YQL_PARAMETER = TextAttributesKey.createTextAttributesKey("YQL.PARAMETER", KEYWORD);


  @Override
  public @NotNull SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile) {
    return new YqlHighlighter(virtualFile);
  }

  private final class YqlHighlighter extends SyntaxHighlighterBase {
    private final Map<IElementType, TextAttributesKey> ourAttributes = new HashMap<>();

    private final @Nullable VirtualFile myFile;

    {
      fillMap(ourAttributes, YQL_BRACES, YqlElementTypes.L_CURLY, YqlElementTypes.R_CURLY);
      fillMap(ourAttributes, YQL_BRACKETS, YqlElementTypes.L_BRACKET, YqlElementTypes.R_BRACKET);
      fillMap(ourAttributes, YQL_COMMA, YqlElementTypes.COMMA);
      fillMap(ourAttributes, YQL_COLON, YqlElementTypes.COLON);
      // fillMap(ourAttributes, YQL_STRING, YqlElementTypes.STRING);
//      fillMap(ourAttributes, YQL_STRING, YqlElementTypes.DOUBLE_QUOTED_STRING);
//      fillMap(ourAttributes, YQL_STRING, YqlElementTypes.SINGLE_QUOTED_STRING);
      fillMap(ourAttributes, YQL_NUMBER, YqlElementTypes.NUMBER);
      fillMap(ourAttributes, YQL_KEYWORD, YqlElementTypes.TRUE, YqlElementTypes.FALSE, YqlElementTypes.NULL,
          YqlElementTypes.BASIC_KEYWORD);
//      fillMap(ourAttributes, YQL_LINE_COMMENT, YqlElementTypes.LINE_COMMENT);
//      fillMap(ourAttributes, YQL_BLOCK_COMMENT, YqlElementTypes.BLOCK_COMMENT);
      fillMap(ourAttributes, YQL_IDENTIFIER, YqlElementTypes.IDENTIFIER);
      fillMap(ourAttributes, HighlighterColors.BAD_CHARACTER, TokenType.BAD_CHARACTER);

      fillMap(ourAttributes, YQL_VALID_ESCAPE, StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN);
      fillMap(ourAttributes, YQL_INVALID_ESCAPE, StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN);
      fillMap(ourAttributes, YQL_INVALID_ESCAPE, StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN);
    }

    YqlHighlighter(@Nullable VirtualFile file) {
      myFile = file;
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
      return new YqlHighlightingLexer(isPermissiveDialect(), isCanEscapeEol(), getLexer());
    }

    private boolean isPermissiveDialect() {
      FileType fileType = myFile == null ? null : myFile.getFileType();
      boolean isPermissiveDialect = false;
      if (fileType instanceof YqlFileType) {
        Language language = ((YqlFileType)fileType).getLanguage();
        isPermissiveDialect = language instanceof YqlLanguage && ((YqlLanguage)language).hasPermissiveStrings();
      }
      return isPermissiveDialect;
    }

    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];
    private static final TextAttributesKey[] BASIC_KEYWORD_KEYS = new TextAttributesKey[]{YQL_KEYWORD};
    private static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{YQL_NUMBER};
    private static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{YQL_STRING};
    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
      // return pack(ourAttributes.get(tokenType));

      if (tokenType.equals(YqlElementTypes.NUMBER)) {
        return NUMBER_KEYS;
      }
      if (tokenType.equals(YqlElementTypes.BASIC_KEYWORD)) {
        return BASIC_KEYWORD_KEYS;
      }
      if (tokenType.equals(YqlElementTypes.STRING)) {
        return STRING_KEYS;
      }
      return EMPTY_KEYS;
    }
  }

  protected @NotNull Lexer getLexer() {
    return new YqlLexerAdapter();
  }

  protected boolean isCanEscapeEol() {
    return false;
  }
}
