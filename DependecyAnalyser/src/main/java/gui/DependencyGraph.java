package gui;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class DependencyGraph {
    private Graph graph;
    private Viewer viewer;
    private ViewPanel viewPanel;

    public DependencyGraph() {
        graph = new SingleGraph("Dependencies");
        graph.setStrict(false);
        graph.setAutoCreate(true);

        graph.setAttribute("ui.stylesheet", styleSheet());
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");

        var layout = new SpringBox(false);
        layout.setQuality(1);
        layout.setForce(1.0);

        viewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout(layout);
    }

    public JComponent getGraphComponent() {
        if (viewPanel == null) {
            viewPanel = (ViewPanel) viewer.addDefaultView(false);
            viewPanel.setPreferredSize(new java.awt.Dimension(2000, 2000));

            graph.setAttribute("ui.stylesheet", styleSheet());
            viewPanel.revalidate();
            viewPanel.repaint();
        }

        JScrollPane scrollPane = new JScrollPane(viewPanel);
        scrollPane.setPreferredSize(new java.awt.Dimension(800, 600));
        return scrollPane;
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

        var node = graph.addNode(fullName);
        node.setAttribute("ui.label", simpleName);
    }

    private String simpleName(String full) {
        int lastDot = full.lastIndexOf('.');
        return (lastDot >= 0) ? full.substring(lastDot + 1) : full;
    }

    public void reset() {
        graph.clear();
        graph.setAttribute("ui.stylesheet", styleSheet());
    }

    private String styleSheet() {
        return " node {" +
                "   shape: box;" +
                "   size-mode: fit;" +
                "   padding: 18px, 12px;" +
                "   text-size: 16;" +
                "   text-color: black;" +
                "   text-style: bold;" +
                "   text-alignment: center;" +
                "   stroke-mode: plain;" +
                "   stroke-color: black;" +
                "   fill-color: #d0e8ff;" +
                "}" +
                "edge {" +
                "   arrow-shape: arrow;" +
                "   arrow-size: 6px, 4px;" +
                "   fill-color: #666;" +
                "}";
    }
}
