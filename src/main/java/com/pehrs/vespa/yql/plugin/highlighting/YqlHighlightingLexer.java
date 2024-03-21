package com.pehrs.vespa.yql.plugin.highlighting;

import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerBase;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import com.pehrs.vespa.yql.plugin.YqlElementTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YqlHighlightingLexer extends LayeredLexer {

  public YqlHighlightingLexer(boolean isPermissiveDialect, boolean canEscapeEol, Lexer baseLexer) {
    super(baseLexer);
//    registerSelfStoppingLayer(new YqlStringLiteralLexer('\"', YqlElementTypes.DOUBLE_QUOTED_STRING, canEscapeEol, isPermissiveDialect),
//                              new IElementType[]{YqlElementTypes.DOUBLE_QUOTED_STRING}, IElementType.EMPTY_ARRAY);
//    registerSelfStoppingLayer(new YqlStringLiteralLexer('\'', YqlElementTypes.SINGLE_QUOTED_STRING, canEscapeEol, isPermissiveDialect),
//                                           new IElementType[]{YqlElementTypes.SINGLE_QUOTED_STRING}, IElementType.EMPTY_ARRAY);
//    registerSelfStoppingLayer(
//        new LexerBase() {
//          @Override
//          public void start(@NotNull CharSequence buffer, int startOffset, int endOffset,
//              int initialState) {
//            baseLexer.start(buffer, startOffset, endOffset, initialState);
//          }
//
//          @Override
//          public int getState() {
//            return baseLexer.getState();
//          }
//
//          @Override
//          public @Nullable IElementType getTokenType() {
//            return baseLexer.getTokenType();
//          }
//
//          @Override
//          public int getTokenStart() {
//            return baseLexer.getTokenStart();
//          }
//
//          @Override
//          public int getTokenEnd() {
//            return baseLexer.getTokenEnd();
//          }
//
//          @Override
//          public void advance() {
//            baseLexer.advance();
//          }
//
//
//          @Override
//          public @NotNull CharSequence getBufferSequence() {
//            return baseLexer.getBufferSequence();
//          }
//
//          @Override
//          public int getBufferEnd() {
//            return baseLexer.getBufferEnd();
//          }
//        },
//        new IElementType[]{YqlElementTypes.STRING},
//        IElementType.EMPTY_ARRAY);
  }
}
