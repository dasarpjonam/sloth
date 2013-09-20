/**
 * AliasDefinitionPanel.java
 * 
 * Revision History:<br>
 * Dec 7, 2008 bpaulson - File created
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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.ladder.recognition.constraint.domains.AliasDefinition;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.ComponentSubPart;
import org.ladder.recognition.constraint.domains.ShapeDefinition;

/**
 * Panel containing alias information
 * 
 * @author bpaulson
 */
public class AliasDefinitionPanel extends JPanel {
	
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 1133563197080683600L;
	
	/**
	 * Default row count for grid layout
	 */
	private static final int DEFAULT_ROW_COUNT = 16;
	
	/**
	 * List of aliases for current shape
	 */
	private List<AliasEntry> m_aliases = new ArrayList<AliasEntry>();
	
	/**
	 * Current shape definition
	 */
	private ShapeDefinition m_currShape = null;
	
	/**
	 * Add new alias button
	 */
	private JButton m_add = new JButton("Add Alias");
	
	/**
	 * Layout manager
	 */
	private GridLayout m_layout;
	
	
	/**
	 * Constructor for alias definition panel
	 * 
	 * @param gui
	 *            shape definition gui this panel belongs to
	 * @param shapeDef
	 *            current shape definition
	 */
	public AliasDefinitionPanel(ShapeDefinitionGUI gui, ShapeDefinition shapeDef) {
		super();
		m_currShape = shapeDef;
		m_add.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				newAlias();
			}
			
		});
		add(m_add);
		m_layout = new GridLayout(DEFAULT_ROW_COUNT, 1);
		setLayout(m_layout);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(m_add);
		add(buttonPanel);
		disablePanel();
	}
	

	/**
	 * Enable the components of the panel
	 */
	public void enablePanel() {
		m_add.setEnabled(true);
		// TODO: uncomment
		// if (m_currShape == null
		// || m_currShape.getComponentDefinitions().size() == 0)
		// disablePanel();
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
	 * Clear the panel
	 */
	public void clear() {
		// TODO
	}
	

	/**
	 * Get a list of definitions that were defined on this panel
	 * 
	 * @return definitions defined on panel
	 */
	public List<AliasDefinition> getDefinitions() {
		List<AliasDefinition> defs = new ArrayList<AliasDefinition>();
		for (AliasEntry ae : m_aliases) {
			String name = ae.getNameField();
			String component = ae.getComponentField();
			if (component.contains(".")) {
				// parse out component sub part
				String subPart = component
				        .substring(component.lastIndexOf('.') + 1);
				String comp = component
				        .substring(0, component.lastIndexOf('.'));
				defs.add(new AliasDefinition(name, comp, ComponentSubPart
				        .fromString(subPart)));
			}
			else {
				defs.add(new AliasDefinition(name, component));
			}
		}
		return defs;
	}
	

	/**
	 * Add a new alias entry object to the panel
	 */
	private void newAlias() {
		AliasEntry comp = new AliasEntry(m_currShape);
		if (m_layout.getRows() < m_aliases.size() + 2)
			m_layout.setRows(m_layout.getRows() + 1);
		add(comp);
		m_aliases.add(comp);
		getParent().getParent().getParent().repaint();
	}
	
	/**
	 * GUI components for an alias entry
	 * 
	 * @author bpaulson
	 */
	private class AliasEntry extends JPanel {
		
		/**
		 * Serial UID
		 */
		private static final long serialVersionUID = 4813518333336310414L;
		
		/**
		 * Alias name
		 */
		private JTextField m_name = new JTextField(10);
		
		/**
		 * Component
		 */
		private JComboBox m_component = new JComboBox();
		
		/**
		 * Vector of strings which represent the component list
		 */
		private Vector<String> m_componentList = new Vector<String>();
		
		/**
		 * Remove button
		 */
		private JButton m_remove = new JButton("X");
		
		
		/**
		 * Constructor for alias entry
		 * 
		 * @param shapeDef
		 *            current shape definition (used to populate component list)
		 */
		public AliasEntry(ShapeDefinition shapeDef) {
			super();
			m_remove.setToolTipText("Remove alias.");
			// TODO: implement remove
			
			JLabel name = new JLabel("Name: ");
			JLabel component = new JLabel("Component: ");
			
			// add component names
			if (shapeDef != null)
				for (ComponentDefinition cd : shapeDef
				        .getComponentDefinitions())
					addComponents(null, cd);
			updateComponentList();
			
			// add components to panel
			add(m_remove);
			add(name);
			add(m_name);
			add(component);
			add(m_component);
		}
		

		/**
		 * Add component (and its sub components) to type list
		 * 
		 * @param parentString
		 *            parent string
		 * @param compDef
		 *            component definition to add
		 */
		public void addComponents(String parentString,
		        ComponentDefinition compDef) {
			if (parentString == null)
				addComponent(compDef.getName());
			else
				addComponent(parentString + "." + compDef.getName());
			for (ComponentDefinition child : compDef.getChildren()) {
				if (parentString != null)
					parentString += "." + compDef.getName();
				addComponents(parentString, child);
			}
		}
		

		/**
		 * Adds a component to the component combo box
		 * 
		 * @param componentName
		 *            name of the component to add
		 */
		public void addComponent(String componentName) {
			m_componentList.add(componentName);
			for (ComponentSubPart csp : ComponentSubPart.values()) {
				if (csp != ComponentSubPart.None)
					m_componentList.add(componentName + "." + csp.toString());
				
			}
		}
		

		/**
		 * Remove component from the combo box
		 * 
		 * @param componentName
		 *            name of the component to remove
		 */
		public void removeComponent(String componentName) {
			for (int i = m_componentList.size() - 1; i >= 0; i--) {
				if (m_componentList.get(i).startsWith(componentName))
					m_componentList.remove(i);
			}
			updateComponentList();
		}
		

		/**
		 * Updates/synchronizes the component list and the combo box
		 */
		private void updateComponentList() {
			Object currItem = m_component.getSelectedItem();
			Collections.sort(m_componentList);
			m_component.removeAllItems();
			for (String s : m_componentList)
				m_component.addItem(s);
			m_component.setSelectedItem(currItem);
		}
		

		/**
		 * Get the entry in the name text field
		 * 
		 * @return alias name
		 */
		public String getNameField() {
			return m_name.getText();
		}
		

		/**
		 * Get the entry in the component combo box
		 * 
		 * @return component name
		 */
		public String getComponentField() {
			return (String) m_component.getSelectedItem();
		}
	}
}
