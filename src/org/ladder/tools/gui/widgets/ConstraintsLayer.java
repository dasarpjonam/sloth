/**
 * ConstraintsLayer.java
 * 
 * Revision History:<br>
 * Nov 25, 2008 intrect - File created
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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.constraint.ConstraintFactory;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.IConstraint;
import org.ladder.recognition.constraint.builders.ShapeBuilder;
import org.ladder.recognition.constraint.domains.ComponentSubPart;
import org.ladder.recognition.constraint.domains.ConstraintParameter;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;
import org.ladder.tools.StrokeManager;
import org.ladder.tools.gui.Layer;

public class ConstraintsLayer extends Layer {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4558276547437446658L;
	
	private static final String name = "ConstraintLayer";
	
	private SRLMenuBar m_menuBar;
	
	private SRLToolPalette m_toolPalette = new SRLToolPalette(
	        "Constraints Tools");
	
	private JComboBox one = new JComboBox(ComponentSubPart.values());
	
	private JComboBox two = new JComboBox(ComponentSubPart.values());
	
	private JComboBox three = new JComboBox(ComponentSubPart.values());
	
	private JComboBox four = new JComboBox(ComponentSubPart.values());
	
	private List<String> constraintNames = null;
	
	private IStroke unarySelection;
	
	private IStroke binarySelection;
	
	private IStroke ternarySelection;
	
	private IStroke quaternarySelection;
	
	private String constraint;
	
	private JLabel constraintCurrent;
	
	private double solve = 0.0;
	
	/**
	 * Low-level recognizer
	 */
	private PaleoSketchRecognizer m_lowLevelRecognizer;
	
	/**
	 * Paleo configuration to use
	 */
	private PaleoConfig m_paleoConfig;
	
	
	public ConstraintsLayer(StrokeManager strokeManager) {
		super(strokeManager);
		
		try {
			constraintNames = new ArrayList<String>(ConstraintFactory
			        .loadAllConstraintNames());
		}
		catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(this,
			        "Could not load some constraints: " + e.getMessage(),
			        "Constraint Load Error", JOptionPane.ERROR_MESSAGE);
		}
		initializeMenuBar();
		initializeToolPalette();
		
		// Primitive recognizer
		m_paleoConfig = PaleoConfig.deepGreenConfig();
		m_lowLevelRecognizer = new PaleoSketchRecognizer(m_paleoConfig);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#registerYourselfAsMouseListener()
	 */
	@Override
	public void registerYourselfAsMouseListener() {
		// add listeners
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	

	@Override
	protected void buildContextMenu(MouseEvent e) {
		
	}
	

	public String getName() {
		return name;
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
				
			}
		});
		
		close.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});
		
		open.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				
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
		
		JMenu constraints = new JMenu("Run Constraint");
		
		for (int i = 0; i < constraintNames.size(); i++) {
			JMenuItem tmpMenuItem = new JMenuItem(constraintNames.get(i));
			constraints.add(tmpMenuItem);
			tmpMenuItem.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					JMenuItem mi = (JMenuItem) e.getSource();
					constraint = mi.getText();
					constraintCurrent.setText(constraint);
					computeConstraints();
				}
			});
		}
		m_menuBar.add(constraints);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.tools.gui.Layer#clear()
	 */
	public void clear() {
		unarySelection = null;
		binarySelection = null;
		ternarySelection = null;
		quaternarySelection = null;
		one.setVisible(false);
		two.setVisible(false);
		three.setVisible(false);
		four.setVisible(false);
		labelLocations.clear();
		
		m_selected.clear();
		m_bufferedGraphics = null;
		refreshScreen();
	}
	

	private void initializeToolPalette() {
		constraintCurrent = new JLabel(constraint);
		m_toolPalette.add(constraintCurrent);
		
		JButton recognize = new JButton("Compute");
		recognize.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				computeConstraints();
			}
			
		});
		m_toolPalette.addButton(recognize);
		
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				clear();
			}
			
		});
		m_toolPalette.addButton(clear);
		
		one.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		two.setBorder(BorderFactory.createLineBorder(Color.RED));
		three.setBorder(BorderFactory.createLineBorder(Color.GREEN));
		four.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
		m_toolPalette.add(one);
		m_toolPalette.add(two);
		m_toolPalette.add(three);
		m_toolPalette.add(four);
		
		JComboBox constraints = new JComboBox(constraintNames.toArray());
		constraints.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JComboBox cb = (JComboBox) arg0.getSource();
				constraint = (String) cb.getSelectedItem();
				constraintCurrent.setText(constraint);
			}
			
		});
		m_toolPalette.add(constraints);
		
		m_toolPalette.getContentPane().setLayout(new FlowLayout());
		m_toolPalette.setSize(225, 250);
	}
	

	@Override
	public SRLMenuBar getMenuBar() {
		return m_menuBar;
	}
	

	@Override
	public List<SRLToolPalette> getToolPalettes() {
		List<SRLToolPalette> list = new ArrayList<SRLToolPalette>();
		list.add(m_toolPalette);
		return list;
	}
	

	@Override
	public void redo() {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void setStatusBar(SRLStatusBar status) {
		this.m_statusBar = status;
	}
	

	@Override
	public void setStrokeManager(StrokeManager strokeManager) {
		m_strokeManager = strokeManager;
	}
	

	@Override
	public void undo() {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void mouseClicked(MouseEvent arg0) {
		
	}
	

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void mousePressed(MouseEvent arg0) {
		trySelection(arg0);
		showContextMenu(arg0);
	}
	

	@Override
	public void mouseReleased(MouseEvent arg0) {
		trySelection(arg0);
		showContextMenu(arg0);
	}
	

	/**
	 * Compute the constraints for the selected reference points
	 */
	private void computeConstraints() {
		if (binarySelection == null && unarySelection != null) {
			// unary
			
			IConstraint c = null;
			try {
				c = ConstraintFactory.getConstraint(constraint + "Constraint",
				        false);
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(this,
				        "Failed to load constraint: " + e.getMessage(),
				        "Constraint Load Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			if (c == null) {
				return;
			}
			
			List<IConstrainable> params = new ArrayList<IConstrainable>();
			IConstrainable constrainable = ShapeBuilder
			        .buildSubShape(getPrimitive(unarySelection));
			params.add(ShapeBuilder.buildParameterizedIConstrainable(
			        constrainable, new ConstraintParameter(getPrimitive(
			                unarySelection).getLabel(), (ComponentSubPart) one
			                .getSelectedItem())));
			solve = c.solve(params);
			m_statusBar.setStatus(constraint + ": " + solve);
		}
		else if (unarySelection != null && binarySelection != null) {
			// binary
			
			IConstraint c = null;
			try {
				c = ConstraintFactory.getConstraint(constraint + "Constraint",
				        false);
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(this,
				        "Failed to load constraint: " + e.getMessage(),
				        "Constraint Load Error", JOptionPane.ERROR_MESSAGE);
			}
			if (c == null) {
				return;
			}
			
			List<IConstrainable> params = new ArrayList<IConstrainable>();
			IConstrainable constrainable1 = ShapeBuilder
			        .buildSubShape(getPrimitive(unarySelection));
			IConstrainable constrainable2 = ShapeBuilder
			        .buildSubShape(getPrimitive(binarySelection));
			params.add(ShapeBuilder.buildParameterizedIConstrainable(
			        constrainable1, new ConstraintParameter(getPrimitive(
			                unarySelection).getLabel(), (ComponentSubPart) one
			                .getSelectedItem())));
			params.add(ShapeBuilder.buildParameterizedIConstrainable(
			        constrainable2, new ConstraintParameter(getPrimitive(
			                binarySelection).getLabel(), (ComponentSubPart) two
			                .getSelectedItem())));
			if (ternarySelection != null) {
				IConstrainable constrainable3 = ShapeBuilder
				        .buildSubShape(getPrimitive(ternarySelection));
				params.add(ShapeBuilder.buildParameterizedIConstrainable(
				        constrainable3, new ConstraintParameter(getPrimitive(
				                ternarySelection).getLabel(),
				                (ComponentSubPart) three.getSelectedItem())));
			}
			if (quaternarySelection != null) {
				IConstrainable constrainable4 = ShapeBuilder
				        .buildSubShape(getPrimitive(quaternarySelection));
				params.add(ShapeBuilder.buildParameterizedIConstrainable(
				        constrainable4, new ConstraintParameter(getPrimitive(
				                quaternarySelection).getLabel(),
				                (ComponentSubPart) four.getSelectedItem())));
			}
			solve = c.solve(params);
			m_statusBar.setStatus(constraint + ": " + solve);
		}
		else if (unarySelection == null) {
			m_statusBar
			        .setStatus("Must selected the proper number of components for computing a constraint.");
			return;
		}
	}
	

	/**
	 * Adds a combo box that allows selection of the proper reference point for
	 * a given stroke.
	 */
	private void addSelectedStrokeWidget() {
		if (binarySelection == null) {
			one.setVisible(true);
		}
		else if (ternarySelection == null) {
			two.setVisible(true);
		}
		else if (quaternarySelection == null) {
			three.setVisible(true);
		}
		else {
			four.setVisible(true);
		}
	}
	

	/**
	 * Get the correct primitive for a stroke
	 * 
	 * @param stroke
	 *            stroke to add
	 */
	public IShape getPrimitive(IStroke stroke) {
		m_lowLevelRecognizer.setStroke(stroke);
		IRecognitionResult primitiveResults = m_lowLevelRecognizer.recognize();
		// System.out.println(primitiveResults);
		IShape bestPrimitive = primitiveResults.getBestShape();
		return bestPrimitive;
	}
	

	protected void trySelection(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1
		    || e.getButton() == MouseEvent.BUTTON3) {
			// left-click or right-click
			IStroke closest = getSelectedStroke(e);
			if (closest != null) {
				if (unarySelection != null && binarySelection != null
				    && ternarySelection != null && quaternarySelection != null) {
					this.refreshScreen();
				}
				if (unarySelection == null) {
					unarySelection = closest;
					addSelectedStrokeWidget();
					this.refreshScreen();
				}
				else if (binarySelection == null && unarySelection != null) {
					if (closest != unarySelection) {
						binarySelection = closest;
						addSelectedStrokeWidget();
						this.refreshScreen();
					}
				}
				else if (binarySelection != null && unarySelection != null
				         && ternarySelection == null) {
					if (closest != unarySelection && closest != binarySelection) {
						ternarySelection = closest;
						addSelectedStrokeWidget();
						this.refreshScreen();
					}
				}
				else if (binarySelection != null && unarySelection != null
				         && ternarySelection != null
				         && quaternarySelection == null) {
					if (closest != unarySelection && closest != binarySelection
					    && closest != ternarySelection) {
						quaternarySelection = closest;
						addSelectedStrokeWidget();
						this.refreshScreen();
					}
				}
			}
			else {
				this.clear();
				this.refreshScreen();
			}
		}
	}
	

	/**
	 * Refresh the draw panel (re-draw all visible elements of the sketch
	 * object)
	 */
	protected void refreshScreen() {
		// To avoid null pointer exception
		if (m_bufferedGraphics == null) {
			m_bufferedGraphics = new BufferedImage(getWidth(), getHeight(),
			        BufferedImage.TYPE_INT_ARGB);
		}
		
		if (unarySelection != null) {
			unarySelection.setColor(Color.BLUE);
			paintStroke(m_bufferedGraphics.getGraphics(), unarySelection);
		}
		if (binarySelection != null) {
			binarySelection.setColor(Color.RED);
			paintStroke(m_bufferedGraphics.getGraphics(), binarySelection);
		}
		if (ternarySelection != null) {
			ternarySelection.setColor(Color.GREEN);
			paintStroke(m_bufferedGraphics.getGraphics(), ternarySelection);
		}
		if (quaternarySelection != null) {
			quaternarySelection.setColor(Color.ORANGE);
			paintStroke(m_bufferedGraphics.getGraphics(), quaternarySelection);
		}
		
		this.repaint();
		this.getParent().repaint();
	}
}
