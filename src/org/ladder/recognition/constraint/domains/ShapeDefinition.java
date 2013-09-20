/**
 * ShapeDefinition.java
 * 
 * Revision History:<br>
 * Aug 18, 2008 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 *                   This work is released under the BSD License:
 *                   (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 *                   All rights reserved.
 *                   
 *                   Redistribution and use in source and binary forms, with or without
 *                   modification, are permitted provided that the following conditions are met:
 *                       * Redistributions of source code must retain the above copyright
 *                         notice, this list of conditions and the following disclaimer.
 *                       * Redistributions in binary form must reproduce the above copyright
 *                         notice, this list of conditions and the following disclaimer in the
 *                         documentation and/or other materials provided with the distribution.
 *                       * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *                         nor the names of its contributors may be used to endorse or promote 
 *                         products derived from this software without specific prior written 
 *                         permission.
 *                   
 *                   THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *                   EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *                   WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *                   DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 *                   DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *                   (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *                   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *                   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *                   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *                   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package org.ladder.recognition.constraint.domains;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class holds information about a shape definition. If you had a rectangle
 * shape in your domain, the information describing it would go here.
 * <p>
 * CompareTo works on the shape name. This means all shapes in the domain must
 * have unique names, or there will be collisions.
 * <p>
 * THIS CLASS IS NOT THREAD SAFE.
 * 
 * @author jbjohns
 */
public class ShapeDefinition implements Comparable<ShapeDefinition> {
	
	/**
	 * The user-assigned name given to this shape, like 'Rectangle'
	 */
	private String m_name;
	
	/**
	 * Description of this shape
	 */
	private String m_description;
	
	/**
	 * The types of shape this definition is a child of. For example, if this
	 * shape is a Rectangle, you might say it isA Polygon and isA Shape
	 */
	private SortedSet<String> m_isASet;
	
	/**
	 * Flag indicating if this shape is meant to be abstract or not. Abstract
	 * shapes do not have any components or constraints, and are used only for
	 * other shapes to use in the isA field. So you can create an abstract
	 * 'Polygon' shape, and then say that the concrete shape 'Rectangle' isA
	 * 'Polygon'
	 */
	private boolean m_abstract;
	
	/**
	 * Name of the file the shape definition was loaded from
	 */
	private String m_filename;
	
	/**
	 * Default value for abstract flag.
	 */
	public static final boolean DEFAULT_ABSTRACT = false;
	
	/**
	 * The map of components that make up this shape. These are the parts that
	 * are put together in certain ways, like the lines making up a rectangle.
	 * We use a map so we can go from component name to definition very quickly.
	 */
	private Map<String, ComponentDefinition> m_componentDefinitionNameMap;
	
	/**
	 * List of component definitions for ordered, quick, random access and
	 * iteration.
	 */
	private List<ComponentDefinition> m_componentDefinitions;
	
	/**
	 * Store the index of each component in the component list mapped with its
	 * component name. We do this for things that need to use a 1:1 mapping of
	 * components in the order in which they appear.
	 */
	private Map<String, Integer> m_componentNameIndexMap;
	
	/**
	 * Maps shape types, like 'Line', to the set of components in this shape
	 * that match that shape type. So, for example, if this shape has name
	 * 'Rectangle' it will have four components of type 'Line'. Searching this
	 * map for 'Line' will return the set of these four 'Line' components. More
	 * complex shapes will have different types.
	 */
	private Map<String, List<ComponentDefinition>> m_componentsForShapeType;
	
	/**
	 * The list of constraints that hold the components together in semantically
	 * meaningful ways. For example, the four lines of a rectangle meet at the
	 * endpoints.
	 */
	private List<ConstraintDefinition> m_constraintDefinitions;
	
	/**
	 * Map tying groups of constraints by constraint name. So of this is a
	 * 'Rectangle' shape, and you have four line components, two of which you
	 * constrain to be 'Horizontal' and two of which you constraint to be
	 * 'Vertical', the map will have the two keys. Each key will be tied to a
	 * set containing two Constraints. Two constraints saying two of the lines
	 * must be horizontal, and two constraints saying the other two lines must
	 * be vertical
	 */
	private Map<String, Set<ConstraintDefinition>> m_constraintsByName;
	
	/**
	 * Map tying a component name to the set of all constraints that use that
	 * component as a parameter.
	 */
	private Map<String, Set<ConstraintDefinition>> m_constraintsByComponent;
	
	/**
	 * The list of alias definitions (component/sub-part renames) defined for
	 * the shape.
	 */
	private List<AliasDefinition> m_aliasDefinitions;
	
	/**
	 * Attributes loaded from the shape definition
	 */
	private Map<String, String> m_attributeMap;
	
	
	/**
	 * Construct a shape definition with the name set to the empty string and
	 * the default value for isA. Obviously this doesn't do much good unless you
	 * follow up with {@link #setName(String)}.
	 * 
	 */
	public ShapeDefinition() {
		this("");
	}
	

