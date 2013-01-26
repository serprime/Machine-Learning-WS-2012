package at.ac.tuwien.knn.gui;

import at.ac.tuwien.knn.data.DataSets;
import at.ac.tuwien.knn.weka.WekaApi;
import weka.classifiers.lazy.IBk;
import weka.core.Instance;
import weka.core.Instances;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The panel in which all the data points are drawn
 *
 * @author BK
 */
public class DrawingArea extends JPanel {

    // data, holds training and test data sets
    private DataSets dataSets;
    private Instances originalInstances;

    // classifier
    private IBk classifier;

    // drawing flags
    private boolean showTrainingData = true;
    private boolean showTestData = false;

    // coordinate and drawing stuff
    private double minX, maxX, minY, maxY;
    private int pointRadius = 5;
    private int highlightRadius = 20;
    private Color unknownColor = new Color(26, 26, 26);
    private Color neighborConnectorColor = new Color(255, 163, 0);
    private static Color[] colors = new Color[]{new Color(230, 0, 0),//red
            new Color(0, 0, 230),//blue
            new Color(0, 220, 0),//green
            new Color(102, 0, 102),//violet
            new Color(255, 153, 0),//orange
            new Color(255, 255, 100)//beige
    };
    private Map<Integer, Color> classColorMap = null;


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
        this.dataSets = WekaApi.getInstance().splitDataSet(originalInstances, 70);
        resetClassifier(k);
        initColorMap(dataSets);
        calibrateDataSetsCoordinates(dataSets);
        repaint();
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

    public void updatePercentage(Integer percentage) throws Exception {
        // make new split
        this.dataSets = WekaApi.getInstance().splitDataSet(originalInstances, percentage);
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

            System.out.println("repaint");

            if (dataSets == null || dataSets.isEmpty()) {
                System.out.println("no data set, return");
                return;
            }

            // paint all dots of the test set + highlighting ovals + connectors to the nearest neigbors
            if (showTestData) {
                this.paintTestSet((Graphics2D) graphics, dataSets.getTestInstances());

            }

            // paint class colored dots from the training set
            if (showTrainingData) {
                this.paintDataPoints((Graphics2D) graphics, dataSets.getTrainingInstances());
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
    protected void paintTestSet(Graphics2D g, Instances instances) throws Exception {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        System.out.println("render test highlights, " + instances.numInstances());

        Point currentPoint;
        for (Instance instance : instances) {
            g.setColor(new Color(230, 230, 230));
            currentPoint = scale(instance);
            g.fillOval(currentPoint.x - highlightRadius + this.pointRadius, currentPoint.y - highlightRadius + this.pointRadius, highlightRadius * 2, highlightRadius * 2);
            paintKnnConnectors(g, instance);
        }

        System.out.println("render test instances, " + instances.numInstances());

        g.setColor(unknownColor);
        int diameter = 2 * this.pointRadius;
        for (Instance instance : instances) {
            currentPoint = this.scale(instance);
            g.fillOval(currentPoint.x, currentPoint.y, diameter, diameter);
        }
    }

    /**
     * Routine to draw some special stuff aroung an new instance that gets classified.
     *
     * @param instance
     * @throws Exception
     */
    private void paintKnnConnectors(Graphics2D g, Instance instance) throws Exception {
        Instances instances = classifier.getNearestNeighbourSearchAlgorithm().kNearestNeighbours(instance, classifier.getKNN());
        Point currentPoint = scale(instance);
        g.setColor(neighborConnectorColor);
        for (Instance neighbor : instances) {
            Point neighborPoint = scale(neighbor);
            g.drawLine(currentPoint.x + pointRadius, currentPoint.y + pointRadius, neighborPoint.x + pointRadius, neighborPoint.y + pointRadius);
        }


    }

    /**
     * Paints data points into the drawing area.
     *
     * @param g
     * @param instances
     */
    protected void paintDataPoints(Graphics2D g, Instances instances) {
        System.out.println("render instances, " + instances.numInstances());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int diameter = 2 * this.pointRadius;
        for (Instance instance : instances) {
            g.setColor(this.colors[(int) (instance.classValue())]);
            Point currentPoint = this.scale(instance);
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
        double scaleX = (this.getSize().getWidth() - 2 * this.pointRadius - 2 * border) / (this.maxX - this.minX);
        double scaleY = (this.getSize().getHeight() - 2 * this.pointRadius - 2 * border) / (this.maxY - this.minY);
        scaledPoint.x = this.pointRadius + border + (int) ((instance.value(0) - this.minX) * scaleX - this.pointRadius);
        scaledPoint.y = this.pointRadius + border + (int) ((instance.value(1) - this.minY) * scaleY - this.pointRadius);
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
        System.out.println("calibrate coordinates of data sets");
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
    private void initColorMap(DataSets dataSets) {
        System.out.println("init color map");
        classColorMap = new HashMap<Integer, Color>(dataSets.numClasses());
        for (int i = 0; i < dataSets.numClasses(); i++) {
            if (i < colors.length) {
                this.classColorMap.put(i, colors[i]);
            } else {
                this.classColorMap.put(i, new Color(new Random().nextInt()));
            }
        }
    }


}
