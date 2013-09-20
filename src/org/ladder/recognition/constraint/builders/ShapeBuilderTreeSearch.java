/**
 * ShapeBuilderTreeSearch.java
 * 
 * Revision History:<br>
 * Mar 16, 2009 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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
package org.ladder.recognition.constraint.builders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IsAConstants;
import org.ladder.datastructures.CountMap;
import org.ladder.datastructures.IgnoreCaseStringSet;
import org.ladder.recognition.DebugShapeSet;
import org.ladder.recognition.constraint.ConstraintFactory;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.confidence.AndConstraint;
import org.ladder.recognition.constraint.constrainable.ConstrainableFactory;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.ConstraintParameter;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;

/**
 * This class is not thread safe and should be externally synchronized.
 * 
 * @author jbjohns
 */
public class ShapeBuilderTreeSearch implements IShapeBuilder {
	
	/**
	 * Logger for this class
	 */
	private static final Logger log = LadderLogger
	        .getLogger(ShapeBuilderTreeSearch.class);
	
	/**
	 * The max time to use if you want to build without time constraints.
	 */
	private static final int S_UNTIMED_MAX_TIME = -1;
	
	/**
	 * The time that recognition started.
	 */
	private long m_startTime;
	
	/**
	 * The maximum time that we have alloted for recognition.
	 */
	private long m_maxTime;
	
	/**
	 * Set of shapes we want debug information for.
	 */
	private DebugShapeSet m_debugShapeSet;
	
	
	/**
	 * Default constructor.
	 */
	public ShapeBuilderTreeSearch() {
		this(new DebugShapeSet());
	}
	

	/**
	 * Construct the builder to use the given set of debug shapes. Defaults to
	 * no max build time.
	 * 
	 * @param debugShapes
	 *            The set of debug shapes. If you provide null, defaults to an
	 *            empty set.
	 */
	public ShapeBuilderTreeSearch(DebugShapeSet debugShapes) {
		setDebugShapeSet(debugShapes);
	}
	

	/**
	 * @return the debugShapeSet
	 */
	public DebugShapeSet getDebugShapeSet() {
		return this.m_debugShapeSet;
	}
	

