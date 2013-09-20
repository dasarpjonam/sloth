/**
 * DomainDefinition.java
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class holds basic information about a domain. It contains all the shapes
 * drawable in your domain, as well as information about editing capabilities
 * and beautification.
 * 
 * @author jbjohns
 */
public class DomainDefinition {
	
	/**
	 * The name of this domain
	 */
	private String m_name;
	
	/**
	 * The description for this domain
	 */
	private String m_description;
	
	/**
	 * The list of shapes that are defined for this domain
	 */
	private List<ShapeDefinition> m_shapeDefinitions;
	
	/**
	 * Map the number of components in a shape def to a set of shape defs that
	 * have that many components. This is useful for seeing if you have enough
	 * shapes in your pool of shapes to recognize a given shape def
	 */
	private Map<Integer, Set<ShapeDefinition>> m_shapesByNumComponents;
	
	/**
	 * Maps the shape definition name to the shape definition
	 */
	private Map<String, ShapeDefinition> m_shapesByName;
	
	
	/**
	 * Construct a domain definition with empty string for name and description
	 */
	public DomainDefinition() {
		this("", "");
	}
	

	/**
	 * Construct a domain definition with the given name and empty string for
	 * description
	 * 
	 * @param name
	 *            The name of this domain
	 */
	public DomainDefinition(String name) {
		this(name, "");
	}
	

	/**
	 * Construct a domain definition with the given name and description.
	 * Initialize the list of shape definitions.
	 * 
	 * @param name
	 *            The name of this domain, see {@link #setName(String)}
	 * @param description
	 *            The description of this domain, see
	 *            {@link #setDescription(String)}
	 */
	public DomainDefinition(String name, String description) {
		m_shapeDefinitions = new ArrayList<ShapeDefinition>();
		m_shapesByNumComponents = new HashMap<Integer, Set<ShapeDefinition>>();
		m_shapesByName = new HashMap<String, ShapeDefinition>();
		
		setName(name);
		setDescription(description);
	}
	

	/**
	 * Set the name of this domain
	 * 
	 * @param name
	 *            The name of this domain, if you pass a null reference the name
	 *            will be set to the empty string
	 */
	public void setName(String name) {
		m_name = (name != null) ? name : "";
	}
	

	/**
	 * Set the description of this domain
	 * 
	 * @param description
	 *            The description of this domain, if you pass a null reference
	 *            the desc will be set to the empty string
	 */
	public void setDescription(String description) {
		m_description = (description != null) ? description : "";
	}
	

	/**
	 * Get the name of this domain
	 * 
	 * @return The name of this domain
	 */
	public String getName() {
		return m_name;
	}
	

	/**
	 * Get the description of this domain
	 * 
	 * @return The description of this domain
	 */
	public String getDescription() {
		return m_description;
	}
	

	/**
	 * Get the list of shape definitions for this domain. Please only add things
	 * to the list through {@link #addShapeDefinition(ShapeDefinition)}
	 * 
	 * @return The list of shape definitions
	 */
	public List<ShapeDefinition> getShapeDefinitions() {
		return Collections.unmodifiableList(m_shapeDefinitions);
	}
	

	/**
	 * Set the list of shape definitions. Updates any hash values, so will take
	 * O(n) in the length of the provided shape list
	 * 
	 * @param shapes
	 *            Set the list of shape definitions. If the reference is null,
	 *            sets to an empty list of shapes.n
	 */
	public void setShapeDefinitions(List<ShapeDefinition> shapes) {
		m_shapeDefinitions = new ArrayList<ShapeDefinition>();
		
		if (shapes != null) {
			for (ShapeDefinition shape : shapes) {
				addShapeDefinition(shape);
			}
		}
	}
	

	/**
	 * Add a shape definition to the list of shapes defined in this domain.
	 * 
	 * @param shapeDef
	 */
	public void addShapeDefinition(ShapeDefinition shapeDef) {
		if (m_shapeDefinitions == null) {
			m_shapeDefinitions = new ArrayList<ShapeDefinition>();
		}
		m_shapeDefinitions.add(shapeDef);
		m_shapesByName.put(shapeDef.getName().toLowerCase(), shapeDef);
		
		// add the incoming shape definition to the set of shape definitions
		// that have the same number of components as it.
		Integer numComps = new Integer(shapeDef.getNumComponents());
		Set<ShapeDefinition> shapeDefsWithNumComps = m_shapesByNumComponents
		        .get(numComps);
		// no shape defs with this number of components yet?
		if (shapeDefsWithNumComps == null) {
			shapeDefsWithNumComps = new TreeSet<ShapeDefinition>();
			m_shapesByNumComponents.put(numComps, shapeDefsWithNumComps);
		}
		shapeDefsWithNumComps.add(shapeDef);
	}
	

	/**
	 * Get the set of sizes of the shape definitions, that is, the number of
	 * components in the shape definitions.
	 * 
	 * @return The set of shape definition sizes in this domain, which you can
	 *         use to get a set of shape definitions with this many constraints.
	 */
	public Set<Integer> getNumComponentSet() {
		return m_shapesByNumComponents.keySet();
	}
	
