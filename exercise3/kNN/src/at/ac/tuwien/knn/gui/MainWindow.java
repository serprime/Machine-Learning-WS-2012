package at.ac.tuwien.knn.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.SystemColor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import at.ac.tuwien.knn.data.DataSet;
import at.ac.tuwien.knn.data.DataSetParser;


public class MainWindow {

	private JFrame frame;
	private DrawingArea panel;
	private JLabel lbFilename;

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
		frame = new JFrame();
		frame.setBounds(100, 100, 1000, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JSpinner spinnerK = new JSpinner();
		spinnerK.setBounds(827, 117, 45, 20);
		frame.getContentPane().add(spinnerK);
		
		JLabel lblK = new JLabel("k:");
		lblK.setBounds(810, 120, 16, 14);
		frame.getContentPane().add(lblK);
		
		panel = new DrawingArea();
		panel.setBackground(Color.WHITE);
		panel.setBorder(new LineBorder(SystemColor.controlDkShadow));
		panel.setBounds(33, 33, 737, 699);
		frame.getContentPane().add(panel);
		
		JCheckBox chckbxUseTestSet = new JCheckBox("Use Training/Test Set");
		chckbxUseTestSet.setSelected(true);
		chckbxUseTestSet.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if(((JCheckBox)arg0.getSource()).isSelected()){
					
				}
			}
		});
		chckbxUseTestSet.setBounds(807, 160, 155, 23);
		frame.getContentPane().add(chckbxUseTestSet);
		
		JSpinner spinnerPercentage = new JSpinner();
		spinnerPercentage.setBounds(917, 195, 45, 20);
		spinnerPercentage.getModel().setValue(70);
		frame.getContentPane().add(spinnerPercentage);
		
		lbFilename = new JLabel("No dataset chosen");
		lbFilename.setBounds(810, 73, 164, 20);
		frame.getContentPane().add(lbFilename);
		
		JButton btnOpenFile = new JButton("Open file...");
		btnOpenFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				int returnVal = fileChooser.showOpenDialog(frame);
				if(returnVal == JFileChooser.APPROVE_OPTION){
					DataSet dataSet = DataSetParser.parseFile(fileChooser.getSelectedFile());
					lbFilename.setText("Dataset: " + fileChooser.getSelectedFile().getName());
					dataSet.split(70);
					panel.setDataSet(dataSet);
					panel.repaint();
				}
			}
		});
		btnOpenFile.setBounds(807, 33, 118, 23);
		frame.getContentPane().add(btnOpenFile);
		
		JLabel lblOfTestdata = new JLabel("% of Training-Data:");
		lblOfTestdata.setBounds(810, 198, 109, 14);
		frame.getContentPane().add(lblOfTestdata);
		
		JCheckBox chckbxShowTestData = new JCheckBox("Show Test Data");
		chckbxShowTestData.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				panel.setShowTestData(e.getStateChange() == e.SELECTED);
				panel.repaint();
			}
		});
		chckbxShowTestData.setBounds(810, 261, 152, 23);
		frame.getContentPane().add(chckbxShowTestData);
		
		JCheckBox chckbxShowTrainingData = new JCheckBox("Show Training Data");
		chckbxShowTrainingData.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				panel.setShowTrainingData(e.getStateChange() == e.SELECTED);
				panel.repaint();
			}
		});
		chckbxShowTrainingData.setSelected(true);
		chckbxShowTrainingData.setBounds(810, 235, 152, 23);
		frame.getContentPane().add(chckbxShowTrainingData);
	}
}
