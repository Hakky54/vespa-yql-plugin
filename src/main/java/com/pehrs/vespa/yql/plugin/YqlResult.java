package com.pehrs.vespa.yql.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.tuple.Pair;

public class YqlResult {

  public static String YQL_SAMPLE_RESULT = YQL.getResource("/vespa-results-sample.json");
  public static String YQL_SAMPLE_INIT_RESULT = YQL.getResource("/vespa-results-init.json");
  static List<YqlResultListener> listeners = new ArrayList();
  private static ObjectMapper mapper = new ObjectMapper();
  private static YqlResult lastResult = null;

  public final JsonNode result;

  public YqlResult(String jsonTxt) throws JsonProcessingException {
    this.result = mapper.readTree(jsonTxt);
  }

  public YqlResult(JsonNode result) {
    this.result = result;
  }

  public JsonNode getJson() {
    return result;
  }

  public static YqlResult getYqlResult() {
    return lastResult;
  }

  public static void updateResult(YqlResult result) {
    lastResult = result;
    notifyResultListeners();
  }

  public static void addResultListener(YqlResultListener listener) {
    if(!listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  public void removeResultListener(YqlResultListener listener) {
    if(YqlResult.listeners.contains(listener)) {
      YqlResult.listeners.remove(listener);
    }
  }
  private static void notifyResultListeners() {
    synchronized (listeners) {
      List<YqlResultListener> toBeRemoved = listeners.stream().flatMap(listener -> {
        try {
          listener.resultUpdated(lastResult);
        } catch (Exception ex) {
          ex.printStackTrace();
          return Stream.of(listener);
        }
        return Stream.empty();
      }).collect(Collectors.toList());
      listeners.removeAll(toBeRemoved);
    }
  }

  public String toString() {
    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public Optional<JsonNode> getTrace() {
    return Optional.ofNullable(this.result.get("trace"));
  }

  public record YqlQueryError(Integer code, String summary, String message) {}

  public List<YqlQueryError> getErrors() {
    // Maybe single error?
    JsonNode error = this.result.get("error");
    if(error != null) {
      return List.of(
          new YqlQueryError(-1, "vespa error", error.asText())
      );
    }
    JsonNode errors = this.result.get("root").get("errors");
    if(errors != null) {
      return StreamSupport.stream(
              Spliterators.spliteratorUnknownSize(errors.iterator(), Spliterator.ORDERED),
              false).collect(Collectors.toList())
          .stream().map(errorNode -> {
            int code = errorNode.get("code").asInt();
            String summary = errorNode.get("summary").asText();
            String message = errorNode.get("message").asText();
            return new YqlQueryError(code, summary, message);
          })
          .collect(Collectors.toList());
    }

    return List.of();
  }

  public List<String> getColumnNames() {
    JsonNode children = this.result.get("root").get("children");
    if(children == null) {
      return List.of();
    }
    JsonNode firstChild = children.iterator().next();
    JsonNode fields = firstChild.get("fields");
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(fields.fieldNames(), Spliterator.ORDERED),
        false).collect(Collectors.toList());
  }

  public interface YqlResultListener {
    void resultUpdated(YqlResult result);
  }

  public record YqlResultRow(Double relevance, Map<String, Object> fields) {

  }

  public List<YqlResultRow> getRows() {
    JsonNode children = this.result.get("root").get("children");
    if(children==null) {
      return List.of();
    }
    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(children.iterator(), Spliterator.ORDERED),
            false)
        .flatMap(child -> {
          if (child.has("fields")) {
            double relvance = child.get("relevance").asDouble();
            JsonNode fields = child.get("fields");
            Map<String, Object> rowFields = StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(fields.fieldNames(), Spliterator.ORDERED),
                    false)
                .map(fieldName -> {
                  JsonNode fieldVal = fields.get(fieldName);
                  Pair<String, Object> pair = switch (fieldVal.getNodeType()) {
                    case NUMBER -> {
                      if (fieldVal.isDouble()) {
                        yield Pair.<String, Object>of(fieldName, fieldVal.asDouble());
                      } else if (fieldVal.isInt()) {
                        yield Pair.<String, Object>of(fieldName, fieldVal.asInt());
                      } else if (fieldVal.isFloat()) {
                        yield Pair.<String, Object>of(fieldName, fieldVal.isFloat());
                      } else if (fieldVal.isLong()) {
                        yield Pair.<String, Object>of(fieldName, fieldVal.asLong());
                      } else {
                        yield Pair.<String, Object>of(fieldName,
                            "UNSUPPORTED TYPE " + fieldVal.getNodeType());
                      }
                    }
                    case STRING -> Pair.<String, Object>of(fieldName, fieldVal.asText());
                    case POJO, OBJECT -> {
                      if (fieldVal.has("type") && fieldVal.has("values")) {
                        String vespaFieldType = fieldVal.get("type").asText();
                        if (vespaFieldType.indexOf("<float>") != -1) {
                          JsonNode valuesArray = fieldVal.get("values");
                          List<Float> floats = jsonNode2Floats(valuesArray);
                          yield Pair.of(fieldName, floats);
                        } else {
                          // FIXME: Support more types when needed...
                          yield Pair.of(fieldName, null); // This would fail!!!
                        }
                      } else {
                        yield Pair.of(fieldName, null); // This would fail!!!
                      }
                    }
                    case BOOLEAN -> Pair.of(fieldName, fieldVal.asBoolean());
                    case NULL, ARRAY, BINARY, MISSING -> Pair.<String, Object>of(fieldName,
                        "UNSUPPORTED TYPE " + fieldVal.getNodeType());
                  };
                  return pair;
                }).collect(Collectors.toMap(
                    p -> p.getLeft(),
                    p -> p.getRight()
                ));
            return Stream.of(new YqlResultRow(relvance, rowFields));
          }
          return Stream.empty();
        })
        .collect(Collectors.toList());
  }

  private List<Float> jsonNode2Floats(JsonNode valuesArray) {
    return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(valuesArray.iterator(), Spliterator.ORDERED),
            false)
        .map(value -> (float) value.asDouble())
        .collect(Collectors.toList());
  }


}
