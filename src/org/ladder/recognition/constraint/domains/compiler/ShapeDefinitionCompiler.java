/**
 * ShapeDefinitionCompiler.java
 * 
 * Revision History:<br>
 * Oct 30, 2008 bpaulson - File created
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
package org.ladder.recognition.constraint.domains.compiler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ladder.recognition.constraint.ConstraintFactory;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.domains.AliasDefinition;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.ComponentSubPart;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.ConstraintParameter;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;

import edu.tamu.deepGreen.recognition.SIDC;

/**
 * Compiler for a shape definition. Takes in a shape definition and a current
 * domain definition and compiles the shape definition into a new, flattened
 * version of the shape definition.
 * 
 * @author bpaulson
 */
public class ShapeDefinitionCompiler {
	
	/**
	 * Shape definition to compile
	 */
	private ShapeDefinition m_shapeDef;
	
	/**
	 * New, compiled shape definition
	 */
	private ShapeDefinition m_compiledShapeDef;
	
	/**
	 * Existing domain definition
	 */
	private DomainDefinition m_domainDef;
	
	/**
	 * Set of "primitives" found while compiling the shape definition. A
	 * "primitive" refers to any shape type that has not been previously defined
	 * in the domain.
	 */
	private Set<String> m_assumedPrimitives;
	
	/**
	 * Tag to add to file name of a shape definition that has been compiled
	 */
	public static final String COMPILE_TAG = "_compiled_";
	
	
	/**
	 * Constructor for shape definition compiler
	 * 
	 * @param shapeDef
	 *            shape definition to compile
	 * @param domainDef
	 *            current domain definition
	 */
	public ShapeDefinitionCompiler(ShapeDefinition shapeDef,
	        DomainDefinition domainDef) {
		m_shapeDef = shapeDef;
		m_domainDef = domainDef;
		m_assumedPrimitives = new HashSet<String>();
		m_compiledShapeDef = null;
	}
	

	/**
	 * Compile the shape definition
	 * 
	 * @return newly compiled (and flattened ShapeDefinition)
	 */
	public ShapeDefinition compile() throws Exception {
		m_compiledShapeDef = new ShapeDefinition();
		
		// copy isA, abstract, name, and description information
		for (String isA : m_shapeDef.getIsASet()) {
			m_compiledShapeDef.addIsA(isA);
		}
		m_compiledShapeDef.setAbstract(m_shapeDef.isAbstract());
		m_compiledShapeDef.setName(m_shapeDef.getName());
		m_compiledShapeDef.setDescription(m_shapeDef.getDescription());
		m_compiledShapeDef.setFilename(COMPILE_TAG + m_shapeDef.getFilename());
		
		// loop through and copy/flatten components
		for (ComponentDefinition component : m_shapeDef
		        .getComponentDefinitions()) {
			
			// see if component exists in domain
			if (m_domainDef.contains(component.getShapeType())) {
				
				// flatten definition and save in compiled shape def
				List<ComponentDefinition> subComponents = getSubComponents(m_domainDef
				        .getShapeDefinition(component.getShapeType()
				                .toLowerCase()));
				for (ComponentDefinition subComp : subComponents) {
					subComp.setParent(component);
					component.addChild(subComp);
					// m_compiledShapeDef.addComponentDefinition(subComp);
				}
				m_compiledShapeDef.addComponentDefinition(component);
			}
			
			// can't find component - assume its a primitive
			else {
				
				// bookkeeping: keep track of unknown components
				m_assumedPrimitives.add(component.getShapeType());
				
				// copy component to compiled shape def
				m_compiledShapeDef.addComponentDefinition(component);
			}
		}
		
		// copy constraints
		for (ConstraintDefinition constraint : m_shapeDef
		        .getConstraintDefinitions()) {
			verifyConstraint(constraint);
			m_compiledShapeDef.addConstraintDefinition(constraint);
		}
		
		// copy aliases
		// TODO: resolve recursive aliases referenced in constraints?
		// (for example: rectangle.line1.BottomMostEnd)
		for (AliasDefinition alias : m_shapeDef.getAliasDefinitions()) {
			verifyAlias(alias);
			m_compiledShapeDef.addAliasDefinition(alias);
		}
		
		// check SIDC (COA only)
		String sidc = m_shapeDef.getAttribute("ATTR_SIDC");
		if (sidc != null) {
			try {
				SIDC.properSIDC(sidc);
			}
			catch (Exception e) {
				throw new Exception(e.getMessage() + "\nin "
				                    + m_shapeDef.getName());
			}
		}
		
		return m_compiledShapeDef;
	}
	

	/**
	 * Verify that the constraint makes sense and contains valid arguments
	 * 
	 * @param constraint
	 *            constraint to check
	 * @throws Exception
	 *             if constraint is invalid
	 */
	protected void verifyConstraint(ConstraintDefinition constraint)
	        throws Exception {
		
		// verify constraint name
		IConstraint c = ConstraintFactory.getConstraint(constraint.getName());
		if (c == null)
			throw new Exception("Unknown constraint: \"" + constraint.getName()
			                    + "\" in " + m_shapeDef.getName());
		
		// verify adequate number of params
		if (constraint.getParameters().size() != c.getNumRequiredParameters())
			throw new Exception("Constraint \"" + constraint.getName()
			                    + "\" in " + m_shapeDef.getName()
			                    + " contains "
			                    + constraint.getParameters().size()
			                    + " parameters.  Required: "
			                    + c.getNumRequiredParameters());
		
		// verify all component names in each param
		for (int i = 0; i < constraint.getParameters().size(); i++) {
			ConstraintParameter param = constraint.getParameters().get(i);
			if (!validComponentName(param.getComponent())) {
				if (constraint.getName().equalsIgnoreCase("ContainsText") && i == 1) {
					// this component name is the text to be recognized so we ignore it
				}
				else {
					throw new Exception(
					        "Constraint \""
					                + constraint.getName()
					                + "\" in "
					                + m_shapeDef.getName()
					                + " contains a parameter with an unknown component name \""
					                + param.getComponent() + "\".");
				}
			}
		}
	}
	

