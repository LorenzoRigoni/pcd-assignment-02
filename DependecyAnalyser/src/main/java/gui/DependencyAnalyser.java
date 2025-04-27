import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class DependencyAnalyser extends JFrame {
    private JButton startBtn;
    private JButton selectFolderBtn;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private JPanel centralPanel;
    private JTextField pathField;
    private JLabel classesAnalyzedLabel;
    private JLabel dependenciesFoundLabel;

    public DependencyAnalyser(){

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

        this.centralPanel = new JPanel();
        this.centralPanel.setBorder(BorderFactory.createEmptyBorder());
        this.centralPanel.setName("Dependency Graph");
        this.centralPanel.setBackground(Color.WHITE);

        this.bottomPanel = new JPanel(new GridLayout(1,2));
        this.bottomPanel.setPreferredSize(new Dimension(800, 30));
        this.bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.classesAnalyzedLabel = new JLabel("Classes/Interfaces analyzed: ");
        this.dependenciesFoundLabel = new JLabel("Dependencies found: ");
        this.bottomPanel.add(classesAnalyzedLabel);
        this.bottomPanel.add(dependenciesFoundLabel);



        // Modifico i colori dei componenti
        /*topPanel.setBackground(Color.WHITE);
        bottomPanel.setBackground(new Color(50, 50, 50));
        selectFolderBtn.setBackground(new Color(70, 70, 70));
        selectFolderBtn.setForeground(Color.WHITE);
        startBtn.setBackground(new Color(70, 70, 70));
        startBtn.setForeground(Color.WHITE);
        classesAnalyzedLabel.setForeground(Color.WHITE);
        dependenciesFoundLabel.setForeground(Color.WHITE);*/

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(centralPanel), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void startAnalysis() {
        String folderPath = pathField.getText();
        if(folderPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select a folder before starting the analysis.");
            return;
        }
        //TODO
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(DependencyAnalyser::new);
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = chooser.getSelectedFile();
            pathField.setText(selectedFolder.getAbsolutePath());
        }
    }
}