package at.ac.tuwien.knn.gui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class MainWindow {

    private JFrame frame;
    private DrawingArea panel;
    private JLabel lbFilename;
    private int defaultK = 3;
    private int defaultPercentage = 70;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainWindow window = new MainWindow();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //DrawingArea drawingArea = new DrawingArea();
    }

    /**
     * Create the application.
     */
    public MainWindow() {
        initialize();

    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {

        // MAIN
        //
        frame = new JFrame();
        frame.setBounds(100, 100, 1000, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        // SPINNER K
        //
        final JSpinner spinnerK = new JSpinner(new SpinnerNumberModel(defaultK, 1, 100, 1));
        spinnerK.setBounds(827, 117, 45, 20);
        spinnerK.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                try {
                    panel.updateK((Integer) spinnerK.getModel().getValue());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        frame.getContentPane().add(spinnerK);
        JLabel lblK = new JLabel("k:");
        lblK.setBounds(810, 120, 16, 14);
        frame.getContentPane().add(lblK);

        // DRAWING PANEL
        //
        panel = new DrawingArea();
        panel.setBackground(Color.WHITE);
        panel.setBorder(new LineBorder(SystemColor.controlDkShadow));
        panel.setBounds(33, 33, 737, 699);
        frame.getContentPane().add(panel);

        // FLAG: TRAINING SET
        //
        JCheckBox chckbxUseTestSet = new JCheckBox("Use Training/Test Set");
        chckbxUseTestSet.setSelected(true);
        chckbxUseTestSet.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                if (((JCheckBox) arg0.getSource()).isSelected()) {

                }
            }
        });
        chckbxUseTestSet.setBounds(807, 160, 155, 23);
        frame.getContentPane().add(chckbxUseTestSet);

        // SPINNER: PERCENTAGE SPLIT (train/test)
        //
        final JSpinner spinnerPercentage = new JSpinner();
        spinnerPercentage.setBounds(917, 195, 45, 20);
        spinnerPercentage.setModel(new SpinnerNumberModel(defaultPercentage, 5, 95, 5));
        frame.getContentPane().add(spinnerPercentage);
        spinnerPercentage.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                try {
                    panel.updatePercentage((Integer) spinnerPercentage.getModel().getValue());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        // FILE HANDLING
        //
        lbFilename = new JLabel("No dataset chosen");
        lbFilename.setBounds(810, 73, 164, 20);
        frame.getContentPane().add(lbFilename);
        JButton btnOpenFile = new JButton("Open file...");
        btnOpenFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                JFileChooser fileChooser = new JFileChooser();
                int returnVal = fileChooser.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        panel.updateDataFile(fileChooser.getSelectedFile(), (Integer) spinnerK.getModel().getValue());
                        lbFilename.setText("Dataset: " + fileChooser.getSelectedFile().getName());
                    } catch (Exception e) {
                        lbFilename.setText("Error loading data from file.");
                        e.printStackTrace();
                    }
                }
            }
        });
        btnOpenFile.setBounds(807, 33, 118, 23);
        frame.getContentPane().add(btnOpenFile);

        JLabel lblOfTestdata = new JLabel("% of Training-Data:");
        lblOfTestdata.setBounds(810, 198, 109, 14);
        frame.getContentPane().add(lblOfTestdata);

        // CHECKBOX: TEST DATA
        //
        JCheckBox chckbxShowTestData = new JCheckBox("Show Test Data");
        chckbxShowTestData.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                panel.updateShowTestData(e.getStateChange() == e.SELECTED);
            }
        });
        chckbxShowTestData.setBounds(810, 261, 152, 23);
        frame.getContentPane().add(chckbxShowTestData);

        // CHECKBOX: TRAINING DATA
        //
        JCheckBox chckbxShowTrainingData = new JCheckBox("Show Training Data");
        chckbxShowTrainingData.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                panel.updateShowTrainingData(e.getStateChange() == e.SELECTED);
            }
        });
        chckbxShowTrainingData.setSelected(true);
        chckbxShowTrainingData.setBounds(810, 235, 152, 23);
        frame.getContentPane().add(chckbxShowTrainingData);
    }
}
