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
    private Map<String, String> packageColors = new HashMap<>();

    public DependencyGraph() {
        graph = new SingleGraph("Dependencies");
        graph.setStrict(false);
        graph.setAutoCreate(true);

        // Primo set del CSS (può non bastare in alcune situazioni Swing)
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
            viewPanel.setPreferredSize(new java.awt.Dimension(2000, 2000)); // dimensione virtuale grande

            graph.setAttribute("ui.stylesheet", styleSheet());

            viewPanel.revalidate();
            viewPanel.repaint();
        }

        JScrollPane scrollPane = new JScrollPane(viewPanel);
        scrollPane.setPreferredSize(new java.awt.Dimension(800, 600)); // dimensione visibile

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
        boolean isStandard = fullName.startsWith("java.") || fullName.startsWith("javax.");

        var node = graph.addNode(fullName);
        node.setAttribute("ui.label", simpleName);
        node.setAttribute("ui.class", isStandard ? "standard" : "custom");

        final String packageName = getPackageName(fullName);
        final String nodeColor = getColorForPackage(packageName);
        node.setAttribute("ui.custom.fill-color", nodeColor);
    }

    private String getPackageName(String fullName) {
        int lastDot = fullName.lastIndexOf('.');
        return (lastDot >= 0) ? fullName.substring(0, lastDot) : "";
    }

    private String simpleName(String full) {
        int lastDot = full.lastIndexOf('.');
        return (lastDot >= 0) ? full.substring(lastDot + 1) : full;
    }

    private String getColorForPackage(String packageName) {
        if (!packageColors.containsKey(packageName)) {
            packageColors.put(packageName, generateColorForPackage(packageName));
        }
        return packageColors.get(packageName);
    }

    private String generateColorForPackage(String packageName) {
        int hashCode = packageName.hashCode();
        return String.format("#%06X", (0xFFFFFF & hashCode));
    }

    public void reset() {
        graph.clear();
        graph.setAttribute("ui.stylesheet", styleSheet()); // Reimposta CSS anche dopo clear()
    }

    private String styleSheet() {
        return "node.standard {" +
                "   fill-color: #f9f9f9;" +
                "   stroke-color: black;" +
                "}" +
                "node.custom {" +
                "   fill-color: #d0e8ff;" +
                "   stroke-color: black;" +
                "}" +
                " node {" +
                "   shape: box;" +
                "   size-mode: fit;" +
                "   padding: 18px, 12px;" +
                "   text-size: 16;" +
                "   text-color: black;" +
                "   text-style: bold;" +
                "   text-alignment: center;" +
                "   stroke-mode: plain;" +
                "   stroke-color: black;" +
                "}" +
                "edge {" +
                "   arrow-shape: arrow;" +
                "   arrow-size: 6px, 4px;" +
                "   fill-color: #666;" +
                "}";
    }
}
