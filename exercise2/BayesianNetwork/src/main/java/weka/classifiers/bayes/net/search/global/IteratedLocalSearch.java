/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 * HillClimber.java
 * Copyright (C) 2004 University of Waikato, Hamilton, New Zealand
 * 
 */

package weka.classifiers.bayes.net.search.global;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;

/**
 * <!-- globalinfo-start --> This Bayes Network learning algorithm uses a hill
 * climbing algorithm adding, deleting and reversing arcs. The search is not
 * restricted by an order on the variables (unlike K2). The difference with B
 * and B2 is that this hill climber also considers arrows part of the naive
 * Bayes structure for deletion.
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start --> Valid options are:
 * <p/>
 * 
 * <pre>
 * -P &lt;nr of parents&gt;
 *  Maximum number of parents
 * </pre>
 * 
 * <pre>
 * -R
 *  Use arc reversal operation.
 *  (default false)
 * </pre>
 * 
 * <pre>
 * -N
 *  Initial structure is empty (instead of Naive Bayes)
 * </pre>
 * 
 * <pre>
 * -mbc
 *  Applies a Markov Blanket correction to the network structure, 
 *  after a network structure is learned. This ensures that all 
 *  nodes in the network are part of the Markov blanket of the 
 *  classifier node.
 * </pre>
 * 
 * <pre>
 * -S [LOO-CV|k-Fold-CV|Cumulative-CV]
 *  Score type (LOO-CV,k-Fold-CV,Cumulative-CV)
 * </pre>
 * 
 * <pre>
 * -Q
 *  Use probabilistic or 0/1 scoring.
 *  (default probabilistic scoring)
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * @author Remco Bouckaert (rrb@xm.co.nz)
 * @version $Revision: 1.9 $
 */
public class IteratedLocalSearch extends GlobalScoreSearchAlgorithm {

	/** for serialization */
	static final long serialVersionUID = -3885042888195820149L;

	/**
	 * the Operation class contains info on operations performed on the current
	 * Bayesian network.
	 */
	class Operation implements Serializable, RevisionHandler {

		/** for serialization */
		static final long serialVersionUID = -2934970456587374967L;

		// constants indicating the type of an operation
		final static int OPERATION_ADD = 0;
		final static int OPERATION_DEL = 1;
		final static int OPERATION_REVERSE = 2;

		/** c'tor **/
		public Operation() {
		}

		/**
		 * c'tor + initializers
		 * 
		 * @param nTail
		 * @param nHead
		 * @param nOperation
		 */
		public Operation(int nTail, int nHead, int nOperation) {
			m_nHead = nHead;
			m_nTail = nTail;
			m_nOperation = nOperation;
		}

		/**
		 * compare this operation with another
		 * 
		 * @param other
		 *            operation to compare with
		 * @return true if operation is the same
		 */
		public boolean equals(Operation other) {
			if (other == null) {
				return false;
			}
			return ((m_nOperation == other.m_nOperation)
					&& (m_nHead == other.m_nHead) && (m_nTail == other.m_nTail));
		} // equals

		/** number of the tail node **/
		public int m_nTail;
		/** number of the head node **/
		public int m_nHead;
		/** type of operation (ADD, DEL, REVERSE) **/
		public int m_nOperation;
		/** change of score due to this operation **/
		public double m_fScore = -1E100;

		/**
		 * Returns the revision string.
		 * 
		 * @return the revision
		 */
		public String getRevision() {
			return RevisionUtils.extract("$Revision: 1.9 $");
		}
	} // class Operation

	/** use the arc reversal operator **/
	boolean m_bUseArcReversal = false;

