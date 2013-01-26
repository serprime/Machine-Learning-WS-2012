package at.ac.tuwien.knn.data;

import weka.core.Instances;

public class DataSets {

    private Instances trainingInstances;
    private Instances testInstances;

    public boolean isEmpty(){
        return trainingInstances.isEmpty();
    }

    public Instances getTrainingInstances() {
        return trainingInstances;
    }

    public void setTrainingInstances(Instances trainingInstances) {
        this.trainingInstances = trainingInstances;
    }

    public Instances getTestInstances() {
        return testInstances;
    }

    public void setTestInstances(Instances testInstances) {
        this.testInstances = testInstances;
    }

    /**
     * determine the number of classes that appear in in training and test set
     * @return
     */
    public int numClasses() {
        if (testInstances.numClasses() > trainingInstances.numClasses()) {
            return testInstances.numClasses();
        }
        return trainingInstances.numClasses();
    }
}