	/**
	 * Construct a shape definition with the given name and the default value
	 * for isA.
	 * 
	 * @param name
	 *            The name of the shape, see {@link #setName(String)}.
	 */
	public ShapeDefinition(String name) {
		m_componentDefinitionNameMap = new TreeMap<String, ComponentDefinition>();
		m_componentDefinitions = new ArrayList<ComponentDefinition>();
		m_componentNameIndexMap = new HashMap<String, Integer>();
		
		m_componentsForShapeType = new HashMap<String, List<ComponentDefinition>>();
		
		m_constraintDefinitions = new ArrayList<ConstraintDefinition>();
		m_constraintsByName = new HashMap<String, Set<ConstraintDefinition>>();
		m_constraintsByComponent = new HashMap<String, Set<ConstraintDefinition>>();
		
		m_aliasDefinitions = new ArrayList<AliasDefinition>();
		
		m_attributeMap = new HashMap<String, String>();
		
		m_isASet = new TreeSet<String>();
	}
	

	/**
	 * Set the name of the shape defined here, cannot be set to null.
	 * 
	 * @param name
	 *            The name of the shape. If you pass in a null object, the name
	 *            of the shape is set to the empty string.
	 */
	public void setName(String name) {
		m_name = (name != null) ? name : "";
	}
	

	/**
	 * Get the name of the shape defined here.
	 * 
	 * @return The name of the shape.
	 */
	public String getName() {
		return m_name;
	}
	

	/**
	 * Set the description of this shape
	 * 
	 * @param desc
	 *            The description
	 */
	public void setDescription(String desc) {
		if (desc != null) {
			m_description = desc;
		}
		else {
			m_description = "";
		}
	}
	

	/**
	 * Get the description of this shape
	 * 
	 * @return the description of this shape
	 */
	public String getDescription() {
		return m_description;
	}
	

	/**
	 * Set the type of shape that this shape isA child of. This method will NOT
	 * add null or empty-string isA values. If you pass in these, it will return
	 * false.
	 * 
	 * @param isA
	 *            The type of shape this shape isA child of.
	 * @return True if the isA value was added (not null or trimmed length > 0),
	 *         false if not added
	 */
	public boolean addIsA(String isA) {
		boolean invalidIsA = (isA == null) || (isA.trim().length() == 0);
		if (!invalidIsA) {
			m_isASet.add(isA.trim());
		}
		
		return !invalidIsA;
	}
	

	/**
	 * Get the set of isA's for this shape
	 * 
	 * @return The isA set for this shape
	 */
	public SortedSet<String> getIsASet() {
		return m_isASet;
	}
	

	/**
	 * Return true if this shape has the given isA in its isASet. So, if this
	 * shape isA "Pollywog," and it has "Pollywog" in its isASet, then
	 * isA("Pollywog") will return true.
	 * 
	 * @param isA
	 *            The isA to see if this shape has in its set
	 * @return True if this shape has the isA in its isA set.
	 */
	public boolean isA(String isA) {
		return m_isASet.contains(isA);
	}
	

	/**
	 * Remove the isA from the set of isA's
	 * 
	 * @param isA
	 *            The isA to remove
	 * @return True if the remove succeeded, false otherwise
	 */
	public boolean removeIsA(String isA) {
		return m_isASet.remove(isA);
	}
	

	/**
	 * Get the flag indicating if this shape is abstract. Abstract shapes do not
	 * have any components or constraints
	 * 
	 * @return True if this shape is abstract, false if not abstract
	 */
	public boolean isAbstract() {
		return m_abstract;
	}
	

	/**
	 * Set if this shape is an abstract shape or not. Abstract shapes do not
	 * have any components or constraints, so if you're setting to true, then
	 * the lists of components and constraints are cleared.
	 * 
	 * @param abs
	 *            True if this shape should be abstract, false if not
	 * 
	 */
	public void setAbstract(boolean abs) {
		m_abstract = abs;
		// if setting to true, make sure we don't have any constraints or
		// subshapes
		if (m_componentDefinitions != null) {
			m_componentDefinitions.clear();
		}
		if (m_constraintDefinitions != null) {
			m_constraintDefinitions.clear();
		}
	}
	

	/**
	 * Get the file name of the file the shape definition was loaded from
	 * 
	 * @return file name
	 */
	public String getFilename() {
		return m_filename;
	}
	

	/**
	 * Set the name of the file that the shape definition was loaded from
	 * 
	 * @param filename
	 *            file name
	 */
	public void setFilename(String filename) {
		m_filename = filename;
	}
	