	/**
	 * search determines the network structure/graph of the network with the
	 * Taby algorithm.
	 * 
	 * @param bayesNet
	 *            the network to search
	 * @param instances
	 *            the instances to work with
	 * @throws Exception
	 *             if something goes wrong
	 */
	protected void search(BayesNet bayesNet, Instances instances)
			throws Exception {
		m_BayesNet = bayesNet;
		bayesNet.setDebug(true);
		System.out.println("Score before local search: " + calcScore(bayesNet));
		localSearch(bayesNet, instances);
		ParentSet[] bestSolution = copyParentSets(bayesNet.getParentSets());
		double scoreOfBestSolution = calcScore(bayesNet);
		System.out.println("Score of solution after local search: " + scoreOfBestSolution);
		double scoreOfFoundSolution;
		int numberOfIterationsWithNoBetterSolution = 0;

		do {
			perturbate(bayesNet, instances);
			localSearch(bayesNet, instances);
			scoreOfFoundSolution = calcScore(bayesNet);
			System.out.println("Score of found solution: " + scoreOfFoundSolution);
			if (scoreOfFoundSolution > scoreOfBestSolution) {
				System.out.println("New best solution found: " + scoreOfFoundSolution);
				bestSolution = copyParentSets(bayesNet.getParentSets());
				scoreOfBestSolution = scoreOfFoundSolution;
				numberOfIterationsWithNoBetterSolution = 0;
			} else {
				copyParentSetsToBayesNet(bayesNet, bestSolution);
				numberOfIterationsWithNoBetterSolution++;
			}
		} while (numberOfIterationsWithNoBetterSolution < 5);
	} // search

	protected void localSearch(BayesNet bayesNet, Instances instances)
			throws Exception {
		System.out.println("Start local search");
		double fScore = calcScore(bayesNet);
		// go do the search
		Operation oOperation = getOptimalOperation(bayesNet, instances);
		while ((oOperation != null) && (oOperation.m_fScore > fScore)) {
			performOperation(bayesNet, instances, oOperation);
			fScore = oOperation.m_fScore;
			oOperation = getOptimalOperation(bayesNet, instances);
		}
		System.out.println("End local search");
	} // search

	private void perturbate(BayesNet bayesNet, Instances instances)
			throws Exception {
		System.out.println("Start perturbation");
		Operation randomOperation = getRandomOperation(bayesNet, instances);
		performOperation(bayesNet, instances, randomOperation);
		System.out.println("End perturbation");
	}

	private ParentSet[] copyParentSets(ParentSet[] parentSets) {
		ParentSet[] copy = new ParentSet[parentSets.length];
		for (int i = 0; i < copy.length; i++) {
			copy[i] = new ParentSet();
			copy[i].copy(parentSets[i]);
		}
		return copy;
	}

	private void copyParentSetsToBayesNet(BayesNet bayesNet,
			ParentSet[] parentSets) {
		for (int i = 0; i < parentSets.length; i++) {
			bayesNet.getParentSets()[i].copy(parentSets[i]);
		}
	}

	/**
	 * check whether the operation is not in the forbidden. For base hill
	 * climber, there are no restrictions on operations, so we always return
	 * true.
	 * 
	 * @param oOperation
	 *            operation to be checked
	 * @return true if operation is not in the tabu list
	 */
	boolean isNotTabu(Operation oOperation) {
		return true;
	} // isNotTabu

	/**
	 * getOptimalOperation finds the optimal operation that can be performed on
	 * the Bayes network that is not in the tabu list.
	 * 
	 * @param bayesNet
	 *            Bayes network to apply operation on
	 * @param instances
	 *            data set to learn from
	 * @return optimal operation found
	 * @throws Exception
	 *             if something goes wrong
	 */
	Operation getOptimalOperation(BayesNet bayesNet, Instances instances)
			throws Exception {
		Operation oBestOperation = new Operation();

		// Add???
		oBestOperation = findBestArcToAdd(bayesNet, instances, oBestOperation);
		// Delete???
		oBestOperation = findBestArcToDelete(bayesNet, instances,
				oBestOperation);
		// Reverse???
		if (getUseArcReversal()) {
			oBestOperation = findBestArcToReverse(bayesNet, instances,
					oBestOperation);
		}

		// did we find something?
		if (oBestOperation.m_fScore == -1E100) {
			return null;
		}

		return oBestOperation;
	} // getOptimalOperation

