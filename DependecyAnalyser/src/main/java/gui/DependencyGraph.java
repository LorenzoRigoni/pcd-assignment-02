package gui;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

public class DependencyGraph {
    private Graph graph;
    private Viewer viewer;

    public DependencyGraph() {

        graph = new SingleGraph("Dependencies");
        graph.setStrict(false);
        graph.setAutoCreate(true);

        graph.setAttribute("ui.stylesheet", styleSheet());

        viewer = graph.display(); // crea una finestra Swing separata TO FIX
        viewer.enableAutoLayout();
    }

    public void addDependency(String from, String to) {
        if (graph.getNode(from) == null) {
            graph.addNode(from).setAttribute("ui.label", from);
        }
        if (graph.getNode(to) == null) {
            graph.addNode(to).setAttribute("ui.label", to);
        }

        String edgeId = from + "->" + to;
        if (graph.getEdge(edgeId) == null) {
            graph.addEdge(edgeId, from, to, true);
        }
    }

    public void reset() {
        graph.clear();
    }

    private String styleSheet() {
        return """
                node {
                         size: 100px, 40px;
                         shape: box;
                         fill-color: lightblue;
                         stroke-mode: plain;
                         stroke-color: black;
                         text-size: 16;
                         text-alignment: center;
                     }
                
                     edge {
                         arrow-shape: arrow;
                         fill-color: gray;
                     }
            """;
    }
}
