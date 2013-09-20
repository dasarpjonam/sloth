/**
 * ShapePool.java
 * 
 * Revision History:<br>
 * Aug 21, 2008 srl - File created
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
package org.ladder.recognition.constraint.filters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.recognition.constraint.ConstraintFactory;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;

/**
 * This class holds the shape pool, and presents several intuitive, natural
 * feeling views incorporating context and streamlining usability and
 * profitability with the concern for interoperability and the user's
 * well-being.
 * <p>
 * Holds a bunch of different maps so we can get different subsets of shapes
 * quickly.
 * 
 * @author srl
 */
public class ShapePool {
	
	private static Logger log = LadderLogger.getLogger(ShapePool.class);
	
	/**
	 * All the shapes that we have right now. This is the UNION of all the
	 * filtered sets
	 */
	private SortedSet<IShape> m_masterPool;
	
	// private List<IShape> m_masterPool;
	
	/**
	 * Map from constraint name to the shape pools for that constraint. This
	 * maps to a set because we can multiple pools for different constraints,
	 * which vary by the thresholds set for those constraints. This allows us to
	 * use customizable thresholds.
	 */
	private Map<String, SortedSet<UnaryConstraintFilter>> m_constraintPools;
	
	/**
	 * Map from shape type to the shape pools for that shape type. This can be
	 * used to get all the shapes of a particular type.
	 */
	private Map<String, ShapeTypeFilter> m_shapeTypePools;
	
	/**
	 * The domain definition that the shape pool will hold shapes for
	 */
	private DomainDefinition m_domainDefinition;
	
	
	/**
	 * Cannot use the default constructor outside of this class
	 */
	private ShapePool() {
		// m_masterPool = Collections.synchronizedSortedSet(new
		// TreeSet<IShape>());
		m_masterPool = new TreeSet<IShape>();
		
		// m_shapeTypePools = Collections
		// .synchronizedMap(new HashMap<String, ShapeTypeFilter>());
		m_shapeTypePools = new HashMap<String, ShapeTypeFilter>();
		
		// m_constraintPools = Collections
		// .synchronizedMap(new HashMap<String,
		// SortedSet<UnaryConstraintFilter>>());
		m_constraintPools = new HashMap<String, SortedSet<UnaryConstraintFilter>>();
	}
	

	/**
	 * Create a shape pool that holds and filters shapes for the given domain
	 * 
	 * @param domain
	 *            The domain to hold and filter shapes for
	 */
	public ShapePool(DomainDefinition domain) {
		this();
		
		if (domain != null) {
			setDomainDefinition(domain);
			
			for (ShapeDefinition shapeDef : domain.getShapeDefinitions()) {
				
				// create filters for constraints
				for (ConstraintDefinition constraintDef : shapeDef
				        .getConstraintDefinitions()) {
					// we're only filtering based on unary constraints. We build
					// a set of pools based on constraint names and the
					// thresholds for those constraints.
					if (constraintDef.getCardinality() == 1) {
						
						// name of this constraint
						String constraintName = constraintDef.getName();
						
						// get the IConstraint object for the given name. will
						// be null if the name is not recognized by the factory.
						// if so, just skip the constraint, since we have no
						// way of telling what the user wants.
						IConstraint constraintObj = null;
						try {
							constraintObj = ConstraintFactory
							        .getConstraint(constraintDef);
						}
						catch (InstantiationException e) {
							log.error("Cannot instantiate constraint for def: "
							          + constraintDef);
							log.error(e);
						}
						catch (IllegalAccessException e) {
							log.error("Cannot instantiate constraint for def: "
							          + constraintDef);
							log.error(e);
						}
						catch (ClassNotFoundException e) {
							log.error("Cannot instantiate constraint for def: "
							          + constraintDef);
							log.error(e);
						}
						if (constraintObj == null) {
							continue;
						}
						
						// get the set of filtered pools for this constraint
						// name
						SortedSet<UnaryConstraintFilter> poolSet = m_constraintPools
						        .get(constraintName);
						// if pool set null, no constraints so far
						if (poolSet == null) {
							// poolSet = Collections
							// .synchronizedSortedSet(new
							// TreeSet<UnaryConstraintFilter>());
							poolSet = new TreeSet<UnaryConstraintFilter>();
							m_constraintPools.put(constraintName, poolSet);
						}
						
						// does this pool set have the given constraint with the
						// given threshold? If not, add it
						
						// pools are compared based on the constraints they
						// contain, so this empty target pool should match a
						// current pool if something uses the same constraint
						// and threshold
						UnaryConstraintFilter targetPool = new UnaryConstraintFilter(
						        constraintObj);
						// a pool with the constraint/threshold is not in the
						// set of pools for this constraint, so add it in
						if (!poolSet.contains(targetPool)) {
							poolSet.add(targetPool);
						}
						// else pool for constraint with name/threshold exists,
						// no need to add another one
					}
				} // end looping over constraints
				
				// create filters for components. Loop over each component in
				// the shape
				for (ComponentDefinition compDef : shapeDef
				        .getComponentDefinitions()) {
					// the shape type of this component
					String componentShapeType = compDef.getShapeType();
					
					// filter by shape component type
					ShapeTypeFilter typeFilter = m_shapeTypePools
					        .get(componentShapeType);
					if (typeFilter == null) {
						typeFilter = new ShapeTypeFilter(componentShapeType);
						m_shapeTypePools.put(componentShapeType, typeFilter);
					}
				}
				
			}
		}
		else {
			throw new NullPointerException("Domain cannot be null in ShapePool");
		}
	}
	