	Operation getRandomOperation(BayesNet bayesNet, Instances instances) throws Exception {
    	Random random = new Random();
    	int operationType = random.nextInt(3);
    	int nNrOfAtts = instances.numAttributes();
    	int iAttributeHead = random.nextInt(nNrOfAtts);
    	int iAttributeTail;
    	if(operationType == 0){//Add random arc
    		List<Integer> possibleNodes = getNodesWhereArcAdditionIsPossible(bayesNet);
    		if(!possibleNodes.isEmpty()){
    			int i=0, maxNumberOfAttempts = 5;
    			do{
    				iAttributeHead = possibleNodes.get(random.nextInt(possibleNodes.size()));
    				iAttributeTail = random.nextInt(instances.numAttributes());
    			}while(i++ < maxNumberOfAttempts && !addArcMakesSense(bayesNet, instances, iAttributeHead, iAttributeTail));
    			if(i <= maxNumberOfAttempts){
    				return new Operation(iAttributeTail, iAttributeHead, Operation.OPERATION_ADD);
    			}
    		}
    	}
    	
    	//Remove or reverse random arc
    	while(bayesNet.getParentSet(iAttributeHead).getNrOfParents()==0){
    		iAttributeHead = random.nextInt(nNrOfAtts);
    	}
    	int numberOfParents = bayesNet.getParentSet(iAttributeHead).getNrOfParents();
    	iAttributeTail = bayesNet.getParentSet(iAttributeHead).getParents()[random.nextInt(numberOfParents)];
	    if(operationType == 1){
	    	return new Operation(iAttributeTail, iAttributeHead, Operation.OPERATION_DEL);
	    }else{
	    	return new Operation(iAttributeTail, iAttributeHead, Operation.OPERATION_REVERSE);
	    }
    }

	private List<Integer> getNodesWhereArcAdditionIsPossible(BayesNet bayesNet) {
		List<Integer> possibleNodes = new ArrayList<Integer>();
		for (int i = 0; i < bayesNet.getParentSets().length; i++) {
			if (bayesNet.getParentSets()[i].getNrOfParents() < m_nMaxNrOfParents) {
				possibleNodes.add(i);
			}
		}
		return possibleNodes;
	}

	private Operation addRandomArc(BayesNet bayesNet, Instances instances)
			throws Exception {
		Random random = new Random();
		int nNrOfAtts = instances.numAttributes();
		int iAttributeHead = random.nextInt(nNrOfAtts);
		int iAttributeTail = random.nextInt(nNrOfAtts);
		while (!addArcMakesSense(bayesNet, instances, iAttributeHead,
				iAttributeTail)) {
			do {
				iAttributeHead = random.nextInt(nNrOfAtts);
			} while (bayesNet.getParentSet(iAttributeHead).getNrOfParents() >= m_nMaxNrOfParents);
			iAttributeTail = random.nextInt(nNrOfAtts);
		}
		return new Operation(iAttributeTail, iAttributeHead,
				Operation.OPERATION_ADD);
	}

	/**
	 * performOperation applies an operation on the Bayes network and update the
	 * cache.
	 * 
	 * @param bayesNet
	 *            Bayes network to apply operation on
	 * @param instances
	 *            data set to learn from
	 * @param oOperation
	 *            operation to perform
	 * @throws Exception
	 *             if something goes wrong
	 */
	void performOperation(BayesNet bayesNet, Instances instances,
			Operation oOperation) throws Exception {
		// perform operation
		switch (oOperation.m_nOperation) {
		case Operation.OPERATION_ADD:
			applyArcAddition(bayesNet, oOperation.m_nHead, oOperation.m_nTail,
					instances);
			if (bayesNet.getDebug()) {
				System.out.println("Add " + oOperation.m_nHead + " -> "
						+ oOperation.m_nTail);
			}
			break;
		case Operation.OPERATION_DEL:
			applyArcDeletion(bayesNet, oOperation.m_nHead, oOperation.m_nTail,
					instances);
			if (bayesNet.getDebug()) {
				System.out.println("Del " + oOperation.m_nHead + " -> "
						+ oOperation.m_nTail);
			}
			break;
		case Operation.OPERATION_REVERSE:
			applyArcDeletion(bayesNet, oOperation.m_nHead, oOperation.m_nTail,
					instances);
			applyArcAddition(bayesNet, oOperation.m_nTail, oOperation.m_nHead,
					instances);
			if (bayesNet.getDebug()) {
				System.out.println("Rev " + oOperation.m_nHead + " -> "
						+ oOperation.m_nTail);
			}
			break;
		}
	} // performOperation

