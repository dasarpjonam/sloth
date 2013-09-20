/**
 * ConstraintDefinitionPanel.java
 * 
 * Revision History:<br>
 * Dec 8, 2008 bpaulson - File created
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
package org.ladder.tools.gui.shapedef;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;

/**
 * Panel containing constraint definition information
 * 
 * @author bpaulson
 */
public class ConstraintDefinitionPanel extends JPanel {
	
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -2020333502212472699L;
	
	/**
	 * Shape definition GUI that this panel belongs to
	 */
	private ShapeDefinitionGUI m_gui = null;
	
	/**
	 * List of constraints for current shape
	 */
	private List<ConstraintEntry> m_constraints = new ArrayList<ConstraintEntry>();
	
	/**
	 * Current shape definition
	 */
	private ShapeDefinition m_currShape = null;
	
	/**
	 * Add new constraint button
	 */
	private JButton m_add = new JButton("Add Constraint");
	
	
	/**
	 * Constructor for constraint definition panel
	 * 
	 * @param gui
	 *            gui the panel belongs to
	 * @param shapeDef
	 *            current shape definition
	 */
	public ConstraintDefinitionPanel(ShapeDefinitionGUI gui,
	        ShapeDefinition shapeDef) {
		super();
		m_gui = gui;
		m_currShape = shapeDef;
		m_add.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				newConstraint();
			}
			
		});
		add(m_add);
		disablePanel();
	}
	

	/**
	 * Clear the panel
	 */
	public void clear() {
		// TODO
	}
	

	/**
	 * Enable the components of the panel
	 */
	public void enablePanel() {
		m_add.setEnabled(true);
	}
	

	/**
	 * Disable the components of the panel
	 */
	public void disablePanel() {
		m_add.setEnabled(false);
	}
	

	/**
	 * Set current shape definition
	 * 
	 * @param shapeDef
	 *            shape definition
	 */
	public void setShapeDef(ShapeDefinition shapeDef) {
		m_currShape = shapeDef;
	}
	

	/**
	 * Get a list of definitions that were defined on this panel
	 * 
	 * @return definitions defined on panel
	 */
	public List<ConstraintDefinition> getDefinitions() {
		List<ConstraintDefinition> defs = new ArrayList<ConstraintDefinition>();
		// TODO
		return defs;
	}
	

	/**
	 * Add a new constraint entry object to the panel
	 */
	private void newConstraint() {
		// TODO
	}
	
	/**
	 * Class containing GUI components for a single constraint entry
	 * 
	 * @author Brandon
	 */
	private class ConstraintEntry {
		
	}
	
}
