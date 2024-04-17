package com.pehrs.vespa.yql.plugin.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface YqlAppSettingsStateListener {

  List<YqlAppSettingsStateListener> listeners = new ArrayList<>();

  void stateChanged(YqlAppSettingsState instance);


  static void addListener(YqlAppSettingsStateListener listener) {
    if (!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  static void removeListener(YqlAppSettingsStateListener listener) {
    if (listeners.contains(listener)) {
      listeners.remove(listener);
    }
  }

  static void notifyListeners(YqlAppSettingsState settings) {
    synchronized (listeners) {
      List<YqlAppSettingsStateListener> toBeRemoved = listeners.stream().flatMap(listener -> {
        try {
          listener.stateChanged(settings);
        } catch (Exception ex) {
          return Stream.of(listener);
        }
        return Stream.empty();
      }).collect(Collectors.toList());
      listeners.removeAll(toBeRemoved);
    }
  }
}