	private boolean m_sortedByNumber = false;
	
	private List<ShapeDefinition> m_sortedList = new ArrayList<ShapeDefinition>();
	
	
	public List<ShapeDefinition> getSortedByNum() {
		
		if (m_sortedByNumber) {
			return m_sortedList;
		}
		
		// Shape definition comparator
		Comparator<ShapeDefinition> numSubshapesComplexityComparator = new Comparator<ShapeDefinition>() {
			
			@Override
			public int compare(ShapeDefinition o1, ShapeDefinition o2) {
				
				if (o1 == null || o2 == null) {
					throw new NullPointerException(
					        "Null shape definition found");
				}
				
				int comparison = o2.getNumComponents() - o1.getNumComponents();
				if (comparison == 0) {
					comparison = o2.getName().compareToIgnoreCase(o1.getName());
				}
				
				return comparison;
			}
			
		};
		
		// Sort the shape definitions
		SortedSet<ShapeDefinition> sortedSet = new TreeSet<ShapeDefinition>(
		        numSubshapesComplexityComparator);
		sortedSet.addAll(getShapeDefinitions());
		
		// Place them into a list
		m_sortedList = new ArrayList<ShapeDefinition>();
		m_sortedList.addAll(sortedSet);
		
		// m_sortedList.addAll(getShapeDefinitions());
		//		
		// for (int i = 0; i < m_sortedList.size() - 1; i++) {
		// for (int j = i + 1; j < m_sortedList.size(); j++) {
		// ShapeDefinition sdi = m_sortedList.get(i);
		// ShapeDefinition sdj = m_sortedList.get(j);
		// if (sdi.numSubShapesComplexity() < sdj.numSubShapesComplexity()) {
		// m_sortedList.remove(j);
		// m_sortedList.add(i, sdj);
		// }
		// }
		// }
		
		m_sortedByNumber = true;
		
		return m_sortedList;
	}
	
	private boolean m_alphaSorted = false;
	
	private List<ShapeDefinition> m_alphaList = new ArrayList<ShapeDefinition>();
	
	
	public List<ShapeDefinition> getSortedAlphabetically() {
		if (m_alphaSorted) {
			return m_alphaList;
		}
		
		// Shape definition comparator
		Comparator<ShapeDefinition> alphabeticComparator = new Comparator<ShapeDefinition>() {
			
			@Override
			public int compare(ShapeDefinition o1, ShapeDefinition o2) {
				
				if (o1 == null || o2 == null) {
					throw new NullPointerException(
					        "Null shape definition found");
				}
				
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
			
		};
		
		// Sort the shape definitions
		SortedSet<ShapeDefinition> sortedSet = new TreeSet<ShapeDefinition>(
		        alphabeticComparator);
		sortedSet.addAll(getShapeDefinitions());
		
		// Place them into a list
		m_alphaList = new ArrayList<ShapeDefinition>();
		m_alphaList.addAll(sortedSet);
		
		// m_alphaList.addAll(getShapeDefinitions());
		// for (int i = 0; i < m_alphaList.size() - 1; i++) {
		// for (int j = i + 1; j < m_alphaList.size(); j++) {
		// ShapeDefinition sdi = m_alphaList.get(i);
		// ShapeDefinition sdj = m_alphaList.get(j);
		// if (sdi.getName().compareToIgnoreCase(sdj.getName()) > 0) {
		// m_alphaList.remove(j);
		// m_alphaList.add(i, sdj);
		// }
		// }
		// }
		m_alphaSorted = true;
		
		return m_alphaList;
	}
	

	/**
	 * Get all the shape definitions with the given number of components. If
	 * there are none, returns null set.
	 * 
	 * @param numComponents
	 *            The size of the shape definitions you want to get
	 * @return The set of shape definitions with the given size
	 */
	public Set<ShapeDefinition> getShapeDefinitionsWithNumComponents(
	        Integer numComponents) {
		return m_shapesByNumComponents.get(numComponents);
	}
	

	/**
	 * Determine if the domain contains a shape definition with the given name
	 * 
	 * @param shapeDefName
	 *            name of the shape definition to look for
	 * @return true if found, else false
	 */
	public boolean contains(String shapeDefName) {
		return m_shapesByName.containsKey(shapeDefName.toLowerCase());
	}
	

	/**
	 * Get a shape definition that has the given name
	 * 
	 * @param shapeDefName
	 *            name of the shape definition to get
	 * @return shape definition
	 */
	public ShapeDefinition getShapeDefinition(String shapeDefName) {
		return m_shapesByName.get(shapeDefName.toLowerCase());
	}
	

	/**
	 * Prints out the shapes in the domain.
	 */
	public String toString() {
		String s = "[";
		for (String k : m_shapesByName.keySet()) {
			s += k + ", ";
		}
		s += "]";
		return s;
	}
}
