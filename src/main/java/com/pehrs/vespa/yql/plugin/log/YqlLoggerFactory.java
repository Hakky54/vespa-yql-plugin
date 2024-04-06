package com.pehrs.vespa.yql.plugin.log;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class YqlLoggerFactory implements ILoggerFactory {

  @Override
  public Logger getLogger(String s) {
    return new YqlLogger(s);
  }
}
