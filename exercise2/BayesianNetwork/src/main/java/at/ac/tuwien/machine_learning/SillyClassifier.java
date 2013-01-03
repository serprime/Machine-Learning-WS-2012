package at.ac.tuwien.machine_learning;

import java.util.Enumeration;

import weka.classifiers.AbstractClassifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.WeightedInstancesHandler;

public class SillyClassifier extends AbstractClassifier implements WeightedInstancesHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4598198166434716177L;

	private Attribute m_Class;

	private double m_ClassValue;

	public String globalInfo() {
		return "Class for building and using a very silly classifier. Permanently classifies new instances with the first class attribute of the training data.";
	}

	/**
	 * Returns default capabilities of the classifier.
	 * 
	 * @return the capabilities of this classifier
	 */
	public Capabilities getCapabilities() {
		Capabilities result = super.getCapabilities();
		result.disableAll();

		// attributes
		result.enable(Capability.NOMINAL_ATTRIBUTES);
		result.enable(Capability.NUMERIC_ATTRIBUTES);
		result.enable(Capability.DATE_ATTRIBUTES);
		result.enable(Capability.STRING_ATTRIBUTES);
		result.enable(Capability.RELATIONAL_ATTRIBUTES);
		result.enable(Capability.MISSING_VALUES);

		// class
		result.enable(Capability.NOMINAL_CLASS);
		result.enable(Capability.NUMERIC_CLASS);
		result.enable(Capability.DATE_CLASS);
		result.enable(Capability.MISSING_CLASS_VALUES);

		// instances
		result.setMinimumNumberInstances(0);

		return result;
	}

	/**
	 * Generates the classifier.
	 * 
	 * @param instances
	 *            set of instances serving as training data
	 * @throws Exception
	 *             if the classifier has not been generated successfully
	 */
	public void buildClassifier(Instances instances) throws Exception {
		// can classifier handle the data?
		getCapabilities().testWithFail(instances);

		// remove instances with missing class
		instances = new Instances(instances);
		instances.deleteWithMissingClass();

		m_Class = instances.classAttribute();
		m_ClassValue = 0;

		Enumeration enu = instances.enumerateInstances();
		if (enu.hasMoreElements()) {
			Instance instance = (Instance) enu.nextElement();
			m_ClassValue = instance.classValue();
		}
	}

	/**
	 * Classifies a given instance.
	 * 
	 * @param instance
	 *            the instance to be classified
	 * @return index of the predicted class
	 */
	public double classifyInstance(Instance instance) {

		return m_ClassValue;
	}

	/**
	 * Calculates the class membership probabilities for the given test
	 * instance.
	 * 
	 * @param instance
	 *            the instance to be classified
	 * @return predicted class probability distribution
	 * @throws Exception
	 *             if class is numeric
	 */
	public double[] distributionForInstance(Instance instance) throws Exception {
		double[] result = new double[1];
		result[0] = m_ClassValue;
		return result;
	}

	/**
	 * Returns a description of the classifier.
	 * 
	 * @return a description of the classifier as a string.
	 */
	public String toString() {

		if (m_Class == null) {
			return "SillyClassifier: No model built yet.";
		} else
			return "SillyClassifier predicts class value: " + m_ClassValue;
	}

	

	/**
	 * Main method for testing this class.
	 * 
	 * @param argv
	 *            the options
	 */
	public static void main(String[] argv) {
		runClassifier(new SillyClassifier(), argv);
	}
}
