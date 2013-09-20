/**
 * DomainDefinitionCompiler.java
 * 
 * Revision History:<br>
 * Nov 3, 2008 bpaulson - File created
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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;

/**
 * Domain definition compiler. Takes a domain and compiles all of its shapes
 * into new, flattened shapes and then returns a new DomainDefinition containing
 * the flattened ShapeDefinitions.
 * 
 * @author bpaulson
 */
public class DomainDefinitionCompiler {
	
	/**
	 * Logger for the class
	 */
	private static final Logger log = LadderLogger
	        .getLogger(DomainDefinitionCompiler.class);
	
	/**
	 * Domain definition to compile
	 */
	private DomainDefinition m_domainDef;
	
	/**
	 * Compiled domain definition
	 */
	private DomainDefinition m_compiledDomainDef;
	
	/**
	 * Set of strings containing names of shapes whose shape definition could
	 * not be found (and are, thus, assumed to be primitive shapes)
	 */
	private Set<String> m_assumedPrimitives;
	
	/**
	 * Set containing the names of shapes that have already been compiled
	 */
	private Set<String> m_compiled;
	
	
	/**
	 * Constructor for compiler
	 * 
	 * @param domainDef
	 *            domain definition to compile
	 */
	public DomainDefinitionCompiler(DomainDefinition domainDef) {
		m_domainDef = domainDef;
	}
	

	/**
	 * Compile the domain definition
	 * 
	 * @return compiled domain definition
	 */
	public DomainDefinition compile() throws Exception {
		m_assumedPrimitives = new HashSet<String>();
		m_compiled = new HashSet<String>();
		
		// copy domain definition information
		m_compiledDomainDef = new DomainDefinition();
		m_compiledDomainDef.setDescription(m_domainDef.getDescription());
		m_compiledDomainDef.setName(m_domainDef.getName());
		
		// create a list of shape definitions to compile
		List<ShapeDefinition> shapesToCompile = new ArrayList<ShapeDefinition>();
		for (ShapeDefinition shapeDef : m_domainDef.getShapeDefinitions()) {
			shapesToCompile.add(shapeDef);
		}
		
		// compile until all shapes are accounted for
		while (shapesToCompile.size() > 0) {
			for (int i = shapesToCompile.size() - 1; i >= 0; i--) {
				ShapeDefinition shapeDef = shapesToCompile.get(i);
				
				// see if shape definition depends on another shape which has
				// not yet been compiled
				boolean okToCompile = true;
				for (ComponentDefinition compDef : shapeDef
				        .getComponentDefinitions()) {
					
					// System.out.println("We have a " + compDef.getShapeType()
					// + " component.");
					// if domain contains sub-component shape then it is high
					// level and if it hasn't been compiled then we need to
					// compile the sub-component before we can compile this
					// shape
					if (log.isDebugEnabled()) {
						log.debug(m_domainDef);
						log.debug(m_compiled);
					}
					if (m_domainDef.contains(compDef.getShapeType())
					    && !m_compiled.contains(compDef.getShapeType()
					            .toLowerCase())) {
						log.debug("Shouldn't compile " + shapeDef.getName()
						          + " yet because it depends on "
						          + compDef.getShapeType());
						
						okToCompile = false;
						break;
					}
				}
				
				// compile if all sub-components have been compiled
				if (okToCompile) {
					log.info("Compiling " + shapeDef.getName());
					ShapeDefinitionCompiler sdCompiler = new ShapeDefinitionCompiler(
					        shapeDef, m_domainDef);
					ShapeDefinition compiledShapeDef = sdCompiler.compile();
					m_compiledDomainDef.addShapeDefinition(compiledShapeDef);
					m_assumedPrimitives.addAll(sdCompiler
					        .getAssumedPrimitives());
					m_compiled.add(shapeDef.getName().toLowerCase());
					shapesToCompile.remove(i);
				}
			}
		}
		
		return m_compiledDomainDef;
	}
	

	/**
	 * Get the compiled domain definition
	 * 
	 * @return compiled domain definition
	 */
	public DomainDefinition getCompiledDomainDefinition() {
		return m_compiledDomainDef;
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
	 * @return the domainDef
	 */
	public DomainDefinition getDomainDef() {
		return m_domainDef;
	}
	

	/**
	 * @param domainDef
	 *            the domainDef to set
	 */
	public void setDomainDef(DomainDefinition domainDef) {
		m_domainDef = domainDef;
	}
	

	/**
	 * @return the compiledDomainDef
	 */
	public DomainDefinition getCompiledDomainDef() {
		return m_compiledDomainDef;
	}
	

	/**
	 * @return the compiled
	 */
	public Set<String> getCompiled() {
		return m_compiled;
	}
	

	/**
	 * @param assumedPrimitives
	 *            the assumedPrimitives to set
	 */
	public void setAssumedPrimitives(Set<String> assumedPrimitives) {
		m_assumedPrimitives = assumedPrimitives;
	}
}
