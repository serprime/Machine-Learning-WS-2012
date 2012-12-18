package at.ac.tuwien.machine_learning;


import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.Instance;

public class SillyClassifier extends Classifier {

	
	private static final long serialVersionUID = 4148318713741359627L;
	
	private double m_ClassValue;
	
	public String globalInfo() {
	    return "Class for building and using a very silly classifier. Simply predicts the value of a class attribute.";	    
	  }
	
	public Capabilities getCapabilities() {
	    Capabilities result = super.getCapabilities();
	    result.disableAll();

	    // attributes
	    result.enable(Capability.NOMINAL_ATTRIBUTES);
	    result.enable(Capability.NUMERIC_ATTRIBUTES);
	    result.enable(Capability.DATE_ATTRIBUTES);
	    result.enable(Capability.MISSING_VALUES);
	 
	    // class
	    result.enable(Capability.NOMINAL_CLASS);
	    result.enable(Capability.MISSING_CLASS_VALUES);

	    // instances
	    result.setMinimumNumberInstances(0);
	    
	    return result;
	  }

	@Override
	public void buildClassifier(Instances instances) throws Exception {
		// can classifier handle the data?
	    getCapabilities().testWithFail(instances);
	    
	    // remove instances with missing class
	    instances = new Instances(instances);
	    instances.deleteWithMissingClass();
	    
	    Instance instance = instances.firstInstance();
	    m_ClassValue = instance.classValue();

	}
	
	public double classifyInstance(Instance instance) {

		return m_ClassValue;
	}
}
