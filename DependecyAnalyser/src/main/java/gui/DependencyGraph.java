package gui;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;

public class DependencyGraph {
    private Graph graph;
    private Viewer viewer;
    private ViewPanel viewPanel;

    public DependencyGraph() {
        graph = new SingleGraph("Dependencies");
        graph.setStrict(false);
        graph.setAutoCreate(true);

        graph.setAttribute("ui.stylesheet", styleSheet());

        viewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout(new LinLog(false));  // Miglior layout

        viewPanel = (ViewPanel) viewer.addDefaultView(false);
    }

    public JComponent getGraphComponent() {
        return viewPanel;
    }

    public void addDependency(String from, String to) {
        addNodeIfAbsent(from);
        addNodeIfAbsent(to);

        String edgeId = from + "->" + to;
        if (graph.getEdge(edgeId) == null) {
            graph.addEdge(edgeId, from, to, true);
        }
    }

    private void addNodeIfAbsent(String fullName) {
        if (graph.getNode(fullName) != null) return;

        String simpleName = simpleName(fullName);
        boolean isStandard = fullName.startsWith("java.") || fullName.startsWith("javax.");

        var node = graph.addNode(fullName);
        node.setAttribute("ui.label", simpleName);
        node.setAttribute("ui.class", isStandard ? "standard" : "custom");
    }


    private String simpleName(String full) {
        int lastDot = full.lastIndexOf('.');
        return (lastDot >= 0) ? full.substring(lastDot + 1) : full;
    }

    public void reset() {
        graph.clear();
    }

    private String styleSheet() {
        return """
        node.standard {
            fill-color: #f9f9f9;
            stroke-color: black;
        }

        node.custom {
            fill-color: #d0e8ff;
            stroke-color: black;
        }

        node {
            shape: box;
            size-mode: fit;
            padding: 10px, 5px;
            text-size: 18;
            text-color: black;
            text-style: bold;
            text-alignment: center;
            stroke-mode: plain;
        }

        edge {
            arrow-shape: arrow;
            arrow-size: 12px, 8px;
            fill-color: #888;
        }
    """;
    }

}
