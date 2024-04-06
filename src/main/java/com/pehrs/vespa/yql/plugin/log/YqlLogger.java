package com.pehrs.vespa.yql.plugin.log;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;

public class YqlLogger implements Logger {

  // FIXME: Set this from configuration
  private static Level level = Level.DEBUG;

  // Let's assume that we are in Intellij console :-)
  // FIXME: Set this from configuration
  private static boolean colorCodeOutput = true; // (System.console() != null);

  public static void setLevel(Level level) {
    level = level;
  }

  private final String name;

  public YqlLogger(String name) {
    this.name = name.replace("com.pehrs.vespa.yql.plugin.", "");
  }

  private void log(Level level, String s) {
    if (level.toInt() >= level.toInt()) {
      if(colorCodeOutput) {
        String levelColor = ConsoleColors.WHITE;
        switch(level) {
          case TRACE, DEBUG -> levelColor = ConsoleColors.WHITE_BOLD;
          case INFO -> levelColor = ConsoleColors.GREEN;
          case WARN -> levelColor = ConsoleColors.YELLOW_BOLD;
          case ERROR -> levelColor = ConsoleColors.RED_BOLD;
        }
        String nameColor = ConsoleColors.BLUE;
        String msgColor = ConsoleColors.WHITE;
        System.out.printf("%s[%s] %s%s: %s%s\n",
            levelColor, level.name(),
            nameColor,
            this.name,
            msgColor, s,
            ConsoleColors.RESET);
      } else {
        System.out.printf("[%s] %s: %s\n", level.name(), this.name, s);
      }
    }
  }


  private void log(Level level, String s, Throwable throwable) {
    if (level.toInt() >= level.toInt()) {
      log(level, s);
      throwable.printStackTrace();
    }
  }

  private void log(Level level, String s, Object o) {
    if (level.toInt() >= level.toInt()) {
      s = s.replace("{}", "" + o);
      log(level, s);
    }
  }

  private void log(Object[] objects, String s, Level level) {
    if (level.toInt() >= level.toInt()) {
      String msg = s;
      for (int i = 0; i < objects.length; i++) {
        msg = msg.replaceFirst("\\{}", "" + objects[i]);
      }
      log(level, msg);
    }
  }

