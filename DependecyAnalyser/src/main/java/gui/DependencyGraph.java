package gui;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
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

        // Primo set del CSS (può non bastare in alcune situazioni Swing)
        graph.setAttribute("ui.stylesheet", styleSheet());
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");

        var layout = new SpringBox(false);
        layout.setQuality(1);
        layout.setForce(0.5);

        viewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
        viewer.enableAutoLayout(layout);
    }

    public JComponent getGraphComponent() {
        if (viewPanel == null) {
            viewPanel = (ViewPanel) viewer.addDefaultView(false);
            viewPanel.setPreferredSize(new java.awt.Dimension(600, 400));

            // Riapplica il CSS per assicurarsi che venga visualizzato correttamente
            graph.setAttribute("ui.stylesheet", styleSheet());

            // Forza ridisegno
            viewPanel.revalidate();
            viewPanel.repaint();
        }
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
        graph.setAttribute("ui.stylesheet", styleSheet()); // Reimposta CSS anche dopo clear()
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
                         shape: rounded-box;            
                         size-mode: fit;             
                         padding: 10px, 5px;         
                         text-size: 16;              
                         stroke-mode: plain;
                         stroke-color: black;
                         fill-color: white;
            }
                

            edge {
                arrow-shape: arrow;
                arrow-size: 4px, 3px;
                fill-color: #444;
            }
        """;
    }
}
