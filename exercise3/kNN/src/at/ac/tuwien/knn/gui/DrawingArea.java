package at.ac.tuwien.knn.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.JPanel;

import at.ac.tuwien.knn.data.DataPoint;
import at.ac.tuwien.knn.data.DataSet;

/**
 * The panel in which all the data points are drawn
 * @author BK
 *
 */
public class DrawingArea extends JPanel {
	private DataSet dataSet;
	private double minX, maxX, minY, maxY;
	private boolean showTrainingData = true;
	private boolean showTestData = false;
	
	private static Color[] colors = new Color[]{new Color(230,0,0),//red
												new Color(0,0,230),//blue
												new Color(0,220,0),//green
												new Color(102,0,102),//violet
												new Color(255,153,0),//orange
												new Color(255,255,100)//beige
												};
	private HashMap<Integer,Color> classColorMap = new HashMap<Integer, Color>();
	private int pointRadius = 3;

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		if(this.dataSet == null || this.dataSet.getAllDataPoints() == null) return;
		this.initColorMap(this.dataSet.getTrainingSet());
		this.setMinMaxCoordinates(this.dataSet.getAllDataPoints());
		if(showTrainingData){
			this.paintDataPoints((Graphics2D)graphics, this.dataSet.getTrainingSet());
		}
		if(showTestData){
			this.paintTestSet((Graphics2D)graphics, this.dataSet.getTestSet());
		}
	}
	
	/**
	 * Paints the test data points into the drawing area. For every test-data point we draw a grey circle around it. At this 
	 * time the circle has just an arbitrary hardcoded radius but in the future the circle should be exactly as big such that
	 * it contains for every test data point the k nearest neighbours of this data point.
	 * @param g The graphics element in which we draw.
	 * @param dataPoints The datapoints which should be drawn.
	 */
	protected void paintTestSet(Graphics2D g, Collection<DataPoint> dataPoints){
		Point currentPoint;
		g.setColor(new Color(230,230,230));
		for(DataPoint dataPoint : dataPoints){
			currentPoint = scale(dataPoint);
			int radius = 20;
			g.fillOval(currentPoint.x-radius+this.pointRadius, currentPoint.y-radius+this.pointRadius, radius*2, radius*2);
		}
		this.paintDataPoints(g, dataPoints);
	}
	
	/**
	 * Paints  data points into the drawing area.
	 * @param g The graphics element in which we draw.
	 * @param dataPoints The datapoints which should be drawn
	 */
	protected void paintDataPoints(Graphics2D g, Collection<DataPoint> dataPoints){
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		int diameter = 2*this.pointRadius;
		if (dataPoints != null) {
			Point currentPoint;
			for (DataPoint dataPoint : dataPoints) {
				g.setColor(this.classColorMap.get(dataPoint.getClassification()));
				currentPoint = this.scale(dataPoint);
				g.fillOval(currentPoint.x, currentPoint.y, diameter, diameter);
			}
		}
	}

	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	/**
	 * This method is used for scaling the datapoints into the drawing area.
	 * @param dataPoints
	 */
	private void setMinMaxCoordinates(Collection<DataPoint> dataPoints) {
		minX = maxX = dataPoints.iterator().next().getX();
		minY = maxY = dataPoints.iterator().next().getY();
		
		for (DataPoint dataPoint : dataPoints) {
			if (dataPoint.getX() < minX) {
				minX = dataPoint.getX();
			}
			if (dataPoint.getX() > maxX) {
				maxX = dataPoint.getX();
			}
			if (dataPoint.getY() < minY) {
				minY = dataPoint.getY();
			}
			if (dataPoint.getY() > maxY) {
				maxY = dataPoint.getY();
			}
		}
	}
	
	/**
	 * Initializes the color map. The color map stores for every data point the color with which it should be drawn.
	 * The color of a datapoint depends on its classification value.
	 * @param dataPoints
	 */
	private void initColorMap(Collection<DataPoint> dataPoints){
		int i=0;
		for (DataPoint dataPoint : dataPoints) {
			if(!this.classColorMap.containsKey(dataPoint.getClassification())){
				if(i<colors.length){
					this.classColorMap.put(dataPoint.getClassification(), colors[i]);
					i++;
				}else{
					this.classColorMap.put(dataPoint.getClassification(), new Color(new Random().nextInt()));
				}
			}
		}
	}

	/**
	 * scales a data point such that it can be drawn into the drawing area.
	 * @param dataPoint
	 * @return
	 */
	public Point scale(DataPoint dataPoint){
		Point scaledPoint = new Point();
		int border = 1;
		double scaleX = (this.getSize().getWidth()-2*this.pointRadius-2*border)/(this.maxX - this.minX);
		double scaleY = (this.getSize().getHeight()-2*this.pointRadius-2*border)/(this.maxY - this.minY);
		scaledPoint.x = this.pointRadius + border + (int)((dataPoint.getX()-this.minX) * scaleX - this.pointRadius);
		scaledPoint.y = this.pointRadius + border + (int)((dataPoint.getY()-this.minY) * scaleY - this.pointRadius);
		return scaledPoint;
	}

	/**
	 * Indicates wether the training data points should be drawn.
	 * @return true if the training data points should be drawn.
	 */
	public boolean isShowTrainingData() {
		return showTrainingData;
	}

	public void setShowTrainingData(boolean isShowTrainingData) {
		this.showTrainingData = isShowTrainingData;
	}

	public boolean isShowTestData() {
		return showTestData;
	}

	public void setShowTestData(boolean isShowTestData) {
		this.showTestData = isShowTestData;
	}
	
	
}
