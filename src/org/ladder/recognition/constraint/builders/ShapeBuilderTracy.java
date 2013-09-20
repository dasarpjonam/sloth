package org.ladder.recognition.constraint.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.DebugShapeSet;
import org.ladder.recognition.constraint.ConstraintFactory;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.constrainable.ConstrainablePoint;
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.ComponentSubPart;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.ConstraintParameter;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;

public class ShapeBuilderTracy {
	
	private static Logger log = LadderLogger.getLogger(ShapeBuilderTracy.class);
	
	private boolean m_failed = false;
	
	private long m_maxTime;
	
	private long m_startTime;
	
	private DebugShapeSet m_debugShapeSet = new DebugShapeSet();
	
	
	/**
	 * @return the debugShape
	 */
	public DebugShapeSet getDebugShapeSet() {
		return this.m_debugShapeSet;
	}
	

	public void setDebugShapeSet(DebugShapeSet debugShapeSet) {
		this.m_debugShapeSet = debugShapeSet;
	}
	

	public void addDebugShape(String debugShape) {
		m_debugShapeSet.addDebugShape(debugShape);
	}
	

	public boolean isFailed() {
		return m_failed;
	}
	

	public void setFailed(boolean m_failed) {
		this.m_failed = m_failed;
	}
	

	public BuiltShape recognize(ArrayList<IShape> lowLevelShapes,
	        ShapeDefinition shapeDef, long maxTime) throws OverTimeException {
		
		m_startTime = System.currentTimeMillis();
		m_maxTime = maxTime;
		
		// just in case these are used elsewhere
		ArrayList<IShape> candidates = (ArrayList<IShape>) lowLevelShapes
		        .clone();
		
		if (log.isInfoEnabled() && m_debugShapeSet.isDebugShape(shapeDef)) {
			String lowLevel = "";
			for (IShape s : lowLevelShapes) {
				lowLevel += s.getLabel();
				if (s.getLabel().equalsIgnoreCase("Text-"))
					lowLevel += s.getAttribute("TEXT_BEST");
				lowLevel += " ";
			}
			log.info("Building " + shapeDef.getName()
			         + ", Shape Builder has [[" + lowLevel + "]] in the pool");
		}
		
		/**
		 * for(IShape candidatetestold: candidates){ Shape candidatetest =
		 * (Shape)candidatetestold; System.out.print("candidate: " +
		 * candidatetest.getLabel());
		 * if(candidatetest.getLabel().startsWith("Line")){ Line2D line =
		 * (Line2D.Double)candidatetest.getBeautifiedShape();
		 * 
		 * } else{ for(IStroke stroke : candidatetest.getStrokes()){
		 * System.out.print("stroke = " + stroke.getFirstPoint() + " " +
		 * stroke.getLastPoint()); } } System.out.println(""); }
		 **/
		
		ArrayList<IConstraint> constraints = new ArrayList<IConstraint>();
		// ArrayList<String> components = new ArrayList<String>();
		// HashMap<String, List<IConstraint>> componentsToConstraints = new
		// HashMap<String, List<IConstraint>>();
		
		// HashMap<ConstraintDefinition, IConstraint> defToConstraint = new
		// HashMap<ConstraintDefinition, IConstraint>();
		// List<ConstraintDefinition> constraintDefs = shapeDef
		// .getConstraintDefinitions();
		//		
		// for (ConstraintDefinition constraintDef : constraintDefs) {
		//			
		// // Check that we have not gone over time
		// OverTime.overTimeCheck(m_startTime, m_maxTime, log);
		//			
		// IConstraint constraint = null;
		// try {
		// constraint = ConstraintFactory.getConstraint(constraintDef);
		// }
		// catch (InstantiationException e) {
		// log.error("Cannot instantiate constraint for def: "
		// + constraintDef);
		// log.error(e.getMessage(), e);
		// }
		// catch (IllegalAccessException e) {
		// log.error("Cannot instantiate constraint for def: "
		// + constraintDef);
		// log.error(e.getMessage(), e);
		// }
		// catch (ClassNotFoundException e) {
		// log.error("Cannot instantiate constraint for def: "
		// + constraintDef);
		// log.error(e.getMessage(), e);
		// }
		//			
		// if (constraint == null) {
		// continue;
		// }
		//			
		// if (!constraints.contains(constraint)) {
		// constraints.add(constraint);
		// }
		//			
		// defToConstraint.put(constraintDef, constraint);
		// // for (ConstraintParameter cp : constraintDef.getParameters()) {
		// // if (constraint.getName().equals("ContainsText")) {
		// // if (constraintDef.getParameters().indexOf(cp) == 1) {
		// // // System.out.println("found text constraint = " +
		// // // cp.getComponent());
		// // }
		// // }// else {
		// // // components.add(cp.getComponent());
		// // // }
		// // /**
		// // * if (componentsToConstraints.containsKey(cp.getComponent())) {
		// // * List<IConstraint> tmpList = componentsToConstraints.get(cp
		// // * .getComponent()); tmpList.add(constraint); } else {
		// // * List<IConstraint> tmpList = new ArrayList<IConstraint>();
		// // * tmpList.add(constraint);
		// // * componentsToConstraints.put(cp.getComponent(), tmpList); }
		// // **/
		// // }
		// }
		
		ArrayList<ComponentDefinition> componentDefinitions = new ArrayList<ComponentDefinition>();
		componentDefinitions.addAll(shapeDef.getComponentDefinitions());
		
		candidates = reduceCandidateList(candidates, shapeDef);
		
		if (log.isInfoEnabled() && m_debugShapeSet.isDebugShape(shapeDef)) {
			StringBuilder sb = new StringBuilder();
			sb.append("Candidates list: [");
			if (candidates != null) {
				for (IShape candidate : candidates) {
					sb.append(candidate.getLabel()).append("  ");
				}
			}
			sb.append(']');
			log.info(sb.toString());
		}
		
		if (candidates == null || candidates.size() == 0) {
			return null;
		}
		
		ArrayList<ComponentDefinition> slotNames = computeSlotArray(shapeDef);
		// XXX
		ArrayList<ArrayList<Integer>> candidateList = null;
		try {
			candidateList = computeCandidateList(candidates, shapeDef,
			        slotNames, componentDefinitions);
		}
		catch (InstantiationException e1) {
			log.error("Cannot build shape", e1);
			return null;
		}
		catch (IllegalAccessException e1) {
			log.error("Cannot build shape", e1);
			return null;
		}
		catch (ClassNotFoundException e1) {
			log.error("Cannot build shape", e1);
			return null;
		}
		// printCombinations(candidateList);
		
		// this.printCombinations(candidateList);
		BuiltShape builtShape = null;
		try {
			builtShape = computeConstraintProbability(candidateList,
			        candidates, shapeDef, slotNames);
		}
		catch (InstantiationException e) {
			log.error("Cannot build shape", e);
			return null;
		}
		catch (IllegalAccessException e) {
			log.error("Cannot build shape", e);
			return null;
		}
		catch (ClassNotFoundException e) {
			log.error("Cannot build shape", e);
			return null;
		}
		
		if (builtShape == null) {
			return null;
		}
		
		int tooManyComponents = lowLevelShapes.size()
		                        - shapeDef.getComponentDefinitions().size();
		
		if (tooManyComponents > 0) {
			// double didNotUseAllThePiecesAdjustmen = .20 * tooManyComponents;
			// builtShape.setConfidence(builtShape.getConfidence()
			// - didNotUseAllThePiecesAdjustmen);
			// System.out.println(tooManyComponents +
			// " Lowered by penalization : " + didNotUseAllThePiecesAdjustmen);
		}
		log.debug("Confidence returned: " + builtShape.getConfidence());
		return builtShape;
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
			// ||
			// shape.getLabel().equalsIgnoreCase(DashLineRecognizer.DASHLINE)) {
			return new ConstrainableLine(shape);
		}
		else {
			return new ConstrainableShape(shape);
		}
	}
	

	/**
	 * Compute the confidence that the constraints in the shape def hold for the
	 * given combination of candidates fitting into the given slots.
	 * 
	 * @param shapeDef
	 *            Shape definition to build
	 * @param slotNames
	 *            Names of the components in the shape definition
	 * @param candidates
	 *            The candidates we can use to build this shape
	 * @return confidence the constraints hold for this shape def
	 * @throws OverTimeException
	 *             if recognition takes too long
	 * @throws InstantiationException
	 *             if error instantiating constraint
	 * @throws IllegalAccessException
	 *             if error instantiating constraint
	 * @throws ClassNotFoundException
	 *             if error instantiating constraint
	 */
	private double computeConstraintConfidence(ShapeDefinition shapeDef,
	        ArrayList<Integer> combination,
	        ArrayList<ComponentDefinition> slotNames,
	        ArrayList<IShape> candidates) throws OverTimeException,
	        InstantiationException, IllegalAccessException,
	        ClassNotFoundException {
		
		int items = 0;
		double confidence = 0;
		
		// COMBINATION tells us what order we're considering the SHAPES in
		ArrayList<IShape> componentOrderingForTemplate = new ArrayList<IShape>();
		for (Integer i : combination) {
			componentOrderingForTemplate.add(candidates.get(i));
		}
		
		// for each constraint in this shape definition
		for (ConstraintDefinition constraintDef : shapeDef
		        .getConstraintDefinitions()) {
			
			// Check if we've gone over time
			OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
			
			try {
				IConstraint constraint = ConstraintFactory
				        .getConstraint(constraintDef);
				
				List<IConstrainable> testShapes = new ArrayList<IConstrainable>();
				List<IConstrainable> superTestShapes = new ArrayList<IConstrainable>();
				
				// for each parameter in this constraint
				for (ConstraintParameter constraintParam : constraintDef
				        .getParameters()) {
					
					// Check if we've gone over time
					OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
					
					// the text we're looking for in a ContainsText constraint
					// ("AA")
					String textString = null;
					// name of the component that goes in this parameter of this
					// constraint
					String shapeName = constraintParam.getComponent();
					
					// if this is a ContainsText constraint and this is the
					// SECOND parameter (param 1) in that constraint, set the
					// text string name of this parameter's component.
					// example:
					// <constraint name="ContainsText">
					// <param component="mainText" /> -- param # 0
					// <param component="AA" /> -- param # 1
					// </constraint>
					// The textString will be "AA"
					if (constraint.getName().equals("ContainsText")) {
						if (constraintDef.getParameters().indexOf(
						        constraintParam) == 1) {
							textString = shapeName;
						}
					}
					
					// The Name of the component (ellipse1) used in this
					// parameter of the constraint, get the component
					// definition (index to it) in the set of component
					// defnitions
					int componentIndex = 0;
					for (ComponentDefinition slot : shapeDef
					        .getComponentDefinitions()) {
						if (slot.getName().equals(shapeName)) {
							break;
						}
						componentIndex++;
					}
					
					// No containsText constraints
					if (textString == null) {
						
						IShape s1 = componentOrderingForTemplate
						        .get(componentIndex);
						
						IConstrainable superTestShape = buildSubShape(s1);
						IConstrainable testShape = superTestShape;
						if (constraintParam.containsSubPart()) {
							testShape = buildParameterizedIConstrainable(
							        superTestShape, constraintParam);
						}
						testShapes.add(testShape);
						// superTestShapes.add(superTestShape);
					}
					else {
						IShape s1 = new Shape();
						s1.setLabel(textString);
						IConstrainable superTestShape = buildSubShape(s1);
						IConstrainable testShape = superTestShape;
						testShapes.add(testShape);
					}
				}
				testShapes.addAll(superTestShapes);
				
				double constraintConfidence = constraint.solve(testShapes);
				if (constraint.getName().equals("ContainsText")) {
					
					// System.out.println("constraint confidence = " +
					// constraintConfidence);
					
				}// else {
				// System.out.println("b: " + shapeDef.getName() + " " +
				// constraint.getName() + " constraintConfidence = " +
				// constraintConfidence + " " +
				// (!constraint.isClearlyFalse(constraintConfidence)));
				// System.out.print("c: ");
				// for(IConstrainable s : testShapes){
				// System.out.print("  " + s.getShapeType() + ": " +
				// s.getBoundingBox());
				// }System.out.println("");
				; // if(constraintConfidence <= 0){
				// return 0;
				// }
				
				if (constraint.isClearlyFalse(constraintConfidence)) {
					return -1;
				}
				if (log.isDebugEnabled()
				    && m_debugShapeSet.isDebugShape(shapeDef)) {
					log.debug(constraint.getName() + " confidence "
					          + constraintConfidence);
				}
				confidence += constraintConfidence;
				items++;
			}
			catch (IndexOutOfBoundsException e) {
				// e.printStackTrace();
			}
			
		}
		if (log.isDebugEnabled() && m_debugShapeSet.isDebugShape(shapeDef)) {
			log.debug(shapeDef.getName() + " confidence before " + confidence);
		}
		
		if (items == 0)
			return 0;
		confidence /= items;
		
		if (log.isDebugEnabled() && m_debugShapeSet.isDebugShape(shapeDef)) {
			log.debug(shapeDef.getName() + " confidence after " + confidence);
		}
		
		return confidence;
	}
	

	private BuiltShape computeConstraintProbability(
	        ArrayList<ArrayList<Integer>> candidateList,
	        ArrayList<IShape> candidates, ShapeDefinition shapeDef,
	        ArrayList<ComponentDefinition> slotNames) throws OverTimeException,
	        InstantiationException, IllegalAccessException,
	        ClassNotFoundException {
		
		BuiltShape builtShape;
		double highestConfidence = -1;
		ArrayList<ArrayList<Integer>> bestCombinations = new ArrayList<ArrayList<Integer>>();
		for (ArrayList<Integer> combination : candidateList) {
			
			// Check if we've gone over time
			OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
			
			double confidence = computeConstraintConfidence(shapeDef,
			        combination, slotNames, candidates);
			if (confidence < 0) {
				continue;
			}
			if (confidence > highestConfidence) {
				highestConfidence = confidence;
				bestCombinations.clear();
				bestCombinations.add(combination);
			}
			else if (confidence == highestConfidence) {
				bestCombinations.add(combination);
			}
		}
		
		if (bestCombinations.size() > 0) {
			builtShape = new BuiltShape();
			builtShape.setLabel(shapeDef.getName());
			int slot = 0;
			
			for (Integer componentChoice : bestCombinations.get(0)) {
				
				// Check if we've gone over time
				OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
				
				IShape candidate = candidates.get(componentChoice);
				String componentname = slotNames.get(slot).getName();
				
				// TODO does setting the desc of the subshape here conflict with
				// anything else?
				// The problem is that candidates can be processed for more than
				// one slot. Putting the description might over-write something
				// else's component name.
				candidate.setDescription(componentname);
				
				slot++;
				builtShape.addSubShape(candidate);
				builtShape.setComponent(componentname, candidate);
			}
			
			builtShape.setConfidence(highestConfidence);
			// if(highestConfidence < 0){return null;}
			return builtShape;
		}
		return null;
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
			return new ConstrainablePoint(parent.getFirstStroke().getFirstPoint(), parent);
		}
		else if (csp == ComponentSubPart.End2) {
			return new ConstrainablePoint(parent.getLastStroke().getLastPoint(), parent);
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
	

	private void printCombinations(ArrayList<ArrayList<Integer>> combinations) {
		for (ArrayList<Integer> combination : combinations) {
			for (Integer componentChoice : combination) {
				System.out.print(componentChoice + " ");
			}
			System.out.println("");
		}
	}
	

	private ArrayList<IShape> reduceCandidateList(ArrayList<IShape> candidates,
	        ShapeDefinition shapeDef) throws OverTimeException {
		
		// how many components of each shape type are in the shape def
		HashMap<String, Integer> componentCount = new HashMap<String, Integer>();
		// how many candidate shapes of each shape type are in the pool
		HashMap<String, Integer> candidateCount = new HashMap<String, Integer>();
		
		// Count the components of each shape type in the shape definition
		for (ComponentDefinition component : shapeDef.getComponentDefinitions()) {
			// Check if we've gone over time
			OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
			
			if (componentCount.get(component.getShapeType()) == null) {
				componentCount.put(component.getShapeType(), 1);
			}
			else {
				componentCount.put(component.getShapeType(), componentCount
				        .get(component.getShapeType()) + 1);
			}
		}
		
		// count how many shapes of each type we have, BY LABEL
		// TODO count by isA as well?
		for (IShape candidate : candidates) {
			
			// Check if we've gone over time
			OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
			
			if (candidateCount.get(candidate.getLabel()) == null) {
				candidateCount.put(candidate.getLabel(), 1);
			}
			else {
				candidateCount.put(candidate.getLabel(), candidateCount
				        .get(candidate.getLabel()) + 1);
			}
		}
		
		// for each component in the shape definition, make sure we have enough
		// shapes of the same type.
		for (String componentType : componentCount.keySet()) {
			
			// Check if we've gone over time
			OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
			
			// no shapes of this type
			if (candidateCount.get(componentType) == null) {
				setFailed(true);
			}
			// not enough shapes of this type
			else if (candidateCount.get(componentType) < componentCount
			        .get(componentType)) {
				setFailed(true);
			}
		}
		
		// we already know we've failed because we don't have enough of the
		// right type of shapes. Kill it now.
		if (isFailed()) {
			return null;
		}
		
		// // for each type of shape that we have in the pool of candidates
		// for (String candidateType : candidateCount.keySet()) {
		//			
		// // Check if we've gone over time
		// OverTime.overTimeCheck(m_startTime, m_maxTime, log);
		//			
		// // // If we do NOT require components of this type, meaning there are
		// // // no components of this type counted
		// // if (componentCount.get(candidateType) == null) {
		// // // loop over the pool
		// // for (int i = candidates.size() - 1; i >= 0; i--) {
		// // // shape i from the pool
		// // IShape candidate = candidates.get(i);
		// // // if this shape's type is the kind that we're not looking
		// // // for (the if-null check above)
		// // if (candidate.getLabel().equals(candidateType)) {
		// // if (i == candidates.size() - 1) {
		// // return candidates;
		// // }
		// // // remove candidates that don't fit into a slot
		// // candidates.remove(candidate);
		// // }
		// // }
		// // }
		//			
		// // Remove candidates from the candidate pool if we don't need them
		// // for this shape
		//			
		// }
		
		// loop over the shape pool and remove any candidates that we don't
		// need. Example: if the pool has an ellipse, but our shape def doesn't
		// call for ellipses, remove the ellipse from the pool.
		for (Iterator<IShape> candidateIter = candidates.iterator(); candidateIter
		        .hasNext();) {
			// for each candidate in the candidate list (shape pool)
			IShape candidate = candidateIter.next();
			// if we don't require components of this candidate's shape type
			if (componentCount.get(candidate.getLabel()) == null) {
				candidateIter.remove();
			}
		}
		
		// System.out.println("components");
		// for(ComponentDefinition component :
		// shapeDef.getComponentDefinitions()){
		// / System.out.println(" component = " + component.getShapeType());
		// }
		
		// System.out.println("candidates");
		// for(IShape candidate : candidates){
		// System.out.println(" candidate = " + candidate.getLabel());
		// }
		return candidates;
	}
	

	public ArrayList<ArrayList<Integer>> computeCandidateList(
	        ArrayList<IShape> candidates, ShapeDefinition shapeDef,
	        ArrayList<ComponentDefinition> slotNames,
	        ArrayList<ComponentDefinition> componentDefinitions)
	        throws OverTimeException, InstantiationException,
	        IllegalAccessException, ClassNotFoundException {
		
		// component listing:
		// 1234 1235 1236 1237 1238 1243
		// for 1 2 3 4 5 6 7 8 first slot
		// then divide and try 11 12 13 14 15 16 17 18 ...
		ArrayList<ArrayList<Integer>> combinations = new ArrayList<ArrayList<Integer>>();
		// Integer confidence = 0;
		// if(candidates.size()>11)
		// return combinations;
		ArrayList<Integer> combination = new ArrayList<Integer>();
		// combination.add(confidence);
		double confidence = computeConstraintConfidence(shapeDef, combination,
		        slotNames, candidates);
		if (confidence >= 0) {
			combinations.add(combination);
		}
		if (combinations.size() == 0) {
			return new ArrayList<ArrayList<Integer>>();
		}
		return recurseCandidates(combinations, candidates, shapeDef, slotNames,
		        componentDefinitions);
	}
	

	private ArrayList<ComponentDefinition> computeSlotArray(
	        ShapeDefinition shapeDef) {
		
		ArrayList<ComponentDefinition> slotNames = new ArrayList<ComponentDefinition>(
		        shapeDef.getComponentDefinitions());
		// int i = 0;
		// for (ComponentDefinition cd : shapeDef.getComponentDefinitions()) {
		// slotNames.add(cd);
		// // System.out.println(i + " slotName = " + cd.getShapeType() + " " +
		// // cd.getName());
		// // i++;
		// }
		// int j=0;
		// for(ComponentDefinition slot: slotNames){
		// System.out.println("compDef: " + j + " = " + slot.getShapeType() +
		// " " + slot.getName());
		// j++;
		// }
		
		return slotNames;
	}
	
	private final int CUTOFF = 50000;
	
	private final int LARGER_CUTOFF = 500000;
	
	
	private ArrayList<ArrayList<Integer>> recurseCandidates(
	        ArrayList<ArrayList<Integer>> combinations,
	        List<IShape> candidates, ShapeDefinition shapeDef,
	        ArrayList<ComponentDefinition> slotNames,
	        ArrayList<ComponentDefinition> componentDefinitions)
	        throws OverTimeException {
		
		ArrayList<ArrayList<Integer>> newcombinations = new ArrayList<ArrayList<Integer>>();
		
		boolean needsLarger = shapeDef.getName().equalsIgnoreCase("speaker")
		                      || shapeDef.getName().indexOf(
		                              "ObstacleBypassLane") > 0;
		final int myCutoff = needsLarger ? LARGER_CUTOFF : CUTOFF;
		
		if (combinations.size() > myCutoff)
			return newcombinations;
		// System.out.println("m_candidates.size =" + candidates.size());
		
		for (ArrayList<Integer> combination : combinations) {
			
			// Check if we've gone over time
			OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
			
			// each slot can have each candidate in it.
			for (int i = 0; i < candidates.size(); i++) {
				
				// Check if we've gone over time
				OverTimeCheckHelper.overTimeCheck(m_startTime, m_maxTime, log);
				
				// System.out.println("creating combination");
				// System.out.print("Processing slot " + combination.size() +
				// ":");
				// System.out.print("about to put a " + i);
				// System.out.print(":  Expecting a " +
				// componentDefinitions.get(combination.size()).getShapeType());
				// System.out.println("But the candidate is a " +
				// candidates.get(i).getLabel());
				if (!componentDefinitions.get(combination.size())
				        .getShapeType().equals(candidates.get(i).getLabel())) {
					// System.out.println("Expecting and seeing different");
					continue;
				}
				if (!combination.contains(i)) {
					ArrayList<Integer> newcombination = (ArrayList<Integer>) combination
					        .clone();
					newcombination.add(i);
					/*
					 * if (newcombination.size() == slotNames.size()) { if
					 * (!newcombination.contains(candidates.size() - 1)) { //
					 * for(Integer icom : newcombination){ //
					 * System.out.print(icom + " "); // } //
					 * System.out.println("didn't have last element " + //
					 * (candidates.size() - 1)); continue; } }
					 */
					// System.out.println("adding combination");
					newcombinations.add(newcombination);
					if (newcombinations.size() > myCutoff)
						return new ArrayList<ArrayList<Integer>>();
				}
			}
		}
		if (newcombinations.size() == 0) {
			return new ArrayList<ArrayList<Integer>>();
		}
		if (newcombinations.get(0).size() == slotNames.size()) {
			return newcombinations;
		}
		else {
			return recurseCandidates(newcombinations, candidates, shapeDef,
			        slotNames, componentDefinitions);
		}
	}
	
}
