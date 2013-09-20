/**
 * ComponentDefinition.java
 * 
 * Revision History:<br>
 * Aug 18, 2008 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 *    This work is released under the BSD License:
 *    (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 *    All rights reserved.
 *    
 *    Redistribution and use in source and binary forms, with or without
 *    modification, are permitted provided that the following conditions are met:
 *        * Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 *        * Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 *        * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *          nor the names of its contributors may be used to endorse or promote 
 *          products derived from this software without specific prior written 
 *          permission.
 *    
 *    THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *    DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 *    DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package org.ladder.recognition.constraint.domains;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class holds information about one component of a shape definition. A
 * component is one "slot" in the shape definition. Each of the four lines in a
 * rectangle would get a component definition.
 * <p>
 * CompareTo works based on the name, so all components defined withing a shape
 * must have a unique name or there will be collisions.
 * 
 * @author jbjohns
 */
public class ComponentDefinition implements Comparable<ComponentDefinition> {
	
	/**
	 * The name assigned to this component, like 'line1'
	 */
	private String m_name;
	
	/**
	 * The shape type for this component, like 'Line'
	 */
	private String m_shapeType;
	
	/**
	 * Place to put optional arguments.
	 */
	private Map<String, String> m_argumentMap;
	
	/**
	 * Parent component (null if no parent exist)
	 */
	private ComponentDefinition m_parent = null;
	
	/**
	 * Children components (mapped by name)
	 */
	private Map<String, ComponentDefinition> m_children = new HashMap<String, ComponentDefinition>();
	
	
	/**
	 * Construct a component definition with the empty string for name and empty
	 * string for shape type. Obviously this does not do you any good.
	 * 
	 */
	public ComponentDefinition() {
		this("", "");
	}
	

	/**
	 * Construct a component definition with the given name and shape type.
	 * 
	 * @param name
	 *            The name to use for this component
	 * @param shapeType
	 *            The type of shape to use for this component
	 */
	public ComponentDefinition(String name, String shapeType) {
		setName(name);
		setShapeType(shapeType);
		m_argumentMap = new HashMap<String, String>();
	}
	

	/**
	 * Add a child to this component
	 * 
	 * @param child
	 *            child component to add
	 */
	public void addChild(ComponentDefinition child) {
		if (!containsChild(child))
			m_children.put(child.getName(), child);
	}
	

	/**
	 * This compareTo call compares the names of component definitions.
	 * Therefore, all components defined in your shape must have unique names.
	 * Case sensitive.
	 * 
	 * @param o
	 *            The other component to compare to
	 * @return -1 if this < other, 0 if ==, 1 if this > other
	 * 
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(ComponentDefinition o) {
		return this.getName().compareTo(o.getName());
	}
	

	/**
	 * Determine if the component contains a given child component
	 * 
	 * @param child
	 *            child component to look for
	 * @return true if child is found; else false
	 */
	public boolean containsChild(ComponentDefinition child) {
		ComponentDefinition found = getChild(child.getName());
		if (found != null)
			return true;
		return false;
	}
	

	/**
	 * Get the map of arguments
	 * 
	 * @return The map of arguments
	 */
	public Map<String, String> getArgumentMap() {
		return m_argumentMap;
	}
	

	/**
	 * Get the value of the given argument
	 * 
	 * @param arg
	 *            The argument to get the value of
	 * @return The value of the argument stored in the map, or null if there is
	 *         no value
	 * @see Map#get(Object)
	 */
	public String getArgumentValue(String arg) {
		return m_argumentMap.get(arg);
	}
	

	/**
	 * Get the child component that has the given name
	 * 
	 * @param name
	 *            name (ComponentDefinition.getName()) of the child component to
	 *            retrieve
	 * @return child component
	 */
	public ComponentDefinition getChild(String name) {
		return m_children.get(name);
	}
	

	/**
	 * Get the children components of this component
	 * 
	 * @return children component definitions
	 */
	public Collection<ComponentDefinition> getChildren() {
		return m_children.values();
	}
	

	/**
	 * Get this component's name
	 * 
	 * @return the name of this component, like 'line1'
	 */
	public String getName() {
		return m_name;
	}
	

	/**
	 * Get the parent component of this component
	 * 
	 * @return parent component definition
	 */
	public ComponentDefinition getParent() {
		return m_parent;
	}
	

	/**
	 * Get the shape type of this component
	 * 
	 * @return The shape type of this component, like 'Line'
	 */
	public String getShapeType() {
		return m_shapeType;
	}
	

	/**
	 * Put an argument and its associated value into the argument map.
	 * 
	 * @param arg
	 *            The argument to put into the map
	 * @param value
	 *            The value of the argument
	 * @return The value that was previously in the map for the arg key.
	 * @see Map#put(Object, Object)
	 */
	public String setArgument(String arg, String value) {
		return m_argumentMap.put(arg, value);
	}
	

	/**
	 * Set the map of arguments. If you try to set a null object, will
	 * initialize to an empty map of arguments
	 * 
	 * @param argumentMap
	 *            The map of arguments
	 */
	public void setArgumentMap(Map<String, String> argumentMap) {
		if (argumentMap != null) {
			m_argumentMap = argumentMap;
		}
		else {
			m_argumentMap = new HashMap<String, String>();
		}
	}
	

	/**
	 * Set the name of this component. If you pass in null object, will set the
	 * name to the empty string
	 * 
	 * @param name
	 *            The name of the component, like 'line1'
	 */
	public void setName(String name) {
		m_name = (name != null) ? name : "";
	}
	

	/**
	 * Set the parent of this component
	 * 
	 * @param parent
	 *            parent of this component
	 */
	public void setParent(ComponentDefinition parent) {
		m_parent = parent;
	}
	

	/**
	 * Set the shape type of this component. If you pass in a null object, will
	 * set the shape type to the empty string
	 * 
	 * @param shapeType
	 *            The shape type of this component, like 'Line'
	 */
	public void setShapeType(String shapeType) {
		m_shapeType = (shapeType != null) ? shapeType : "";
	}
	
	public String toString() {
		return super.toString() + " " + this.getName() + ": " + this.getShapeType();
	}
}
