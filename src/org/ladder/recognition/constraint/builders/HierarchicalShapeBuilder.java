package org.ladder.recognition.constraint.builders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.confidence.UnaryConstraint;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.ComponentSubPart;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.ConstraintParameter;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;

public class HierarchicalShapeBuilder extends ShapeBuilder {
	
	private static Logger log = LadderLogger.getLogger(HierarchicalShapeBuilder.class);
	
	private DomainDefinition domainDef;
	
	private BuiltShape built = null;
	
	Map<String, BuiltShape> builtSubComponents = new HashMap<String, BuiltShape>();

	public HierarchicalShapeBuilder(List<IShape> candidates,
			ShapeDefinition shapeDef, DomainDefinition domainDefinition) throws IllegalArgumentException {
		super(candidates, shapeDef);
		domainDef = domainDefinition;
	}
	
	public BuiltShape buildHierarchicalShape() {
		log.debug("bHS Method Called.");
		if (built != null) {
			return built;
		}
		
		
		int primitives = 0;
		for (ComponentDefinition compDef : shapeDef.getComponentDefinitions()) {
			if (isPrimitive(compDef.getShapeType())) {
				// No Children -- Probably a Primitive
				log.debug("Adding a primitive count for: " + compDef.getName() + " of type " + compDef.getShapeType());
				primitives++;
			} else if (compDef.getChildren().size() == 0) {
				// This guy is composed of primitives.
				List<IShape> subshapeCandidates = new ArrayList<IShape>();
				subshapeCandidates.addAll(getAllPrimitiveCandidates());
				log.debug("Building a " + compDef.getShapeType() + " with " + subshapeCandidates + " of " + candidates);
				BuiltShape b = new ShapeBuilder(subshapeCandidates, getShapeDef(compDef)).buildShape();
				log.debug("Adding a built shape (of primitives) for: " + compDef.getName() + " as " + b);
				builtSubComponents.put(compDef.getName(), b);
			} else {
				// build a subshape with ShapeBuilder
				List<IShape> subshapeCandidates = new ArrayList<IShape>();
				// TODO make it only the right types of primitives for this compDef
				// then also worry on the use of subshapeCandidates for the next use inside
				// the for loop below. The child shapes might have high level children, but
				// all should be able to be assembled from the primitives we have and the
				// children can't require high level shapes from this shape definition
				subshapeCandidates.addAll(getAllPrimitiveCandidates());
				
				for (ComponentDefinition compDefinition : compDef.getChildren()) {
					if (!builtSubComponents.containsKey(compDefinition.getName())) {
						BuiltShape b = new HierarchicalShapeBuilder(subshapeCandidates, getShapeDef(compDef), domainDef).buildHierarchicalShape();
						log.debug("Adding a built [sub]shape for: " + compDef.getName() + "." +compDefinition.getName() + " as " + b);
						builtSubComponents.put(compDefinition.getName(), b);
						subshapeCandidates.add(b);

					} else {
						subshapeCandidates.add(builtSubComponents.get(compDefinition.getName()));
					}
				}
				
				// TODO find which candidates are which
				BuiltShape b = new HierarchicalShapeBuilder(subshapeCandidates, getShapeDef(compDef), domainDef).buildHierarchicalShape();
				log.debug("Adding a built shape for: " + compDef.getName() + " as " + b);
				builtSubComponents.put(compDef.getName(), b);
			}
		}
		
		// Compute the constraints on the child shapes we just sorted out and the primitives we
		// have left. Then assemble and return a BuiltShape.
		computeConstraints();
		if (!areAllConstraintsSatisfied()) {
//			log.debug("Failed to assemble: " + shapeDef.getName());
			this.constraintFailure = true;
			if (debugShape.equalsIgnoreCase(shapeDef.getName())) {
				this.buildFailureReport();
				this.logReport();
			}
			return null;
		}
		
		if (this.getComponentShapes() == null) {
//			log.debug("Failed to assemble: " + shapeDef.getName() + " gCS null");
			this.emptyComponentsList = true;
			if (debugShape.equalsIgnoreCase(shapeDef.getName())) {
				this.buildFailureReport();
				this.logReport();
			}
			return null;
		}
		
		List<IShape> subShapes = new ArrayList<IShape>();
		//System.out.println(finalComponents);
		subShapes.addAll(this.getComponentShapes());
		builtShape.setSubShapes(subShapes);
		double conf = 0;
		double sum = 0;
		for (Double d : confidence) {
			sum += d;
		}
		double didNotUseAllThePiecesAdjustment = 0;
		if (subShapes.size() < candidates.size()) {
			didNotUseAllThePiecesAdjustment = .10 * ((candidates
			        .size() - subShapes.size()));
		}
		conf = (sum / (double) confidence.size())
		       - didNotUseAllThePiecesAdjustment;
		if (conf < 0) {
			conf = 0.0;
		}
		
		if (subShapes.size() == candidates.size() && conf < 0.84001) {
			conf += .13;
		}
		
		// If there are a ton of lines in Candidate shapes, then low level stuff isn't working
		// and the adjustment heuristic should be turned off. IMHO RG.
		if (candidates.size() > 7 + shapeDef.getNumComponents()) {
			conf = (sum / (double) confidence.size());
		}
		
		report = "[ShapeBuilder#buildShape] Confidence for " + builtShape.getLabel()
		                   + " is " + conf + " which was adjusted by: " + didNotUseAllThePiecesAdjustment;
		builtShape.setConfidence(conf);
		builtShape.setComponents(finalComponents);
		//this.printReport();
		return builtShape;
		
	}
	
