package at.ac.tuwien.knn.data;
import java.util.Collection;
import java.util.LinkedList;

/**
 * The dataset stores the data points. It contains two separate collections which represent the training and the test data.
 * @author BK
 *
 */
public class DataSet {
	Collection<DataPoint> dataPoints;
	Collection<DataPoint> trainingSet = new LinkedList<DataPoint>();
	Collection<DataPoint> testSet = new LinkedList<DataPoint>();
	
	int trainingSetPercentage = 80;
	
	public DataSet(){}
	
	public DataSet(Collection<DataPoint> dataPoints){
		this.dataPoints = dataPoints;
	}
	
	public Collection<DataPoint> getTrainingSet() {
		return trainingSet;
	}

	public void setTrainingSet(Collection<DataPoint> trainingSet) {
		this.trainingSet = trainingSet;
	}

	public Collection<DataPoint> getTestSet() {
		return testSet;
	}

	public void setTestSet(Collection<DataPoint> testSet) {
		this.testSet = testSet;
	}
	
	public Collection<DataPoint> getAllDataPoints() {
		return this.dataPoints;
	}

	public void setAllDataPoints(Collection<DataPoint> datapoints) {
		this.dataPoints = datapoints;
	}

	/**
	 * Sets the percentage of data which should be used as training set.
	 * @param percentage
	 */
	public void setTrainingSetPercentage(int percentage){
		if(percentage > 100){
			percentage = 100;
		}else if(percentage < 0){
			percentage = 0;
		}
		this.trainingSetPercentage = percentage;
		this.split(percentage);
	}
	
	/**
	 * Splits the data into training and test data.
	 * @param percentage
	 */
	public void split(int percentage){
		double step = 100/(double)percentage;
		double i=1;
		for(DataPoint dataPoint : this.dataPoints){
			if(i >= step){
				i -= step;
				this.trainingSet.add(dataPoint);
			}else{
				this.testSet.add(dataPoint);
			}
			i++;
		}
		int realPercentage = (int)(100 * this.trainingSet.size() / (double)this.dataPoints.size());
		System.out.println("Percentage: " + realPercentage);
	}
}
