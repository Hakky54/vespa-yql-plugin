package com.pehrs.vespa.yql.plugin.results;

import com.intellij.openapi.util.NlsContexts.ColumnName;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlResultListener;
import com.pehrs.vespa.yql.plugin.trace.TraceUtils;
import com.pehrs.vespa.yql.plugin.trace.YqlTraceMessage;
import com.pehrs.vespa.yql.plugin.trace.YqlTraceNodeBase;
import com.pehrs.vespa.yql.plugin.trace.YqlTraceThread;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;

public class YqlResultsTraceTreeTableModel implements TreeTableModel, YqlResultListener {

  private static String[] columns = {
      "Node",
      "Timestamp",
      "Doc Type",
      "Dist Key",
      "Details"
  };

  private final static int NODE_COLUMN = 0;
  private final static int TS_COLUMN = 1;
  private final static int DOC_TYPE_COLUMN = 2;
  private final static int DIST_KEY_COLUMN = 3;
  private final static int DETAILS_COLUMN = 4;

  private final static Class[] columnClasses = {
      TreeTableModel.class,
      String.class,
      String.class,
      String.class,
      String.class
  };
  private List<YqlTraceMessage> tracesMessages = List.of();

  private final String ROOT = "/search/";

  public YqlResultsTraceTreeTableModel() {
    // List<YqlTraceMessage> tracesMessages = TraceUtils.getYqlTraceMessages(result);
  }

  @Override
  public int getColumnCount() {
    return columns.length;
  }

  @Override
  public @ColumnName String getColumnName(int column) {
    return columns[column];
  }

  @Override
  public Class getColumnClass(int column) {
    return columnClasses[column];
  }

  @Override
  public Object getValueAt(Object node, int column) {
    if (node.equals(ROOT)) {
      switch (column) {
        case NODE_COLUMN:
          return ROOT;
        case DETAILS_COLUMN: {
          double max = this.tracesMessages.stream()
              .mapToDouble(msg -> msg.getDurationNs())
              .max()
              .orElseGet(() -> 0d);
          if(max > 0d) {
            return String.format("total duration: %.3fms", (max / 1_000_000d));
          }
          return "";
        }
        default:
          return "";
      }
    }
    if (node instanceof YqlTraceNodeBase traceNode) {
      switch (column) {
        case NODE_COLUMN:
          return traceNode.getClass().getSimpleName();
        case TS_COLUMN:
          if(traceNode instanceof YqlTraceMessage msg) {
            return msg.getStartTime();
          }
          return String.format("%.3fms", traceNode.getRelativeEndTsMs());
        case DIST_KEY_COLUMN:
          return traceNode.distributionKey();
        case DOC_TYPE_COLUMN:_COLUMN:
          return traceNode.documentType();
        default:
          if(traceNode instanceof YqlTraceMessage msg) {
            return String.format("duration: %.3fms", msg.getDurationNs() / 1_000_000d);
          } else if(traceNode instanceof YqlTraceThread) {
            return "";
          }
          return traceNode.getName();
      }
    }
    return null;
  }

  @Override
  public boolean isCellEditable(Object node, int column) {
    return false;
  }

  @Override
  public void setValueAt(Object aValue, Object node, int column) {
    // Ignore for now
  }

  @Override
  public void setTree(JTree tree) {
    // Ignode..
  }

  @Override
  public Object getRoot() {
    return ROOT;
  }

  @Override
  public Object getChild(Object parent, int index) {
    if (parent.equals(ROOT)) {
      return tracesMessages.get(index);
    }
    if (parent instanceof YqlTraceNodeBase) {
      YqlTraceNodeBase traceNode = (YqlTraceNodeBase) parent;
      return traceNode.getChildren().get(index);
    }
    return null;
  }

  @Override
  public int getChildCount(Object parent) {
    if (parent.equals(ROOT)) {
      return tracesMessages.size();
    }
    if (parent instanceof YqlTraceNodeBase) {
      YqlTraceNodeBase traceNode = (YqlTraceNodeBase) parent;
      return traceNode.getChildren().size();
    }
    return 0;
  }

  @Override
  public boolean isLeaf(Object node) {
    return getChildCount(node) == 0;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    // Ignore for now
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    if (parent.equals(ROOT)) {
      return tracesMessages.indexOf(child);
    }
    if (parent instanceof YqlTraceNodeBase) {
      YqlTraceNodeBase traceNode = (YqlTraceNodeBase) parent;
      return traceNode.getChildren().indexOf(child);
    }
    return 0;
  }

  List<TreeModelListener> listeners = new ArrayList<>();

  private void notifyListeners() {
    synchronized (listeners) {
      List<TreeModelListener> toBeRemoved = listeners.stream().flatMap(listener -> {
        try {
          listener.treeStructureChanged(new TreeModelEvent(this, (TreePath) null));
          listener.treeNodesChanged(new TreeModelEvent(this, (TreePath) null));
        } catch (Exception ex) {
          ex.printStackTrace();
          return Stream.of(listener);
        }
        return Stream.empty();
      }).collect(Collectors.toList());
      // Clean list if needed
      listeners.removeAll(toBeRemoved);
    }
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    listeners.add(l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    listeners.remove(l);
  }

  @Override
  public void resultUpdated(YqlResult result) {
    this.tracesMessages = TraceUtils.getYqlTraceMessages(result);
    notifyListeners();
  }
}
