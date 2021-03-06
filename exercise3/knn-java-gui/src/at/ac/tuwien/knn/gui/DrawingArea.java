package at.ac.tuwien.knn.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;

import weka.classifiers.lazy.IBk;
import weka.core.Instance;
import weka.core.Instances;
import at.ac.tuwien.knn.data.DataSets;
import at.ac.tuwien.knn.weka.WekaApi;

/**
 * The panel in which all the data points are drawn
 *
 * @author BK
 */
public class DrawingArea extends JPanel {

	//MainWindow to render things into it
	private MainWindow window;
	
    // data, holds training and test data sets
    private DataSets dataSets;
    private Instances originalInstances;
    private Collection<Component> legendElements = new LinkedList<Component>();

    // classifier
    private IBk classifier;

    // drawing flags
    private boolean showTrainingData = true;
    private boolean showTestData = false;
    private boolean showConnections = true;

    // coordinate and drawing stuff
    private double minX, maxX, minY, maxY;

    private int pointRadius = 5;
    private int highlightRadius = 15;

    private Color highlightColor = new Color(230, 230, 230);
    private Color neighborConnectorColor = new Color(255, 163, 0);

    private static Color[] colors = new Color[]{new Color(230, 0, 0),//red
            new Color(0, 0, 230),//blue
            new Color(0, 220, 0),//green
            new Color(102, 0, 102),//violet
            new Color(255, 153, 0),//orange
            new Color(255, 255, 100)//beige
    };
    private Map<Integer, Color> classColorMap = null;

    public DrawingArea(MainWindow window){
    	this.window = window;
    }

    //
    // CONTROL METHODS FOR USERS OF THIS COMPONENT (MAIN WINDOW)
    //

    private void resetClassifier(int k) throws Exception {
        classifier = WekaApi.getInstance().buildClassifierFromTrainingInstances(dataSets.getTrainingInstances(), k);
    }

    /**
     * Update dataset file. Reload data, get a new classifier, re-everything.
     *
     * @param dataFile
     * @throws Exception
     */
    public void updateDataFile(File dataFile, Integer k) throws Exception {
        originalInstances = WekaApi.getInstance().loadData(dataFile);
        dataSets = WekaApi.getInstance().splitDataSet(originalInstances, 70);
        Collection<Object> attributeValues = WekaApi.getAttributeValues(dataSets);
        resetClassifier(k);
        initColorMap(attributeValues);
        calibrateDataSetsCoordinates(dataSets);
        createLegend(attributeValues);
        window.getLblXAxis().setText("x-Axis: " + getNameOfAttribute(0));
        window.getLblYAxis().setText("y-Axis: " + getNameOfAttribute(1));
        repaint();
    }
    
    private void createLegend(Collection<Object> attributeValues){
    	int x = window.getLblClasses().getX();
    	int y = window.getLblClasses().getY() + 25;
    	int i=0;
    	int vSpace = 25;
    	int hSpace = 10;
    	for(Component c : this.legendElements){
    		window.getFrame().remove(c);
    	}
    	this.legendElements.clear();
    	for(Object o : attributeValues){
    		JLabel lblClass = new JLabel(o.toString());
    		lblClass.setBounds(x + this.pointRadius*2 + hSpace, y + i*vSpace-2, 140, 14);
    		window.getFrame().getContentPane().add(lblClass);
    		this.legendElements.add(lblClass);
    		ColorPoint colorPoint = new ColorPoint(this.classColorMap.get(getIndexOfAttribute(o)));
    		colorPoint.setLocation(x, y + i*vSpace);
    		colorPoint.setRadius(this.pointRadius);
    		window.getFrame().getContentPane().add(colorPoint);
    		this.legendElements.add(colorPoint);
    		i++;
    	}
    }

    /**
     * Update the value for k of kNN.
     * This builds a new classifier and restarts the classification.
     *
     * @param k
     */
    public void updateK(Integer k) throws Exception {
        // create new classifier.
        resetClassifier(k);
        repaint();
    }