	/**
	 * Add the given shape to the pool of shapes and filters as appropriate.
	 * Needs to have its {@link IShape#getLabel()} set if you want any shape
	 * type filters to work
	 * 
	 * @param shape
	 *            The shape to add to the pool and filter as applicable.
	 */
	public void addShape(IShape shape) {
		m_masterPool.add(shape);
		
		// Loop over the unary constraint pools
		// for each set of constraint pools (groups of pools based on constraint
		// name)
		// synchronized (m_constraintPools) {
		for (Set<UnaryConstraintFilter> poolSet : m_constraintPools.values()) {
			// for each pool in the set (based on different threshold values
			// for solving the constraint)
			// synchronized (poolSet) {
			for (Iterator<UnaryConstraintFilter> poolIter = poolSet.iterator(); poolIter
			        .hasNext();) {
				poolIter.next().addShape(shape);
			}
			// }
		}
		// }
		
		// Loop over shape type pools and add
		// synchronized (m_shapeTypePools) {
		for (ShapeTypeFilter typeFilter : m_shapeTypePools.values()) {
			typeFilter.addShape(shape);
		}
		// }
	}
	

	/**
	 * Remove the shape from the pool and all the filters
	 * 
	 * @param shape
	 *            The shape to remove
	 */
	public void removeShape(IShape shape) {
		m_masterPool.remove(shape);
		
		// unary constraint filters
		// synchronized (m_constraintPools) {
		for (Set<UnaryConstraintFilter> poolSet : m_constraintPools.values()) {
			// synchronized (poolSet) {
			for (Iterator<UnaryConstraintFilter> poolIter = poolSet.iterator(); poolIter
			        .hasNext();) {
				poolIter.next().removeShape(shape);
			}
			// }
		}
		// }
		
		// type filters
		// synchronized (m_shapeTypePools) {
		for (ShapeTypeFilter typeFilter : m_shapeTypePools.values()) {
			typeFilter.removeShape(shape);
		}
		// }
	}
	

	/**
	 * Clear all the filters and the master pool.
	 */
	public void clear() {
		m_masterPool.clear();
		
		// unary constraint filters
		for (Set<UnaryConstraintFilter> poolSet : m_constraintPools.values()) {
			for (Iterator<UnaryConstraintFilter> poolIter = poolSet.iterator(); poolIter
			        .hasNext();) {
				poolIter.next().clear();
			}
		}
		
		// type filters
		for (ShapeTypeFilter typeFilter : m_shapeTypePools.values()) {
			typeFilter.clear();
		}
	}
	