	/**
	 * Add the new component definition to this shape.
	 * 
	 * @param newCompDef
	 *            The component to add to this shape
	 * @return The component that was previously stored in this map, or just
	 *         null if there was no former {@link ComponentDefinition} component
	 *         with the same name
	 */
	public ComponentDefinition addComponentDefinition(
	        ComponentDefinition newCompDef) {
		// make sure we're not trying to insert into a null list or map
		if (m_componentDefinitionNameMap == null) {
			m_componentDefinitionNameMap = new TreeMap<String, ComponentDefinition>();
		}
		if (m_componentDefinitions == null) {
			m_componentDefinitions = new ArrayList<ComponentDefinition>();
		}
		if (m_componentNameIndexMap == null) {
			m_componentNameIndexMap = new HashMap<String, Integer>();
		}
		
		// add to the list
		m_componentDefinitions.add(newCompDef);
		
		// which index in the list is it?
		m_componentNameIndexMap.put(newCompDef.getName(),
		        m_componentDefinitions.size() - 1);
		
		// put the new component definition into the map of components, keyed
		// by name. Prev val is not null if there was a component with the same
		// name and just got replaced.
		ComponentDefinition prevVal = m_componentDefinitionNameMap.put(
		        newCompDef.getName(), newCompDef);
		
		// bookeeping to register the shape type of this component with our
		// map to get components based on shape type
		String shapeType = newCompDef.getShapeType();
		// set of components that have the same shape type as the new component
		List<ComponentDefinition> sameShapeComps = m_componentsForShapeType
		        .get(shapeType);
		// if there are no components with the same shape type, create a new set
		if (sameShapeComps == null) {
			sameShapeComps = new ArrayList<ComponentDefinition>();
			// put the new, empty set into the hash
			m_componentsForShapeType.put(shapeType, sameShapeComps);
		}
		// add the new component into the set of components with the same shape
		// type
		sameShapeComps.add(newCompDef);
		
		return prevVal;
	}
	

	/**
	 * Get the component definition for the component with the given name, or
	 * null if there is no such component
	 * 
	 * @param compName
	 *            The name of the component to get
	 * @return The component with the given name, or null if there is no such
	 *         component
	 */
	public ComponentDefinition getComponentDefinition(String compName) {
		if (m_componentDefinitions == null) {
			return null;
		}
		return m_componentDefinitionNameMap.get(compName);
	}
	

	/**
	 * Get the index of the component in the component list (
	 * {@link #getComponentDefinitions()}) using the component's name. If a
	 * component with the name does not exist, return -1.
	 * 
	 * @param name
	 *            Name of a component
	 * @return Index into {@link #getComponentDefinitions()} this component
	 *         appears, useful for mapping another array of objects 1:1 with the
	 *         list of components. Return -1 if there's not a component with
	 *         that name.
	 */
	public int getComponentIndex(String name) {
		int idx = -1;
		
		Integer idxInteger = m_componentNameIndexMap.get(name);
		if (idxInteger != null) {
			idx = idxInteger.intValue();
		}
		
		return idx;
	}
	

	/**
	 * Get a collection of all the components of this shape. Don't modify this
	 * collection, or undefined behavio in the ShapeDefinition instance may
	 * occur.
	 * 
	 * @return A collection of all the components in this shape
	 * @see Map#values()
	 */
	public List<ComponentDefinition> getComponentDefinitions() {
		return m_componentDefinitions;
	}
	

	/**
	 * Get the set of components defined in this shape that have the given shape
	 * type. For example, if this shape is a 'Rectangle', it will have four
	 * components that are of shape type 'Line' (
	 * {@link ComponentDefinition#getShapeType()}. If you call this method with
	 * shapeType = 'Line', you will get a set of four component definitions
	 * back, one component for each line. This assumes, however, that all the
	 * components are uniquely named. If you search your 'Rectangle' for
	 * components with shape type = 'Circle', you will get a null set back,
	 * since there are no 'Circle' components in a simple 'Rectangle'
	 * 
	 * @param shapeType
	 *            The shape type you want to search the components for
	 * @return The set of components that are of the given shape type.
	 */
	public List<ComponentDefinition> getComponentsOfShapeType(String shapeType) {
		return m_componentsForShapeType.get(shapeType);
	}
	

	/**
	 * Get the number of components in this shape definition
	 * 
	 * @return The number of component definitions
	 */
	public int getNumComponents() {
		return m_componentDefinitions.size();
	}
	

	/**
	 * Gets the list of all the different shape types of all the components in
	 * this shape. So, if this is a 'Rectangle' shape, it will return a set with
	 * only one element: 'Line'. This is because 'Rectangles' are composed of
	 * only 'Line' components. If you have a more complex shape, you will get
	 * more shape types in the returned set. Remember, any changes made to this
	 * set via iterator methods will affect the map in this class.
	 * 
	 * @return The set of unique shape types (Strings) found in the components
	 *         of this shape
	 */
	public Set<String> getShapeTypesOfComponents() {
		return m_componentsForShapeType.keySet();
	}
	