	/**
	 * @param debugShapeSet
	 *            the debugShapeSet to set
	 */
	public void setDebugShapeSet(DebugShapeSet debugShapeSet) {
		if (debugShapeSet != null) {
			m_debugShapeSet = debugShapeSet;
		}
		else {
			m_debugShapeSet = new DebugShapeSet();
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.constraint.builders.IShapeBuilder#buildShape(java
	 * .util.List, org.ladder.recognition.constraint.domains.ShapeDefinition)
	 */
	@Override
	public BuiltShape buildShape(List<IShape> shapePool,
	        ShapeDefinition shapeDef) throws ShapeBuildFailureException {
		
		BuiltShape builtShape = null;
		try {
			builtShape = buildShapeTimed(shapePool, shapeDef,
			        S_UNTIMED_MAX_TIME);
		}
		catch (OverTimeException e) {
			// ignore, should not happen
		}
		return builtShape;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.constraint.builders.IShapeBuilder#buildShapeTimed
	 * (java.util.List,
	 * org.ladder.recognition.constraint.domains.ShapeDefinition, long)
	 */
	@Override
	public BuiltShape buildShapeTimed(List<IShape> shapePool,
	        ShapeDefinition shapeDef, long maxTime)
	        throws ShapeBuildFailureException, OverTimeException {
		
		m_maxTime = maxTime;
		m_startTime = System.currentTimeMillis();
		OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
		
		BuiltShape builtShape = null;
		
		// clone the pool so we're not messing up anyone else's list.
		List<IShape> clonedPool = new ArrayList<IShape>(shapePool);
		// if this is a debug shape, log.info all the shapes in the low level
		// pool
		if (log.isDebugEnabled() && m_debugShapeSet.isDebugShape(shapeDef)) {
			StringBuilder poolString = new StringBuilder();
			for (IShape s : clonedPool) {
				poolString.append(s.getLabel());
				if (s.getLabel().equalsIgnoreCase("Text")) {
					poolString.append('\'').append(s.getAttribute("TEXT_BEST"))
					        .append('\'');
				}
				poolString.append(' ');
			}
			log.debug("Building " + shapeDef.getName()
			          + ", Shape Builder has [[" + poolString.toString()
			          + "]] in the pool");
		}
		
		// remove any shapes from the pool that aren't needed by this shape
		// definition
		pruneShapePool(clonedPool, shapeDef);
		
		// see if we can build the shape with what's not been pruned from the
		// pool.
		builtShape = buildShapeTreeSearch(clonedPool, shapeDef);
		putShapeDefinitionIntoBuiltShape(shapeDef, builtShape);
		
		return builtShape;
	}
	

	/**
	 * This method removes any shapes that we don't need from the pool. It looks
	 * at the shape type and any isA attributes on the shapes, removing those
	 * that aren't present as shape types in the list of the shape definition's
	 * components.
	 * <p>
	 * At the same time, this method counts the number of each shape type needed
	 * by the shape definition (eg a rectangle takes 4 'line' shapes) and the
	 * number of each type of shape in the pool (eg we have 5 'line' shapes, 1
	 * 'ellipse', and a 'cat'). We then see if the pool has enough shapes to
	 * satisfy the shape definition (eg there are 5 'line' shapes and the def.
	 * needs only 4, so we have enough, and we prune the pool of the 'ellipse'
	 * and 'cat' since they're not needed to build the 'rectangle').
	 * <p>
	 * If there are not enough shapes in the pool (only 3 shapes when the shape
	 * being built requires 4) or not enough shapes of each type (only 3 'line'
	 * shapes when we require 4), this method will throw the appropriate
	 * {@link ShapeBuildFailureException}
	 * 
	 * @param pool
	 *            Pool of shapes to remove things from
	 * @param shapeDef
	 *            Shape definition we're trying to build.
	 * @throws ShapeBuildFailureException
	 *             If it's clear from the required shapes in the shape
	 *             definition and the available shapes in the shape pool, that
	 *             this shape cannot be built.
	 * @throws OverTimeException
	 *             If recognition goes over time in this method
	 */
	private void pruneShapePool(List<IShape> pool, ShapeDefinition shapeDef)
	        throws ShapeBuildFailureException, OverTimeException {
		
		if (pool == null) {
			throw new NullPointerException("Null pool of shapes");
		}
		if (shapeDef == null) {
			throw new NullPointerException("Shape definition is null");
		}
		
		// does the pool even have enough components to satisfy the shape def?
		checkSizeOfPool(pool, shapeDef);
		
		// We store this result so we don't have to construct again. We wrap
		// the set in ShapeDefinition in a SortedSet so we can be sure of
		// O(log n) access in case of a large shape definition with many
		// components. Also, ignore the case of the shape types.
		// 
		// the types of shapes that this shape def is built up from
		IgnoreCaseStringSet componentTypesInShapeDef = new IgnoreCaseStringSet();
		componentTypesInShapeDef.addAll(shapeDef.getShapeTypesOfComponents());
		
		// number of each type of thing in the pool
		CountMap<String> poolCounts = new CountMap<String>();
		// number of each type of thing in the shape def
		CountMap<String> definitionCounts = new CountMap<String>();
		
		// count the things in the shape definition
		for (String type : componentTypesInShapeDef) {
			int numOfType = shapeDef.getComponentsOfShapeType(type).size();
			definitionCounts.setCount(type.toLowerCase(), numOfType);
		}
		
		// ///////////
		// For this loop we don't use shapeHasComponentsType(...) because
		// we have to count things, as well.
		// ///////////
		
		// count the things in the pool, and also prune the pool at the same
		// time
		for (Iterator<IShape> poolIter = pool.iterator(); poolIter.hasNext(); /*
																			 * iter
																			 * next
																			 */) {
			OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
			
			IShape shape = poolIter.next();
			
			// shape's label is a type in the shape def?
			String label = shape.getLabel();
			boolean labelIsNeededType = false;
			if (label != null) {
				label = label.toLowerCase();
				labelIsNeededType = definitionCounts.containsKey(label);
				
				poolCounts.increment(label.toLowerCase());
			}
			
			// check for each isAConstant in the shape's attributes and see if
			// it's in the component type set, and count each one that we have.
			boolean isAIsNeededType = false;
			for (Iterator<String> isAConstIter = IsAConstants.getConstantsSet()
			        .iterator(); isAConstIter.hasNext() && !isAIsNeededType;) {
				String isA = isAConstIter.next();
				// shape has this isA as an attribute?
				boolean hasIsA = shape.hasAttribute(isA);
				
				// this is needed if the shape has the attribute AND their is
				// a component of this type
				// The OR here ensures that if one isAConstant is needed, this
				// flag will stay true once it's set to true for this shape.
				//
				// The set of component types in the shape def ignores case of
				// the isA
				isAIsNeededType = (isAIsNeededType)
				                  || ((hasIsA) && (componentTypesInShapeDef
				                          .contains(isA)));
				
				// we have one shape of this isA type
				poolCounts.increment(isA.toLowerCase());
			}
			
			// we might need this shape if it either has a label or an isA that
			// appears as a component in the shape definition. Otherwise, we can
			// remove the shape from the pool
			if (!labelIsNeededType && !isAIsNeededType) {
				if (log.isDebugEnabled()
				    && m_debugShapeSet.isDebugShape(shapeDef)) {
					log.debug(shapeDef.getName() + " does not need a "
					          + shape.getLabel()
					          + ", removing from candidate shapes");
				}
				poolIter.remove();
			}
		}
		
		// after pruning, are there still enough things in the pool?
		checkSizeOfPool(pool, shapeDef);
		
		// are there enough things in the pool to satisfy the components in the
		// shape definition? Note that this count might not be exactly accurate,
		// if the label and isA are both counted but can't both be used because
		// they're on one shape
		Set<String> definitionShapes = definitionCounts.keySet();
		for (String definitionShape : definitionShapes) {
			int definitionCount = definitionCounts.getCount(definitionShape);
			int poolCount = poolCounts.getCount(definitionShape);
			
			if (poolCount < definitionCount) {
				String failureMessage = "Not enough " + definitionShape
				                        + " (need " + definitionCount
				                        + " but have " + poolCount + ")";
				if (log.isDebugEnabled()
				    && m_debugShapeSet.isDebugShape(shapeDef)) {
					log.debug(failureMessage);
				}
				throw new ShapeBuildFailureException.Builder().withMessage(
				        failureMessage).withShapeName(shapeDef.getName())
				        .withReason(ShapeBuildFailureReason.MissingComponent)
				        .build();
			}
		}
	}
	

	/**
	 * See if there are enough objects in the pool to satisfy the number of
	 * components in the shape type. This doesn't check that the right things
	 * are in the pool, only that there are enough (if the shape def has 4
	 * components, regardless of their type, then the pool must have at least 4
	 * things in it). If the pool is too small, throw a
	 * {@link ShapeBuildFailureException}.
	 * <p>
	 * If the pool or the shape definition is null, throw a
	 * {@link NullPointerException}
	 * 
	 * @param pool
	 *            Pool of shapes to examine
	 * @param shapeDef
	 *            Shape definition to examine
	 * @return True if there are enough things in the pool. This method will
	 *         never return false because if there aren't enough things, it
	 *         throws a {@link ShapeBuildFailureException}.
	 * @throws ShapeBuildFailureException
	 *             If there are not enough things in the pool
	 * @throw {@link NullPointerException} if the pool or shapeDef is null
	 */
	private boolean checkSizeOfPool(List<IShape> pool, ShapeDefinition shapeDef)
	        throws ShapeBuildFailureException {
		if (pool == null) {
			throw new NullPointerException("Pool cannot be null");
		}
		if (shapeDef == null) {
			throw new NullPointerException("Shape definition cannot be null");
		}
		
		int numPool = pool.size();
		int numShapeDef = shapeDef.getNumComponents();
		if (numPool < numShapeDef) {
			String failureMessage = "Not enough shape in the pool (have "
			                        + numPool + ", require " + numShapeDef;
			if (log.isDebugEnabled() && m_debugShapeSet.isDebugShape(shapeDef)) {
				log.debug(failureMessage);
			}
			throw new ShapeBuildFailureException.Builder().withMessage(
			        failureMessage).withShapeName(shapeDef.getName())
			        .withReason(ShapeBuildFailureReason.NotEnoughComponents)
			        .build();
		}
		
		// meaningless, really, since a "false" is an exception
		return true;
	}
	

	/**
	 * Use a tree search algorithm to brute-force all possible combinations and
	 * build the shape. If a shape cannot be built, throw
	 * {@link ShapeBuildFailureException}.
	 * <p>
	 * If the pool or shape def are null, throw a {@link NullPointerException}.
	 * 
	 * @param pool
	 *            The pool of shapes
	 * @param shapeDef
	 *            The shape definition to try and build
	 * @return The shape that's built
	 * @throws ShapeBuildFailureException
	 *             If a shape cannot be built
	 * @throws OverTimeException
	 *             If recognition goes over time
	 */
	private BuiltShape buildShapeTreeSearch(List<IShape> pool,
	        ShapeDefinition shapeDef) throws ShapeBuildFailureException,
	        OverTimeException {
		if (pool == null) {
			throw new NullPointerException("Pool of shapes is null");
		}
		if (shapeDef == null) {
			throw new NullPointerException("Shape definition is null");
		}
		if (pool.size() == 0) {
			throw new ShapeBuildFailureException.Builder().withMessage(
			        "Pool contains 0 shapes").withShapeName(shapeDef.getName())
			        .withReason(ShapeBuildFailureReason.NotEnoughComponents)
			        .build();
		}
		
		BuiltShape builtShape = permuteAndBuild(pool, new ArrayList<IShape>(),
		        shapeDef);
		
		if (builtShape == null) {
			throw new ShapeBuildFailureException.Builder().withShapeName(
			        shapeDef.getName()).build();
		}
		
		return builtShape;
	}
	

	/**
	 * Recursive method to search through all possible permutations of the pool
	 * and find the best shape.
	 * 
	 * @param unusedShapeList
	 *            The shapes that have not been used in the current permutation
	 * @param permutedShapeList
	 *            The permutation, order matters
	 * @param shapeDef
	 *            The shape definition we're trying to build.
	 * @return The best shape that's built (max confidence) from all the
	 *         permutations of the shapes in the pool. If null, then nothing
	 *         could be built at all!
	 * @throws ShapeBuildFailureException
	 *             If the shape fails to build.
	 * @throws OverTimeException
	 *             If building goes over time
	 */
	private BuiltShape permuteAndBuild(List<IShape> unusedShapeList,
	        List<IShape> permutedShapeList, final ShapeDefinition shapeDef)
	        throws ShapeBuildFailureException, OverTimeException {
		
		OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
		
		if (unusedShapeList == null) {
			throw new NullPointerException("Unused shape list is nul");
		}
		if (permutedShapeList == null) {
			throw new NullPointerException("Permuted shape list is null");
		}
		if (shapeDef == null) {
			throw new NullPointerException("Component list is null");
		}
		if (shapeDef.getNumComponents() <= 0) {
			throw new IllegalArgumentException("Must have > 0 components!");
		}
		
		BuiltShape bestShape = null;
		
		// the base case is when we've used as many shapes as there are
		// components, maintaining a 1:1 mapping between permutation and
		// component slot.
		if (permutedShapeList.size() == shapeDef.getNumComponents()) {
			// this is the base case, there's no farther down to go
			// perform actions on the permutation here--try to put the
			// shapes in the component slots and compute confidence
			bestShape = putShapeTogether(permutedShapeList, shapeDef);
			
			// map the shapes we used to the components they went into
			for (int i = 0; i < permutedShapeList.size(); i++) {
				IShape shape = permutedShapeList.get(i);
				ComponentDefinition component = shapeDef
				        .getComponentDefinitions().get(i);
				bestShape.setComponent(component.getName(), shape);
			}
			
			return bestShape;
		}
		
		for (int i = 0; i < unusedShapeList.size(); i++) {
			// the shape that we're going to add to the permutation
			IShape shape = unusedShapeList.get(i);
			// there's a 1:1 correspondence between this shape that we're going
			// to try and add to the permutation, and the components in the
			// shape def. The component that maps to the shape we're about to
			// add lies at the permuted list size(), since we've not yet
			// actually added the shape to the permutation yet.
			int componentIndex = permutedShapeList.size();
			// this check ensures that putting this shape in this position in
			// the permutation actually makes sense--it is of the correct type.
			// We don't want to put a line shape in a rectangle component slot,
			// especially if it's the root of a large recursion tree searching
			// the permutation space.
			if (!shapeHasComponentsType(shape, shapeDef
			        .getComponentDefinitions().get(componentIndex))) {
				continue;
			}
			
			// TODO check constraints so far and cut out if too low of a
			// confidence?
			
			List<IShape> newUnused = new ArrayList<IShape>();
			for (int j = 0; j < unusedShapeList.size(); j++) {
				if (i != j) {
					newUnused.add(unusedShapeList.get(j));
				}
			}
			
			List<IShape> newPermuted = new ArrayList<IShape>(permutedShapeList);
			newPermuted.add(shape);
			
			// Collect the built shapes here, and return the best.
			// Recursion is magic!
			BuiltShape nextShape = permuteAndBuild(newUnused, newPermuted,
			        shapeDef);
			// if we don't have a best shape, or we just built a shape and it's
			// confidence is higher than the best shape so far, then this is
			// the best shape
			if ((bestShape == null)
			    || (nextShape != null && nextShape.getConfidence() > bestShape
			            .getConfidence())) {
				bestShape = nextShape;
			}
		}
		
		return bestShape;
	}
	

	/**
	 * Given a list of shapes and the shape definition, put the shapes into the
	 * component slots of the shape definition, in the order they appear, as a
	 * 1:1 mapping.
	 * <p>
	 * List of shapes must not be null, shape def must not be null, and the
	 * number of shapes must equal the number of components in the shape def.
	 * 
	 * @param shapes
	 *            The list of shapes, in order 1:1 with list of components in
	 *            the shape definition.
	 * @param shapeDef
	 *            The shape definition, with components listed 1:1 for the
	 *            shapes in the list
	 * @return The BuiltShape put together using the list of shapes and the
	 *         shape definition, with confidence computed using the constraints
	 *         in the shape definition, and attributes copied over from the
	 *         shape definition.
	 * @throws ShapeBuildFailureException
	 *             If there
	 * @throws OverTimeException
	 */
	private BuiltShape putShapeTogether(final List<IShape> shapes,
	        final ShapeDefinition shapeDef) throws ShapeBuildFailureException,
	        OverTimeException {
		if (shapes == null) {
			throw new NullPointerException("List of shapes cannot be null");
		}
		if (shapeDef == null) {
			throw new NullPointerException("Shape Definition cannot be null");
		}
		if (shapes.size() != shapeDef.getNumComponents()) {
			throw new IllegalArgumentException(
			        "We must have the same number of shapes (" + shapes.size()
			                + ") as components (" + shapeDef.getNumComponents()
			                + ")");
		}
		// technically the check on both of these is redundant, since we know
		// that the sizes are equal. We only need to check one.
		if (shapes.size() == 0 || shapeDef.getNumComponents() == 0) {
			throw new ShapeBuildFailureException.Builder().withMessage(
			        "No shapes or components!").withShapeName(
			        shapeDef.getName()).build();
		}
		
		if (log.isDebugEnabled() && m_debugShapeSet.isDebugShape(shapeDef)) {
			StringBuilder sb = new StringBuilder().append("Pool: [");
			for (IShape shape : shapes) {
				sb.append(shape.getLabel()).append(", ");
			}
			sb.append("]");
			log.debug(sb.toString());
		}
		
		BuiltShape builtShape = new BuiltShape();
		
		double shapeConfidence = 0;
		List<IConstraint> constraints = new ArrayList<IConstraint>();
		
		for (ConstraintDefinition constraintDef : shapeDef
		        .getConstraintDefinitions()) {
			
			if (log.isDebugEnabled() && m_debugShapeSet.isDebugShape(shapeDef)) {
				log.debug("Considering constraint [[" + constraintDef.toString()+"]]");
			}
			
			OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
			
			// parameters are how the constraint is defined
			List<ConstraintParameter> constraintParams = constraintDef
			        .getParameters();
			// args are the shapes we're passing into the constraint as defined
			// by the parameters
			List<IConstrainable> constraintArgs = new ArrayList<IConstrainable>();
			for (ConstraintParameter param : constraintParams) {
				String componentName = param.getComponent();
				int componentIndex = shapeDef.getComponentIndex(componentName);
				IShape componentShape = shapes.get(componentIndex);
				
				IConstrainable constrainable = ConstrainableFactory
				        .buildConstrainable(componentShape);
				if (param.containsSubPart()) {
					constrainable = ConstrainableFactory
					        .getConstrainableSubPart(constrainable, param
					                .getComponentSubPart());
				}
				constraintArgs.add(constrainable);
			}
			
			try {
				IConstraint constraint = ConstraintFactory
				        .getConstraint(constraintDef);
				constraint.setParameters(constraintArgs);
				constraints.add(constraint);
			}
			catch (InstantiationException e) {
				String msg = "Cannot put together constraint";
				log.error(msg, e);
				throw new ShapeBuildFailureException.Builder().withMessage(msg)
				        .withShapeName(shapeDef.getName()).withCause(e).build();
			}
			catch (IllegalAccessException e) {
				String msg = "Cannot put together constraint";
				log.error(msg, e);
				throw new ShapeBuildFailureException.Builder().withMessage(msg)
				        .withShapeName(shapeDef.getName()).withCause(e).build();
			}
			catch (ClassNotFoundException e) {
				String msg = "Cannot put together constraint";
				log.error(msg, e);
				throw new ShapeBuildFailureException.Builder().withMessage(msg)
				        .withShapeName(shapeDef.getName()).withCause(e).build();
			}
		}
		
		shapeConfidence = AndConstraint.solve(constraints);
		builtShape.setConfidence(shapeConfidence);
		
		return builtShape;
	}
	

	/**
	 * Given that you've built a shape from the shape definition, copy the shape
	 * definition's attributes and properties into the built shape. This
	 * completes the link from shape definition in file on disk, to BuiltShape
	 * interpretation with semantics to use in the system
	 * 
	 * @param shapeDef
	 *            Shape definition we built, can't be null
	 * @param builtShape
	 *            Built shape you build from the def'n, can't be null
	 */
	private void putShapeDefinitionIntoBuiltShape(ShapeDefinition shapeDef,
	        BuiltShape builtShape) {
		if (shapeDef == null) {
			throw new NullPointerException("Shape def cannot be null");
		}
		if (builtShape == null) {
			throw new NullPointerException("Built shape cannot be null");
		}
		
		builtShape.setLabel(shapeDef.getName());
		builtShape.setDescription(shapeDef.getDescription());
		// is A
		for (String isAKey : shapeDef.getIsASet()) {
			shapeDef.addAttribute(isAKey, "true");
		}
		
		// attributes
		for (String attrKey : shapeDef.getAttributeKeys()) {
			String attrVal = shapeDef.getAttribute(attrKey);
			builtShape.setAttribute(attrKey, attrVal);
		}
	}
	

	/**
	 * Does this shape have a type (label or isA) that is the same as the
	 * component's type?
	 * 
	 * @param shape
	 *            Shape we're checking the types for
	 * @param component
	 *            Component that we're matching the shape's types against
	 * @return True if the shape has a type that matches the component's.
	 */
	private boolean shapeHasComponentsType(IShape shape,
	        ComponentDefinition component) {
		if (shape == null) {
			throw new NullPointerException("Shape is null");
		}
		if (component == null) {
			throw new NullPointerException("Component definition is null");
		}
		
		boolean hasType = false;
		
		// TODO when we do shape inheritance via isA, this will be a set that
		// includes the component's parent types as well.
		String componentType = component.getShapeType();
		if (componentType == null) {
			throw new NullPointerException("Component has null shape type");
		}
		
		if (componentType != null) {
			hasType = componentType.equalsIgnoreCase(shape.getLabel());
			
			for (String isA : IsAConstants.getConstantsSet()) {
				// no need to go on if we find a match.
				if (hasType) {
					break;
				}
				// shape has this ISA and the ISA is the component's type
				hasType = shape.hasAttribute(isA)
				          && isA.equalsIgnoreCase(componentType);
			}
		}
		
		return hasType;
	}
}
