/**
 * ComponentDefinitionPanel.java
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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;

/**
 * Panel containing component definitions
 * 
 * @author bpaulson
 */
public class ComponentDefinitionPanel extends JPanel {
	
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 3142790995019104831L;
	
	/**
	 * Default row count for grid layout
	 */
	private static final int DEFAULT_ROW_COUNT = 16;
	
	/**
	 * List of components for current shape
	 */
	private List<ComponentEntry> m_components = new ArrayList<ComponentEntry>();
	
	/**
	 * Component type list
	 */
	private Vector<String> m_typeList = new Vector<String>();
	
	/**
	 * Add new component button
	 */
	private JButton m_add = new JButton("Add Component");
	
	/**
	 * Layout manager
	 */
	private GridLayout m_layout;
	
	
	/**
	 * Constructor for component definition panel
	 * 
	 * @param domainDef
	 *            domain definition
	 * @param primList
	 *            primitive list
	 */
	public ComponentDefinitionPanel(ShapeDefinitionGUI gui,
	        DomainDefinition domainDef, ShapeDefinition shapeDef,
	        DefaultListModel primList) {
		super();
		for (int i = 0; i < primList.size(); i++)
			m_typeList.add((String) primList.elementAt(i));
		for (ShapeDefinition sd : domainDef.getShapeDefinitions()) {
			if (sd != shapeDef)
				m_typeList.add(sd.getName());
		}
		
		m_add.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				newComponent();
			}
			
		});
		m_layout = new GridLayout(DEFAULT_ROW_COUNT, 1);
		setLayout(m_layout);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(m_add);
		add(buttonPanel);
		disablePanel();
	}
	

	/**
	 * Add a new component entry object to the panel
	 */
	private void newComponent() {
		ComponentEntry comp = new ComponentEntry(this, m_typeList);
		if (m_layout.getRows() < m_components.size() + 2)
			m_layout.setRows(m_layout.getRows() + 1);
		add(comp);
		m_components.add(comp);
		getParent().getParent().getParent().repaint();
	}
	

	/**
	 * Clear the panel
	 */
	public void clear() {
		for (ComponentEntry comp : m_components) {
			remove(comp);
			m_layout.setRows(DEFAULT_ROW_COUNT);
		}
		m_components.clear();
	}
	

	/**
	 * Enable the components of the panel
	 */
	public void enablePanel() {
		m_add.setEnabled(true);
		
		// can't create components if we have no component types
		if (m_typeList.size() <= 0)
			disablePanel();
	}
	

	/**
	 * Disable the components of the panel
	 */
	public void disablePanel() {
		m_add.setEnabled(false);
	}
	

	/**
	 * Add a component type
	 * 
	 * @param type
	 *            type of component to add
	 */
	public void addComponentType(String type) {
		for (ComponentEntry ce : m_components) {
			ce.addComponent(type);
		}
		if (!m_typeList.contains(type))
			m_typeList.add(type);
	}
	

	/**
	 * Remove a component type
	 * 
	 * @param type
	 *            component type to remove
	 */
	public void removeComponentType(String type) {
		for (ComponentEntry ce : m_components) {
			ce.removeComponent(type);
		}
		m_typeList.remove(type);
	}
	

	/**
	 * Remove a specific component entry
	 * 
	 * @param compEntry
	 *            component entry to remove
	 */
	public void removeComponentEntry(ComponentEntry compEntry) {
		m_components.remove(compEntry);
		getParent().getParent().getParent().repaint();
	}
	

	/**
	 * Get a list of definitions that were defined on this panel
	 * 
	 * @return definitions defined on panel
	 */
	public List<ComponentDefinition> getDefinitions() {
		List<ComponentDefinition> defs = new ArrayList<ComponentDefinition>();
		for (ComponentEntry ce : m_components) {
			ComponentDefinition def = new ComponentDefinition();
			def.setName(ce.getNameField());
			def.setShapeType(ce.getTypeField());
			defs.add(def);
		}
		return defs;
	}
	
	/**
	 * GUI components for an alias entry
	 * 
	 * @author bpaulson
	 */
	private class ComponentEntry extends JPanel {
		
		/**
		 * Serial UID
		 */
		private static final long serialVersionUID = 4813518333336310414L;
		
		/**
		 * Component name
		 */
		private JTextField m_name = new JTextField(10);
		
		/**
		 * Component type
		 */
		private JComboBox m_type = new JComboBox();
		
		/**
		 * Remove button
		 */
		private JButton m_remove = new JButton("X");
		
		/**
		 * Self reference
		 */
		private ComponentEntry m_self;
		
		
		/**
		 * Constructor for component entry
		 * 
		 * @param parent
		 *            component definition panel that this entry belongs to
		 * @param typeList
		 *            list of component types
		 */
		public ComponentEntry(final ComponentDefinitionPanel parent,
		        Vector<String> typeList) {
			super();
			m_self = this;
			m_remove.setToolTipText("Remove component.");
			m_remove.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					parent.removeComponentEntry(m_self);
				}
				
			});
			
			JLabel name = new JLabel("Name: ");
			JLabel component = new JLabel("Type: ");
			
			// add component types
			m_typeList = typeList;
			updateComponentTypeList();
			
			// add components to panel
			add(m_remove);
			add(name);
			add(m_name);
			add(component);
			add(m_type);
		}
		

		/**
		 * Adds a component type to the combo box
		 * 
		 * @param componentType
		 *            name of the component type to add
		 */
		public void addComponent(String componentType) {
			if (!m_typeList.contains(componentType)) {
				m_typeList.add(componentType);
				updateComponentTypeList();
			}
		}
		

		/**
		 * Remove component type from the combo box
		 * 
		 * @param componentName
		 *            name of the component to remove
		 */
		public void removeComponent(String componentName) {
			for (int i = m_typeList.size() - 1; i >= 0; i--) {
				if (m_typeList.get(i).startsWith(componentName))
					m_typeList.remove(i);
			}
			updateComponentTypeList();
		}
		

		/**
		 * Updates/synchronizes the component type list and the combo box
		 */
		public void updateComponentTypeList() {
			Object currItem = m_type.getSelectedItem();
			Collections.sort(m_typeList);
			m_type.removeAllItems();
			for (String s : m_typeList)
				m_type.addItem(s);
			m_type.setSelectedItem(currItem);
		}
		

		/**
		 * Get the entry in the name text field
		 * 
		 * @return component name
		 */
		public String getNameField() {
			return m_name.getText();
		}
		

		/**
		 * Get the entry in the type combo box
		 * 
		 * @return type name
		 */
		public String getTypeField() {
			return (String) m_type.getSelectedItem();
		}
	}
}