	/**
	 * 
	 * @param bayesNet
	 * @param iHead
	 * @param iTail
	 * @param instances
	 */
	void applyArcAddition(BayesNet bayesNet, int iHead, int iTail,
			Instances instances) {
		ParentSet bestParentSet = bayesNet.getParentSet(iHead);
		bestParentSet.addParent(iTail, instances);
	} // applyArcAddition

	/**
	 * 
	 * @param bayesNet
	 * @param iHead
	 * @param iTail
	 * @param instances
	 */
	void applyArcDeletion(BayesNet bayesNet, int iHead, int iTail,
			Instances instances) {
		ParentSet bestParentSet = bayesNet.getParentSet(iHead);
		bestParentSet.deleteParent(iTail, instances);
	} // applyArcAddition

	/**
	 * find best (or least bad) arc addition operation
	 * 
	 * @param bayesNet
	 *            Bayes network to add arc to
	 * @param instances
	 *            data set
	 * @param oBestOperation
	 * @return Operation containing best arc to add, or null if no arc addition
	 *         is allowed (this can happen if any arc addition introduces a
	 *         cycle, or all parent sets are filled up to the maximum nr of
	 *         parents).
	 * @throws Exception
	 *             if something goes wrong
	 */
	Operation findBestArcToAdd(BayesNet bayesNet, Instances instances,
			Operation oBestOperation) throws Exception {
		int nNrOfAtts = instances.numAttributes();
		// find best arc to add
		for (int iAttributeHead = 0; iAttributeHead < nNrOfAtts; iAttributeHead++) {
			if (bayesNet.getParentSet(iAttributeHead).getNrOfParents() < m_nMaxNrOfParents) {
				for (int iAttributeTail = 0; iAttributeTail < nNrOfAtts; iAttributeTail++) {
					if (addArcMakesSense(bayesNet, instances, iAttributeHead,
							iAttributeTail)) {
						Operation oOperation = new Operation(iAttributeTail,
								iAttributeHead, Operation.OPERATION_ADD);
						double fScore = calcScoreWithExtraParent(
								oOperation.m_nHead, oOperation.m_nTail);
						if (fScore > oBestOperation.m_fScore) {
							if (isNotTabu(oOperation)) {
								oBestOperation = oOperation;
								oBestOperation.m_fScore = fScore;
							}
						}
					}
				}
			}
		}
		return oBestOperation;
	} // findBestArcToAdd

	/**
	 * find best (or least bad) arc deletion operation
	 * 
	 * @param bayesNet
	 *            Bayes network to delete arc from
	 * @param instances
	 *            data set
	 * @param oBestOperation
	 * @return Operation containing best arc to delete, or null if no deletion
	 *         can be made (happens when there is no arc in the network yet).
	 * @throws Exception
	 *             of something goes wrong
	 */
	Operation findBestArcToDelete(BayesNet bayesNet, Instances instances,
			Operation oBestOperation) throws Exception {
		int nNrOfAtts = instances.numAttributes();
		// find best arc to delete
		for (int iNode = 0; iNode < nNrOfAtts; iNode++) {
			ParentSet parentSet = bayesNet.getParentSet(iNode);
			for (int iParent = 0; iParent < parentSet.getNrOfParents(); iParent++) {
				Operation oOperation = new Operation(parentSet
						.getParent(iParent), iNode, Operation.OPERATION_DEL);
				double fScore = calcScoreWithMissingParent(oOperation.m_nHead,
						oOperation.m_nTail);
				if (fScore > oBestOperation.m_fScore) {
					if (isNotTabu(oOperation)) {
						oBestOperation = oOperation;
						oBestOperation.m_fScore = fScore;
					}
				}
			}
		}
		return oBestOperation;
	} // findBestArcToDelete