	/**
	 * Verify that the alias makes sense and contains valid arguments
	 * 
	 * @param alias
	 *            alias to check
	 * @throws Exception
	 *             if alias is invalid
	 */
	protected void verifyAlias(AliasDefinition alias) throws Exception {
		// verify component name
		if (!validComponentName(alias.getComponent()))
			throw new Exception(
			        "Alias \""
			                + alias.getAliasName()
			                + "\" in "
			                + m_shapeDef.getName()
			                + " contains a parameter with an unknown component name \""
			                + alias.getComponent() + "\".");
	}
	

	/**
	 * Determine if a given component name is valid
	 * 
	 * @param name
	 *            name of the component to validate
	 * @return true if valid name (corresponds to a real component or
	 *         sub-component), else false
	 */
	protected boolean validComponentName(String name) {
		String n = new String(name);
		List<String> componentNames = new ArrayList<String>();
		
		// break name up into component list (components separated by a period)
		while (n.contains(".")) {
			int periodIndex = n.indexOf('.');
			componentNames.add(n.substring(0, periodIndex));
			n = n.substring(periodIndex + 1);
		}
		componentNames.add(n);
		
		// see if component is plausible
		boolean plausible = false;
		Iterator<ComponentDefinition> i = m_compiledShapeDef
		        .getComponentDefinitions().iterator();
		while (i.hasNext() && !plausible) {
			plausible = validComponentName(componentNames, i.next());
		}
		
		return plausible;
	}
	

	/**
	 * Determine if the component list (recursively matches a possibility of the
	 * current component). For example a list that contains [rectangle1, line1]
	 * with the given component rectangle1 (which has a subcomponent line1) will
	 * be true.
	 * 
	 * @param componentList
	 *            list of components to check for recursively
	 * @param currentComponent
	 *            current component to check
	 * @return true if possibility exists; else false
	 */
	protected boolean validComponentName(List<String> componentList,
	        ComponentDefinition currentComponent) {
		if (componentList.size() == 0)
			return false;
		boolean valid = false;
		
		// see if top level component's name matches
		if (componentList.get(0)
		        .compareToIgnoreCase(currentComponent.getName()) == 0)
			valid = true;
		
		// see if we have a top level component that is a ComponentSubPart
		if (componentList.size() == 1
		    && isComponentSubPart(componentList.get(0)))
			valid = true;
		
		// check sub-components if they exist
		if (valid && componentList.size() > 1) {
			boolean found = false;
			valid = false;
			Iterator<ComponentDefinition> i = currentComponent.getChildren()
			        .iterator();
			
			// see if child component is a ComponentSubPart
			if (componentList.size() == 2
			    && isComponentSubPart(componentList.get(1))) {
				found = true;
				valid = true;
			}
			
			// find the correct child component
			while (i.hasNext() && !found) {
				ComponentDefinition compDef = i.next();
				
				// child found, recursively check children further
				if (compDef.getName().compareToIgnoreCase(componentList.get(1)) == 0) {
					found = true;
					valid = true;
					if (componentList.size() > 2) {
						List<String> subCompList = new ArrayList<String>();
						for (int j = 1; j < componentList.size(); j++)
							subCompList.add(componentList.get(j));
						valid = validComponentName(subCompList, compDef);
					}
				}
			}
			
			// child component not found so we return false
			if (!found)
				valid = false;
		}
		return valid;
	}
	

	/**
	 * Determines if a given string matches a component sub part
	 * 
	 * @param name
	 *            name to check
	 * @return true if name is a component subpart else false
	 */
	protected boolean isComponentSubPart(String name) {
		boolean found = false;
		for (int i = 0; i < ComponentSubPart.values().length && !found; i++) {
			if (ComponentSubPart.values()[i].toString().equalsIgnoreCase(name)) {
				found = true;
			}
		}
		return found;
	}
	

	/**
	 * Get the compiled shape definition
	 * 
	 * @return compiled shape definition
	 */
	public ShapeDefinition getCompiledShapeDefinition() {
		return m_compiledShapeDef;
	}
	

	/**
	 * Get the list of "unknown" components which were assumed to be primitives
	 * 
	 * @return list of component names
	 */
	public Set<String> getAssumedPrimitives() {
		return m_assumedPrimitives;
	}
	

	/**
	 * Get a list of flattened sub-components of a given shape definition
	 * 
	 * @return list of flattened sub-components
	 */
	protected List<ComponentDefinition> getSubComponents(
	        ShapeDefinition shapeDef) {
		List<ComponentDefinition> subComps = new ArrayList<ComponentDefinition>();
		for (ComponentDefinition component : shapeDef.getComponentDefinitions()) {
			/*
			 * if (m_domainDef.contains(component.getShapeType())) {
			 * subComps.addAll(getSubComponents(m_domainDef
			 * .getShapeDefinition(component.getShapeType()))); } else {
			 * subComps.add(component); }
			 */
			subComps.add(component);
		}
		return subComps;
	}
}
