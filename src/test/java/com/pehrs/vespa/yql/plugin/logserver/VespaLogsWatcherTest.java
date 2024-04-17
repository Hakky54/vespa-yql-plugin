package com.pehrs.vespa.yql.plugin.logserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Optional;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VespaLogsWatcherTest {

  @Test
  public void testGetLogarchivePath() {
    String archiverPath = new File("./src/test/testData/logarchive").getAbsolutePath();

    Optional<String> logarchiverPath = VespaLogsWatcher.getLogarchivePath(archiverPath);
    assertTrue(logarchiverPath.isPresent());

    assertEquals(archiverPath, logarchiverPath.get());
  }

  @Test
  public void testGetLogarchiveSubPath() {
    String archiverPath = new File("./src/test/testData/logarchive").getAbsolutePath();
    String absPath = String.format("%s/2024/01", archiverPath);

    Optional<String> logarchiverPath = VespaLogsWatcher.getLogarchivePath(absPath);
    assertTrue(logarchiverPath.isPresent());

    assertEquals(archiverPath, logarchiverPath.get());
  }


  @Test
  public void testGetLatestYearPath() {
    String archiverPath = new File("./src/test/testData/logarchive").getAbsolutePath();

    Optional<String> latestYear = VespaLogsWatcher.getLatestYearPath(archiverPath);
    assertTrue(latestYear.isPresent());

    assertEquals(archiverPath + "/2024", latestYear.get());
  }

  @Test
  public void testGetLatestMonthPath() {
    String archiverPath = new File("./src/test/testData/logarchive").getAbsolutePath();

    Optional<String> latestMonthPath = VespaLogsWatcher.getLatestMonthPath(archiverPath);
    assertTrue(latestMonthPath.isPresent());

    assertEquals(archiverPath + "/2024/02", latestMonthPath.get());
  }


  @Test
  public void testGetLatestDayPath() {
    String archiverPath = new File("./src/test/testData/logarchive").getAbsolutePath();

    Optional<String> latestDayPath = VespaLogsWatcher.getLatestDayPath(archiverPath);
    assertTrue(latestDayPath.isPresent());

    assertEquals(archiverPath + "/2024/02/03", latestDayPath.get());
  }

  @Test
  public void testGetLatestDayPathFromSubDir() {
    String archiverPath = new File("./src/test/testData/logarchive").getAbsolutePath();
    String subPath = String.format("%s/2024/01", archiverPath);

    Optional<String> latestDayPath = VespaLogsWatcher.getLatestDayPath(subPath);
    assertTrue(latestDayPath.isPresent());

    assertEquals(archiverPath + "/2024/02/03", latestDayPath.get());
  }
}