  private void log(Level level, String s, Object o, Object o1) {
    if (level.toInt() >= level.toInt()) {
      String msg = s
          .replaceFirst("\\{}", "" + o)
          .replaceFirst("\\{}", "" + o1);
      log(level, msg);
    }
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public boolean isTraceEnabled() {
    return level.toInt() >= Level.TRACE.toInt();
  }

  @Override
  public void trace(String s) {
    log(Level.TRACE, s);
  }

  @Override
  public void trace(String s, Object o) {
    log(Level.TRACE, s, o);
  }

  @Override
  public void trace(String s, Object o, Object o1) {
    log(Level.TRACE, s, o, o1);
  }


  @Override
  public void trace(String s, Object... objects) {
    log(objects, s, Level.TRACE);
  }

  @Override
  public void trace(String s, Throwable throwable) {
    log(Level.TRACE, s, throwable);
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return isTraceEnabled();
  }

  @Override
  public void trace(Marker marker, String s) {
    this.trace(s);
  }

  @Override
  public void trace(Marker marker, String s, Object o) {
    trace(s, o);
  }

  @Override
  public void trace(Marker marker, String s, Object o, Object o1) {
    trace(s, o, o1);
  }

  @Override
  public void trace(Marker marker, String s, Object... objects) {
    trace(s, objects);
  }

  @Override
  public void trace(Marker marker, String s, Throwable throwable) {
    trace(s, throwable);
  }

  @Override
  public boolean isDebugEnabled() {
    return level.toInt() >= Level.DEBUG.toInt();
  }

  @Override
  public void debug(String s) {
    log(Level.DEBUG, s);
  }

  @Override
  public void debug(String s, Object o) {
    log(Level.DEBUG, s, o);
  }

  @Override
  public void debug(String s, Object o, Object o1) {
    log(Level.DEBUG, s, o, o1);
  }

  @Override
  public void debug(String s, Object... objects) {
    log(Level.DEBUG, s, objects);
  }

  @Override
  public void debug(String s, Throwable throwable) {
    log(Level.DEBUG, s, throwable);
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return isDebugEnabled();
  }

  @Override
  public void debug(Marker marker, String s) {
    log(Level.DEBUG, s);
  }

  @Override
  public void debug(Marker marker, String s, Object o) {
    log(Level.DEBUG, s, o);
  }

  @Override
  public void debug(Marker marker, String s, Object o, Object o1) {
    log(Level.DEBUG, s, o, o1);
  }

  @Override
  public void debug(Marker marker, String s, Object... objects) {
    log(Level.DEBUG, s, objects);
  }

  @Override
  public void debug(Marker marker, String s, Throwable throwable) {
    log(Level.DEBUG, s, throwable);
  }

  @Override
  public boolean isInfoEnabled() {
    return level.toInt() >= Level.INFO.toInt();
  }

  @Override
  public void info(String s) {
    log(Level.INFO, s);
  }

  @Override
  public void info(String s, Object o) {
    log(Level.INFO, s, o);
  }

  @Override
  public void info(String s, Object o, Object o1) {
    log(Level.INFO, s, o, o1);
  }

  @Override
  public void info(String s, Object... objects) {
    log(Level.INFO, s, objects);
  }

  @Override
  public void info(String s, Throwable throwable) {
    log(Level.INFO, s, throwable);
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return isInfoEnabled();
  }

  @Override
  public void info(Marker marker, String s) {
    log(Level.INFO, s);
  }

  @Override
  public void info(Marker marker, String s, Object o) {
    log(Level.INFO, s, o);
  }

  @Override
  public void info(Marker marker, String s, Object o, Object o1) {
    log(Level.INFO, s, o, o1);
  }

  @Override
  public void info(Marker marker, String s, Object... objects) {
    log(Level.INFO, s, objects);
  }

  @Override
  public void info(Marker marker, String s, Throwable throwable) {
    log(Level.INFO, s, throwable);
  }

  @Override
  public boolean isWarnEnabled() {
    return level.toInt() >= Level.WARN.toInt();
  }

  @Override
  public void warn(String s) {
    log(Level.WARN, s);
  }

  @Override
  public void warn(String s, Object o) {
    log(Level.WARN, s, o);
  }

  @Override
  public void warn(String s, Object... objects) {
    log(Level.WARN, s, objects);
  }

  @Override
  public void warn(String s, Object o, Object o1) {
    log(Level.WARN, s, o, o1);
  }

  @Override
  public void warn(String s, Throwable throwable) {
    log(Level.WARN, s, throwable);
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return isWarnEnabled();
  }

  @Override
  public void warn(Marker marker, String s) {
    log(Level.WARN, s);
  }

  @Override
  public void warn(Marker marker, String s, Object o) {
    log(Level.WARN, s, o);
  }

  @Override
  public void warn(Marker marker, String s, Object o, Object o1) {
    log(Level.WARN, s, o, o1);
  }

  @Override
  public void warn(Marker marker, String s, Object... objects) {
    log(Level.WARN, s, objects);
  }

  @Override
  public void warn(Marker marker, String s, Throwable throwable) {
    log(Level.WARN, s, throwable);
  }

  @Override
  public boolean isErrorEnabled() {
    return level.toInt() >= Level.ERROR.toInt();
  }

  @Override
  public void error(String s) {
    log(Level.ERROR, s);
  }

  @Override
  public void error(String s, Object o) {
    log(Level.ERROR, s, o);
  }

  @Override
  public void error(String s, Object o, Object o1) {
    log(Level.ERROR, s, o, o1);
  }

  @Override
  public void error(String s, Object... objects) {
    log(Level.ERROR, s, objects);
  }

  @Override
  public void error(String s, Throwable throwable) {
    log(Level.ERROR, s, throwable);
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return isErrorEnabled();
  }

  @Override
  public void error(Marker marker, String s) {
    log(Level.ERROR, s);
  }

  @Override
  public void error(Marker marker, String s, Object o) {
    log(Level.ERROR, s, o);
  }

  @Override
  public void error(Marker marker, String s, Object o, Object o1) {
    log(Level.ERROR, s, o, o1);
  }

  @Override
  public void error(Marker marker, String s, Object... objects) {
    log(Level.ERROR, s, objects);
  }

  @Override
  public void error(Marker marker, String s, Throwable throwable) {
    log(Level.ERROR, s, throwable);
  }
}
