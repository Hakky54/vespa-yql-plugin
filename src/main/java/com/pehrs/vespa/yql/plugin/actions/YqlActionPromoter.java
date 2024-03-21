package com.pehrs.vespa.yql.plugin.actions;

import com.intellij.openapi.actionSystem.ActionPromoter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.actions.SplitLineAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.vfs.VirtualFile;
import com.pehrs.vespa.yql.plugin.YqlFileType;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class YqlActionPromoter implements ActionPromoter {

  public List<AnAction> suppress(@NotNull List<? extends AnAction> actions, @NotNull DataContext context) {
    // Intrinsics.checkNotNullParameter(actions, "actions");
    // Intrinsics.checkNotNullParameter(context, "context");
    if ((EditorEx) CommonDataKeys.EDITOR.getData(context) == null) {
      // return super.suppress(actions, context);
      return Collections.emptyList();
    }
    EditorEx editor = (EditorEx)CommonDataKeys.EDITOR.getData(context);

    if (editor.getVirtualFile() == null) {
      // return super.suppress(actions, context);
      return Collections.emptyList();
    }
    VirtualFile virtualFile = editor.getVirtualFile();
    if (virtualFile.getFileType() instanceof YqlFileType) {

      // Let's remove the SplitLineAction as it is mapped to ctrl+Enter which we want to replace! :-)
      List<AnAction> destinations = actions.stream()
          .filter(action -> action instanceof SplitLineAction)
          .collect(Collectors.toList());
      return destinations;
    }
    return Collections.emptyList();
  }
}
