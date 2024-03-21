// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.pehrs.vespa.yql.plugin.formatter;


import static com.intellij.json.formatter.JsonCodeStyleSettings.ALIGN_PROPERTY_ON_COLON;
import static com.intellij.json.formatter.JsonCodeStyleSettings.ALIGN_PROPERTY_ON_VALUE;
import static com.intellij.json.psi.JsonPsiUtil.hasElementType;

import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.json.formatter.JsonCodeStyleSettings;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.pehrs.vespa.yql.plugin.YqlElementTypes;
import com.pehrs.vespa.yql.plugin.psi.YqlArray;
import com.pehrs.vespa.yql.plugin.psi.YqlObject;
import com.pehrs.vespa.yql.plugin.psi.YqlProperty;
import com.pehrs.vespa.yql.plugin.psi.YqlPropertyValue;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Mikhail Golubev
 */
public final class YqlBlock implements ASTBlock {
  private static final TokenSet YQL_OPEN_BRACES = TokenSet.create(YqlElementTypes.L_BRACKET, YqlElementTypes.L_CURLY);
  private static final TokenSet YQL_CLOSE_BRACES = TokenSet.create(YqlElementTypes.R_BRACKET, YqlElementTypes.R_CURLY);
  private static final TokenSet YQL_ALL_BRACES = TokenSet.orSet(YQL_OPEN_BRACES, YQL_CLOSE_BRACES);

  private static final TokenSet YQL_CONTAINERS = TokenSet.create(new IElementType[]{
      YqlElementTypes.OBJECT, YqlElementTypes.ARRAY});

  private final YqlBlock myParent;

  private final ASTNode myNode;
  private final PsiElement myPsiElement;
  private final Alignment myAlignment;
  private final Indent myIndent;
  private final Wrap myWrap;
  private final JsonCodeStyleSettings myCustomSettings;
  private final SpacingBuilder mySpacingBuilder;
  // lazy initialized on first call to #getSubBlocks()
  private List<Block> mySubBlocks = null;

  private final Alignment myPropertyValueAlignment;
  private final Wrap myChildWrap;

  public YqlBlock(@Nullable YqlBlock parent,
                   @NotNull ASTNode node,
                   @NotNull JsonCodeStyleSettings customSettings,
                   @Nullable Alignment alignment,
                   @NotNull Indent indent,
                   @Nullable Wrap wrap,
                   @NotNull SpacingBuilder spacingBuilder) {
    myParent = parent;
    myNode = node;
    myPsiElement = node.getPsi();
    myAlignment = alignment;
    myIndent = indent;
    myWrap = wrap;
    mySpacingBuilder = spacingBuilder;
    myCustomSettings = customSettings;

    if (myPsiElement instanceof YqlObject) {
      myChildWrap = Wrap.createWrap(myCustomSettings.OBJECT_WRAPPING, true);
    }
    else if (myPsiElement instanceof YqlArray) {
      myChildWrap = Wrap.createWrap(myCustomSettings.ARRAY_WRAPPING, true);
    }
    else {
      myChildWrap = null;
    }

    myPropertyValueAlignment = myPsiElement instanceof YqlObject ? Alignment.createAlignment(true) : null;
  }

  @Override
  public ASTNode getNode() {
    return myNode;
  }

  @Override
  public @NotNull TextRange getTextRange() {
    return myNode.getTextRange();
  }

  @Override
  public @NotNull List<Block> getSubBlocks() {
    if (mySubBlocks == null) {
      int propertyAlignment = myCustomSettings.PROPERTY_ALIGNMENT;
      ASTNode[] children = myNode.getChildren(null);
      mySubBlocks = new ArrayList<>(children.length);
      for (ASTNode child: children) {
        if (isWhitespaceOrEmpty(child)) continue;
        mySubBlocks.add(makeSubBlock(child, propertyAlignment));
      }
    }
    return mySubBlocks;
  }

