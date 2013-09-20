/**
 * ShapeDefinitionGUI.java
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;

import org.ladder.core.config.LadderConfig;
import org.ladder.recognition.constraint.domains.AliasDefinition;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.constraint.domains.compiler.DomainDefinitionCompiler;

/**
 * GUI used to create and edit domain and shape definitions
 * 
 * @author bpaulson
 */
public class ShapeDefinitionGUI extends JFrame {
	
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -742314942214571996L;
	
	/**
	 * Directory where domains are saved
	 */
	private File m_domainDir;
	
	/**
	 * Directory where shapes are saved
	 */
	private File m_shapeDir;
	
	/**
	 * Top menu
	 */
	private JMenuBar m_menu;
	
	/**
	 * Bottom panel
	 */
	private JPanel m_bottomPanel;
	
	/**
	 * Domain information panel (top left)
	 */
	private JPanel m_domainInfoPanel;
	
	/**
	 * Textbox containing the domain name
	 */
	private JTextField m_domainNameBox;
	
	/**
	 * Textbox containing the domain file name
	 */
	private JTextField m_domainFileNameBox;
	
	/**
	 * Textbox containing the domain description
	 */
	private JTextField m_domainDescBox;
	
	/**
	 * Primitive list panel (center left)
	 */
	private JPanel m_primitiveListPanel;
	
	/**
	 * Primitive list
	 */
	private JList m_primitiveList;
	
	/**
	 * List model (this is what elements should actually be added to)
	 */
	private DefaultListModel m_primitiveListModel;
	
	/**
	 * Shape list panel (bottom left)
	 */
	private JPanel m_shapeListPanel;
	
	/**
	 * Shape list
	 */
	private JList m_shapeList;
	
	/**
	 * List model (this is what elements should actually be added to)
	 */
	private DefaultListModel m_shapeListModel;
	
	/**
	 * Tabbed panel (middle right)
	 */
	private JPanel m_tabbedPanel;
	
	/**
	 * Tabbed pane
	 */
	private JTabbedPane m_tab;
	
	/**
	 * Shape information panel (contained in tab)
	 */
	private ShapeInformationPanel m_shapeInfoPanel;
	
	/**
	 * Component definition panel (contained in tab)
	 */
	private ComponentDefinitionPanel m_compDefPanel;
	
	/**
	 * Constraint definition panel (contained in tab)
	 */
	private ConstraintDefinitionPanel m_constDefPanel;
	
	/**
	 * Alias definition panel (contained in tab)
	 */
	private AliasDefinitionPanel m_aliasDefPanel;
	
	/**
	 * Listener fired if a change is made to the current domain definition
	 */
	private KeyListener m_domainChangedKeyListener;
	
	/**
	 * Listener fired if a change is made to the current shape definition
	 */
	private KeyListener m_shapeChangedKeyListener;
	
	/**
	 * Listener fired if a change is made to the current shape definition
	 */
	private ItemListener m_shapeChangedItemListener;
	
	/**
	 * Flag denoting if a change has been made to the current domain definition
	 */
	private boolean m_domainChangeMade = false;
	
	/**
	 * Current domain definition
	 */
	private DomainDefinition m_domain;
	
	/**
	 * Domain compiler
	 */
	private DomainDefinitionCompiler m_compiler;
	
	/**
	 * Current shape definition
	 */
	private ShapeDefinition m_currShape;
	
