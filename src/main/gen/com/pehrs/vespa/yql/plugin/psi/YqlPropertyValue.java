// This is a generated file. Not intended for manual editing.
package com.pehrs.vespa.yql.plugin.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface YqlPropertyValue extends PsiElement {

  @Nullable
  YqlArray getArray();

  @Nullable
  YqlObject getObject();

  @Nullable
  YqlStringLiteral getStringLiteral();

}
