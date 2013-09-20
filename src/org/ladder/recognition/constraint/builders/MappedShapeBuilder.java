package org.ladder.recognition.constraint.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.tools.gui.event.BuildShapeDataEvent;
import org.ladder.tools.gui.event.BuildShapeEventListener;

public class MappedShapeBuilder extends ShapeBuilder {
	
	private static Logger log = LadderLogger.getLogger(MappedShapeBuilder.class);
	protected double[][] candidateToComponentMap;
	private List<BuildShapeEventListener> listeners = new ArrayList<BuildShapeEventListener>();

	public MappedShapeBuilder(List<IShape> candidates, ShapeDefinition shapeDef)
			throws IllegalArgumentException {
		super(candidates, shapeDef);
		
		candidateToComponentMap = new double[candidates.size()][components.size()];
	}
	
	/**
	 * Put it all together and return the shape we built or null. Compute the final
	 * confidence.
	 * 
	 * @return The shape built from the definition and candidates or null.
	 */
	public BuiltShape buildShape() {
		if (built) {
			return builtShape;
		}
		
		computeConstraints();
		log.debug("Building " + shapeDef.getName());
		log.debug("Constraints computed.");
		computeSatisfied();

		//log.debug("Component satisfaction computed.");

		if (!buildBestShape()) {
			log.debug("Failed to assemble: " + shapeDef.getName());
			this.constraintFailure = true;
			if (debugShape.equalsIgnoreCase(shapeDef.getName()) || DEBUG) {
				this.buildFailureReport();
				this.logReport();
				fireBuildShapeDataEvent(new BuildShapeDataEvent(candidates, components, constraints, graph, candidateToComponentMap, shapeDef));
			}
			return null;
		}
		
		if (this.getComponentShapes() == null) {
			log.debug("Failed to assemble: " + shapeDef.getName() + " gCS null");
			this.emptyComponentsList = true;
			if (debugShape.equalsIgnoreCase(shapeDef.getName()) || DEBUG) {
				this.buildFailureReport();
				this.logReport();
				fireBuildShapeDataEvent(new BuildShapeDataEvent(candidates, components, constraints, graph, candidateToComponentMap, shapeDef));
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
		conf = (sum / (double) confidence.size()) - didNotUseAllThePiecesAdjustment;
		
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
		
		report = "Confidence for " + builtShape.getLabel()
		                   + " is " + conf + " which was adjusted by: " + didNotUseAllThePiecesAdjustment;
		
		
		builtShape.setConfidence(conf);
		builtShape.setComponents(finalComponents);
		
		if (debugShape == shapeDef.getName()) {
			fireBuildShapeDataEvent(new BuildShapeDataEvent(candidates, components, constraints, graph, candidateToComponentMap, shapeDef));
		}
		
		built = true;
		return builtShape;
	}
	
	private boolean buildBestShape() {
		for (String component : components) {
			//log.debug("Finding best fit for " + component + " from " + candidates.size());
			List<Integer> allIndexes = new ArrayList<Integer>();
			for (int i = 0; i < candidates.size(); i++)
				allIndexes.add(i);
			Set<Integer> usedAlready = new TreeSet<Integer>();
			int indexOfBestCandidate = getBestCandidateForComponent(components.indexOf(component));
			//log.debug("Initial best candidate " + indexOfBestCandidate);
			// Verify this shape isn't in use already.
			usedAlready.add(indexOfBestCandidate);
			while (finalComponents.containsValue(candidates.get(indexOfBestCandidate))) {
				//log.debug("finalComponents has " + indexOfBestCandidate + " which is " + candidates.get(indexOfBestCandidate));
				//log.debug(finalComponents);
				indexOfBestCandidate = getNextBestCandidateForComponent(components.indexOf(component), usedAlready);
				usedAlready.add(indexOfBestCandidate);
				//log.debug("Next best candidate for " + component + " " + indexOfBestCandidate);
				//log.debug(usedAlready);
				// All options exhausted
				if (usedAlready.containsAll(allIndexes) || indexOfBestCandidate == -1) {
					this.failedComponent = component;
					return false;
				}
			}
			// Take the best available.
			IShape candidate = candidates.get(indexOfBestCandidate);
			// Check Shape Type
			if (shapeDef.getComponentDefinition(component).getShapeType().equalsIgnoreCase(candidate.getLabel())) {
				//log.debug("Selected a candidate for " + component + " " + candidate);
				confidence.add(candidateToComponentMap[candidates.indexOf(candidate)][components.indexOf(component)]);
				finalComponents.put(component, candidate);
				continue;
			}
			//log.error("Failed because the shape is the wrong type. " + candidate + " " + component);
			this.failedComponent = component;
			return false;
		}
		return true;
	}
	
	private int getNextBestCandidateForComponent(int componentIndex, Set<Integer> bestCandidates) {
		int maxIndex = 0;
		for (int i=0; i < candidates.size(); i++) {
			if ((!bestCandidates.contains(i)) && maxIndex != i) {
				//log.debug("Checking on " + i);
				//log.debug("Is [" + i + "][" + componentIndex + "] : " + candidateToComponentMap[i][componentIndex] + " > [" + maxIndex + "][" + componentIndex + "] : " + candidateToComponentMap[maxIndex][componentIndex] + "?");
				//log.debug("\t" + candidates.get(i) + " " + candidates.get(maxIndex));
				if (candidateToComponentMap[i][componentIndex] > candidateToComponentMap[maxIndex][componentIndex]) {
					//log.debug("Setting next best to: " + i);
					maxIndex = i;
				}
			} else {
				//log.debug("Skipping " + i + " because it was selected before.");
			}
		}
		if (!bestCandidates.contains(maxIndex)) {
			return maxIndex;
		} else {
			return -1;
		}
	}
	
	private int getBestCandidateForComponent(int componentIndex) {
		Set<Integer> set = new TreeSet<Integer>();
		return getNextBestCandidateForComponent(componentIndex, set);
	}

	/**
	 * Determine if this component's constraints were satisfied by the candidate.
	 * 
	 * @param componentName
	 * @return True if the constraints on this component for this candidate are satisfied and false otherwise
	 */
	protected void computeConstraintsSatisfiedForCandidate(String componentName, IShape candidate) {		
		List<IConstraint> constraintsForComponent = componentsToConstraints.get(componentName);
		int candidateIndex = candidates.indexOf(candidate);
		double sum = 0.0;
		for (IConstraint c : constraintsForComponent) {
			int constraintIndex = constraints.indexOf(c);
			if (graph[candidateIndex][constraintIndex] <= constraintThreshold) {
				//if (shapeDef.getName().equalsIgnoreCase(m_debugShape)) 
				//		System.out.println(candidate + " confidence too low for " + 
				//		componentName + " at: " + graph[candidateIndex][constraintIndex] +
				//		" for " + constraintInfo.get(c));
				
				this.failedConstraints.put(c, graph[candidateIndex][constraintIndex]);
				this.failedCandidates.put(c, candidate);
			}
			sum += graph[candidateIndex][constraintIndex];
		}

		setCandidateLikelihood(candidate, componentName, (sum / (double)constraintsForComponent.size()));
	}
	
	private void setCandidateLikelihood(IShape candidate, String component, double value) {
		int candidateIndex = candidates.indexOf(candidate);
		int componentIndex = components.indexOf(component);
		// Makes sure this is a larger value AND that the shape types match up...otherwise we can't use this
		// shape for a component with a different type.
		if (value > candidateToComponentMap[candidateIndex][componentIndex] &&
				shapeDef.getComponentDefinition(component).getShapeType().equalsIgnoreCase(candidate.getLabel())) {
			//log.debug("Setting " + candidate + " to have " + value + " confidence for " + component);
			candidateToComponentMap[candidateIndex][componentIndex] = value;
		}
		
	}
	
	/**
	 * Determine if this component's constraints were satisfied.
	 * 
	 * @param componentName
	 * @return True if the constraints on this component are satisfied and false otherwise
	 */
	public void computeConstraintsSatisfiedForComponent(String componentName) {
		for (IShape candidate : candidates) {
			this.computeConstraintsSatisfiedForCandidate(componentName, candidate);
		}
	}
	
	/**
	 * Determine if this shape's constraints were satisfied.
	 * 
	 * @return True if the constraints on this shape are satisfied and false otherwise
	 */
	public boolean computeSatisfied() {
		if (!satisfiedConstraints) {
			return false;
		}
		
		for (String component : components) {
			this.computeConstraintsSatisfiedForComponent(component);
		}
		return true;
	}
	
	public static String printMatrix(double[][] matrix) {
		String str = "";
		for (int i = 0; i < matrix.length; i++) {
			str += "[ ";
			for (int j = 0; j < matrix[i].length; j++) {
				str += String.valueOf(matrix[i][j]);
			}
			str += " ]\n";
		}
		return str;
	}
	
	public static String printMatrixWithLabels(List rows, List cols, double[][] matrix) {
		String str = "";
		
		for (int j = 0; j < matrix[0].length; j++) {
			str += cols.get(j).toString() + ",\t";
		}
		str += "\n";
		
		for (int i = 0; i < matrix.length; i++) {
			str += rows.get(i).toString() + " [ ";
			for (int j = 0; j < matrix[i].length; j++) {
				str += String.valueOf(matrix[i][j]).substring(0, 3);
				if (j != matrix[i].length-1) {
					str += ",\t";
				}
			}
			str += " ]\n";
		}
		return str;
	}
	
	/**
	 * Add a listener for BuildShape data and events. Used to create better debugging.
	 * @param bsel
	 */
	public void addBuildShapeEventListener(BuildShapeEventListener bsel) {
		listeners.add(bsel);
	}
	
	/**
	 * Add a listener for BuildShape data and events. Used to create better debugging.
	 * @param bsel
	 */
	public void addBuildShapeEventListeners(List<BuildShapeEventListener> bsel) {
		listeners.addAll(bsel);
	}
	
	/**
	 * Fire the event.
	 * @param bse
	 */
	public void fireBuildShapeDataEvent(BuildShapeDataEvent bse) {
		if (listeners.size() > 0) {
			for (BuildShapeEventListener listener : listeners)
				listener.handleBuildShape(bse);
		}
	}

}
