package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DependencyAnalyzer extends JFrame {
    private JButton startBtn;
    private JButton selectFolderBtn;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private JPanel centralPanel;
    private JTextField pathField;
    private JLabel classesAnalyzedLabel;
    private JLabel dependenciesFoundLabel;
    private int classesCounter = 0;
    private int dependenciesCounter = 0;
    private DependencyGraph dependencyGraph;
    private DependencyScanner dependencyScanner;

    public DependencyAnalyzer() {


        this.dependencyGraph = new DependencyGraph();
        this.dependencyScanner = null;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        this.topPanel = new JPanel(new BorderLayout(10,10));
        this.topPanel.setPreferredSize(new Dimension(800, 50));
        this.topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.pathField = new JTextField();
        this.selectFolderBtn = new JButton("Select folder");
        this.selectFolderBtn.addActionListener(e -> chooseFolder());
        this.startBtn = new JButton("Start");
        this.startBtn.addActionListener(e -> startAnalysis());
        this.topPanel.add(selectFolderBtn, BorderLayout.WEST);
        this.topPanel.add(pathField, BorderLayout.CENTER);
        this.topPanel.add(startBtn, BorderLayout.EAST);

        this.centralPanel = new JPanel(new BorderLayout());
        this.centralPanel.setBorder(BorderFactory.createEmptyBorder());
        this.centralPanel.setName("Dependency Graph");
        this.centralPanel.setBackground(Color.WHITE);

        JComponent graphComponent = dependencyGraph.getGraphComponent();
        this.centralPanel.add(graphComponent, BorderLayout.CENTER);

        this.bottomPanel = new JPanel(new GridLayout(1,2));
        this.bottomPanel.setPreferredSize(new Dimension(800, 35));
        this.bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.classesAnalyzedLabel = new JLabel("Classes/Interfaces analyzed: ");
        this.dependenciesFoundLabel = new JLabel("Dependencies found: ");
        this.bottomPanel.add(classesAnalyzedLabel);
        this.bottomPanel.add(dependenciesFoundLabel);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(centralPanel), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void startAnalysis() {
        String folderPath = pathField.getText();
        if (folderPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a folder before starting the analysis.");
            return;
        }

        dependencyGraph.reset();
        classesCounter = 0;
        dependenciesCounter = 0;
        classesAnalyzedLabel.setText("Classes/Interfaces analyzed: 0");
        dependenciesFoundLabel.setText("Dependencies found: 0");

        // Inizializza DependencyScannerRx ora che abbiamo il path
        this.dependencyScanner= new DependencyScanner(folderPath);

        dependencyScanner.analyze(folderPath)
                .subscribe(result -> SwingUtilities.invokeLater(() -> updateGUIWithResult(result)),
                        error -> SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(this, "Error analyzing dependencies: " + error.getMessage())));
    }


    private void updateGUIWithResult(DependencyScanner.DependencyResult result) {
        classesCounter++;
        dependenciesCounter += result.dependencies.size();
        classesAnalyzedLabel.setText("Classes/Interfaces analyzed: " + classesCounter);
        dependenciesFoundLabel.setText("Dependencies found: " + dependenciesCounter);

        for (String dep : result.dependencies) {
            dependencyGraph.addDependency(result.className, dep);
        }
    }


    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui", "swing");
        SwingUtilities.invokeLater(DependencyAnalyzer::new);
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = chooser.getSelectedFile();
            pathField.setText(selectedFolder.getAbsolutePath());
        }
    }
}
