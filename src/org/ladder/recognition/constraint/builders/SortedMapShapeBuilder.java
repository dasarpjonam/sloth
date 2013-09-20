package org.ladder.recognition.constraint.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.tools.gui.event.BuildShapeDataEvent;

public class SortedMapShapeBuilder extends MappedShapeBuilder {
	private static Logger log = LadderLogger.getLogger(MappedShapeBuilder.class);
	
	private class ComponentFit implements Comparable {
		
		private int m_row;
		private int m_col;
		private double m_confidence;
		
		public ComponentFit(int row, int col, double confidence) {
			m_row = row;
			m_col = col;
			m_confidence = confidence;
		}
				
		public String toString() {
			return "Row: " + m_row + " Col: " + m_col + " Confidence: " + m_confidence;
		}
		
		public double getConfidence() {
			return m_confidence;
		}
		
		public int getRow() {
			return m_row;
		}
		
		public int getCol() {
			return m_col;
		}

		@Override
		public int compareTo(Object o) {
			if (o instanceof ComponentFit) 
				return new Double(this.m_confidence).compareTo(((ComponentFit)o).getConfidence());
			return -1;
		}
	}
	
	private ComponentFit[] m_fits;
	private List<List<ComponentFit>> m_sortedCols;
	
	public SortedMapShapeBuilder(List<IShape> candidates,
			ShapeDefinition shapeDef) throws IllegalArgumentException {
		super(candidates, shapeDef);
		m_fits  = new ComponentFit[components.size()];
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
		sortAllColumns();
		
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
		boolean buildable = true;
		
		while (buildable) {
			for (String component : components) {	
				int compIndex = components.indexOf(component);
				// TODO Complete algorithm
			}
		}
		return false;
	}
	
	
	private List<ComponentFit> sortColumn(int col) {
		List<ComponentFit> confs = new ArrayList<ComponentFit>();
		for (int i = 0; i < candidateToComponentMap.length; i++)
			confs.add(new ComponentFit(i, col, candidateToComponentMap[i][col]));
		Collections.sort(confs);
		return confs;
	}
	
	private void sortAllColumns() {
		m_sortedCols = new ArrayList<List<ComponentFit>>();
		for (int i = 0; i < candidateToComponentMap[0].length; i++) {
			m_sortedCols.add(sortColumn(i));
		}
	}
}