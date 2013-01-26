package at.ac.tuwien.knn.classification;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import at.ac.tuwien.knn.data.DataPoint;

/**
 * Does the kNN-Classification. This was the last code I wrote and I haven't yet tested wether the algorithms work correctly.
 * @author BK
 *
 */
public class KnnClassifier {
	
	/**
	 * Returns a HashMap which stores as keys all the elements from the test set and as value the k nearest neighbours of the key-element.
	 * @param trainingData The training data set
	 * @param testData The test data set
	 * @param k The k value
	 * @return a HashMap which stores as keys all the elements from the test set and as value the k nearest neighbours of the key-element.
	 */
	public HashMap<DataPoint,Collection<DataPoint>> determineKNearestNeighbours(Collection<DataPoint> trainingData, Collection<DataPoint> testData, int k){
		HashMap<DataPoint,Collection<DataPoint>> kNearestNeighboursToElements = new HashMap<DataPoint, Collection<DataPoint>>();
		double farthestDist = Integer.MIN_VALUE;
		for(DataPoint testDataPoint : testData){
			Collection<DataPoint> kNearestNeighbours = new LinkedList<DataPoint>();
			kNearestNeighbours.add(testDataPoint);
			for(DataPoint trainingDataPoint : trainingData){
				if(kNearestNeighbours.size() < k || calculateDistance(testDataPoint, trainingDataPoint) < farthestDist){
					kNearestNeighbours.add(trainingDataPoint);
					for(DataPoint neighbour : kNearestNeighbours){
						double distance = calculateDistance(testDataPoint,neighbour);
						if(distance>farthestDist){
							farthestDist = distance;
						}
					}
				}
			}
			kNearestNeighboursToElements.put(testDataPoint, kNearestNeighbours);
		}
		return kNearestNeighboursToElements;
	}
	
	/**
	 * Calculates the distance of two elements
	 * @param a element 1
	 * @param b element 2
	 * @return the distance of two elements
	 */
	protected double calculateDistance(DataPoint a, DataPoint b){
		return Math.sqrt((a.getX() - b.getX())*(a.getX() - b.getX()) + (a.getY() - b.getY())*(a.getY() - b.getY()));
	}
}
