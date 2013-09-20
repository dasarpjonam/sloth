/**
 * AliasParameter.java
 * 
 * Revision History:<br>
 * Oct 1, 2008 bpaulson - File created
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
 * Class wrapping an alias parameter. A parameter consists of a required alias
 * name, component that is being constrained, plus an optional subPart of that
 * component. So, your name might be 'shaftMidpoint', your component might be
 * 'line1', and your subPart might be 'center', if you're constraining to the
 * center point of the line. Or, you might not have any subPart if you're
 * constraining the line as a whole. <br>
 * <br>
 * Note: this class extends ConstraintParameter only because they contain
 * overlapping information and not necessarily because its a logical sub-class
 * of ConstraintParameter
 * 
 * @author bpaulson
 */
public class AliasDefinition extends ConstraintParameter implements
        Comparable<AliasDefinition> {
	
	/**
	 * Name of the alias
	 */
	private String m_aliasName;
	
	
	/**
	 * Construct an alias with the given name for the given component. The
	 * component sub-part is defaulted to {@link ComponentSubPart#None}
	 * 
	 * @param aliasName
	 *            Name of the alias to use for the component
	 * @param component
	 *            The component we're constraining, cannot be null or the empty
	 *            string
	 */
	public AliasDefinition(String aliasName, String component) {
		this(aliasName, component, ComponentSubPart.None);
	}
	

	/**
	 * Construct an alias parameter for the given component and component
	 * sub-part
	 * 
	 * @param aliasName
	 *            The alias name for the component and given sub-part
	 * @param component
	 *            The component we're constraining, cannot be null or the empty
	 *            string
	 * @param subPart
	 *            The sub-part of the component
	 */
	public AliasDefinition(String aliasName, String component,
	        ComponentSubPart subPart) {
		super(component, subPart);
		setAliasName(aliasName);
	}
	

	/**
	 * Get the alias name
	 * 
	 * @return alias name
	 */
	public String getAliasName() {
		return m_aliasName;
	}
	

	/**
	 * Set the alias name
	 * 
	 * @param aliasName
	 *            alias name
	 */
	public void setAliasName(String aliasName) {
		m_aliasName = aliasName;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(AliasDefinition o) {
		// compare based on the name, component, and subpart
		return this.toString().compareTo(o.toString());
	}
	

	/**
	 * String representation of alias definition
	 */
	@Override
	public String toString() {
		return m_aliasName + SEPARATOR + getComponent() + SEPARATOR + getComponentSubPart();
	}
}
