/**
 * ShapeBuilder.java
 * 
 * Revision History:<br>
 * Sep 4, 2008 intrect - File created
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
package org.ladder.recognition.constraint.builders;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.recognition.constraint.ConstraintFactory;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.confidence.UnaryConstraint;
import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.constrainable.ConstrainablePoint;
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;
import org.ladder.recognition.constraint.domains.ComponentSubPart;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.ConstraintParameter;
import org.ladder.recognition.constraint.domains.ShapeDefinition;

/**
 * This class is intended to keep track of what shape components are actually
 * used by a shape definition by knowing what constraints apply where and which
 * components satisfy them.
 * 
 * @author rgraham
 * 
 */
public class ShapeBuilder {
	
	private static Logger log = LadderLogger.getLogger(ShapeBuilder.class);
	
	protected List<IShape> candidates;
	
	protected List<String> components;
	
	protected List<IConstraint> constraints;
	
	protected ShapeDefinition shapeDef;
	
	protected Map<String, List<IConstraint>> componentsToConstraints;
	
	protected Map<String, IShape> finalComponents;
	
	protected Map<ConstraintDefinition, IConstraint> defToConstraint;
	
	protected BuiltShape builtShape;
	
	protected String filename;
	
	protected String failedComponent;
	
	protected Map<IConstraint, IShape> failedCandidates;
	
	protected boolean constraintFailure;
	
	protected boolean shapeTypeError;
	
	protected boolean emptyComponentsList;
	
	protected Map<IConstraint, Double> failedConstraints;
	
	protected static boolean DEBUG = false;
	
	// TODO Remove
	protected Map<IConstraint, String> constraintInfo = new HashMap<IConstraint, String>();
	
	/**
	 * A graph that represents whether or not a constraint was fulfilled by a
	 * candidate shape
	 * 
	 * Rows: Candidates.size() Cols: Constraints.size()
	 */
	protected double[][] graph;
	
	protected boolean built;
	
	protected static final double constraintThreshold = .35000;
	
	protected boolean satisfiedConstraints;
	
	protected List<Double> confidence;
	
	protected String report;
	
	// TODO remove
	protected String debugShape = "";
	
	
	/**
	 * Constructor, won't allow empty lists or null arguments
	 * 
	 * @param candidates
	 *            List of IShapes from Low Level Recognition Pool
	 * @param shapeDef
	 *            ShapeDefinition object for the shape we're building
	 */
	public ShapeBuilder(List<IShape> candidates, ShapeDefinition shapeDef)
	        throws IllegalArgumentException {
		
		if (candidates == null || candidates.size() == 0) {
			throw new IllegalArgumentException("Must have a candidate list.");
		}
		
		if (shapeDef == null) {
			throw new IllegalArgumentException(
			        "Must have a non-null shape definition.");
		}
		
		this.candidates = candidates;
		this.shapeDef = shapeDef;
		
		components = new ArrayList<String>();
		constraints = new ArrayList<IConstraint>();
		componentsToConstraints = new HashMap<String, List<IConstraint>>();
		finalComponents = new HashMap<String, IShape>();
		defToConstraint = new HashMap<ConstraintDefinition, IConstraint>();
		this.setupConstraintsAndComponentsFromDefinition();
		
		graph = new double[candidates.size()][constraints.size()];
		built = false;
		builtShape = new BuiltShape();
		builtShape.setLabel(shapeDef.getName());
		satisfiedConstraints = false;
		confidence = new ArrayList<Double>();
		failedConstraints = new HashMap<IConstraint, Double>();
		failedCandidates = new HashMap<IConstraint, IShape>();
	}
	