  public static boolean isPropertyValue(@NotNull PsiElement element) {
    final PsiElement parent = element.getParent();
    // return parent instanceof YqlPropertyValue && element == ((YqlPropertyValue)parent).getValue();
    return parent instanceof YqlPropertyValue;
  }

  private Block makeSubBlock(@NotNull ASTNode childNode, int propertyAlignment) {
    Indent indent = Indent.getNoneIndent();
    Alignment alignment = null;
    Wrap wrap = null;

    if (hasElementType(myNode, YQL_CONTAINERS)) {
      if (hasElementType(childNode, YqlElementTypes.COMMA)) {
        wrap = Wrap.createWrap(WrapType.NONE, true);
      }
      else if (!hasElementType(childNode, YQL_ALL_BRACES)) {
        assert myChildWrap != null;
        wrap = myChildWrap;
        indent = Indent.getNormalIndent();
      }
      else if (hasElementType(childNode, YQL_OPEN_BRACES)) {
        if (isPropertyValue(myPsiElement) && propertyAlignment == ALIGN_PROPERTY_ON_VALUE) {
          // WEB-13587 Align compound values on opening brace/bracket, not the whole block
          assert myParent != null && myParent.myParent != null && myParent.myParent.myPropertyValueAlignment != null;
          alignment = myParent.myParent.myPropertyValueAlignment;
        }
      }
    }
    // Handle properties alignment
    else if (hasElementType(myNode, YqlElementTypes.PROPERTY) ) {
      assert myParent != null && myParent.myPropertyValueAlignment != null;
      if (hasElementType(childNode, YqlElementTypes.COLON) && propertyAlignment == ALIGN_PROPERTY_ON_COLON) {
        alignment = myParent.myPropertyValueAlignment;
      }
      else if (isPropertyValue(childNode.getPsi()) && propertyAlignment == ALIGN_PROPERTY_ON_VALUE) {
        if (!hasElementType(childNode, YQL_CONTAINERS)) {
          alignment = myParent.myPropertyValueAlignment;
        }
      }
    }
    return new YqlBlock(this, childNode, myCustomSettings, alignment, indent, wrap, mySpacingBuilder);
  }

  @Override
  public @Nullable Wrap getWrap() {
    return myWrap;
  }

  @Override
  public @Nullable Indent getIndent() {
    return myIndent;
  }

  @Override
  public @Nullable Alignment getAlignment() {
    return myAlignment;
  }

  @Override
  public @Nullable Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
    return mySpacingBuilder.getSpacing(this, child1, child2);
  }

  @Override
  public @NotNull ChildAttributes getChildAttributes(int newChildIndex) {
    if (hasElementType(myNode, YQL_CONTAINERS)) {
      // WEB-13675: For some reason including alignment in child attributes causes
      // indents to consist solely of spaces when both USE_TABS and SMART_TAB
      // options are enabled.
      return new ChildAttributes(Indent.getNormalIndent(), null);
    }
    else if (myNode.getPsi() instanceof PsiFile) {
      return new ChildAttributes(Indent.getNoneIndent(), null);
    }
    // Will use continuation indent for cases like { "foo"<caret>  }
    return new ChildAttributes(null, null);
  }

  @Override
  public boolean isIncomplete() {
    final ASTNode lastChildNode = myNode.getLastChildNode();
    if (hasElementType(myNode, YqlElementTypes.OBJECT)) {
      return lastChildNode != null && lastChildNode.getElementType() != YqlElementTypes.R_CURLY;
    }
    else if (hasElementType(myNode, YqlElementTypes.ARRAY)) {
      return lastChildNode != null && lastChildNode.getElementType() != YqlElementTypes.R_BRACKET;
    }
//    else if (hasElementType(myNode, YqlElementTypes.PROPERTY)) {
//      return ((YqlPropertyValue)myPsiElement).getText() == null;
//    }
    return false;
  }

  @Override
  public boolean isLeaf() {
    return myNode.getFirstChildNode() == null;
  }

  private static boolean isWhitespaceOrEmpty(ASTNode node) {
    return node.getElementType() == TokenType.WHITE_SPACE || node.getTextLength() == 0;
  }
}
