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

public class ExponentialShapeBuilder extends MappedShapeBuilder {
	private static Logger log = LadderLogger.getLogger(MappedShapeBuilder.class);
	
	private class ComponentFit implements Comparable {
		
		private int m_row;
		private int m_col;
		private double m_confidence;
		
		public ComponentFit(int row, int col) {
			m_row = row;
			m_col = col;
			m_confidence = candidateToComponentMap[row][col];
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
	
	private class ShapeFit implements Comparable {
		
		private List<ComponentFit> m_fits = new ArrayList<ComponentFit>();
		private boolean m_failed = false;
		private List<Integer> m_colsUsed = new ArrayList<Integer>();
		private List<Integer> m_rowsUsed = new ArrayList<Integer>();
		
		public ShapeFit() {
			
		}
		
		public ShapeFit clone() {
			ShapeFit clone = new ShapeFit();
			for (ComponentFit cf : m_fits) {
				clone.addComponentFit(cf);
			}
			return clone;
		}
		
		public double getConfidence() {
			if (m_failed)
				return 0;
			
			double sum = 0;
			for (ComponentFit cf : m_fits)
				sum += cf.getConfidence();
			return sum/((double)m_fits.size());
		}

		@Override
		public int compareTo(Object o) {
			if (o instanceof ShapeFit) 
				return new Double(this.getConfidence()).compareTo(((ShapeFit)o).getConfidence());
			return -1;
		}
		
		public void addComponentFit(ComponentFit fit) {
			m_fits.add(fit);
			m_rowsUsed.add(fit.getRow());
			m_colsUsed.add(fit.getCol());
		}
		
		public List<ComponentFit> getFits() {
			return m_fits;
		}

		public boolean isFailed() {
			return m_failed;
		}

		public void setFits(List<ComponentFit> fits) {
			m_fits = fits;
		}

		public void setFailed(boolean failed) {
			m_failed = failed;
		}

		public ArrayList<Integer> getColsUsed() {
			return (ArrayList<Integer>)m_colsUsed;
		}

		public ArrayList<Integer> getRowsUsed() {
			return (ArrayList<Integer>)m_rowsUsed;
		}
		
		public String toString() {
			return "Confidence: " + this.getConfidence();
		}
		
	}
	
	private List<ShapeFit> m_shapeFits;
	private List<List<ComponentFit>> m_sortedCols;
	
	public ExponentialShapeBuilder(List<IShape> candidates,
			ShapeDefinition shapeDef) throws IllegalArgumentException {
		super(candidates, shapeDef);
		m_shapeFits = new ArrayList<ShapeFit>();
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
		
		//boolean succeeded = 
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
			if(d <= 0){return null;}
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
	
	private void pick(ShapeFit fit) {
		List<Integer> prevCols = (List<Integer>) fit.getColsUsed().clone();
		List<Integer> cols = range(components.size());
		cols.removeAll(prevCols);
		for (Integer col : cols) {
			// Setting col used and row used happens when I pick an element
			List<Integer> prevRows = (List<Integer>) fit.getRowsUsed().clone();
			List<Integer> rows = range(candidates.size());
			rows.removeAll(prevRows);
			for (Integer row: rows) {
				// ShapeType check
				if (shapeDef.getComponentDefinition(components.get(col)).getShapeType().equals(candidates.get(row).getLabel())) {
					fit.addComponentFit(new ComponentFit(row, col));
					pick(fit.clone());
				}
			}
		}
		cols = range(components.size());
		cols.removeAll(fit.m_colsUsed);
		if (cols.size() == 0)
			m_shapeFits.add(fit);
	}
	
	private void buildForFit(ShapeFit fit) {
		for (ComponentFit cfit : fit.getFits()) {
			finalComponents.put(components.get(cfit.getCol()), candidates.get(cfit.getRow()));
			confidence.add(candidateToComponentMap[cfit.getRow()][cfit.getCol()]);
		}
		//System.out.println(finalComponents);
	}
	
	private boolean buildBestShape() {
		ShapeFit sf = new ShapeFit();
		pick(sf);
		//System.out.println(shapeDef.getName() + " Pick finished.");
		Collections.sort(m_shapeFits);
		//System.out.println(m_shapeFits);
		if (m_shapeFits.get(0).getConfidence() == 0) {
			return false;
		}
		//System.out.println(m_shapeFits.get(0));
		buildForFit(m_shapeFits.get(0));
		return true;
	}
	
	private List<ComponentFit> sortColumn(int col) {
		List<ComponentFit> confs = new ArrayList<ComponentFit>();
		for (int i = 0; i < candidateToComponentMap.length; i++)
			confs.add(new ComponentFit(i, col));
		Collections.sort(confs);
		return confs;
	}
	
	private void sortAllColumns() {
		m_sortedCols = new ArrayList<List<ComponentFit>>();
		for (int i = 0; i < candidateToComponentMap[0].length; i++) {
			m_sortedCols.add(sortColumn(i));
		}
	}
	
	private List<Integer> range(int start, int end) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = start; i < end; i++) {
			list.add(new Integer(i));
		}
		return list;
	}
	
	private List<Integer> range(int end) {
		return range(0, end);
	}
}
