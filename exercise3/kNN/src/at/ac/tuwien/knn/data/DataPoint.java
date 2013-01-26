package at.ac.tuwien.knn.data;

public class DataPoint {
	private double x, y;
	private int classification;
	
	public DataPoint(){
		
	}
	
	public DataPoint(double x, double y, int classification){
		this.x = x;
		this.y = y;
		this.classification = classification;
	}
	
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public int getClassification() {
		return classification;
	}
	public void setClassification(int classification) {
		this.classification = classification;
	}
}