	/**
	 * Boolean flag denoting if current shape being edited is a brand new shape
	 * or not
	 */
	private boolean m_isNew = false;
	
	
	/**
	 * Default constructor
	 */
	public ShapeDefinitionGUI() {
		
		// initialize frame
		super("Shape Definition GUI");
		setSize(1024, 750);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		initializeListeners();
		m_domainDir = new File(LadderConfig
		        .getProperty(LadderConfig.DOMAIN_DESC_LOC_KEY));
		m_shapeDir = new File(LadderConfig
		        .getProperty(LadderConfig.SHAPE_DESC_LOC_KEY));
		m_domain = new DomainDefinition();
		createMenuBar();
		createBottomPanel();
		createDomainInfoPanel();
		createPrimitiveListPanel();
		createShapeListPanel();
		createTabbedPanel();
		
		// add all panels to main frame
		setLayout(new BorderLayout());
		JPanel westPanel = new JPanel();
		westPanel.setLayout(new BorderLayout());
		westPanel.add(m_domainInfoPanel, BorderLayout.NORTH);
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
		listPanel.add(m_primitiveListPanel, BorderLayout.NORTH);
		listPanel.add(m_shapeListPanel, BorderLayout.CENTER);
		westPanel.add(listPanel, BorderLayout.CENTER);
		getContentPane().add(westPanel, BorderLayout.WEST);
		getContentPane().add(m_tabbedPanel, BorderLayout.CENTER);
		getContentPane().add(m_bottomPanel, BorderLayout.SOUTH);
	}
	

