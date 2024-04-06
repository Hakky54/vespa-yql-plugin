package com.pehrs.vespa.yql.plugin.log;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public class YqlSLF4JServiceProvider implements SLF4JServiceProvider {

  public static String REQUESTED_API_VERSION = "2.0.99";

  private BasicMarkerFactory markerFactory;
  private BasicMDCAdapter mdcAdapter;
  private YqlLoggerFactory loggerFactory;

  @Override
  public void initialize() {
    this.markerFactory = new BasicMarkerFactory();
    this.mdcAdapter = new BasicMDCAdapter();
    this.loggerFactory = new YqlLoggerFactory();
  }

  @Override
  public ILoggerFactory getLoggerFactory() {
    return this.loggerFactory;
  }

  @Override
  public IMarkerFactory getMarkerFactory() {
    return this.markerFactory;
  }

  @Override
  public MDCAdapter getMDCAdapter() {
    return this.mdcAdapter;
  }

  @Override
  public String getRequestedApiVersion() {
    return REQUESTED_API_VERSION;
  }
}

