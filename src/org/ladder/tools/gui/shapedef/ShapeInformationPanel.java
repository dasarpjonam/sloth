/**
 * ShapeInformationPanel.java
 * 
 * Revision History:<br>
 * Nov 19, 2008 bpaulson - File created
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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import org.ladder.recognition.constraint.domains.ShapeDefinition;

/**
 * Panel containing shape information
 * 
 * @author bpaulson
 */
public class ShapeInformationPanel extends JPanel {
	
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 7847188762201693120L;
	
	/**
	 * Shape name
	 */
	private JTextField m_name;
	
	/**
	 * Shape filename
	 */
	private JTextField m_filename;
	
	/**
	 * Shape description
	 */
	private JTextField m_description;
	
	/**
	 * Shape isA field
	 */
	private JComboBox m_isA;
	
	/**
	 * Checkbox used to denote is shape is abstract or not
	 */
	private JCheckBox m_abstract;
	
	/**
	 * Flag denoting if text is highlighted (null is no highlight)
	 */
	private Object m_isHighlighted = null;
	
	/**
	 * Text highlighter
	 */
	private Highlighter m_highlighter;
	
	
	/**
	 * Default constructor
	 */
	public ShapeInformationPanel(final ShapeDefinitionGUI gui) {
		super();
		JPanel mainPanel = new JPanel();
		JLabel nameLabel = new JLabel("Name: ");
		m_name = new JTextField(50);
		m_name.addKeyListener(gui.getShapeChangedKeyListener());
		m_name.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
			

			@Override
			public void keyReleased(KeyEvent e) {
				m_filename.setText(m_name.getText() + ".xml");
				gui.setCurrShapeName(m_name.getText());
			}
			

			@Override
			public void keyTyped(KeyEvent e) {
				if (m_isHighlighted != null) {
					m_name.setText("");
					m_isHighlighted = null;
				}
			}
			
		});
		m_name.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
			}
			

			@Override
			public void mouseEntered(MouseEvent e) {
			}
			

			@Override
			public void mouseExited(MouseEvent e) {
			}
			

			@Override
			public void mousePressed(MouseEvent e) {
				if (m_isHighlighted != null) {
					m_highlighter.removeAllHighlights();
					m_isHighlighted = null;
				}
			}
			

			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
		});
		m_name.addFocusListener(new FocusListener() {
			
			@Override
			public void focusGained(FocusEvent e) {
			}
			

			@Override
			public void focusLost(FocusEvent e) {
				if (m_isHighlighted != null) {
					m_highlighter.removeAllHighlights();
					m_isHighlighted = null;
				}
			}
			
		});
		
		JLabel filenameLabel = new JLabel("File Name: ");
		m_filename = new JTextField(50);
		m_filename.setText(".xml");
		m_filename.setEditable(false);
		m_filename.setFocusable(false);
		
		JLabel descLabel = new JLabel("Description: ");
		m_description = new JTextField(50);
		m_description.addKeyListener(gui.getShapeChangedKeyListener());
		
		JLabel isALabel = new JLabel("Is A: ");
		m_isA = new JComboBox();
		m_isA.addItemListener(gui.getShapeChangedItemListener());
		
		m_abstract = new JCheckBox("Abstract");
		m_abstract.setSelected(ShapeDefinition.DEFAULT_ABSTRACT);
		m_abstract.addItemListener(gui.getShapeChangedItemListener());
		
		mainPanel.setLayout(new SpringLayout());
		mainPanel.add(nameLabel);
		mainPanel.add(m_name);
		mainPanel.add(filenameLabel);
		mainPanel.add(m_filename);
		mainPanel.add(descLabel);
		mainPanel.add(m_description);
		mainPanel.add(isALabel);
		mainPanel.add(m_isA);
		mainPanel.add(m_abstract);
		
		// needed to give even number of components for
		// spring layout
		mainPanel.add(new JPanel());
		
		SpringUtilities.makeCompactGrid(mainPanel, 5, 2, 10, 10, 6, 10);
		setLayout(new FlowLayout(FlowLayout.LEADING));
		add(mainPanel);
		disablePanel();
	}
	

	/**
	 * Get the value of the abstract check box
	 * 
	 * @return value of abstract check box
	 */
	public boolean getShapeAbstract() {
		return m_abstract.isSelected();
	}
	

	/**
	 * Get the value of the description text box
	 * 
	 * @return value of the description text box
	 */
	public String getShapeDescription() {
		return m_description.getText();
	}
	

	/**
	 * Get the value of the filename text box
	 * 
	 * @return value of the filename text box
	 */
	public String getShapeFilename() {
		return m_filename.getText();
	}
	

	/**
	 * Get the value of the isA combo box
	 * 
	 * @return value of the isA combo box
	 */
	public String getShapeIsA() {
		return (String) m_isA.getSelectedItem();
	}
	

	/**
	 * Get the value of the name text box
	 * 
	 * @return value of the name text box
	 */
	public String getShapeName() {
		return m_name.getText();
	}
	

	/**
	 * Sets the shape name
	 * 
	 * @param name
	 *            name of shape
	 */
	public void setShapeName(String name) {
		m_name.setText(name);
		m_filename.setText(name + ".xml");
		m_name.requestFocus();
		m_highlighter = m_name.getHighlighter();
		try {
			m_name.getHighlighter().addHighlight(
			        0,
			        m_name.getText().length(),
			        new DefaultHighlighter.DefaultHighlightPainter(new Color(
			                184, 207, 229)));
			m_isHighlighted = true;
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}
		m_name.setCaretPosition(0);
	}
	

	/**
	 * Clear the panel
	 */
	public void clear() {
		m_name.setText("");
		m_filename.setText(".xml");
		m_description.setText("");
		m_abstract.setSelected(ShapeDefinition.DEFAULT_ABSTRACT);
	}
	

	/**
	 * Enable the components of the panel
	 */
	public void enablePanel() {
		m_name.setEnabled(true);
		m_filename.setEnabled(true);
		m_description.setEnabled(true);
		m_isA.setEnabled(true);
		m_abstract.setEnabled(true);
	}
	

	/**
	 * Disable the components of the panel
	 */
	public void disablePanel() {
		m_name.setEnabled(false);
		m_filename.setEnabled(false);
		m_description.setEnabled(false);
		m_isA.setEnabled(false);
		m_abstract.setEnabled(false);
	}
}
