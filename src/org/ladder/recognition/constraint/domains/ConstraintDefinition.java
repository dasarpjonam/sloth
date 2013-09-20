/**
 * ConstraintDefinition.java
 * 
 * Revision History:<br>
 * Aug 18, 2008 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 *  This work is released under the BSD License:
 *  (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 *  All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *        nor the names of its contributors may be used to endorse or promote 
 *        products derived from this software without specific prior written 
 *        permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package org.ladder.recognition.constraint.domains;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds information about a constraint definition found in a shape
 * definition. This class allows us to provide a layer between the actual
 * constraint implementations and the IO level, and we can add nice
 * functionality to constraints like probabilities, etc.
 * <p>
 * Compare to is based on the name of this constraint, plus the parameters of
 * this constraint, in the order they appear. So, we can have a Horizontal
 * constraint on line1 that will be equal to a Horizontal constraint on line1.
 * This seems obvious. However, a Horizontal constraint on line2 is not equal.
 * Again, obvious. Order is vital, so Coincident line1 line2 is NOT EQUAL to
 * Coincident line2 line1.
 * <p>
 * This requires maintaining a cached version of a string version of the list of
 * parameters. This is so that each time we compare, we don't have to loop over
 * the list of parameters and build that string. This would get very expensive.
 * Methods that maintain this cached value are noted in their JavaDoc.
 * 
 * @author jbjohns
 */
public class ConstraintDefinition implements Comparable<ConstraintDefinition> {
	
	/**
	 * The name of the constraint you're using, like 'Horizontal'
	 */
	private String m_name;
	
	/**
	 * The parameters you're plugging into the constraints
	 */
	private List<ConstraintParameter> m_parameters;
	
	/**
	 * Multiplier to use for the threshold, for customization. Lower multipliers
	 * mean lower thresholds and tighter constraint. Higher multipliers mean
	 * larger thresholds and looser constraints.
	 */
	private double m_thresholdMultiplier;
	
	/**
	 * This is a string of the parameters that's used internally for compare to.
	 * This value is cached and usually requires O(1) update when adding a
	 * parameter. However, on setting the list of paramters, rebuilding this
	 * String takes O(n) in the number of parameters. This cached value allows
	 * compareTo to take O(1) when comparing the constraint name and all the
	 * parameters of the constraint, in the order they appear.
	 */
	private String m_parameterString;
	
	
	/**
	 * Construct a constraint definition with the empty string for a name and an
	 * empty list of paramters
	 * 
	 */
	public ConstraintDefinition() {
		this("");
	}
	

	/**
	 * Construct a constraint definition with the given name and an empty list
	 * of parameters
	 * 
	 * @param name
	 *            The name of the constraint
	 */
	public ConstraintDefinition(String name) {
		m_parameters = new ArrayList<ConstraintParameter>();
		m_parameterString = "";
		setName(name);
		setThresholdMultiplier(1.0);
	}
	

	/**
	 * Set the name of this constraint. If you pass in a null reference, the
	 * name is set to the empty string
	 * 
	 * @param name
	 *            The name of the constraint.
	 */
	public void setName(String name) {
		m_name = (name != null) ? name : "";
	}
	

	/**
	 * Get the name of the constraint
	 * 
	 * @return The name of the constraint
	 */
	public String getName() {
		return m_name;
	}
	

	/**
	 * Set the list of paramters. If you pass in a null reference, the list is
	 * initialized to a new, empty list. This method call takes O(n) in the size
	 * of the list to build a string representation of all the parameters for
	 * compare to purposes.
	 * 
	 * @param parameters
	 *            The list of parameters. If null reference, sets the list to a
	 *            new empty instance
	 */
	public void setParameters(List<ConstraintParameter> parameters) {
		if (parameters != null) {
			m_parameters = parameters;
		}
		else {
			m_parameters = new ArrayList<ConstraintParameter>();
		}
		
		// rebuild the parameter string based on the new list
		StringBuilder sb = new StringBuilder();
		for (ConstraintParameter param : m_parameters) {
			sb.append("  :").append(param.toString());
		}
		m_parameterString = sb.toString();
	}
	

	/**
	 * Get the list of parameters. DO NOT MODIFY THE LIST OF PARAMETERS
	 * EXTERNALLY. USE THE CLASS METHODS.
	 * 
	 * @return the list of parameters
	 */
	public List<ConstraintParameter> getParameters() {
		// TODO send out a copy..not the reference to our actual data
		return m_parameters;
	}
	

	/**
	 * Add a parameter to the list of params. This method always checks for a
	 * null list and if the list is null, it is initialized and then the
	 * parameter added.
	 * 
	 * @param param
	 *            The parameter to add to the list
	 */
	public void addParameter(ConstraintParameter param) {
		if (m_parameters == null) {
			m_parameters = new ArrayList<ConstraintParameter>();
			m_parameterString = "";
		}
		
		// add this parameter info to the end of the list
		m_parameters.add(param);
		m_parameterString += "  :" + param.toString();
	}
	

	/**
	 * Get the cardinality of this constraint. How many parameters does it take?
	 * 
	 * @return The num params this constraint takes
	 */
	public int getCardinality() {
		if (m_parameters == null) {
			return 0;
		}
		return m_parameters.size();
	}
	

	/**
	 * Get the threshold multiplier specified for this constraint
	 * 
	 * @return the threshold multiplier
	 */
	public double getThresholdMultiplier() {
		return m_thresholdMultiplier;
	}
	

	/**
	 * Set the threshold multiplier for this constraint definition. Lower
	 * multipliers mean lower thresholds and tighter constraint. Higher
	 * multipliers mean larger thresholds and looser constraints. Multiplier ==
	 * 1.0 means use the default threshold for the constraint. This is the
	 * default value, and you only need to set a multiplier if you want to use a
	 * threshold value other than the default.
	 * 
	 * @param multiplier
	 *            the thresholdMultiplier to set, must be > 0 or defaults to 1
	 */
	public void setThresholdMultiplier(double multiplier) {
		m_thresholdMultiplier = (multiplier > 0) ? multiplier : 1.0;
	}
	

	/**
	 * Compare two constraints based on toString() representations
	 * 
	 * @param o
	 *            The other constraint definition to compare to
	 * @return -1 if this < o, 0 if ==, 1 if this > o
	 */
	public int compareTo(ConstraintDefinition o) {
		// compare based on the name and the parameters
		return this.toString().compareTo(o.toString());
	}
	

	/**
	 * Get a string representation of this constraint, which is the name of the
	 * constraint with all the parameters appended to it. Runs in O(1) because
	 * we use a cached value for the parameter string.
	 * 
	 * @return The string representation of this constraint
	 */
	public String toString() {
		return this.getName() + m_parameterString;
	}
}
