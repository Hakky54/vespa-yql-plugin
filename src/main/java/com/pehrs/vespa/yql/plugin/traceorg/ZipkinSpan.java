package com.pehrs.vespa.yql.plugin.traceorg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Deprecated
public record ZipkinSpan(String traceId,
                         String id,
                         String parentId,
                         String name,
                         String kind,
                         Long timestamp, // Microseconds
                         Long duration, // Microseconds
                         ZipkinEndpoint localEndpoint,
                         ZipkinEndpoint remoteEndpoint,
                         Map<String, Object> tags) {

  public static final String VESPA_PROTON_SERVICE = "Vespa Proton Service";
  public static final String VESPA_QUERY_SERVICE = "Vespa QUERY Service";

  public static ZipkinSpan create(String traceId, String name) {
    return new ZipkinSpan(traceId,
        ZipkinUtils.generateId(),
        null,
        name, "CLIENT",
        0L,
        0L,
        new ZipkinEndpoint(VESPA_PROTON_SERVICE),
        new ZipkinEndpoint(VESPA_PROTON_SERVICE),
        new HashMap<>());
  }

  public ZipkinSpan withTraceId(String newValue) {
    return new ZipkinSpan(newValue, id, parentId, name, kind, timestamp, duration, localEndpoint,
        remoteEndpoint, tags);
  }

  public ZipkinSpan withId(String newValue) {
    return new ZipkinSpan(traceId, newValue, parentId, name, kind, timestamp, duration,
        localEndpoint,
        remoteEndpoint, tags);
  }

  public ZipkinSpan withParentId(String newValue) {
    return new ZipkinSpan(traceId, id, newValue, name, kind, timestamp, duration, localEndpoint,
        remoteEndpoint, tags);
  }

  public ZipkinSpan withName(String newValue) {
    return new ZipkinSpan(traceId, id, parentId, newValue, kind, timestamp, duration, localEndpoint,
        remoteEndpoint, tags);
  }

  public ZipkinSpan withKind(String newValue) {
    return new ZipkinSpan(traceId, id, parentId, name, newValue, timestamp, duration, localEndpoint,
        remoteEndpoint, tags);
  }

  public ZipkinSpan withTimestamp(Long newValue) {
    return new ZipkinSpan(traceId, id, parentId, name, kind, newValue, duration, localEndpoint,
        remoteEndpoint, tags);
  }

  public ZipkinSpan withDuration(Long newValue) {
    return new ZipkinSpan(traceId, id, parentId, name, kind, timestamp, newValue, localEndpoint,
        remoteEndpoint, tags);
  }

  public ZipkinSpan withLocalEndpoint(ZipkinEndpoint newValue) {
    return new ZipkinSpan(traceId, id, parentId, name, kind, timestamp, duration, newValue,
        remoteEndpoint,
        tags);
  }

  public ZipkinSpan withRemoteEndpoint(ZipkinEndpoint newValue) {
    return new ZipkinSpan(traceId, id, parentId, name, kind, timestamp, duration, localEndpoint,
        newValue,
        tags);
  }

  public ZipkinSpan withTags(Map<String, Object> newValue) {
    return new ZipkinSpan(traceId, id, parentId, name, kind, timestamp, duration, localEndpoint,
        remoteEndpoint, newValue);
  }

//  public Long getAdjustedTimestamp(List<ZipkinSpan> inList){
//    List<ZipkinSpan> children = getChildren(inList);
//    if(children.size() > 0) {
//      List<ZipkinSpan> peers = getPeers(inList);
//      Optional<ZipkinSpan> prev = getPrevSpan(peers);
//      if(prev.isEmpty()) {
//        // Get parent timstamp
//        return getParent(inList).map(parent -> parent.timestamp)
//            .orElse(this.timestamp);
//      } else {
//        return prev.map(p -> p.timestamp)
//            .orElse(this.timestamp);
//      }
//    }
//    return this.timestamp;
//  }
//
//  public Long getDurationInMicros(List<ZipkinSpan> inList) {
//    List<ZipkinSpan> children = getChildren(inList);
//    if(children.size() == 0) {
//
//      List<ZipkinSpan> peers = getPeers(inList);
////      Optional<ZipkinSpan> next = getNextSpan(peers);
////      return next.map(
////          nextSpan -> nextSpan.timestamp - this.timestamp
////      ).orElse(getDurationViaParent(inList));
//      Optional<ZipkinSpan> prev  = getPrevSpan(peers);
//      if(prev.isPresent()) {
//        return this.timestamp - prev.get().timestamp;
//      } else {
//        return getParent(inList).map(parent -> this.timestamp - parent.timestamp)
//            .orElse(this.duration);
//      }
//    } else {
//      List<ZipkinSpan> peers = getPeers(inList);
//      Optional<ZipkinSpan> prev = getPrevSpan(peers);
//      if(prev.isPresent()) {
//        return prev.map(p -> this.timestamp - p.timestamp)
//            .orElse(this.duration);
//      } else {
//        Optional<ZipkinSpan> parent = getParent(inList);
//        return parent.map(p -> this.timestamp - p.timestamp)
//            .orElse(this.duration);
//      }
//    }
//  }

  private Long getDurationViaParent(List<ZipkinSpan> inList) {
    return getParent(inList)
        .map(parent -> parent.timestamp - timestamp)
        .orElse(0L);
  }


  private List<ZipkinSpan> getChildren(List<ZipkinSpan> inList) {
    return inList.stream().filter(span -> span.parentId().equals(this.id()))
        .collect(Collectors.toList());
  }

  private Optional<ZipkinSpan> getParent(List<ZipkinSpan> inList) {
    return inList.stream().filter(span -> span.id().equals(this.parentId()))
        .findFirst();
  }


  private Optional<ZipkinSpan> getPrevSpan(List<ZipkinSpan> peers) {
    for (int i = 0; i < peers.size(); i++) {
      if (peers.get(i).id.equals(this.id)) {
        if (i > 0) {
          return Optional.of(peers.get(i - 1));
        } else {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  private Optional<ZipkinSpan> getNextSpan(List<ZipkinSpan> peers) {
    for (int i = 0; i < peers.size(); i++) {
      if (peers.get(i).id.equals(this.id)) {
        if ((i + 1) < peers.size()) {
          return Optional.of(peers.get(i + 1));
        } else {
          return Optional.empty();
        }
      }
    }
    return Optional.empty();
  }

  private List<ZipkinSpan> getPeers(List<ZipkinSpan> inList) {
    return inList.stream()
        .filter(span -> span.parentId().equals(this.parentId))
        .collect(Collectors.toList());
  }

//  public ZipkinSpan adjustDuration(List<ZipkinSpan> inList) {
//    return withDuration(this.getDurationInMicros(inList));
//  }
//
//  /***
//   * Adjust the timstamp if needed
//   */
//  public ZipkinSpan adjustTimestamp(List<ZipkinSpan> inList) {
//    return withTimestamp(this.getAdjustedTimestamp(inList));
//  }
}

