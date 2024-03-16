package com.pehrs.vespa.yql.plugin.psi.impl;

import com.intellij.icons.AllIcons.Json;
import com.intellij.json.psi.JsonPsiChangeUtils;
import com.intellij.json.psi.JsonPsiUtil;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PlatformIcons;
import com.pehrs.vespa.yql.plugin.YqlTokenSets;
import com.pehrs.vespa.yql.plugin.psi.YqlArray;
import com.pehrs.vespa.yql.plugin.psi.YqlBooleanLiteral;
import com.pehrs.vespa.yql.plugin.psi.YqlLiteral;
import com.pehrs.vespa.yql.plugin.psi.YqlNumberLiteral;
import com.pehrs.vespa.yql.plugin.psi.YqlObject;
import com.pehrs.vespa.yql.plugin.psi.YqlProperty;
import com.pehrs.vespa.yql.plugin.psi.YqlReferenceExpression;
import com.pehrs.vespa.yql.plugin.psi.YqlStringLiteral;
import com.pehrs.vespa.yql.plugin.psi.YqlValue;
import java.util.List;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class YqlPsiImplUtils {
  static final Key<List<Pair<TextRange, String>>> STRING_FRAGMENTS = new Key("YQL string fragments");
  private static final String ourEscapesTable = "\"\"\\\\//b\bf\fn\nr\rt\t";

  public YqlPsiImplUtils() {
  }

  public static @NotNull String getName(@NotNull YqlProperty property) {
    String name = StringUtil.unescapeStringCharacters(JsonPsiUtil.stripQuotes(property.getNameElement().getText()));
    if (name == null) {
      throw new RuntimeException("name is null");
    }
    return name;
  }

  public static @NotNull YqlValue getNameElement(@NotNull YqlProperty property) {
    PsiElement firstChild = property.getFirstChild();
    assert firstChild instanceof YqlLiteral || firstChild instanceof YqlReferenceExpression;
    YqlValue firstValue = (YqlValue)firstChild;
    if (firstValue == null) {
      throw new RuntimeException("Could not get first yql node.");
    }
    return firstValue;
  }

  public static @Nullable YqlValue getValue(@NotNull YqlProperty property) {
    return (YqlValue)PsiTreeUtil.getNextSiblingOfType(getNameElement(property), YqlValue.class);
  }

  public static boolean isQuotedString(@NotNull YqlLiteral literal) {
    // return literal.getNode().findChildByType(YqlTokenSets.STRING_LITERALS) != null;
    return false;
  }

  public static @Nullable ItemPresentation getPresentation(final @NotNull YqlProperty property) {
    return new ItemPresentation() {
      public @Nullable String getPresentableText() {
        return property.getName();
      }

      public @Nullable String getLocationString() {
        YqlValue value = property.getValue();
        return value instanceof YqlLiteral ? value.getText() : null;
      }

      public @Nullable Icon getIcon(boolean unused) {
        if (property.getValue() instanceof YqlArray) {
          return Json.Array;
        } else {
          return property.getValue() instanceof YqlObject ? PlatformIcons.CLASS_ICON : PlatformIcons.PROPERTY_ICON;
        }
      }
    };
  }

  public static @Nullable ItemPresentation getPresentation(@NotNull YqlArray array) {
    return new ItemPresentation() {
      public @Nullable String getPresentableText() {
        // return YqlBundle.message("yql.array", new Object[0]);
        return "array";
      }

      public @Nullable Icon getIcon(boolean unused) {
        return Json.Array;
      }
    };
  }

  public static @Nullable ItemPresentation getPresentation(@NotNull YqlObject object) {
    return new ItemPresentation() {
      public @Nullable String getPresentableText() {
        // return JsonBundle.message("json.object", new Object[0]);
        return "object";
      }

      public @Nullable Icon getIcon(boolean unused) {
        return Json.Object;
      }
    };
  }


  public static void delete(@NotNull YqlProperty property) {
    ASTNode myNode = property.getNode();
    JsonPsiChangeUtils.removeCommaSeparatedFromList(myNode, myNode.getTreeParent());
  }

  public static @NotNull String getValue(@NotNull YqlStringLiteral literal) {
    String txt = StringUtil.unescapeStringCharacters(JsonPsiUtil.stripQuotes(literal.getText()));
    if (txt == null) {
      throw new RuntimeException("Could not get text");
    }
    return txt;
  }

  public static boolean isPropertyName(@NotNull YqlStringLiteral literal) {
    PsiElement parent = literal.getParent();
    return parent instanceof YqlProperty && ((YqlProperty)parent).getNameElement() == literal;
  }

  public static boolean getValue(@NotNull YqlBooleanLiteral literal) {
    return literal.textMatches("true");
  }

  public static double getValue(@NotNull YqlNumberLiteral literal) {
    return Double.parseDouble(literal.getText());
  }
}
