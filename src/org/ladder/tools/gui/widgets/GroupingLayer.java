/**
 * GroupingLayer.java
 * 
 * Revision History:<br>
 * Dec 3, 2008 jbjohns - File created
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
package org.ladder.tools.gui.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Sketch;
import org.ladder.tools.StrokeManager;
import org.ladder.tools.gui.Layer;

/**
 * 
 * @author jbjohns
 */
public class GroupingLayer extends Layer {
	
	/**
	 * Name of this layer in the layer widget
	 */
	private static final String S_NAME = "Grouping and Labeling";
	
	/**
	 * Special menu bar particular to this layer
	 */
	private SRLMenuBar m_menuBar;
	
	private SRLToolPalette m_toolPalette;
	
	
	public GroupingLayer(StrokeManager strokeManager) {
		super(strokeManager);
		
		initializeMenuBar();
		initializeToolPalette();
		
		setOpaque(false);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#registerYourselfAsMouseListener()
	 */
	@Override
	public void registerYourselfAsMouseListener() {
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	

	private void initializeToolPalette() {
		final int WIDTH = 200;
		final int HEIGHT = 500;
		Component rigidArea = Box.createRigidArea(new Dimension(WIDTH, 20));
		
		m_toolPalette = new SRLToolPalette("Grouping Tools");
		m_toolPalette.setSize(WIDTH, HEIGHT);
		m_toolPalette.setLayout(new FlowLayout());
		
		m_toolPalette.add(new JLabel("Group and Label Strokes"));
		
		m_toolPalette.add(rigidArea);
		m_toolPalette.add(new JLabel("Assign Label to Selected Group"));
		m_toolPalette.add(new JLabel("Label: "));
		final JTextField labelText = new JTextField(10);
		m_toolPalette.add(labelText);
		m_toolPalette.add(new JLabel("Assign Description to Selected Group"));
		m_toolPalette.add(new JLabel("Description: "));
		final JTextField descriptionText = new JTextField(10);
		m_toolPalette.add(descriptionText);
		JButton assignLabelButton = new JButton("Assign Label / Description");
		assignLabelButton.addActionListener(new ActionListener() {
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				groupStrokes(labelText.getText(), descriptionText.getText());
			}
			
		});
		m_toolPalette.add(assignLabelButton);
		
		m_toolPalette.add(rigidArea);
		JButton extractButton = new JButton("Extract Selection to Sketch File");
		extractButton.addActionListener(new ActionListener() {
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
				
			}
		});
		m_toolPalette.add(extractButton);
		
		m_toolPalette.add(rigidArea);
		m_toolPalette.add(new JLabel("View Labelled Stroke/Group"));
		m_toolPalette.add(new JLabel("Select Label:"));
		final JComboBox labelList = new JComboBox();
		m_toolPalette.add(labelList);
		JButton refreshListButton = new JButton("Refresh Label Lists");
		refreshListButton.addActionListener(new ActionListener() {
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				SortedSet<String> labels = new TreeSet<String>();
				for (IStroke stroke : m_strokeManager.getStrokes()) {
					String label = stroke.getLabel();
					if (label != null) {
						labels.add(label);
					}
				}
				for (IShape shape : m_strokeManager.getShapes()) {
					String label = shape.getLabel();
					if (label != null) {
						labels.add(label);
					}
				}
				labelList.removeAllItems();
				for (String label : labels) {
					labelList.addItem(label);
				}
			}
		});
		m_toolPalette.add(refreshListButton);
		JButton highlightButton = new JButton("Highlight Labeled Items");
		highlightButton.addActionListener(new ActionListener() {
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				clear();
				String selectedLabel = (String) labelList.getSelectedItem();
				if (selectedLabel != null) {
					for (IStroke stroke : m_strokeManager.getStrokes()) {
						if (selectedLabel.equalsIgnoreCase(stroke.getLabel())) {
							m_selected.add(stroke);
						}
					}
					for (IShape shape : m_strokeManager.getShapes()) {
						if (selectedLabel.equalsIgnoreCase(shape.getLabel())) {
							m_selected.addAll(shape.getStrokes());
						}
					}
				}
				refreshScreen();
			}
		});
		m_toolPalette.add(highlightButton);
	}
	

	private void extractSelection() {
		if (m_selected.size() > 0) {
			ISketch newSketch = new Sketch();
			for (IStroke stroke : m_selected) {
				stroke.setColor(Color.BLACK);
				newSketch.addStroke(stroke);
			}
			saveSketch(newSketch);
		}
	}
	

	private void initializeMenuBar() {
		m_menuBar = new SRLMenuBar(true);
		
		JMenuItem save = m_menuBar.getSaveItem();
		JMenuItem open = m_menuBar.getOpenItem();
		JMenuItem close = m_menuBar.getCloseItem();
		
		JMenuItem redo = m_menuBar.getRedoItem();
		JMenuItem undo = m_menuBar.getUndoItem();
		
		save.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				saveSketch(m_strokeManager.getSketch());
			}
		});
		
		close.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});
		
		open.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				openSketch();
			}
		});
		
		undo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				undo();
			}
		});
		
		redo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				redo();
			}
		});
		
		JMenu file = m_menuBar.getFileMenu();
		JMenuItem clear = new JMenuItem("Clear");
		file.add(clear);
		
		clear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clear();
			}
		});
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#getMenuBar()
	 */
	@Override
	public SRLMenuBar getMenuBar() {
		return m_menuBar;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#clear()
	 */
	@Override
	public void clear() {
		m_selected.clear();
		m_bufferedGraphics = null;
		refreshScreen();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#refreshScreen()
	 */
	@Override
	protected void refreshScreen() {
		// To avoid null pointer exception
		if (m_bufferedGraphics == null) {
			m_bufferedGraphics = new BufferedImage(getWidth(), getHeight(),
			        BufferedImage.TYPE_INT_ARGB);
		}
		
		if (super.getSelectionRect() != null) {
			System.out.println("Painting the rect.");
			Rectangle2D.Double rect = super.getSelectionRect();
			m_bufferedGraphics.getGraphics().setColor(Color.BLACK);
			m_bufferedGraphics.getGraphics().drawRect((int)rect.getX(), 
					(int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight());
			
		}
		
		for (IStroke s : m_selected) {
			s.setColor(Color.ORANGE);
			paintStroke(m_bufferedGraphics.getGraphics(), s);
			s.setColor(Color.BLACK);
		}
		
		this.repaint();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#redo()
	 */
	@Override
	public void redo() {
		// TODO Auto-generated method stub
		super.redo();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#undo()
	 */
	@Override
	public void undo() {
		// TODO Auto-generated method stub
		super.undo();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#getToolPalettes()
	 */
	@Override
	public List<SRLToolPalette> getToolPalettes() {
		List<SRLToolPalette> tools = new ArrayList<SRLToolPalette>();
		tools.add(m_toolPalette);
		return tools;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#getName()
	 */
	@Override
	public String getName() {
		return S_NAME;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent arg0) {
		//trySelection(arg0);
		super.setFromX(arg0.getX());
		super.setFromY(arg0.getY());
		showContextMenu(arg0);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent arg0) {
		if (super.isDragged()) {
			multipleSelection();
		} else {
			trySelection(arg0);
		}
		showContextMenu(arg0);
	}
	
	@Override
	public void mouseDragged(MouseEvent arg0) {
		this.dragging(arg0);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.tools.gui.Layer#buildContextMenu(java.awt.event.MouseEvent)
	 */
	protected void buildContextMenu(MouseEvent e) {
		contextMenu = new JPopupMenu();
		
		// This section added by JMPeschel to add individual stroke labeling and saving capabilities
		
		// Create sub-menu and add low-level primitive saving options
		JMenu contextSubMenu = new JMenu("Label & Extract Primitive");
	
		JMenuItem labelExtractArcStrokeSubMenuItem = new JMenuItem("Arc");
		labelExtractArcStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});		
		
		JMenuItem labelExtractArrowStrokeSubMenuItem = new JMenuItem("Arrow");
		labelExtractArrowStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});				
		
		JMenuItem labelExtractCircleStrokeSubMenuItem = new JMenuItem("Circle");
		labelExtractCircleStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});				
		
		JMenuItem labelExtractCurveStrokeSubMenuItem = new JMenuItem("Curve");
		labelExtractCurveStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});						
		
		JMenuItem labelExtractDiamondStrokeSubMenuItem = new JMenuItem("Diamond");
		labelExtractDiamondStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});					
		
		JMenuItem labelExtractDotStrokeSubMenuItem = new JMenuItem("Dot");
		labelExtractDotStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});					
		
		JMenuItem labelExtractEllipseStrokeSubMenuItem = new JMenuItem("Ellipse");
		labelExtractEllipseStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});					
		
		JMenuItem labelExtractGullStrokeSubMenuItem = new JMenuItem("Gull");
		labelExtractGullStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});					
		
		JMenuItem labelExtractHelixStrokeSubMenuItem = new JMenuItem("Helix");
		labelExtractHelixStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});			
		
		JMenuItem labelExtractLineStrokeSubMenuItem = new JMenuItem("Line");
		labelExtractLineStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});					
		
		JMenuItem labelExtractPolygonStrokeSubMenuItem = new JMenuItem("Polygon");
		labelExtractPolygonStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});							
		
		JMenuItem labelExtractRectangleStrokeSubMenuItem = new JMenuItem("Rectangle");
		labelExtractRectangleStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});			
		
		JMenuItem labelExtractSpiralStrokeSubMenuItem = new JMenuItem("Spiral");
		labelExtractSpiralStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});			
		
		JMenuItem labelExtractSquareStrokeSubMenuItem = new JMenuItem("Square");
		labelExtractSquareStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});			
		
		JMenuItem labelExtractWaveStrokeSubMenuItem = new JMenuItem("Wave");
		labelExtractWaveStrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});			
		
		// Create sub-menu and add low-level primitive saving options
		JMenu polylineSubMenu = new JMenu("Polyline");

		JMenuItem labelExtractPolylineOrder1StrokeSubMenuItem = new JMenuItem("Order 1");
		labelExtractPolylineOrder1StrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});			
		
		JMenuItem labelExtractPolylineOrder2StrokeSubMenuItem = new JMenuItem("Order 2");
		labelExtractPolylineOrder2StrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});		
		
		JMenuItem labelExtractPolylineOrder3StrokeSubMenuItem = new JMenuItem("Order 3");
		labelExtractPolylineOrder3StrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});			
		
		JMenuItem labelExtractPolylineOrder4StrokeSubMenuItem = new JMenuItem("Order 4");
		labelExtractPolylineOrder4StrokeSubMenuItem.addActionListener(new ActionListener() {
	
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
	
		});	
		
		// -----------------------------------------------------------------------------------------
		
		JMenuItem groupStrokesMenu = new JMenuItem("Group Selected Strokes");
		groupStrokesMenu.addActionListener(new ActionListener() {
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				String label = JOptionPane
				        .showInputDialog("Enter a label for the selected strokes");
				groupStrokes(label);
			}
		});
		
		JMenuItem extractStrokesMenu = new JMenuItem(
		        "Extract Selected Strokes to new Sketch");
		extractStrokesMenu.addActionListener(new ActionListener() {
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.ActionListener#actionPerformed(java.awt.event.
			 * ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				extractSelection();
			}
			
		});
		
		if (m_selected.size() > 0) {
			contextMenu.add(contextSubMenu);
			contextSubMenu.add(labelExtractArcStrokeSubMenuItem);
			contextSubMenu.add(labelExtractArrowStrokeSubMenuItem);			
			contextSubMenu.add(labelExtractCircleStrokeSubMenuItem);
			contextSubMenu.add(labelExtractCurveStrokeSubMenuItem);			
			contextSubMenu.add(labelExtractDiamondStrokeSubMenuItem);			
			contextSubMenu.add(labelExtractDotStrokeSubMenuItem);
			contextSubMenu.add(labelExtractEllipseStrokeSubMenuItem);
			contextSubMenu.add(labelExtractGullStrokeSubMenuItem);
			contextSubMenu.add(labelExtractHelixStrokeSubMenuItem);			
			contextSubMenu.add(labelExtractLineStrokeSubMenuItem);
			contextSubMenu.add(labelExtractPolygonStrokeSubMenuItem);			
			contextSubMenu.add(polylineSubMenu);
			polylineSubMenu.add(labelExtractPolylineOrder1StrokeSubMenuItem);
			polylineSubMenu.add(labelExtractPolylineOrder2StrokeSubMenuItem);
			polylineSubMenu.add(labelExtractPolylineOrder3StrokeSubMenuItem);
			polylineSubMenu.add(labelExtractPolylineOrder4StrokeSubMenuItem);			
			contextSubMenu.add(labelExtractRectangleStrokeSubMenuItem);			
			contextSubMenu.add(labelExtractSpiralStrokeSubMenuItem);			
			contextSubMenu.add(labelExtractSquareStrokeSubMenuItem);
			contextSubMenu.add(labelExtractWaveStrokeSubMenuItem);			
			contextMenu.add(groupStrokesMenu);
			contextMenu.add(extractStrokesMenu);
		}
	}
	

	private void groupStrokes(String label) {
		System.out.println("GroupStrokes, label==" + label + ", num strokes=="
		                   + m_selected.size());
		
		// must have selected strokes
		if (m_selected.size() < 1) {
			return;
		}
		
		// number of selected strokes == 1? then just label this stroke
//		if (m_selected.size() == 1) {
//			m_selected.get(0).setLabel(label);
//			System.out.println("Label stroke as:"
//			                   + m_selected.get(0).getLabel());
//		}
//		// multiple strokes, group into shape and label the shape, and label
//		// each individual stroke
//		else {
			System.out.println("Group " + m_selected.size() + " and label as: "
			                   + label);
			IShape group = new Shape();
			for (IStroke stroke : m_selected) {
				// stroke.setLabel(label);
				group.addStroke(stroke);
			}
			group.setLabel(label);
			
			m_strokeManager.addShape(group);
//		}
	}
	
	private void groupStrokes(String label, String description) {
		System.out.println("GroupStrokes, label==" + label + ", num strokes=="
		                   + m_selected.size());
		
		// must have selected strokes
		if (m_selected.size() < 1) {
			return;
		}
		
		// number of selected strokes == 1? then just label this stroke
//		if (m_selected.size() == 1) {
//			m_selected.get(0).setLabel(label);
//			System.out.println("Label stroke as:"
//			                   + m_selected.get(0).getLabel());
//		}
//		// multiple strokes, group into shape and label the shape, and label
//		// each individual stroke
//		else {
			System.out.println("Group " + m_selected.size() + " and label as: "
			                   + label);
			IShape group = new Shape();
			for (IStroke stroke : m_selected) {
				// stroke.setLabel(label);
				group.addStroke(stroke);
			}
			group.setLabel(label);
			group.setDescription(description);
			
			m_strokeManager.addShape(group);
//		}
	}
	
}