	/**
	 * Get the list of constraints defined on the components of this shape
	 * 
	 * @return The list of constraints
	 */
	public List<ConstraintDefinition> getConstraintDefinitions() {
		return m_constraintDefinitions;
	}
	

	/**
	 * Add a constraint definition to the list of constraints for this shape
	 * 
	 * @param constDef
	 *            The constraint definition for this shape
	 */
	public void addConstraintDefinition(ConstraintDefinition constDef) {
		if (m_constraintDefinitions == null) {
			m_constraintDefinitions = new ArrayList<ConstraintDefinition>();
		}
		m_constraintDefinitions.add(constDef);
		
		// add the constraint to the map of constraint names
		String constraintName = constDef.getName();
		Set<ConstraintDefinition> sameNameConstraints = m_constraintsByName
		        .get(constraintName);
		// if the map is null, there are no other constraints by the same name.
		if (sameNameConstraints == null) {
			sameNameConstraints = new TreeSet<ConstraintDefinition>();
			m_constraintsByName.put(constDef.getName(), sameNameConstraints);
		}
		// add the new constraint to the set of constraints with the same name
		sameNameConstraints.add(constDef);
		
		// this constraint affects a subset of components in this shape. Cache
		// which constraints affect each component for quick lookup.
		for (ConstraintParameter param : constDef.getParameters()) {
			String component = param.getComponent();
			
			// the set of constraints this component is involved with
			
			Set<ConstraintDefinition> constraints = m_constraintsByComponent
			        .get(component);
			
			// if null, there are no existing constraints
			if (constraints == null) {
				// create new set for this component and put into map
				constraints = new TreeSet<ConstraintDefinition>();
				m_constraintsByComponent.put(component, constraints);
			}
			// add this constraint to the set for this component.
			constraints.add(constDef);
		}
	}
	

	/**
	 * Get the set of constraints that affects this component. May return null
	 * if there are no constraints for the given component.
	 * 
	 * @param componentName
	 *            The name of the component, case sensitive, that you want to
	 *            get the constraints for
	 * @return The set of constraints affecting this component.
	 */
	public Set<ConstraintDefinition> getConstraintsForComponent(
	        String componentName) {
		return m_constraintsByComponent.get(componentName);
	}
	

	/**
	 * Get the list of aliases defined on the components/sub-parts of this shape
	 * 
	 * @return The list of aliases
	 */
	public List<AliasDefinition> getAliasDefinitions() {
		return m_aliasDefinitions;
	}
	

	/**
	 * Add an alias definition to the list of aliases for this shape
	 * 
	 * @param aliasDef
	 *            The alias definition for this shape
	 */
	public void addAliasDefinition(AliasDefinition aliasDef) {
		if (m_aliasDefinitions == null) {
			m_aliasDefinitions = new ArrayList<AliasDefinition>();
		}
		m_aliasDefinitions.add(aliasDef);
	}
	

	/**
	 * This compareTo method compares based on the shapeDefinition names. This
	 * means all shape definitions in your domain must have unique names, or
	 * there will be collisions. Case sensitive.
	 * 
	 * @param other
	 *            The other shape definition to compare to
	 * @return negative if this < other, 0 if ==, 1 if this > other
	 * 
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(ShapeDefinition other) {
		// compare by name. Shapes must have unique names within the domain.
		return this.getName().compareTo(other.getName());
	}
	

	public String toString() {
		return m_name;
	}
	

	/**
	 * Add an attribute with the given key and given value.
	 * 
	 * @param key
	 *            The key for the attr
	 * @param value
	 *            The value for the attr
	 */
	public void addAttribute(String key, String value) {
		m_attributeMap.put(key, value);
	}
	

	/**
	 * Get an attribute value with the given key
	 * 
	 * @param key
	 *            Key
	 * @return Value for the key
	 */
	public String getAttribute(String key) {
		return m_attributeMap.get(key);
	}
	

	/**
	 * Get the set of all keys from the attribute map
	 * 
	 * @return The set of all keys from the map
	 */
	public Set<String> getAttributeKeys() {
		return m_attributeMap.keySet();
	}
	

	/**
	 * Get the map of attributes
	 * 
	 * @return The map of attributes
	 */
	public Map<String, String> getAttributeMap() {
		return m_attributeMap;
	}
	

	/**
	 * Get all the constraints in the shape definition with the given constraint
	 * name. Case sensitive.
	 * 
	 * @param name
	 *            Name of the constraint (eg Horizontal, ContainsText, etc)
	 * @return Set of constraint definitions defined for this shape definition
	 *         that use the contraint with the given name
	 */
	public Set<ConstraintDefinition> getConstraintsByConstraintName(String name) {
		return m_constraintsByName.get(name);
	}
}
