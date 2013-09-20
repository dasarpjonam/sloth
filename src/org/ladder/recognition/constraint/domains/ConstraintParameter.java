/**
 * ConstraintParameter.java
 * 
 * Revision History:<br>
 * Aug 20, 2008 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package org.ladder.recognition.constraint.domains;

/**
 * Class wrapping a constraint parameter. A parameter consists of a required
 * component that is being constrained, plus an optional subPart of that
 * component. So, your component might be 'line1' and your subPart might be
 * 'center', if you're constraining to the center point of the line. Or, you
 * might not have any subPart if you're constraining the line as a whole.
 * 
 * @author jbjohns
 */
public class ConstraintParameter {
	
	/**
	 * String that separates the component from the subpart when using text
	 * conversion :: Component+SEPARATOR+SubPart
	 */
	public static final String SEPARATOR = ".";
	
	/**
	 * The component that is being constrained. This name must match, exactly,
	 * some component in the shape definition.
	 */
	private String m_component;
	
	/**
	 * An optional subPart of the component that is being constrained, like a
	 * part of a line or bounding box. Defaults to {@link ComponentSubPart#None}
	 */
	private ComponentSubPart m_subPart = null;
	
	
	/**
	 * Construct a constraint parameter for the given component. The component
	 * sub-part is defaulted to {@link ComponentSubPart#None}
	 * 
	 * @param component
	 *            The component we're constraining, cannot be null or the empty
	 *            string
	 * @see #setComponent(String)
	 */
	public ConstraintParameter(String component) {
		this(component, ComponentSubPart.None);
	}
	

	/**
	 * Construct a constraint parameter for the given component and component
	 * sub-part
	 * 
	 * @param component
	 *            The component we're constraining, cannot be null or the empty
	 *            string
	 * @param subPart
	 *            The sub-part of the component
	 * @see #setComponent(String)
	 */
	public ConstraintParameter(String component, ComponentSubPart subPart) {
		setComponent(component);
		setComponentSubPart(subPart);
	}
	

	/**
	 * Set the name of the component that's being constrained. This must match
	 * the name of a component listed in the same shape definition as the
	 * constraint using this parameter. If you pass in a null reference, we
	 * throw a null pointer exception. If you pass in an empty string, we throw
	 * an illegal argument exception
	 * 
	 * @param component
	 *            The name of the component that's being constrained
	 */
	public void setComponent(String component) {
		if (component != null) {
			if (component.trim().length() >= 1) {
				m_component = component;
			}
			else {
				throw new IllegalArgumentException(
				        "Component of constraint paramter cannot be empty string");
			}
		}
		else {
			throw new NullPointerException(
			        "Component of constraint parameter cannot be null");
		}
	}
	

	/**
	 * Set the sub part of the component that we're constraining
	 * 
	 * @param subPart
	 *            The sub-part of the component we're constraining
	 */
	public void setComponentSubPart(ComponentSubPart subPart) {
		m_subPart = subPart;
	}
	

	/**
	 * Get the name of the component we're constraining
	 * 
	 * @return The name of the component
	 */
	public String getComponent() {
		return m_component;
	}
	

	/**
	 * Get the sub-part of the component we're constraining
	 * 
	 * @return The sub-part of the component we're constraining
	 */
	public ComponentSubPart getComponentSubPart() {
		return m_subPart;
	}
	

	/**
	 * Construc the string representation of this paramter, which is:: component +
	 * SEPARATOR + subpart
	 * 
	 * @return String string representation
	 */
	public String toString() {
		return m_component + SEPARATOR + m_subPart;
	}
	
	public boolean containsSubPart(){
		if(m_subPart == ComponentSubPart.None){
			return false;
		} 
		return true;
	}

	/**
	 * Parse a string into a Constraint parameter object. The string must either
	 * just be the name of the component, with no SEPARATOR present, or be in
	 * the form: component+SEPARATOR+subpart
	 * 
	 * @param sourceString
	 *            The string to parse
	 * @return A constraint parameter object parsed from the string
	 */
	public static ConstraintParameter fromString(String sourceString) {
		
		int splitIdx = sourceString.indexOf(SEPARATOR);
		
		String component = "";
		ComponentSubPart subPart = ComponentSubPart.None;
		
		// no separator?
		if (splitIdx < 0) {
			component = sourceString;
		}
		// else split at the separator
		else {
			component = sourceString.substring(0, splitIdx);
			subPart = ComponentSubPart.fromString(sourceString
			        .substring(splitIdx + 1));
		}
		
		return new ConstraintParameter(component, subPart);
	}
}