	/**
	 * find best (or least bad) arc reversal operation
	 * 
	 * @param bayesNet
	 *            Bayes network to reverse arc in
	 * @param instances
	 *            data set
	 * @param oBestOperation
	 * @return Operation containing best arc to reverse, or null if no reversal
	 *         is allowed (happens if there is no arc in the network yet, or
	 *         when any such reversal introduces a cycle).
	 * @throws Exception
	 *             if something goes wrong
	 */
	Operation findBestArcToReverse(BayesNet bayesNet, Instances instances,
			Operation oBestOperation) throws Exception {
		int nNrOfAtts = instances.numAttributes();
		// find best arc to reverse
		for (int iNode = 0; iNode < nNrOfAtts; iNode++) {
			ParentSet parentSet = bayesNet.getParentSet(iNode);
			for (int iParent = 0; iParent < parentSet.getNrOfParents(); iParent++) {
				int iTail = parentSet.getParent(iParent);
				// is reversal allowed?
				if (reverseArcMakesSense(bayesNet, instances, iNode, iTail)
						&& bayesNet.getParentSet(iTail).getNrOfParents() < m_nMaxNrOfParents) {
					// go check if reversal results in the best step forward
					Operation oOperation = new Operation(parentSet
							.getParent(iParent), iNode,
							Operation.OPERATION_REVERSE);
					double fScore = calcScoreWithReversedParent(
							oOperation.m_nHead, oOperation.m_nTail);
					if (fScore > oBestOperation.m_fScore) {
						if (isNotTabu(oOperation)) {
							oBestOperation = oOperation;
							oBestOperation.m_fScore = fScore;
						}
					}
				}
			}
		}
		return oBestOperation;
	} // findBestArcToReverse

	/**
	 * Sets the max number of parents
	 * 
	 * @param nMaxNrOfParents
	 *            the max number of parents
	 */
	public void setMaxNrOfParents(int nMaxNrOfParents) {
		m_nMaxNrOfParents = nMaxNrOfParents;
	}

	/**
	 * Gets the max number of parents.
	 * 
	 * @return the max number of parents
	 */
	public int getMaxNrOfParents() {
		return m_nMaxNrOfParents;
	}

	/**
	 * Returns an enumeration describing the available options.
	 * 
	 * @return an enumeration of all the available options.
	 */
	public Enumeration listOptions() {
		Vector newVector = new Vector(2);

		newVector.addElement(new Option("\tMaximum number of parents", "P", 1,
				"-P <nr of parents>"));
		newVector.addElement(new Option(
				"\tUse arc reversal operation.\n\t(default false)", "R", 0,
				"-R"));
		newVector.addElement(new Option(
				"\tInitial structure is empty (instead of Naive Bayes)", "N",
				0, "-N"));

		Enumeration enu = super.listOptions();
		while (enu.hasMoreElements()) {
			newVector.addElement(enu.nextElement());
		}
		return newVector.elements();
	} // listOptions

