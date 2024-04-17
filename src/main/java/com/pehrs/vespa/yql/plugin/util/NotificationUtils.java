package com.pehrs.vespa.yql.plugin.util;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class NotificationUtils {

  public static void showException(Project project, Exception ex) {
    String msg = ex.getMessage()
        // Make sure we render the error message correctly
        .replace("\n", "<br/>")
        .replace("<", "&lt;")
        .replace(">", "&gt;");
    // Links are not handled by the notifications, please see below for details
    // .replaceAll("(?:https|http)://([\\w/%.\\-?&=!#]+(?!.*\\[/))", "<a href=\"$0\">$0</a>")

    showNotification(project, NotificationType.ERROR, msg);
  }

  public static void showNotification(Project project,
      NotificationType notificationType,
      String msg) {
    showNotification(project, notificationType, "Vespa YQL", msg);
  }

  public static void showNotification(Project project,
      NotificationType notificationType,
      String groupId,
      String msg) {

    List<String> urls = BrowserUtils.extractUrls(msg);

    @NotNull Notification notification = NotificationGroupManager.getInstance()
        .getNotificationGroup(groupId)
        .createNotification("Vespa YQL: " + notificationType.name(),
            msg + (urls.size() > 0 ? "<h4>Links:</h4>" : ""),
            notificationType);

    // setListener is deprecated, and we need to use the addAction method instead
    for (String url : urls) {
      // .setListener(NotificationListener.URL_OPENING_LISTENER)
      notification = notification.addAction(NotificationAction.create(
          url,
          (event, notif) -> {
            try {
              BrowserUtils.openBrowser(new URI(url));
            } catch (IOException | URISyntaxException browserEx) {
              throw new RuntimeException(browserEx);
            }
          })
      );
    }

    notification
        .notify(project);
  }
}