	/**
	 * Get all the shapes in the master pool. This method returns a copy of the
	 * list, so that any changes you make to the list that's returned ARE NOT
	 * reflected in this pool.
	 * 
	 * @return The list of shapes in the master pool
	 */
	public SortedSet<IShape> getAllShapes() {
		SortedSet<IShape> ret = new TreeSet<IShape>();
		// synchronized (m_masterPool) {
		ret.addAll(m_masterPool);
		// }
		
		return ret;
	}
	

	/**
	 * Get all the shapes in the pool that have the given shape type.
	 * 
	 * @param shapeType
	 *            The shape type we want to get the shapes for
	 * @return Set of shapes that have the given shape type. If the shape type
	 *         is not recognized (not in the domain) will return an empty list.
	 */
	public SortedSet<IShape> getShapesByShapeType(String shapeType) {
		// get the filter for this shape type
		ShapeTypeFilter typeFilter = m_shapeTypePools.get(shapeType);
		
		// if the type filter is null, this is an invalid shape type and we'll
		// return empty set
		
		SortedSet<IShape> shapes = new TreeSet<IShape>();
		
		if (typeFilter != null) {
			shapes = typeFilter.getShapes();
		}
		
		// if (shapeSet.size() == 0) {
		// for (IShape s : m_masterPool) {
		// if (s.getLabel().equalsIgnoreCase(shapeType)) {
		// shapeSet.add(s);
		// // System.out.println("+1");
		// }
		// }
		// }
		
		return shapes;
	}
	

	/**
	 * Get the shapes that have passed the given constraint. This method only
	 * works if the shape pool has been set up to filter shapes using this
	 * constraint.
	 * 
	 * @param constraint
	 *            We want the set of shapes that has passed this constraint.
	 * @return The set of shapes that pass this filter. If the constraint has
	 *         not been used to filter shapes, or no shapes pass this
	 *         constraint, will return an empty list.
	 */
	public SortedSet<IShape> getShapesByConstraint(IConstraint constraint) {
		// empty set in case there is no filter for this constraint
		SortedSet<IShape> filteredShapes = new TreeSet<IShape>();
		
		// all the different filters for this type of constraint by name (ie
		// horizontal)
		SortedSet<UnaryConstraintFilter> constraintFilters = m_constraintPools
		        .get(constraint.getName());
		
		if (constraintFilters != null) {
			// get the filter for this constraint, which depends on the
			// threshold set for the constraint (ie horizontal, with threshold
			// == 29).
			UnaryConstraintFilter targetFilter = new UnaryConstraintFilter(
			        constraint);
			// tailSet gets all elements >= targetFilter. And first() gets the
			// least element of that tail set. So, if target filter is in the
			// constraintFilters set, this should be the filter we want.
			// Otherwise, the returned value will not be equal. it might also
			// be the case that, if the targetFilter is greater than all the
			// elements in the set, we'll get an illegal argument exception.
			UnaryConstraintFilter filter = null;
			try {
				filter = constraintFilters.tailSet(targetFilter).first();
			}
			catch (IllegalArgumentException e) {
				filter = null;
			}
			
			if (filter != null && filter.equals(targetFilter)) {
				filteredShapes = filter.getShapes();
			}
		}
		
		return filteredShapes;
	}
	

	/**
	 * The number of shapes, total, in the pool.
	 * 
	 * @return The number of shapes in the pool
	 */
	public int size() {
		return m_masterPool.size();
	}
	

	public int numSubShapesComplexity(){
		int count = 0;
		for(IShape shape : m_masterPool){
			if(shape.getLabel().startsWith("Line") || shape.getLabel().startsWith("Dot")){
				count++;				
			} else {
				count += 4;
			}
		}
		return count;
	}
	
	
	/**
	 * @param domainDefinition
	 *            the domainDefinition to set
	 */
	public void setDomainDefinition(DomainDefinition domainDefinition) {
		m_domainDefinition = domainDefinition;
	}
	

	/**
	 * @return the domainDefinition
	 */
	public DomainDefinition getDomainDefinition() {
		return m_domainDefinition;
	}
}