	/**
	 * Parses a given list of options.
	 * <p/>
	 * 
	 * <!-- options-start --> Valid options are:
	 * <p/>
	 * 
	 * <pre>
	 * -P &lt;nr of parents&gt;
	 *  Maximum number of parents
	 * </pre>
	 * 
	 * <pre>
	 * -R
	 *  Use arc reversal operation.
	 *  (default false)
	 * </pre>
	 * 
	 * <pre>
	 * -N
	 *  Initial structure is empty (instead of Naive Bayes)
	 * </pre>
	 * 
	 * <pre>
	 * -mbc
	 *  Applies a Markov Blanket correction to the network structure, 
	 *  after a network structure is learned. This ensures that all 
	 *  nodes in the network are part of the Markov blanket of the 
	 *  classifier node.
	 * </pre>
	 * 
	 * <pre>
	 * -S [LOO-CV|k-Fold-CV|Cumulative-CV]
	 *  Score type (LOO-CV,k-Fold-CV,Cumulative-CV)
	 * </pre>
	 * 
	 * <pre>
	 * -Q
	 *  Use probabilistic or 0/1 scoring.
	 *  (default probabilistic scoring)
	 * </pre>
	 * 
	 * <!-- options-end -->
	 * 
	 * @param options
	 *            the list of options as an array of strings
	 * @throws Exception
	 *             if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception {
		setUseArcReversal(Utils.getFlag('R', options));

		setInitAsNaiveBayes(!(Utils.getFlag('N', options)));

		String sMaxNrOfParents = Utils.getOption('P', options);
		if (sMaxNrOfParents.length() != 0) {
			setMaxNrOfParents(Integer.parseInt(sMaxNrOfParents));
		} else {
			setMaxNrOfParents(100000);
		}

		super.setOptions(options);
	} // setOptions

	/**
	 * Gets the current settings of the search algorithm.
	 * 
	 * @return an array of strings suitable for passing to setOptions
	 */
	public String[] getOptions() {
		String[] superOptions = super.getOptions();
		String[] options = new String[7 + superOptions.length];
		int current = 0;
		if (getUseArcReversal()) {
			options[current++] = "-R";
		}

		if (!getInitAsNaiveBayes()) {
			options[current++] = "-N";
		}

		options[current++] = "-P";
		options[current++] = "" + m_nMaxNrOfParents;

		// insert options from parent class
		for (int iOption = 0; iOption < superOptions.length; iOption++) {
			options[current++] = superOptions[iOption];
		}

		// Fill up rest with empty strings, not nulls!
		while (current < options.length) {
			options[current++] = "";
		}
		return options;
	} // getOptions

	/**
	 * Sets whether to init as naive bayes
	 * 
	 * @param bInitAsNaiveBayes
	 *            whether to init as naive bayes
	 */
	public void setInitAsNaiveBayes(boolean bInitAsNaiveBayes) {
		m_bInitAsNaiveBayes = bInitAsNaiveBayes;
	}

	/**
	 * Gets whether to init as naive bayes
	 * 
	 * @return whether to init as naive bayes
	 */
	public boolean getInitAsNaiveBayes() {
		return m_bInitAsNaiveBayes;
	}

	/**
	 * get use the arc reversal operation
	 * 
	 * @return whether the arc reversal operation should be used
	 */
	public boolean getUseArcReversal() {
		return m_bUseArcReversal;
	} // getUseArcReversal

	/**
	 * set use the arc reversal operation
	 * 
	 * @param bUseArcReversal
	 *            whether the arc reversal operation should be used
	 */
	public void setUseArcReversal(boolean bUseArcReversal) {
		m_bUseArcReversal = bUseArcReversal;
	} // setUseArcReversal

	/**
	 * This will return a string describing the search algorithm.
	 * 
	 * @return The string.
	 */
	public String globalInfo() {
		return "This Bayes Network learning algorithm uses a hill climbing algorithm "
				+ "adding, deleting and reversing arcs. The search is not restricted by an order "
				+ "on the variables (unlike K2). The difference with B and B2 is that this hill "
				+ "climber also considers arrows part of the naive Bayes structure for deletion.";
	} // globalInfo

	/**
	 * @return a string to describe the Use Arc Reversal option.
	 */
	public String useArcReversalTipText() {
		return "When set to true, the arc reversal operation is used in the search.";
	} // useArcReversalTipText

	/**
	 * Returns the revision string.
	 * 
	 * @return the revision
	 */
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 1.9 $");
	}
} // HillClimber