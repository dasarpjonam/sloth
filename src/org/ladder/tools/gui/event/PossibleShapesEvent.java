package org.ladder.tools.gui.event;

import java.util.List;
import java.util.Set;

import org.ladder.core.sketch.IShape;
import org.ladder.recognition.constraint.domains.ShapeDefinition;

public class PossibleShapesEvent implements BuildShapeEvent {

	private List<ShapeDefinition> m_shapes;
	private boolean m_none = false;
	private String m_failureReason = "";
	private String m_forShape;
	private Set<IShape> m_components;
	
	public PossibleShapesEvent(final List<ShapeDefinition> shapes, final Set<IShape> components, String forShape) {
		m_shapes = shapes;
		m_components = components;
		m_forShape = forShape;
	}
	
	public PossibleShapesEvent(String failureReason, final Set<IShape> components, String forShape) {
		m_failureReason = failureReason;
		m_none = true;
		m_forShape = forShape;
		m_components = components;
	}
	
	public String getForShape() {
		return m_forShape;
	}

	public Set<IShape> getComponents() {
		return m_components;
	}



	public boolean isNone() {
		return m_none;
	}

	public String getFailureReason() {
		return m_failureReason;
	}

	public List<ShapeDefinition> getShapes() {
		return m_shapes;
	}
}