	private IShape lookupParameter(String parameter) throws Exception {
		if (parameter.contains(".")) {
			String[] params = parameter.split("[.]");
			//log.debug("Looking for: " + params[0] + " of " + parameter + " in " + this.builtSubComponents);
			BuiltShape component = builtSubComponents.get(params[0]);
			//log.debug("Initial Component: " + component);
			for (int i = 1; i < params.length; i++) {
				if (component.getComponent(params[i]) instanceof BuiltShape) {
					// Safe to cast
					component = (BuiltShape) component.getComponent(params[i]);
					//log.debug("New Component: " + component);
				} else {
					// Can't cast, but the IShape should be a primitive and
					// it's only a problem if we have more in params[] and that
					// last bit isn't a ComponentSubPart
					if (i < (params.length-2)) {
						// More than one thing left
						throw new Exception("Parameter: " + parameter + " cannot be found.");
					} else if (((i+2) == params.length) && 
							(ComponentSubPart.fromString(params[i+1])) != ComponentSubPart.None) {
						throw new Exception("Parameter" + parameter + " cannot be returned. You must build your own IConstrainables.");
					} else {
						//log.debug("Returning Component: " + component);
						return component.getComponent(params[i]);
					}
				}
			}
			//log.debug("Returning Component: " + component);
			return component;
		} else {
			if (builtSubComponents.containsKey(parameter)) {
				//log.debug("Returning Component: " + builtSubComponents.get(parameter));
				return builtSubComponents.get(parameter);
			}
			//log.debug("Returning null");
			return null;
		}
	}
	
	private ComponentSubPart getComponentSubPart(String param) {
		String[] params = param.split(".");
		if (ComponentSubPart.fromString(params[params.length-1]) != ComponentSubPart.None) {
			return ComponentSubPart.fromString(params[params.length-1]);
		} else {
			log.debug("Probably asking for a ComponentSubPart on a string that isn't meant to have one: " + param);
			return ComponentSubPart.None;
		}
	}
	
	private String truncateComponentSubPart(String param) {
		String[] params = param.split(".");
		if (ComponentSubPart.fromString(params[params.length-1]) != ComponentSubPart.None) {
			String p = "";
			for (int i = 0; i < params.length-1; i++)
				p += params[i] + ".";
			return p.substring(0, p.length()-2);
		} else {
			return param;
		}
	}
	
	private ShapeDefinition getShapeDef(ComponentDefinition compDef) {
		ShapeDefinition shapeDef = new ShapeDefinition(compDef.getName());
		shapeDef.addIsA(compDef.getShapeType());
		shapeDef.addComponentDefinition(compDef);
		return shapeDef;
	}
	
	private boolean isPrimitive(ComponentDefinition compDef) {
		return isPrimitive(compDef.getShapeType());
	}
	
	private boolean isPrimitive(String shapeType) {
		if (domainDef.contains(shapeType)) {
			return false;
		}
		return true;
	}
	
	private List<IShape> getCandidatesOfType(String shapeType) {
		List<IShape> shapes = new ArrayList<IShape>();
		for (IShape s : candidates) {
			if (s.getLabel() == shapeType) {
				shapes.add(s);
			}
		}
		return shapes;
	}
	
	private List<IShape> getAllPrimitiveCandidates() {
		List<IShape> shapes = new ArrayList<IShape>();
		for (IShape s : candidates) {
			if (isPrimitive(s.getLabel())) {
				shapes.add(s);
			}
		}
		return shapes;
	}
	
