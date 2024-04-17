package com.pehrs.vespa.yql.plugin.logserver;

import com.pehrs.vespa.yql.plugin.logserver.VespaLogsWatcher.VespaLogsWatcherListener;
import java.nio.file.Path;

public class VespaLogsWatcherSample {

  public static void main(String[] args) throws InterruptedException, ClassNotFoundException {

    VespaLogsWatcher.setPlainThreadSpawner();

    Path logsPath = Path.of(
        "./vespa-cluster/docker-compose/logs-simulator/logs/logarchive");

    VespaLogsWatcher.addListener(new VespaLogsWatcherListener() {
      @Override
      public void logUpdated(Path path) {
        System.out.println("LOG UPDATED: " + path);
      }
    });

    VespaLogsWatcher.setLogsPath(logsPath);

    for(;;) { // ever
      Thread.sleep(2000);
    }
  }

}