    /**
     * Update the percentage value where to split the data set.
     *
     * @param percentage
     * @throws Exception
     */
    public void updatePercentage(Integer percentage, Integer k) throws Exception {
        // make new split
        dataSets = WekaApi.getInstance().splitDataSet(originalInstances, percentage);
        resetClassifier(k);
        repaint();
    }

    /**
     * Update if the trainingdata should be rendered.
     *
     * @param isShowTrainingData
     */
    public void updateShowTrainingData(boolean isShowTrainingData) {
        this.showTrainingData = isShowTrainingData;
        repaint();
    }
    
    /**
     * Update if the connections to the k nearest neighbours should be rendered.
     *
     * @param isShowConnections
     */
    public void updateShowConnections(boolean isShowConnections){
    	this.showConnections = isShowConnections;
    	repaint();
    }

    /**
     * Update if the test data should be rendered
     *
     * @param isShowTestData
     */
    public void updateShowTestData(boolean isShowTestData) {
        this.showTestData = isShowTestData;
        repaint();
    }


    //
    // DRAW ROUTINES
    //


    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        try {

            if (dataSets == null || dataSets.isEmpty()) {
                return;
            }

            // order of how to paint all elements:
            // test:
            // - highlights of test data
            // - knn connectors
            // train:
            // - training data points
            // test:
            // - test data points halfs

            if (showTestData) {
                paintTestSetHighlights((Graphics2D) graphics, dataSets.getTestInstances());
            }
            if(showConnections){
                paintTestSetKnnConnectors((Graphics2D) graphics, dataSets.getTestInstances());
            }
            if (showTrainingData) {
                paintDataPoints((Graphics2D) graphics, dataSets.getTrainingInstances());
            }
            if (showTestData) {
                paintTestSetDataPoints((Graphics2D) graphics, dataSets.getTestInstances());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Paints the test data points into the drawing area. For every test-data point we draw a grey circle around it. At this
     * time the circle has just an arbitrary hardcoded highlightRadius but in the future the circle should be exactly as big such that
     * it contains for every test data point the k nearest neighbours of this data point.
     * <p/>
     * For an initial step we paint the circle as highlighting and connect the current instance to its neighbors with lines.
     *
     * @param g
     * @param instances
     */
    private void paintTestSetDataPoints(Graphics2D g, Instances instances) throws Exception {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        for (Instance instance : instances) {
            Point currentPoint = scale(instance);
            g.setColor(classColorMap.get((int) instance.classValue()));
            g.fill(new Arc2D.Double(currentPoint.x, currentPoint.y, 2 * pointRadius, 2 * pointRadius, 90, 180, Arc2D.OPEN));

            // draw half circle for predicted class
            Integer classValue = (int) classifier.classifyInstance(instance);
            g.setColor(classColorMap.get(classValue));
            g.fill(new Arc2D.Double(currentPoint.x, currentPoint.y, 2 * pointRadius, 2 * pointRadius, 90, -180, Arc2D.OPEN));
        }

    }

    private void paintTestSetHighlights(Graphics2D g, Instances instances) throws Exception {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        for (Instance instance : instances) {
            g.setColor(highlightColor);
            Point currentPoint = scale(instance);
            g.fillOval(currentPoint.x - highlightRadius + pointRadius, currentPoint.y - highlightRadius + pointRadius, highlightRadius * 2, highlightRadius * 2);
        }
    }

    /**
     * Routine to draw some special stuff around the new instance that get classified.
     *
     * @param instances
     * @throws Exception
     */
    private void paintTestSetKnnConnectors(Graphics2D g, Instances instances) throws Exception {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        for (Instance instance : instances) {
            Instances neighbors = classifier.getNearestNeighbourSearchAlgorithm().kNearestNeighbours(instance, classifier.getKNN());
            Point currentPoint = scale(instance);
            // print connectors to all neighbors
            g.setColor(neighborConnectorColor);
            for (Instance neighbor : neighbors) {
                Point neighborPoint = scale(neighbor);
                g.drawLine(currentPoint.x + pointRadius, currentPoint.y + pointRadius, neighborPoint.x + pointRadius, neighborPoint.y + pointRadius);
            }
        }
    }

    /**
     * Paints data points into the drawing area.
     *
     * @param g
     * @param instances
     */
    protected void paintDataPoints(Graphics2D g, Instances instances) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int diameter = 2 * pointRadius;
        for (Instance instance : instances) {
            g.setColor(classColorMap.get((int)instance.classValue()));
            Point currentPoint = scale(instance);
            g.fillOval(currentPoint.x, currentPoint.y, diameter, diameter);
        }
    }

    /**
     * scales a data point such that it can be drawn into the drawing area.
     *
     * @param instance
     * @return
     */
    public Point scale(Instance instance) {
        Point scaledPoint = new Point();
        int border = 1;
        double scaleX = (getSize().getWidth() - 2 * pointRadius - 2 * border) / (maxX - minX);
        double scaleY = (getSize().getHeight() - 2 * pointRadius - 2 * border) / (maxY - minY);
        scaledPoint.x = pointRadius + border + (int) ((instance.value(0) - minX) * scaleX - pointRadius);
        scaledPoint.y = pointRadius + border + (int) ((instance.value(1) - minY) * scaleY - pointRadius);
        return scaledPoint;
    }


    //
    // INITIALIZATION ROUTINES
    //


    /**
     * This method is used for scaling the datapoints into the drawing area.
     *
     * @param dataSets wrapper for training and test data sets
     */
    private void calibrateDataSetsCoordinates(DataSets dataSets) {
        minX = minY = Double.MAX_VALUE;
        maxX = maxY = Double.MIN_VALUE;
        // calibrate for training and test set
        calibrateInstancesCoordinates(dataSets.getTrainingInstances());
        calibrateInstancesCoordinates(dataSets.getTestInstances());
        // normalize -- add 1/20 of the plot span as border
        minX -= (maxX - minX) / 20;
        maxX += (maxX - minX) / 20;
        minY -= (maxY - minY) / 20;
        maxY += (maxY - minY) / 20;
    }

    /**
     * find the min and max coordinates for instances
     * This should only be called from the calibrateDataSetCoordinates Method.
     *
     * @param instances
     */
    private void calibrateInstancesCoordinates(Instances instances) {
        double[] xValues = instances.attributeToDoubleArray(0);
        double[] yValues = instances.attributeToDoubleArray(1);
        for (double x : xValues) {
            if (x > maxX) {
                maxX = x;
            }
            if (x < minX) {
                minX = x;
            }
        }
        for (double y : yValues) {
            if (y > maxY) {
                maxY = y;
            }
            if (y < minY) {
                minY = y;
            }
        }
    }

    /**
     * Initializes the color map. The color map stores a color for each class value
     *
     * @param dataSets training and test sets
     */
    private void initColorMap(Collection<Object> attributeValues) {
        System.out.println("init color map");
        classColorMap = new HashMap<Integer, Color>(dataSets.numClasses());
        int i=0;
        for(Object attributeValue : attributeValues){
        	System.out.println(attributeValue + ", index: " + dataSets.getTestInstances().classAttribute().indexOfValue(attributeValue.toString()));
        	if (i < colors.length) {
                classColorMap.put(getIndexOfAttribute(attributeValue), colors[i]);
            } else {
                classColorMap.put(getIndexOfAttribute(attributeValue), new Color(new Random().nextInt()));
            }
        	i++;
        }
    }
    
    private int getIndexOfAttribute(Object attributeValue){
    	return dataSets.getTestInstances().classAttribute().indexOfValue(attributeValue.toString());
    }

    private String getNameOfAttribute(int index){
    	return dataSets.getTestInstances().attribute(index).name();
    }
}