	/**
	 * Initialize listeners
	 */
	private void initializeListeners() {
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowActivated(WindowEvent e) {
			}
			

			@Override
			public void windowClosed(WindowEvent e) {
			}
			

			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
			

			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			

			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			

			@Override
			public void windowIconified(WindowEvent e) {
			}
			

			@Override
			public void windowOpened(WindowEvent e) {
			}
			
		});
		m_domainChangedKeyListener = new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
			

			@Override
			public void keyReleased(KeyEvent e) {
			}
			

			@Override
			public void keyTyped(KeyEvent e) {
				m_domainChangeMade = true;
			}
		};
		setShapeChangedKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
			

			@Override
			public void keyReleased(KeyEvent e) {
			}
			

			@Override
			public void keyTyped(KeyEvent e) {
				m_domainChangeMade = true;
			}
		});
		setShapeChangedItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				m_domainChangeMade = true;
			}
		});
	}
	

	/**
	 * Creates the menu bar for the GUI
	 */
	private void createMenuBar() {
		m_menu = new JMenuBar();
		
		// file menu
		JMenu file = new JMenu("File");
		file.setMnemonic('F');
		m_menu.add(file);
		
		// new option
		JMenuItem newOption = new JMenuItem("New Domain");
		newOption.setMnemonic('N');
		newOption.setAccelerator(KeyStroke.getKeyStroke(
		        java.awt.event.KeyEvent.VK_N, java.awt.Event.CTRL_MASK));
		newOption.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				openNew();
			}
		});
		file.add(newOption);
		
		// open option
		JMenuItem open = new JMenuItem("Open Domain");
		open.setMnemonic('O');
		open.setAccelerator(KeyStroke.getKeyStroke(
		        java.awt.event.KeyEvent.VK_O, java.awt.Event.CTRL_MASK));
		open.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				openExisting();
			}
		});
		file.add(open);
		
		// save option
		JMenuItem save = new JMenuItem("Save Domain");
		save.setMnemonic('S');
		save.setAccelerator(KeyStroke.getKeyStroke(
		        java.awt.event.KeyEvent.VK_S, java.awt.Event.CTRL_MASK));
		save.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				saveDomain();
			}
		});
		file.add(save);
		
		file.addSeparator();
		
		// exit option
		JMenuItem exit = new JMenuItem("Exit");
		exit.setMnemonic('X');
		exit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});
		file.add(exit);
		
		setJMenuBar(m_menu);
	}
	

	/**
	 * Create the top panel giving the location of the domain and shapes
	 * directory
	 */
	private void createBottomPanel() {
		m_bottomPanel = new JPanel();
		JLabel directoryLbl = new JLabel("Domain Directory:   "
		                                 + m_domainDir.getAbsolutePath());
		directoryLbl.setForeground(Color.LIGHT_GRAY);
		JLabel shapeLbl = new JLabel("Shape Directory:     "
		                             + m_shapeDir.getAbsolutePath());
		shapeLbl.setForeground(Color.LIGHT_GRAY);
		JLabel dummyLbl = new JLabel(" ");
		m_bottomPanel.setLayout(new BorderLayout());
		m_bottomPanel.add(dummyLbl, BorderLayout.NORTH);
		m_bottomPanel.add(directoryLbl, BorderLayout.CENTER);
		m_bottomPanel.add(shapeLbl, BorderLayout.SOUTH);
	}
	

	/**
	 * Create the domain information panel (top left)
	 */
	private void createDomainInfoPanel() {
		m_domainInfoPanel = new JPanel();
		m_domainInfoPanel.setBorder(BorderFactory
		        .createTitledBorder("Domain Information"));
		
		// domain name
		JLabel nameLabel = new JLabel("Name: ");
		m_domainNameBox = new JTextField(10);
		m_domainNameBox.addKeyListener(m_domainChangedKeyListener);
		m_domainNameBox.addKeyListener(new KeyListener() {
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
			

			@Override
			public void keyReleased(KeyEvent e) {
				m_domainFileNameBox.setText(m_domainNameBox.getText() + ".xml");
			}
			

			@Override
			public void keyTyped(KeyEvent e) {
			}
			
		});
		nameLabel.setLabelFor(m_domainNameBox);
		
		// domain file name
		JLabel fileLabel = new JLabel("File Name: ");
		m_domainFileNameBox = new JTextField(10);
		m_domainFileNameBox.setEditable(false);
		m_domainFileNameBox.setText(".xml");
		fileLabel.setLabelFor(m_domainFileNameBox);
		
		// domain description
		JLabel descLabel = new JLabel("Description: ");
		m_domainDescBox = new JTextField(15);
		m_domainDescBox.addKeyListener(m_domainChangedKeyListener);
		descLabel.setLabelFor(m_domainDescBox);
		
		// add components to domain info panel
		m_domainInfoPanel.setLayout(new SpringLayout());
		m_domainInfoPanel.add(nameLabel);
		m_domainInfoPanel.add(m_domainNameBox);
		m_domainInfoPanel.add(fileLabel);
		m_domainInfoPanel.add(m_domainFileNameBox);
		m_domainInfoPanel.add(descLabel);
		m_domainInfoPanel.add(m_domainDescBox);
		SpringUtilities.makeCompactGrid(m_domainInfoPanel, 3, 2, 6, 6, 6, 6);
	}
	

	/**
	 * Create the primitive list panel (center left)
	 */
	private void createPrimitiveListPanel() {
		m_primitiveListPanel = new JPanel();
		m_primitiveListPanel.setBorder(BorderFactory
		        .createTitledBorder("Primitive List"));
		
		// create shape list
		m_primitiveListModel = new DefaultListModel();
		m_primitiveList = new JList(m_primitiveListModel);
		m_primitiveList.setPreferredSize(new Dimension(100, 125));
		m_primitiveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// create buttons
		JPanel buttonPanel = new JPanel();
		
		// add button
		JButton add = new JButton("Add Primitive");
		add.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				addNewPrimitive();
			}
		});
		buttonPanel.add(add);
		
		// delete button
		JButton delete = new JButton("Delete Primitive");
		delete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteCurrentPrimitive();
			}
		});
		buttonPanel.add(delete);
		
		// add components to panel
		m_primitiveListPanel.setLayout(new BorderLayout());
		JScrollPane scroll = new JScrollPane(m_primitiveList);
		m_primitiveListPanel.add(scroll, BorderLayout.CENTER);
		m_primitiveListPanel.add(buttonPanel, BorderLayout.SOUTH);
	}
	

	/**
	 * Sets the name of the currently selected shape
	 * 
	 * @param currShapeName
	 *            shape name
	 */
	public void setCurrShapeName(String currShapeName) {
		m_shapeListModel.set(m_shapeList.getSelectedIndex(), currShapeName);
	}
	

	/**
	 * Create the shape list panel (bottom left)
	 */
	private void createShapeListPanel() {
		m_shapeListPanel = new JPanel();
		m_shapeListPanel.setBorder(BorderFactory
		        .createTitledBorder("Shape List"));
		
		// create shape list
		m_shapeListModel = new DefaultListModel();
		m_shapeList = new JList(m_shapeListModel);
		m_shapeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// create buttons
		JPanel buttonPanel = new JPanel();
		
		// add button
		JButton add = new JButton("Add Shape");
		add.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				addNewShape();
			}
		});
		buttonPanel.add(add);
		
		// delete button
		JButton delete = new JButton("Delete Shape");
		delete.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteCurrentShape();
			}
		});
		buttonPanel.add(delete);
		
		// add components to panel
		m_shapeListPanel.setLayout(new BorderLayout());
		JScrollPane scroll = new JScrollPane(m_shapeList);
		m_shapeListPanel.add(scroll, BorderLayout.CENTER);
		m_shapeListPanel.add(buttonPanel, BorderLayout.SOUTH);
	}
	

	/**
	 * Create the tabbed panel (middle right)
	 */
	private void createTabbedPanel() {
		m_tabbedPanel = new JPanel();
		m_tab = new JTabbedPane();
		m_shapeInfoPanel = new ShapeInformationPanel(this);
		m_compDefPanel = new ComponentDefinitionPanel(this, m_domain,
		        m_currShape, m_primitiveListModel);
		m_constDefPanel = new ConstraintDefinitionPanel(this, m_currShape);
		m_aliasDefPanel = new AliasDefinitionPanel(this, m_currShape);
		JScrollPane scroll1 = new JScrollPane(m_shapeInfoPanel);
		JScrollPane scroll2 = new JScrollPane(m_compDefPanel);
		JScrollPane scroll3 = new JScrollPane(m_constDefPanel);
		JScrollPane scroll4 = new JScrollPane(m_aliasDefPanel);
		m_tab.add("Shape Information", scroll1);
		m_tab.add("Components", scroll2);
		m_tab.add("Constraints", scroll3);
		m_tab.add("Aliases", scroll4);
		
		// add components
		m_tabbedPanel.setLayout(new BorderLayout());
		m_tabbedPanel.add(m_tab, BorderLayout.CENTER);
	}
	

	/**
	 * Exit the program
	 */
	private void exit() {
		if (m_domainChangeMade) {
			int r = JOptionPane
			        .showConfirmDialog(
			                this,
			                "Would you like to save the changes made to the current domain?",
			                "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION);
			if (r == JOptionPane.CANCEL_OPTION)
				return;
			if (r == JOptionPane.YES_OPTION)
				saveDomain();
		}
		System.exit(0);
	}
	

	/**
	 * Open/create a new domain
	 */
	private void openNew() {
		// TODO
	}
	

	/**
	 * Open/load an existing domain
	 */
	private void openExisting() {
		// TODO
	}
	

	/**
	 * Save domain to file
	 */
	private void saveDomain() {
		// TODO
	}
	

	/**
	 * Save the current shape definition
	 */
	private void saveCurrentShape() {
		m_currShape.setAbstract(m_shapeInfoPanel.getShapeAbstract());
		m_currShape.setDescription(m_shapeInfoPanel.getShapeDescription());
		m_currShape.setFilename(m_shapeInfoPanel.getShapeFilename());
		m_currShape.addIsA(m_shapeInfoPanel.getShapeIsA());
		m_currShape.setName(m_shapeInfoPanel.getShapeName());
		m_currShape.getComponentDefinitions().clear();
		for (ComponentDefinition def : m_compDefPanel.getDefinitions())
			m_currShape.addComponentDefinition(def);
		m_currShape.getConstraintDefinitions().clear();
		for (ConstraintDefinition def : m_constDefPanel.getDefinitions())
			m_currShape.addConstraintDefinition(def);
		m_currShape.getAliasDefinitions().clear();
		for (AliasDefinition def : m_aliasDefPanel.getDefinitions())
			m_currShape.addAliasDefinition(def);
		if (m_isNew)
			m_domain.addShapeDefinition(m_currShape);
		m_compiler = new DomainDefinitionCompiler(m_domain);
		try {
			m_domain = m_compiler.compile();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		m_compDefPanel.addComponentType(m_currShape.getName());
	}
	

	/**
	 * Add a new primitive to the domain
	 */
	private void addNewPrimitive() {
		String input = JOptionPane
		        .showInputDialog(this,
		                "Please enter name of primitive.  Use comma to separate multiple values.");
		if (input == null || input == "")
			return;
		String[] prims = input.split(",");
		for (String prim : prims) {
			prim = prim.trim();
			if (prim != "" && !m_primitiveListModel.contains(prim)) {
				m_primitiveListModel.addElement(prim);
				m_primitiveList
				        .setSelectedIndex(m_primitiveListModel.getSize() - 1);
				m_compDefPanel.addComponentType(prim);
			}
		}
		// enable component definition panel (it may have been disabled due to
		// having no component types available)
		if (m_currShape != null)
			m_compDefPanel.enablePanel();
	}
	

	/**
	 * Delete the currently selected primitive
	 */
	private void deleteCurrentPrimitive() {
		int removeIndex = m_primitiveList.getSelectedIndex();
		if (removeIndex >= 0) {
			// TODO: check to make sure primitive isn't essential (currently
			// used) before deleting
			m_compDefPanel.removeComponentType((String) m_primitiveListModel
			        .get(removeIndex));
			m_primitiveListModel.remove(removeIndex);
			if (removeIndex == m_primitiveListModel.size())
				m_primitiveList.setSelectedIndex(removeIndex - 1);
			else
				m_primitiveList.setSelectedIndex(removeIndex);
		}
	}
	

	/**
	 * Add a new shape to the domain
	 */
	private void addNewShape() {
		
		// save current shape
		if (m_currShape != null)
			saveCurrentShape();
		
		m_isNew = true;
		m_tab.setSelectedIndex(0);
		
		// clear panels
		m_shapeInfoPanel.clear();
		m_compDefPanel.clear();
		m_constDefPanel.clear();
		m_aliasDefPanel.clear();
		
		// find good default "name" for shape
		int num = 1;
		String newName = "Shape" + num;
		boolean found = true;
		while (found) {
			found = false;
			for (int i = 0; i < m_shapeListModel.size() && !found; i++) {
				if (m_shapeListModel.get(i).toString().compareToIgnoreCase(
				        newName) == 0) {
					found = true;
					num++;
					newName = "Shape" + num;
				}
			}
		}
		
		// create new shape
		m_currShape = new ShapeDefinition();
		m_shapeListModel.addElement(newName);
		m_shapeList.setSelectedIndex(m_shapeListModel.getSize() - 1);
		m_shapeInfoPanel.setShapeName(newName);
		
		// enable panels
		m_shapeInfoPanel.enablePanel();
		m_compDefPanel.enablePanel();
		m_constDefPanel.enablePanel();
		m_aliasDefPanel.enablePanel();
	}
	

	/**
	 * Delete the currently selected shape
	 */
	private void deleteCurrentShape() {
		// TODO
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ShapeDefinitionGUI gui = new ShapeDefinitionGUI();
		gui.setVisible(true);
	}
	

	/**
	 * @param shapeChangedKeyListener
	 *            the shapeChangedKeyListener to set
	 */
	public void setShapeChangedKeyListener(KeyListener shapeChangedKeyListener) {
		m_shapeChangedKeyListener = shapeChangedKeyListener;
	}
	

	/**
	 * @return the shapeChangedKeyListener
	 */
	public KeyListener getShapeChangedKeyListener() {
		return m_shapeChangedKeyListener;
	}
	

	/**
	 * @param shapeChangedItemListener
	 *            the shapeChangedItemListener to set
	 */
	public void setShapeChangedItemListener(
	        ItemListener shapeChangedItemListener) {
		m_shapeChangedItemListener = shapeChangedItemListener;
	}
	

	/**
	 * @return the shapeChangedItemListener
	 */
	public ItemListener getShapeChangedItemListener() {
		return m_shapeChangedItemListener;
	}
}