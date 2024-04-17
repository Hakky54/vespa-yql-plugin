package com.pehrs.vespa.yql.plugin.logserver;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import com.pehrs.vespa.yql.plugin.util.NotificationUtils;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jheaps.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VespaLogsWatcher {

  private static Path logPath;
  private static String fn2Match = "vespa.log";


  private static final Logger log = LoggerFactory.getLogger(VespaLogsWatcher.class);


  private static final WatchService logarchiveWatcher = createWatcher();

  private static Path logarchiveWatchPath = null;
  private static Future<?> logarchiveWatchFuture; // Only used for logarchive monitoring

  private static final WatchService yearWatcher = createWatcher();
  private static Path yearWatchPath = null;
  private static Future<?> yearWatchFuture; // Only used for logarchive monitoring

  private static final WatchService monthWatcher = createWatcher();
  private static Path monthWatchPath = null;
  private static Future<?> monthWatchFuture; // Only used for logarchive monitoring

  private static final WatchService dayWatcher = createWatcher();
  private static Path dayWatchPath;
  private static Future<?> watchFuture;


  private static List<VespaLogsWatcherListener> listeners = new ArrayList<>();

  private VespaLogsWatcher() {
  }


  // Montior only ONE file (typically vespa.log)
  // Set to true if we see "logarchive" in the logPath.
  private static boolean inLogarchive() {
    return logPath.toFile().getAbsolutePath().contains(String.format("/logarchive"));
  }


  public interface VespaLogsWatcherListener {

    void logUpdated(Path path);
  }

  static WatchService createWatcher() {
    try {
      return FileSystems.getDefault().newWatchService();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void forceReload() {
    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    setLogsPath(Path.of(settings.logsPath));
  }


  public static void resolveLogsDir(Path path2resolve) {

    File file = path2resolve.toFile();
    if (file.isFile()) {
      logPath = Path.of(file.getParent());
      fn2Match = file.getName();
    } else {
      // Directory

      if (path2resolve.toFile().getAbsolutePath().contains("logarchive")) {
        log.debug("Monitor logarchive files...");
      }
      logPath = path2resolve;
      try {
        Path fnPath = path2resolve.resolve("vespa.log");
        if (fnPath.toFile().exists() && fnPath.toFile().isFile()) {
          fn2Match = "vespa.log";
        }
      } catch (InvalidPathException ex) {
        log.error("Could not resolve vespa.log in path " + path2resolve);
        // FIXME: Resolve the logarchive dir and file to look for
        // path2resolve.resolve("logarchive");
      }
    }
  }



  private static Function<Runnable, Future<?>> threadSpawner = VespaLogsWatcher::ideThreadSpawner;
  private static boolean inIde = true;

  @VisibleForTesting
  public static void setPlainThreadSpawner() {
    inIde = false;
    threadSpawner = VespaLogsWatcher::plainThreadSpawner;
  }

  @VisibleForTesting
  private static ExecutorService plainThreadExecutor = null;

  @VisibleForTesting
  static Future<?> plainThreadSpawner(Runnable runnable) {
    if (plainThreadExecutor == null) {
      plainThreadExecutor = Executors.newFixedThreadPool(10);
    }
    return plainThreadExecutor.submit(runnable);
  }

  static Future<?> ideThreadSpawner(Runnable runnable) {
    return ApplicationManager.getApplication()
        .executeOnPooledThread(() -> {
          runnable.run();
        });
  }


  public static void setLogsPath(Path path) {
    log.info("setLogsPath: " + path);
    resolveLogsDir(path);

    if (inLogarchive()) {
      Optional<String> logarchiveAbsPath = getLogarchivePath(logPath.toFile().getAbsolutePath());
      if (logarchiveWatchFuture != null) {
        // Check if path has changed
        if (!(logarchiveAbsPath.orElseGet(() -> "")).equals(
            logarchiveWatchPath.toFile().getAbsolutePath())) {
          logarchiveWatchFuture.cancel(true);
          logarchiveWatchFuture = null;
          logarchiveWatchPath = null;
        }
      }
      Optional<String> lastYearAbsPath = getLatestYearPath(logPath.toFile().getAbsolutePath());
      if (yearWatchPath != null) {
        if (!(lastYearAbsPath.orElseGet(() -> "")).equals(
            yearWatchPath.toFile().getAbsolutePath())) {
          yearWatchFuture.cancel(true);
          yearWatchFuture = null;
          yearWatchPath = null;
        }
      }
      Optional<String> lastMonthAbsPath = getLatestMonthPath(logPath.toFile().getAbsolutePath());
      if (monthWatchFuture != null) {
        if (!(lastMonthAbsPath.orElseGet(() -> "")).equals(
            monthWatchPath.toFile().getAbsolutePath())) {
          monthWatchFuture.cancel(true);
          monthWatchFuture = null;
          monthWatchPath = null;
        }
      }
      Optional<String> lastDayAbsPath = getLatestDayPath(logPath.toFile().getAbsolutePath());
      if (watchFuture != null) {
        if (!(lastDayAbsPath.orElseGet(() -> "")).equals(
            dayWatchPath.toFile().getAbsolutePath())) {
          watchFuture.cancel(true);
          watchFuture = null;
          dayWatchPath = null;
        }
      }

      // Add the year and mont monitors
      AtomicInteger notificationCount = new AtomicInteger(0);
      if (logarchiveWatchFuture == null) {
        logarchiveWatchFuture = threadSpawner.apply(() -> {
          logarchiveAbsPath
              .ifPresentOrElse(archiveAbsolutePath -> {
                log.debug("Starting logarchive watcher on " + archiveAbsolutePath);
                logarchiveWatchPath = Path.of(archiveAbsolutePath);
                watchPath(logarchiveWatcher,
                    logarchiveWatchPath,
                    (child) -> {
                  // setLogsPath(logPath);
                  System.out.println("LOGARCHIVE CHANGE: " + child);
                });
              }, () -> {
                String msg = "Could not monitor log path " + logarchiveAbsPath.get();
                log.error(msg);
                if (inIde) {
                  Project[] projects = ProjectManager.getInstance().getOpenProjects();
                  if (projects != null && projects.length > 0) {
                    NotificationUtils.showNotification(projects[0], NotificationType.ERROR,
                        msg);
                    notificationCount.incrementAndGet();
                  }
                }
              });
        });
      }
      if (yearWatchFuture == null) {
        yearWatchFuture = threadSpawner.apply(() -> {
          lastYearAbsPath
              .ifPresentOrElse(archiveAbsolutePath -> {
                log.debug("Starting YEAR log watcher on " + archiveAbsolutePath);
                yearWatchPath = Path.of(archiveAbsolutePath);
                watchPath(yearWatcher, yearWatchPath, (child) -> {
                  setLogsPath(logPath);
                });
              }, () -> {
                String msg = "Could not monitor log path " + logarchiveAbsPath.get();
                log.error(msg);
                if (notificationCount.getAndIncrement() == 0 && inIde) {
                  Project[] projects = ProjectManager.getInstance().getOpenProjects();
                  if (projects != null && projects.length > 0) {
                    NotificationUtils.showNotification(projects[0], NotificationType.ERROR,
                        msg);
                    notificationCount.incrementAndGet();
                  }
                }
              });
        });
      }
      if (monthWatchPath == null) {
        monthWatchFuture = threadSpawner.apply(() -> {
          lastMonthAbsPath
              .ifPresentOrElse(archiveAbsolutePath -> {
                log.debug("Starting MONTH log watcher on " + archiveAbsolutePath);
                monthWatchPath = Path.of(archiveAbsolutePath);
                watchPath(monthWatcher, monthWatchPath, (child) -> {
                  setLogsPath(logPath);
                });
              }, () -> {
                String msg = "Could not monitor log path " + lastMonthAbsPath.get();
                log.error(msg);
                // Only notify if we have not done so yet!
                if (notificationCount.getAndIncrement() == 0 && inIde) {
                  Project[] projects = ProjectManager.getInstance().getOpenProjects();
                  if (projects != null && projects.length > 0) {
                    NotificationUtils.showNotification(projects[0], NotificationType.ERROR,
                        msg);
                  }
                }
              });
        });
      }
      if (watchFuture == null) {
        watchFuture = threadSpawner.apply(() -> {
          lastDayAbsPath
              .ifPresentOrElse(archiveAbsolutePath -> {
                dayWatchPath = Path.of(archiveAbsolutePath);
                log.debug("Starting DAY log watcher on " + dayWatchPath);
                watchPath(dayWatcher, dayWatchPath, (child) -> {
                  // setLogsPath(logPath);
                  notifyListeners(child);
                });
              }, () -> {
                String msg = "Could not monitor log path " + lastDayAbsPath.get();
                log.error(msg);
                // Only notify if we have not done so yet!
                if (notificationCount.getAndIncrement() == 0 && inIde) {
                  Project[] projects = ProjectManager.getInstance().getOpenProjects();
                  if (projects != null && projects.length > 0) {
                    NotificationUtils.showNotification(projects[0], NotificationType.ERROR,
                        msg);
                  }
                }
              });
        });
      }

    } else {
      if (watchFuture != null) {
        watchFuture.cancel(true);
        watchFuture = null;
        dayWatchPath = null; // Not really needed but just because :-)
      }
      watchFuture = threadSpawner.apply(() -> {
        log.debug("Starting log watcher on " + logPath);
        dayWatchPath = logPath; // Not really needed but just because :-)
        watchPath(dayWatcher, logPath, (child) -> {
          if (child.toFile().getName().endsWith(fn2Match)) {
            notifyListeners(child);
          }
        });
      });
    }
  }


  public static Optional<String> getLogarchivePath(String absPath) {
    Pattern logarchivePattern = Pattern.compile("(.*/logarchive).*");

    Matcher matcher = logarchivePattern.matcher(absPath);
    if (!matcher.matches()) {
      return Optional.empty();
    }
    return Optional.of(matcher.group(1));
  }

  static Pattern yearPattern = Pattern.compile("\\d\\d\\d\\d");
  static Pattern monthAndDayPattern = Pattern.compile("\\d\\d");

  // Get the path of the latest year in logarchive path
  static Optional<String> getLatestYearPath(String fromPath) {
    return getLogarchivePath(fromPath)
        .map(archiverPath -> {
          File dir = new File(archiverPath);
          String[] years = dir.list((dir1, name) -> yearPattern.matcher(name).matches());
          // Make sure list is mutable
          List<String> yearList = new ArrayList<>(Arrays.asList(years));
          Collections.sort(yearList);
          return archiverPath + "/" + yearList.get(yearList.size() - 1);
        });
  }

  static Optional<String> getLatestMonthPath(String fromPath) {
    return getLatestYearPath(fromPath)
        .map(latestYearPath -> {
          File dir = new File(latestYearPath);
          String[] months = dir.list((dir1, name) -> monthAndDayPattern.matcher(name).matches());
          // Make sure list is mutable
          List<String> monthList = new ArrayList<>(Arrays.asList(months));
          Collections.sort(monthList);
          return latestYearPath + "/" + monthList.get(monthList.size() - 1);
        });
  }


  static Optional<String> getLatestDayPath(String fromPath) {
    return getLatestMonthPath(fromPath)
        .map(latestMonthPath -> {
          File dir = new File(latestMonthPath);
          String[] days = dir.list((dir1, name) -> monthAndDayPattern.matcher(name).matches());
          // Make sure list is mutable
          List<String> dayList = new ArrayList<>(Arrays.asList(days));
          Collections.sort(dayList);
          return latestMonthPath + "/" + dayList.get(dayList.size() - 1);
        });
  }

  public static void start() throws IOException {
    forceReload();
  }

  public static void addListener(VespaLogsWatcherListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  public static void removeListener(VespaLogsWatcherListener listener) {
    if (listeners.contains(listener)) {
      listeners.remove(listener);
    }
  }

  private static void notifyListeners(Path path) {
    List<VespaLogsWatcherListener> toBeRemoved = listeners.stream().flatMap(listener -> {
      try {
        listener.logUpdated(path);
      } catch (Exception ex) {
        log.warn("Listener failed " + listener);
        return Stream.of(listener);
      }
      return Stream.empty();
    }).collect(Collectors.toList());
    listeners.removeAll(toBeRemoved);
  }

  private static void watchPath(
      WatchService watcher,
      Path path,
      Consumer<Path> fileChangedFn) {
    log.debug("watching files in " + path);
    try {

      WatchKey key = path.register(watcher,
          ENTRY_CREATE,
          //  ENTRY_DELETE,
          ENTRY_MODIFY);

      pollPendingEvents(key, path, fileChangedFn);

      for (; ; ) { // ever...

        // wait for key to be signaled
        try {
          key = watcher.take();
        } catch (InterruptedException x) {
          return;
        }

        pollPendingEvents(key, path, fileChangedFn);

        // Reset the key -- this step is critical if you want to
        // receive further watch events.  If the key is no longer valid,
        // the directory is inaccessible so exit the loop.
        boolean valid = key.reset();
        if (!valid) {
          break;
        }
      }
    } catch (IOException e) {
      watchFuture = null;
      log.warn(e.getMessage(), e);
      ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects != null && projects.length > 0) {
          NotificationUtils.showNotification(projects[0], NotificationType.ERROR, e.getMessage());
        }
      });
    }
  }

  public static void refreshAll() {
    try {
      if (inLogarchive()) {
        getLatestDayPath(logPath.toFile().getAbsolutePath())
            .ifPresentOrElse(archiveAbsolutePath -> {
              log.debug("Starting log watcher on " + archiveAbsolutePath);
              Path archivePath = Path.of(archiveAbsolutePath);
              String[] logFiles = archivePath.toFile().list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                  return !name.startsWith(".");
                }
              });
              List<String> sortedLogs = Arrays.asList(logFiles);
              Collections.sort(sortedLogs);
              String latestFn = sortedLogs.get(sortedLogs.size() - 1);
              String latestAbsFn = archiveAbsolutePath + "/" + latestFn;
              notifyListeners(Path.of(latestAbsFn));
            }, () -> {
              log.warn("could not get latest day log from " + logPath);
            });
      } else {
        notifyListeners(Path.of(logPath.toFile().getAbsolutePath(), fn2Match));
      }
    } catch (Exception ex) {
      log.warn(ex.getMessage(), ex);
      forceReload();
    }
  }

  private static void pollPendingEvents(WatchKey key,
      Path parentPath,
      Consumer<Path> fileChangedFn
  ) throws IOException {
    for (WatchEvent<?> event : key.pollEvents()) {
      WatchEvent.Kind<?> kind = event.kind();

      // Ignore overflow events
      if (kind == OVERFLOW) {
        continue;
      }

      // Ignore Delete events (should be none)
      if (kind == ENTRY_DELETE) {
        continue;
      }

      // The filename is the
      // context of the event.
      WatchEvent<Path> ev = (WatchEvent<Path>) event;
      Path filename = ev.context();
      Path child = parentPath.resolve(filename);
      // String fileType = Files.probeContentType(child);
      log.debug("FILE CHANGED: filename: " + filename + ", child: " + child);
      fileChangedFn.accept(child);
    }
  }
}
