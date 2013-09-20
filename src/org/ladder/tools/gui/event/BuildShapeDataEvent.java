package org.ladder.tools.gui.event;

import java.util.List;

import org.ladder.core.sketch.IShape;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.domains.ShapeDefinition;

public class BuildShapeDataEvent implements BuildShapeEvent {
	
	private List<IShape> m_candidates;
	private List<String> m_components;
	private List<IConstraint> m_constraints;
	private double[][] m_constraintsMap;
	private double[][] m_componentsMap;
	private ShapeDefinition m_shapeDef;
	
	public BuildShapeDataEvent(List<IShape> candidates, List<String> components,
			List<IConstraint> constraints, double[][] constraintsMap, 
			double[][] componentsMap, ShapeDefinition shapeDef) {
		
		m_candidates = candidates;
		m_components = components;
		m_constraints = constraints;
		m_constraintsMap = constraintsMap;
		m_componentsMap = componentsMap;
		m_shapeDef = shapeDef;
		
	}

	public List<IShape> getCandidates() {
		return m_candidates;
	}

	public List<String> getComponents() {
		return m_components;
	}

	public List<IConstraint> getConstraints() {
		return m_constraints;
	}

	public double[][] getConstraintsMap() {
		return m_constraintsMap;
	}

	public double[][] getComponentsMap() {
		return m_componentsMap;
	}

	public ShapeDefinition getShapeDef() {
		return m_shapeDef;
	}
	
}
