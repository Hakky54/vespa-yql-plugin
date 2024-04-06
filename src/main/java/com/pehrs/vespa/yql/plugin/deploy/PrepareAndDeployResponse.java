package com.pehrs.vespa.yql.plugin.deploy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intellij.notification.NotificationType;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PrepareAndDeployResponse {

  String message;
  @JsonProperty("session-id")
  String sessionId;
  Boolean activated;
  String tenant;
  String url;
  List<LogMessage> log;

  @Value
  @JsonIgnoreProperties(ignoreUnknown = true)
  @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
  @AllArgsConstructor
  public static class LogMessage {

    Long time;
    String level;
    String message;
    Boolean applicationPackage;
  }

  @JsonIgnore
  public NotificationType getNotificationType() {
    NotificationType res = NotificationType.INFORMATION;
    for(LogMessage logMessage:this.getLog()) {
      if (logMessage.getLevel().equals("WARNING")) {
        if(res.ordinal() < NotificationType.WARNING.ordinal()) {
          res = NotificationType.WARNING;
        }
      } else if (logMessage.getLevel().equals("ERROR")) {
        if(res.ordinal() < NotificationType.ERROR.ordinal()) {
          res = NotificationType.ERROR;
        }
      }
    }
    return res;
  }

  public String toHtml() {
    StringBuilder out = new StringBuilder();

    out.append("<h3>").append(this.getMessage()).append("</h3>").append(" \n");
    out.append("<ul>");
    this.getLog().forEach(logMessage -> {
      out
          .append("<li>")
          .append("[").append(logMessage.getLevel()).append("]: ")
          .append(logMessage.getMessage())
          .append("</li>")
          .append(" \n");
    });
    out.append("</ul>");

    return out.toString();
  }

}
