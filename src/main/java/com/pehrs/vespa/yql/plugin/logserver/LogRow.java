package com.pehrs.vespa.yql.plugin.logserver;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.pehrs.vespa.yql.plugin.util.NotificationUtils;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record LogRow(
    String timestamp, // formated timestamp.
    String host,
    String pid,
    String service,
    String component,
    String level,
    String message
) {


  enum Column {
    Timestamp, Level, Message, Service, Component, Host, Pid,
  }

  private static final Logger log = LoggerFactory.getLogger(LogRow.class);
  private static final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");


  public static List<LogRow> parseLogLines(String logContent) {
    String[] lines = logContent.split("\n");
    return Arrays.stream(lines)
        .map(LogRow::parseLogLine)
        .filter(opt -> opt.isPresent())
        .map(opt -> opt.get())
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "LogRow{" +
        "timestamp='" + timestamp + '\'' +
        ", host='" + host + '\'' +
        ", pid='" + pid + '\'' +
        ", service='" + service + '\'' +
        ", component='" + component + '\'' +
        ", level='" + level + '\'' +
        ", message='" + message + '\'' +
        '}';
  }

  public static Optional<LogRow> parseLogLine(String logLine) {
    try {
      String[] columns = logLine.split("\t");
      if (columns == null || columns.length != 7) {
        return Optional.empty();
      }
      LogRow row = new LogRow(
          getFormattedTimestamp(Double.parseDouble(columns[0])),
          columns[1],
          columns[2],
          columns[3],
          columns[4],
          columns[5],
          columns[6]
      );
      return Optional.of(row);
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      return Optional.empty();
    }


  }

  public String getColumnValue(Column column) {
    switch (column) {
      case Timestamp -> {
        return timestamp;
      }
      case Host -> {
        return host;
      }
      case Pid -> {
        return pid;
      }
      case Service -> {
        return service;
      }
      case Component -> {
        return component;
      }
      case Level -> {
        return level;
      }
      case Message -> {
        return message;
      }
    }
    return null;
  }

  public static String getFormattedTimestamp(double timestamp) {
    long ts = 1000L * (long) timestamp;
    return dateFmt.format(new Date(ts));
  }

  public static Path getLogToRead(Path rootPathOrFile) {

    File file = rootPathOrFile.toFile();
    if (file.isFile()) {
      return Path.of(file.getParent());
    } else {
      // Directory
      if (rootPathOrFile.toFile().getAbsolutePath().contains("logarchive")) {
        log.debug("Resolve last path of logarchive...");

        Optional<String> lastDayAbsPath = VespaLogsWatcher.getLatestDayPath(
            rootPathOrFile.toFile().getAbsolutePath());

        return lastDayAbsPath
            .map(archiveDir -> {
              File dir = new File(archiveDir);
              File[] logFiles = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                  return !name.startsWith(".") && !name.endsWith(".gz");
                }
              });

              List<File> sortedLogFiles = new ArrayList<>(Arrays.asList(logFiles));
              Collections.sort(sortedLogFiles);
              return sortedLogFiles.get(sortedLogFiles.size() - 1).toPath();
            })
            .orElseThrow(() -> new IllegalArgumentException(
                "Could not find any logarchiver logs in " + rootPathOrFile));
      } else {
        log.debug("Try to resolve vespa.log");
        try {
          Path fnPath = rootPathOrFile.resolve("vespa.log");
          if (fnPath.toFile().exists() && fnPath.toFile().isFile()) {
            return fnPath;
          } else {
            throw new IllegalArgumentException(
                "Could not find any vespa.log to tail in " + rootPathOrFile);
          }
        } catch (InvalidPathException ex) {
          throw new IllegalArgumentException(
              "Could not find any vespa.log to tail in/from " + rootPathOrFile);
        }
      }
    }
  }
}
