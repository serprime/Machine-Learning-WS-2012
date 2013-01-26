package at.ac.tuwien.knn.weka;

import at.ac.tuwien.knn.data.DataSets;
import weka.classifiers.lazy.IBk;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.neighboursearch.LinearNNSearch;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemovePercentage;

import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Random;


/**
 * This is a stateless api class that is an interface to the weka library.
 */
public class WekaApi {

    /**
     * private ctor to disable multiple instances
     */
    private WekaApi() {
    }

    /**
     * static var that holds the one and only instance of this singleton
     */
    private static WekaApi instance = null;

    public static WekaApi getInstance() {
        if (instance == null) {
            instance = new WekaApi();
        }
        return instance;
    }

    /**
     * Give me an instance, i predict its class.
     *
     * @param classifier
     * @param instance
     * @throws Exception
     */
    public void classifyInstance(IBk classifier, Instance instance) throws Exception {
        double[] probabilities = classifier.distributionForInstance(instance);
        for (double probability : probabilities) {
            System.out.println(probability);
        }
    }

    /**
     * takes Instances of trainings data and builds a knn classifier.
     *
     * @param instances training data
     * @param k         main parameter for kNN
     * @throws Exception kaboom
     */
    public IBk buildClassifierFromTrainingInstances(Instances instances, int k) throws Exception {
        IBk knn = new IBk(k);

        LinearNNSearch nearestNeighbourSearchAlgorithm = new LinearNNSearch();
        knn.setNearestNeighbourSearchAlgorithm(nearestNeighbourSearchAlgorithm);

        knn.buildClassifier(instances);
        return knn;
    }

    /**
     * Loads arff data set using the parser of weka.
     *
     * @param file file to read data from
     * @return Instances
     * @throws Exception if something goes wrong
     */
    public Instances loadData(File file) throws Exception {
        DataSource dataSource = new DataSource(file.getAbsolutePath());
        Instances instances = dataSource.getDataSet();
        instances.randomize(new Random());
        instances.setClassIndex(instances.numAttributes() - 1);
        System.out.println("loaded data. class attribute: " + instances.classAttribute());
        return instances;
    }

    /**
     * splits the instances in two sets: training and test.
     * The percentage tells the size of the training set. 70% -> 70% training set, 30% test set.
     *
     * @param instances  data
     * @param percentage number from 0-1.
     * @return
     */
    public DataSets splitDataSet(Instances instances, int percentage) throws Exception {
        if (percentage > 99 || percentage < 1) {
            throw new Exception("what the?? percentage ought to be between 1 and 99 inclusive.");
        }


        // assign data sets
        DataSets dataSets = new DataSets();
        dataSets.setTrainingInstances(percentageSplit(instances, percentage, true));
        dataSets.setTestInstances(percentageSplit(instances, percentage, false));

        System.out.println(instances.numInstances() + ", " + dataSets.getTrainingInstances().numInstances() + ", " + dataSets.getTestInstances().numInstances());

        // and back to you
        return dataSets;
    }

    /**
     * Gets a percentage of instances. If inverse is false, ...
     *
     * @param instances
     * @param percentage number between 1 and 99
     * @return
     * @throws Exception
     */
    private Instances percentageSplit(Instances instances, int percentage, boolean inverse) throws Exception {
        RemovePercentage percentageFilter = new RemovePercentage();
        percentageFilter.setInputFormat(instances);
        percentageFilter.setPercentage(percentage);
        // get other part
        percentageFilter.setInvertSelection(inverse);
        return Filter.useFilter(instances, percentageFilter);
    }

    public static Collection<Object> getAttributeValues(DataSets dataSets) {
        Collection<Object> attributeValues = new HashSet<Object>();
        Enumeration<Object> testSetAttributes = dataSets.getTestInstances().classAttribute().enumerateValues();
        Enumeration<Object> trainingSetAttributes = dataSets.getTrainingInstances().classAttribute().enumerateValues();
        while (testSetAttributes.hasMoreElements()) {
            attributeValues.add(testSetAttributes.nextElement());
        }
        while (trainingSetAttributes.hasMoreElements()) {
            attributeValues.add(trainingSetAttributes.nextElement());
        }
        return attributeValues;
    }
}