	protected void computeConstraints() {
		for (ConstraintDefinition constraintDef : shapeDef.getConstraintDefinitions()) {
			// check that constraint is satisfied, return null if not
			IConstraint constraint = defToConstraint.get(constraintDef);
			
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
					testShape = buildParameterizedIConstrainable(testShape, constraintParam);
					testShapes.add(testShape);
					
					
					
					double constraintConfidence = constraint.solve(testShapes);
					
					//TODO remove
					constraintInfo.put(constraint,
							constraintDef.getParameters().get(0).getComponent() +
							" " + constraintDef.getParameters().get(0).getComponentSubPart().name());
					
					this.setConstraintConfidence(s1, constraint, constraintConfidence);
					
					if(constraint.isClearlyFalse(constraintConfidence)){
						constraintConfidence = -1;
					}
				//	if (0.35 < constraintConfidence) {
						confidence.add(new Double(constraintConfidence));
						//System.out.println(constraintParam.getComponent() + " gets " + testShapeCopy.getShapeType() + " " + testShapeCopy.toString());
						
						// System.out.println(
						// "[buildShape] UnaryConstraint satisfied: " +
						// constraintConfidence);
						continue;
				//	}
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
						IConstrainable testShape1, testShape2;
						
						
						
						log.debug("CP: " + cp + " CP2: " + cp2);
						try {
							log.debug("CP2: " + cp2.getComponent() + " " + shapeDef.getComponentDefinition(cp2.getComponent()) + " lookupParameter: " + lookupParameter(cp2.getComponent()));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						String shapeType = shapeDef.getComponentDefinition(
						        cp.getComponent()).getShapeType();
						String shapeType2 = shapeDef.getComponentDefinition(
						        cp2.getComponent()).getShapeType();
						if (s1.getLabel().equalsIgnoreCase(shapeType)
						    && s2.getLabel().equalsIgnoreCase(shapeType2)) {
							testShape1 = buildSubShape(s1);
							testShape2 = buildSubShape(s2);
						}
						else if (s1.getLabel().equalsIgnoreCase(shapeType2)
						         && s2.getLabel().equalsIgnoreCase(shapeType)) {
							testShape1 = buildSubShape(s2);
							testShape2 = buildSubShape(s1);
	
						} else {
	
							continue;
						}
						
						try {
							testShape1 = buildParameterizedIConstrainable(
							        testShape1, cp);
							testShape2 = buildParameterizedIConstrainable(
							        testShape2, cp2);
						}
						catch (IllegalArgumentException e) {
							// TODO log this
							e.printStackTrace();
						}
						testShapes.add(testShape1);
						testShapes.add(testShape2);
						double constraintConfidence = constraint
						        .solve(testShapes);
						
						//TODO remove
						constraintInfo.put(constraint,
								constraintDef.getParameters().get(0).getComponent() +
								" " + constraintDef.getParameters().get(0).getComponentSubPart().name() + 
								" " + constraintDef.getParameters().get(1).getComponent() +
								" " + constraintDef.getParameters().get(1).getComponentSubPart().name());
						
						this.setConstraintConfidence(s1, constraint, constraintConfidence);
						
						if (0.40 < constraintConfidence) {
							confidence.add(new Double(constraintConfidence));
							this.setConstraintConfidence(s2, constraint, constraintConfidence);
	
							//System.out.println(cp.getComponent() + " gets " + testShape1Copy.getShapeType() + " " + testShape1Copy.toString());
							//System.out.println(cp2.getComponent() + " gets " + testShape2Copy.getShapeType() + " " + testShape2Copy.toString());
							
							// System.out.println(
							// "[buildShape] Constraint satisfied: " +
							// constraintConfidence);
							break;
	
						} else { 
							// System.out.println(
							// "[buildShape] Constraint NOT satisfied: " +
							// constraintConfidence);
						}
					}
				}
			}
			
			// Output that explains failing constraints. Should be logging this?
			/*if (!satisfied) {
				System.out.print("[ShapeBuilder#computeConstraints] Unsatisfied constraint for "
				                 + shapeDef.getName() + " "
				                 + constraintDef.getName() + "\n");
				for (ConstraintParameter cp : constraintDef.getParameters()) {
					System.out.print("\tParameter: " + cp.getComponent() + "."
					                 + cp.getComponentSubPart() + "\n");
				}
			}*/
		}
		
		satisfiedConstraints = true;
	}

}