	/**
	 * Put it all together and return the shape we built or null. Compute the
	 * final confidence.
	 * 
	 * @return The shape built from the definition and candidates or null.
	 */
	public BuiltShape buildShape() {
		if (built) {
			return builtShape;
		}
		
		computeConstraints();
		if (!areAllConstraintsSatisfied()) {
			// log.debug("Failed to assemble: " + shapeDef.getName());
			this.constraintFailure = true;
			if (debugShape.equalsIgnoreCase(shapeDef.getName()) || DEBUG) {
				this.buildFailureReport();
				this.logReport();
			}
			return null;
		}
		
		if (this.getComponentShapes() == null) {
			// log.debug("Failed to assemble: " + shapeDef.getName() +
			// " gCS null");
			this.emptyComponentsList = true;
			if (debugShape.equalsIgnoreCase(shapeDef.getName()) || DEBUG) {
				this.buildFailureReport();
				this.logReport();
			}
			return null;
		}
		
		List<IShape> subShapes = new ArrayList<IShape>();
		// System.out.println(finalComponents);
		subShapes.addAll(this.getComponentShapes());
		builtShape.setSubShapes(subShapes);
		double conf = 0;
		double sum = 0;
		for (Double d : confidence) {
			sum += d;
		}
		double didNotUseAllThePiecesAdjustment = 0;
		if (subShapes.size() < candidates.size()) {
			didNotUseAllThePiecesAdjustment = .10 * ((candidates.size() - subShapes
			        .size()));
		}
		conf = (sum / (double) confidence.size())
		       - didNotUseAllThePiecesAdjustment;
		
		if (conf < 0) {
			conf = 0.0;
		}
		
		if (subShapes.size() == candidates.size() && conf < 0.84001) {
			conf += .13;
		}
		
		// If there are a ton of lines in Candidate shapes, then low level stuff
		// isn't working
		// and the adjustment heuristic should be turned off. IMHO RG.
		if (candidates.size() > 7 + shapeDef.getNumComponents()) {
			conf = (sum / (double) confidence.size());
		}
		
		report = "Confidence for " + builtShape.getLabel() + " is " + conf
		         + " which was adjusted by: " + didNotUseAllThePiecesAdjustment;
		
		builtShape.setConfidence(conf);
		builtShape.setComponents(finalComponents);
		// log.debug(report);
		built = true;
		return builtShape;
	}
	

