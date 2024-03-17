// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.pehrs.vespa.yql.plugin.highlighting;

import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import com.pehrs.vespa.yql.plugin.YqlElementTypes;

public class YqlHighlightingLexer extends LayeredLexer {
  public YqlHighlightingLexer(boolean isPermissiveDialect, boolean canEscapeEol, Lexer baseLexer) {
    super(baseLexer);
//    registerSelfStoppingLayer(new YqlStringLiteralLexer('\"', YqlElementTypes.DOUBLE_QUOTED_STRING, canEscapeEol, isPermissiveDialect),
//                              new IElementType[]{YqlElementTypes.DOUBLE_QUOTED_STRING}, IElementType.EMPTY_ARRAY);
//    registerSelfStoppingLayer(new YqlStringLiteralLexer('\'', YqlElementTypes.SINGLE_QUOTED_STRING, canEscapeEol, isPermissiveDialect),
//                                           new IElementType[]{YqlElementTypes.SINGLE_QUOTED_STRING}, IElementType.EMPTY_ARRAY);
  }
}
