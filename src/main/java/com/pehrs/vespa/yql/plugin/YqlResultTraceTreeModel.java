package com.pehrs.vespa.yql.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.util.ui.tree.AbstractTreeModel;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlResultListener;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.swing.tree.TreePath;

public class YqlResultTraceTreeModel extends AbstractTreeModel implements YqlResultListener {

  public record JsonProperty(String name, JsonNode value) {

  }

  JsonProperty traceRoot;

  static ObjectMapper mapper = new ObjectMapper();

  public YqlResultTraceTreeModel() {
    // Start with empty node
    this.traceRoot = new JsonProperty("empty", mapper.createObjectNode());
  }

  @Override
  public Object getRoot() {
    return traceRoot;
  }

  private List<String> getFieldNames(JsonNode node) {
    Iterable<String> iterable = () -> node.fieldNames();
    return StreamSupport
        .stream(iterable.spliterator(), false)
        .collect(Collectors.toList());
  }

  @Override
  public Object getChild(Object parent, int index) {
    JsonProperty property = (JsonProperty) parent;
    JsonNode node = property.value();
    if (node instanceof ObjectNode) {
      List<String> fields = getFieldNames(node);
      String fieldName = fields.get(index);
      JsonNode value = node.get(fieldName);
      return new JsonProperty(fieldName, value);
    } else if (node instanceof ArrayNode) {
      return new JsonProperty("" + index, node.get(index));
    } else {
      // Others do not have children!!!
      return null;
    }
  }

  @Override
  public int getChildCount(Object parent) {
    JsonProperty property = (JsonProperty) parent;
    JsonNode node = property.value();

    if (node instanceof ObjectNode) {
      List<String> fields = getFieldNames(node);
      return fields.size();
    } else if (node instanceof ArrayNode arrayNode) {
      return arrayNode.size();
    } else {
      return 0;
    }
  }

  @Override
  public boolean isLeaf(Object node) {
    return getChildCount(node) == 0;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    System.out.println("value changed for " + path + " value: " + newValue);
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    JsonProperty property = (JsonProperty) parent;
    JsonNode node = property.value();
    JsonProperty childProperty = (JsonProperty) child;
    JsonNode childNode = childProperty.value();

    if (node instanceof ObjectNode) {
      List<String> fields = getFieldNames(node);
      for (int i = 0; i < fields.size(); i++) {
        String fieldName = fields.get(i);
        JsonNode value = node.get(fieldName);
        if (value.equals(childNode)) {
          return i;
        }
      }
    } else if (node instanceof ArrayNode arrayNode) {
      for (int i = 0; i < arrayNode.size(); i++) {
        JsonNode c = node.get(i);
        if (c.equals(childNode)) {
          return i;
        }
      }
    }

    return -1;
  }

  @Override
  public void resultUpdated(YqlResult result) {

    result.getTrace()
        .ifPresentOrElse(traceNode -> {
          this.traceRoot = new JsonProperty("trace", traceNode);
        }, () -> {
          this.traceRoot = new JsonProperty("empty", mapper.createObjectNode());
        });
    // Notify that the root has changed
    // TreePath rootPath = new TreePath(treeRoot);
    super.treeStructureChanged(null, null, null);
  }
}