	/**
	 * Sets up the data structures for processing a shape definition by
	 * iterating through the definition and extracting all the constraints and
	 * components
	 */
	protected void setupConstraintsAndComponentsFromDefinition() {
		List<ConstraintDefinition> constraintDefs = shapeDef
		        .getConstraintDefinitions();
		for (ConstraintDefinition constraintDef : constraintDefs) {
			
			IConstraint constraint = null;
			try {
				constraint = ConstraintFactory.getConstraint(constraintDef);
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
			if (constraint == null) {
				continue;
			}
			
			this.addConstraint(constraint);
			defToConstraint.put(constraintDef, constraint);
			for (ConstraintParameter cp : constraintDef.getParameters()) {
				this.addComponent(cp.getComponent());
				this.addConstraintForComponent(cp.getComponent(), constraint);
			}
		}
	}
	

	/**
	 * Adds the string to a list without duplicates
	 * 
	 * @param component
	 *            String XML name of a shape's component
	 */
	protected void addComponent(String component) {
		if (!components.contains(component)) {
			components.add(component);
		}
	}
	

	/**
	 * Adds the IConstraint to a list without duplicates
	 * 
	 * @param constraint
	 *            IConstraint constraint from the XML shape definition
	 */
	protected void addConstraint(IConstraint constraint) {
		if (!constraints.contains(constraint)) {
			constraints.add(constraint);
		}
	}
	

	/**
	 * Add a constraint to the list of constraints for a component
	 * 
	 * @param componentName
	 *            String from XML for a component of a shape
	 * @param constraint
	 *            IConstraint object built in CALVIN#buildShape
	 */
	protected void addConstraintForComponent(String componentName,
	        IConstraint constraint) {
		if (componentsToConstraints.containsKey(componentName)) {
			List<IConstraint> tmpList = componentsToConstraints
			        .get(componentName);
			tmpList.add(constraint);
		}
		else {
			List<IConstraint> tmpList = new ArrayList<IConstraint>();
			tmpList.add(constraint);
			componentsToConstraints.put(componentName, tmpList);
		}
	}
	

	/**
	 * Loops over every shape combination possible and tests each constraint on
	 * them. Records the confidence values in the graph data structure.
	 * 
	 */
	protected void computeConstraints() {
		for (ConstraintDefinition constraintDef : shapeDef
		        .getConstraintDefinitions()) {
			// check that constraint is satisfied, return null if not
			IConstraint constraint = defToConstraint.get(constraintDef);
			log.debug("Constraint: " + constraint.getName());
			
			// For each constraint we need to iterate over each possible
			// way for the components to satisfy it.
			
			for (IShape s1 : candidates) {
				// Check for and solve UnaryConstraints
				if (constraint instanceof UnaryConstraint) {
					List<IConstrainable> testShapes = new ArrayList<IConstrainable>();
					List<ConstraintParameter> constraintParams = constraintDef
					        .getParameters();
					// Grab the first one because it is only a unary
					// constraint
					ConstraintParameter constraintParam = constraintParams
					        .get(0);
					IConstrainable testShape = buildSubShape(s1);
					testShape = buildParameterizedIConstrainable(testShape,
					        constraintParam);
					testShapes.add(testShape);
					
					double constraintConfidence = constraint.solve(testShapes);
					log.debug("confidence = " + constraintConfidence
					          + " on params: " + testShapes);
					
					boolean failedShape = constraint.isClearlyFalse(constraintConfidence);
					// TODO remove
					if(failedShape){
						//return false;
						constraintConfidence = -1;
						//satisfiedConstraints = false;
					}
					constraintInfo.put(constraint,
					        constraintDef.getParameters().get(0).getComponent()
					                + " "
					                + constraintDef.getParameters().get(0)
					                        .getComponentSubPart().name());
					
					this.setConstraintConfidence(s1, constraint,
					        constraintConfidence);
					
					if(failedShape){continue;}
					final double thresh = 0.35;
					if (thresh < constraintConfidence) {
						// confidence.add(new Double(constraintConfidence));
						// System.out.println(constraintParam.getComponent() +
						// " gets " + testShapeCopy.getShapeType() + " " +
						// testShapeCopy.toString());
						
						// System.out.println(
						// "[buildShape] UnaryConstraint satisfied: " +
						// constraintConfidence);
						continue;
					}
				}
				
				if (!(constraint instanceof UnaryConstraint)) {
					for (IShape s2 : candidates) {
						if (s2.getID() == s1.getID()) {
							continue;
						}
						// System.out.println("  [buildShape] and " +
						// s2.getLabel() + " " + s2.getID());
						List<IConstrainable> testShapes = new ArrayList<IConstrainable>();
						List<ConstraintParameter> constraintParams = constraintDef
						        .getParameters();
						
						// System.out.println(constraintParams.size());
						ConstraintParameter cp = constraintParams.get(0);
						ConstraintParameter cp2 = constraintParams.get(1);
						
						// IConstrainable superTestShape1, superTestShape2;
						// IShape parent1, parent2;
						
						// these are the candidates that we're considering
						// plugging into the constraint
						IShape candidate1, candidate2;
						
						// these are the placeholders for the things we're
						// plugging into the constraint
						IConstrainable testShape1, testShape2;
						
						String shapeType = null;
						String shapeType2 = null;
						
						
						// Two components, one per constraint parameter
						try {
							shapeType = shapeDef.getComponentDefinition(
						        cp.getComponent()).getShapeType();
							shapeType2 = shapeDef.getComponentDefinition(
						        cp2.getComponent()).getShapeType();
						}
						catch(Exception e) {
							System.out.println(shapeDef.getFilename());
							System.out.println(constraint.getName());
							System.out.println(cp.getComponent());
							System.out.println(cp2.getComponent());
						}
						// which candidate mathces which label?
						if (s1.getLabel().equalsIgnoreCase(shapeType)
						    && s2.getLabel().equalsIgnoreCase(shapeType2)) {
							// superTestShape1 = buildSubShape(s1);
							candidate1 = s1;
							// superTestShape2 = buildSubShape(s2);
							candidate2 = s2;
						}
						else if (s1.getLabel().equalsIgnoreCase(shapeType2)
						         && s2.getLabel().equalsIgnoreCase(shapeType)) {
							// superTestShape1 = buildSubShape(s2);
							candidate1 = s2;
							// superTestShape2 = buildSubShape(s1);
							candidate2 = s1;
							
						}
						else {
							
							continue;
						}
						
						// So we know which candidates match which label. So
						// wrap the candidates in IConstrainable.
						
						// testShape1 = superTestShape1;
						testShape1 = buildSubShape(candidate1);
						// testShape2 = superTestShape2;
						testShape2 = buildSubShape(candidate2);
						
						// get the specific sup-part for each IConstrainable
						try {
							testShape1 = buildParameterizedIConstrainable(
							        testShape1, cp);
							testShape2 = buildParameterizedIConstrainable(
							        testShape2, cp2);
						}
						catch (IllegalArgumentException e) {
							log
							        .error("Error trying to parameterize the IConstrainable");
							log.error(e);
							continue;
						}
						
						testShapes.add(testShape1);
						testShapes.add(testShape2);
						
						if (log.isDebugEnabled()) {
							log.debug("testShape1 " + testShape1);
							for (IStroke stroke : testShape1.getParentShape()
							        .getStrokes()) {
								log.debug("\t\t"
								          + stroke.getBoundingBox()
								                  .getDiagonalLength());
							}
							log.debug("testShape2 " + testShape2);
							for (IStroke stroke : testShape2.getParentShape()
							        .getStrokes()) {
								log.debug("\t\t"
								          + stroke.getBoundingBox()
								                  .getDiagonalLength());
							}
						}
						
						// testShapes.add(superTestShape1);
						// testShapes.add(superTestShape2);
						double constraintConfidence = constraint
						        .solve(testShapes);
						log.debug("conf = " + constraintConfidence
						          + " on params =" + testShapes);
						
						// TODO remove
						constraintInfo.put(constraint,
						        constraintDef.getParameters().get(0)
						                .getComponent()
						                + " "
						                + constraintDef.getParameters().get(0)
						                        .getComponentSubPart().name()
						                + " "
						                + constraintDef.getParameters().get(1)
						                        .getComponent()
						                + " "
						                + constraintDef.getParameters().get(1)
						                        .getComponentSubPart().name());
						
						this.setConstraintConfidence(s1, constraint,
						        constraintConfidence);
						
						final double thresh = 0.3;
						if (thresh < constraintConfidence) {
							// confidence.add(new Double(constraintConfidence));
							this.setConstraintConfidence(s2, constraint,
							        constraintConfidence);
							
							// System.out.println(cp.getComponent() + " gets " +
							// testShape1Copy.getShapeType() + " " +
							// testShape1Copy.toString());
							// System.out.println(cp2.getComponent() + " gets "
							// + testShape2Copy.getShapeType() + " " +
							// testShape2Copy.toString());
							
							// System.out.println(
							// "[buildShape] Constraint satisfied: " +
							// constraintConfidence);
							
						}
						else {
							// System.out.println(
							// "[buildShape] Constraint NOT satisfied: " +
							// constraintConfidence);
						}
					}
				}
			}
			
			// Output that explains failing constraints. Should be logging this?
			/*
			 * if (!satisfied) {System.out.print(
			 * "[ShapeBuilder#computeConstraints] Unsatisfied constraint for " +
			 * shapeDef.getName() + " " + constraintDef.getName() + "\n"); for
			 * (ConstraintParameter cp : constraintDef.getParameters()) {
			 * System.out.print("\tParameter: " + cp.getComponent() + "." +
			 * cp.getComponentSubPart() + "\n"); } }
			 */
		}
		
		satisfiedConstraints = true;
//		return true;
	}
	

	/**
	 * Updates the record to show that this candidate shape satisfies this
	 * constraint.
	 * 
	 * @param candidate
	 * @param constraint
	 */
	public void setConstraintConfidence(IShape candidate,
	        IConstraint constraint, double confidence) {
		int candidateIndex = candidates.indexOf(candidate);
		int constraintIndex = constraints.indexOf(constraint);
		if (!(graph[candidateIndex][constraintIndex] > confidence)) {
			// if (shapeDef.getName().equalsIgnoreCase(m_debugShape))
			// System.out.println("Setting: " + constraintInfo.get(constraint) +
			// " " + candidate + " CC: " + confidence);
			graph[candidateIndex][constraintIndex] = confidence;
		}
		
	}
	

	/**
	 * Returns the candidate shape that fulfills the constraints for this
	 * component (or null).
	 * 
	 * @param componentName
	 * @return The proper candidate shape or null
	 */
	public IShape getComponent(String componentName) {
		if (finalComponents.containsKey(componentName)) {
			return finalComponents.get(componentName);
		}
		else {
			return null;
		}
	}
	

	/**
	 * Determine if this component's constraints were satisfied by the
	 * candidate.
	 * 
	 * @param componentName
	 * @return True if the constraints on this component for this candidate are
	 *         satisfied and false otherwise
	 */
	protected boolean areConstraintsSatisfiedForCandidate(String componentName,
	        IShape candidate) {
		if (finalComponents.containsValue(candidate)) {
			// if (shapeDef.getName().equalsIgnoreCase(m_debugShape))
			// System.out.println("Already assigned " + candidate);
			return false;
		}
		
		List<IConstraint> constraintsForComponent = componentsToConstraints
		        .get(componentName);
		int candidateIndex = candidates.indexOf(candidate);
		for (IConstraint c : constraintsForComponent) {
			int constraintIndex = constraints.indexOf(c);
			if (graph[candidateIndex][constraintIndex] <= constraintThreshold) {
				// if (shapeDef.getName().equalsIgnoreCase(m_debugShape))
				// System.out.println(candidate + " confidence too low for " +
				// componentName + " at: " +
				// graph[candidateIndex][constraintIndex] +
				// " for " + constraintInfo.get(c));
				
				this.failedConstraints.put(c,
				        graph[candidateIndex][constraintIndex]);
				this.failedCandidates.put(c, candidate);
				return false;
			}
		}
		
		if (shapeDef.getComponentDefinition(componentName).getShapeType()
		        .equalsIgnoreCase(candidate.getLabel())) {
			finalComponents.put(componentName, candidate);
			// if (shapeDef.getName().equalsIgnoreCase(m_debugShape))
			// System.out.println("Assigning " + candidate + " to " +
			// componentName);
			return true;
		}
		// if (shapeDef.getName().equalsIgnoreCase(m_debugShape))
		// System.out.println("Thresholds okay...something wrong with shape type? "
		// + candidate + " : " + componentName);
		return false;
	}
	

	/**
	 * Determine if this component's constraints were satisfied.
	 * 
	 * @param componentName
	 * @return True if the constraints on this component are satisfied and false
	 *         otherwise
	 */
	public boolean areConstraintsSatisfiedForComponent(String componentName) {
		for (IShape candidate : candidates) {
			if (this.areConstraintsSatisfiedForCandidate(componentName,
			        candidate)) {
				return true;
			}
		}
		this.failedComponent = componentName;
		return false;
	}
	

	/**
	 * Determine if this shape's constraints were satisfied.
	 * 
	 * @return True if the constraints on this shape are satisfied and false
	 *         otherwise
	 */
	public boolean areAllConstraintsSatisfied() {
		if (!satisfiedConstraints) {
			return false;
		}
		
		for (String component : components) {
			if (!this.areConstraintsSatisfiedForComponent(component)) {
				return false;
			}
		}
		return true;
	}
	

	/**
	 * Gets the candidate shapes that should be included in this shapeDef
	 * 
	 * @return The Candidate shapes that should be subshapes to the newly built
	 *         shape or null
	 */
	public Collection<IShape> getComponentShapes() {
		if (finalComponents.size() == shapeDef.getNumComponents()) {
			return finalComponents.values();
		}
		else {
			return null;
		}
	}
	

	/**
	 * Gets the candidate shapes that should be included in this shapeDef mapped
	 * to by the component names
	 * 
	 * @return The Candidate shapes that should be subshapes to the newly built
	 *         shape mapped by component name or null
	 */
	public Map<String, IShape> getComponentShapesMap() {
		return finalComponents;
	}
	

	/**
	 * This method accepts an IConstrainable and builds the appropriate
	 * IConstrainable object, be it a ConstrainableShape, ConstrainablePoint, or
	 * ConstrainableLine based on the ComponentSubPart
	 * 
	 * @param constrainable
	 *            An IConstrainable that is to be transformed into an
	 *            IConstrainable based on a ConstraintParameter
	 * @param csp
	 *            A ComponentSubPart that gets used for grabbing the right piece
	 *            of an IConstraiable
	 * @return IConstrainable
	 */
	public static IConstrainable buildParameterizedIConstrainable(
	        IConstrainable constrainable, ConstraintParameter c)
	        throws IllegalArgumentException {
		IConstrainable parameterized;
		
		ComponentSubPart csp = c.getComponentSubPart();
		if (constrainable instanceof ConstrainableLine) {
			parameterized = getLineSubPart((ConstrainableLine) constrainable,
			        csp);
		}
		else if (constrainable instanceof ConstrainableShape) {
			parameterized = getShapeSubPart((ConstrainableShape) constrainable,
			        csp);
		}
		else {
			// It's a ConstrainablePoint?
			return constrainable;
		}
		return parameterized;
	}
	

	/**
	 * This method accepts an IShape and builds the appropriate IConstrainable
	 * object, be it a ConstrainableShape or ConstrainableLine
	 * 
	 * @param shape
	 *            An IShape that is to be transformed into an IConstrainable
	 * @return IConstrainable
	 */
	public static IConstrainable buildSubShape(IShape shape) {
		if (shape.getLabel().equalsIgnoreCase("Line")) {
			return new ConstrainableLine(shape);
		}
		else {
			return new ConstrainableShape(shape);
		}
	}
	

	/**
	 * This method accepts a ConstrainableLine and a ComponentSubPart and
	 * acquires the part from the ConstrainableLine
	 * 
	 * @param line
	 *            A ConstrainableLine that gets analyzed to provide a sub part
	 *            for a constraint
	 * @param csp
	 *            A ComponentSubPart that gets used for grabbing the right piece
	 *            of an IConstraiable
	 * @return IConstrainable
	 */
	public static IConstrainable getLineSubPart(ConstrainableLine line,
	        ComponentSubPart csp) throws IllegalArgumentException {
		if (csp == ComponentSubPart.None) {
			return line;
		}
		else if (csp == ComponentSubPart.End1) {
			return line.getEndLB();
		}
		else if (csp == ComponentSubPart.End2) {
			return line.getEndRT();
		}
		else if (csp == ComponentSubPart.BottomMostEnd) {
			return line.getBottomMostEnd();
		}
		else if (csp == ComponentSubPart.TopMostEnd) {
			return line.getTopMostEnd();
		}
		else if (csp == ComponentSubPart.RightMostEnd) {
			return line.getRightMostEnd();
		}
		else if (csp == ComponentSubPart.LeftMostEnd) {
			return line.getLeftMostEnd();
		}
		else {
			BoundingBox bb = line.getBoundingBox();
			IShape parent = line.getParentShape();
			if (csp == ComponentSubPart.BottomCenter) {
				return new ConstrainablePoint(bb.getBottomCenterPoint(), parent);
			}
			else if (csp == ComponentSubPart.BottomLeft) {
				return new ConstrainablePoint(bb.getBottomLeftPoint(), parent);
			}
			else if (csp == ComponentSubPart.BottomRight) {
				return new ConstrainablePoint(bb.getBottomRightPoint(), parent);
			}
			else if (csp == ComponentSubPart.Center) {
				return new ConstrainablePoint(bb.getCenterPoint(), parent);
			}
			else if (csp == ComponentSubPart.CenterLeft) {
				return new ConstrainablePoint(bb.getCenterLeftPoint(), parent);
			}
			else if (csp == ComponentSubPart.CenterRight) {
				return new ConstrainablePoint(bb.getCenterRightPoint(), parent);
			}
			else if (csp == ComponentSubPart.TopCenter) {
				return new ConstrainablePoint(bb.getTopCenterPoint(), parent);
			}
			else if (csp == ComponentSubPart.TopLeft) {
				return new ConstrainablePoint(bb.getTopLeftPoint(), parent);
			}
			else if (csp == ComponentSubPart.TopRight) {
				return new ConstrainablePoint(bb.getTopRightPoint(), parent);
			}
			else {
				throw new IllegalArgumentException(
				        "A ConstrainableLine doesn't have a " + csp + ".");
			}
		}
	}
	

	/**
	 * This method accepts a ConstrainableShape and a ComponentSubPart and
	 * acquires the part from the ConstrainableLine
	 * 
	 * @param shape
	 *            A ConstrainableShape that gets analyzed to provide a sub part
	 *            for a constraint
	 * @return IConstrainable
	 */
	public static IConstrainable getShapeSubPart(ConstrainableShape shape,
	        ComponentSubPart csp) throws IllegalArgumentException {
		BoundingBox bb = shape.getBoundingBox();
		IShape parent = shape.getParentShape();
		if (csp == ComponentSubPart.None) {
			return shape;
		}
		else if (csp == ComponentSubPart.BottomCenter) {
			return new ConstrainablePoint(bb.getBottomCenterPoint(), parent);
		}
		else if (csp == ComponentSubPart.BottomLeft) {
			return new ConstrainablePoint(bb.getBottomLeftPoint(), parent);
		}
		else if (csp == ComponentSubPart.BottomRight) {
			return new ConstrainablePoint(bb.getBottomRightPoint(), parent);
		}
		else if (csp == ComponentSubPart.Center) {
			return new ConstrainablePoint(bb.getCenterPoint(), parent);
		}
		else if (csp == ComponentSubPart.CenterLeft) {
			return new ConstrainablePoint(bb.getCenterLeftPoint(), parent);
		}
		else if (csp == ComponentSubPart.CenterRight) {
			return new ConstrainablePoint(bb.getCenterRightPoint(), parent);
		}
		else if (csp == ComponentSubPart.TopCenter) {
			return new ConstrainablePoint(bb.getTopCenterPoint(), parent);
		}
		else if (csp == ComponentSubPart.TopLeft) {
			return new ConstrainablePoint(bb.getTopLeftPoint(), parent);
		}
		else if (csp == ComponentSubPart.TopRight) {
			return new ConstrainablePoint(bb.getTopRightPoint(), parent);
		}
		else if (csp == ComponentSubPart.End1) {
			return new ConstrainablePoint(bb.getBottomLeftPoint(), parent);
		}
		else if (csp == ComponentSubPart.End2) {
			return new ConstrainablePoint(bb.getBottomRightPoint(), parent);
		}
		else if (csp == ComponentSubPart.BottomMostEnd) {
			return new ConstrainablePoint(bb.getBottomLeftPoint(), parent);
		}
		else if (csp == ComponentSubPart.TopMostEnd) {
			return new ConstrainablePoint(bb.getTopRightPoint(), parent);
		}
		else if (csp == ComponentSubPart.LeftMostEnd) {
			return new ConstrainablePoint(bb.getBottomLeftPoint(), parent);
		}
		else if (csp == ComponentSubPart.RightMostEnd) {
			return new ConstrainablePoint(bb.getTopRightPoint(), parent);
		}
		else {
			throw new IllegalArgumentException(
			        "A ConstrainableShape doesn't have a " + csp + ".");
		}
	}
	

	/**
	 * Prints the report to screen.
	 */
	public void buildFailureReport() {
		report = " ***** Report on Building Shape " + shapeDef.getName()
		         + " ***** \n";
		report += "Failing Component: " + failedComponent + "\n";
		if (constraintFailure) {
			report += "We failed to meet one or more constraints: \n";
			for (Map.Entry<IConstraint, Double> entry : this.failedConstraints
			        .entrySet()) {
				report += "\t" + entry.getKey().getName() + ":\t\t"
				          + entry.getValue() + "\n";
				report += "\t\t" + this.constraintInfo.get(entry.getKey())
				          + "\n";
				report += "\t\t" + this.failedCandidates.get(entry.getKey())
				          + "\n";
			}
		}
		else if (shapeTypeError) {
			report += "We could not match the components to the candidates. The constraints were met by the wrong shape type.\n";
		}
		else if (emptyComponentsList) {
			report += "We have a null list of components in buildShape. This usually indicates a lacking constraint in the definitions file.\n";
		}
		report += "The final mapping of components: "
		          + finalComponents.toString() + "\n";
		
	}
	

	/**
	 * Print the report to the file.
	 */
	public void logReport() {
		log.debug(report);
	}
	

	/**
	 * Print the report to the file.
	 */
	public void printToFile(String filename) {
		this.setFilename(filename);
		this.printToFile();
	}
	

	protected void printToFile() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(
			        this.filename));
			out.write(report);
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Sets the filename.
	 * 
	 * @param filename
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}
	

	public String getDebugShape() {
		return debugShape;
	}
	

	public void setDebugShape(String debugShape) {
		// System.out.println("Setting m_debugShape:" + m_debugShape);
		this.debugShape = debugShape;
	}
	

	public void toggleDEBUG() {
		DEBUG = !DEBUG;
	}
	

	public String getFilename() {
		return filename;
	}
}
