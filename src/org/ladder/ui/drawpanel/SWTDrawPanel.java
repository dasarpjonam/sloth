/**
 * SWTDrawPanel.java
 * 
 * Revision History:<br>
 * Oct 29, 2008 jbjohns - File created
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
package org.ladder.ui.drawpanel;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Sketch;
import org.ladder.core.sketch.Stroke;
import org.ladder.io.srl.DOMInputSRL;
import org.ladder.io.srl.DOMOutputSRL;

/**
 * 
 * @author jbjohns
 */
public class SWTDrawPanel {
	
	/**
	 * Default size for the shell (ie JFrame/window) that opens
	 */
	final static Point S_SHELL_SIZE = new Point(1024, 768);
	
	/**
	 * The display is the hook from the UI to the underlying native OS resources
	 */
	private Display m_display;
	
	/**
	 * The shell is the window/JFrame that opens. One shell per window.
	 */
	private Shell m_shell;
	
	/**
	 * Are we in drawing mode?
	 */
	private boolean m_drawing;
	
	/**
	 * The last point that was drawn
	 */
	private Point m_lastMousePoint;
	
	/**
	 * The canvas is the object that I can draw to
	 */
	private Canvas m_drawPanelCanvas;
	
	/**
	 * I also draw to the image, so that I can refresh the screen after things
	 * like popup windows, etc, destroy the UI.
	 */
	private Image m_drawPanelImage;
	
	/**
	 * The sketch I'm adding strokes to
	 */
	private ISketch m_sketch;
	
	/**
	 * The current stroke I'm drawing and adding points to
	 */
	private IStroke m_currentStroke;
	
	
	/**
	 * This draw panel creates its own shell. You just specify the display that
	 * we're to run against.
	 * <p>
	 * This is a bad way to do things in the future, because we'd not want
	 * DrawPanel to be a window, but actually a panel that we can plug into any
	 * arbitrary shell. We'd want to make it an SWT Composite (ie JPanel)
	 * 
	 * @param display
	 *            The display (hook to the operating system resources) that
	 *            we're to run against
	 */
	public SWTDrawPanel(Display display) {
		m_display = display;
		// init the empty sketch
		m_sketch = new Sketch();
		
		// set up the shell (JFrame)
		constructShell();
	}
	

	/**
	 * Construct the shell (window/JFrame) for the draw panel
	 */
	private void constructShell() {
		m_shell = new Shell(m_display, SWT.SHELL_TRIM);
		// title bar for the frame
		m_shell.setText("SWT Test");
		m_shell.setSize(S_SHELL_SIZE);
		
		// how we're laying out the shell.
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		m_shell.setLayout(layout);
		
		// put our widgets in the shell
		populateShellWidgets();
		
		// open and show the shell. We don't pack it because we want the canvas
		// that we're drawing on to be nice and big.
		m_shell.open();
	}
	

	/**
	 * Put our widgets in the shell
	 */
	private void populateShellWidgets() {
		Composite canvasComposite = new Composite(m_shell, SWT.NO_FOCUS);
		canvasComposite.setBackground(m_display.getSystemColor(SWT.COLOR_BLUE));
		// how the canvas is laid out
		GridData layoutData = new GridData();
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.verticalAlignment = SWT.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.grabExcessVerticalSpace = true;
		canvasComposite.setLayoutData(layoutData);
		canvasComposite.setLayout(new FillLayout());
		
		// the canvas that we're drawing on
		m_drawPanelCanvas = new Canvas(canvasComposite, SWT.NO_BACKGROUND);
		
		// the paint listener makes sure that when something obstructs the UI
		// and the canvas needs to be repainted, we restore the sketch that 
		// was underneath.
		m_drawPanelCanvas.addPaintListener(new DrawPanelPaintListener());
		m_drawPanelCanvas.addMouseListener(new DrawPanelMouseListener());
		m_drawPanelCanvas
		        .addMouseMoveListener(new DrawPanelMouseMoveListener());
		
		m_drawPanelImage = new Image(m_display, m_display.getBounds());
		
		Label separator = new Label(m_shell, SWT.HORIZONTAL | SWT.SEPARATOR
		                                     | SWT.CENTER);
		layoutData = new GridData();
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(layoutData);
		
		populateButtons();
	}
	

	private void populateButtons() {
		Composite buttonComposite = new Composite(m_shell, SWT.None);
		GridData layoutData = new GridData();
		layoutData.horizontalAlignment = SWT.FILL;
		layoutData.grabExcessHorizontalSpace = true;
		buttonComposite.setLayoutData(layoutData);
		
		RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.justify = false;
		buttonComposite.setLayout(rowLayout);
		
		Button button = new Button(buttonComposite, SWT.PUSH);
		button.setText("Clear");
		button.addSelectionListener(new SelectionListener() {
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing
			}
			

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				clearCanvas();
			}
		});
		
		Button saveSketchButton = new Button(buttonComposite, SWT.PUSH);
		saveSketchButton.setText("Write Sketch to XML");
		saveSketchButton.addSelectionListener(new SelectionListener() {
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// nothing
			}
			

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fileDialog = new FileDialog(m_shell, SWT.SAVE);
				fileDialog.setText("Save sketch to XML file...");
				fileDialog.setFilterExtensions(new String[] { "*.xml" });
				String filePath = fileDialog.open();
				if (filePath != null) {
					File toFile = new File(filePath);
					DOMOutputSRL output = new DOMOutputSRL();
					try {
						output.toFile(m_sketch, toFile);
						
						MessageBox msgBox = new MessageBox(m_shell,
						        SWT.ICON_INFORMATION | SWT.OK);
						msgBox.setMessage("Sketch [" + toFile.getName()
						                  + "] written");
						msgBox.open();
					}
					catch (Exception err) {
						MessageBox errMsg = new MessageBox(m_shell,
						        SWT.ICON_ERROR | SWT.OK);
						errMsg.setMessage("Writing sketch to file failed: "
						                  + err);
						errMsg.open();
					}
				}
			}
			
		});
		
		Button loadSketchButton = new Button(buttonComposite, SWT.PUSH);
		loadSketchButton.setText("Load Sketch from XML");
		loadSketchButton.addSelectionListener(new SelectionListener() {
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// nothing
			}
			

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent event) {
				FileDialog fileDialog = new FileDialog(m_shell, SWT.SAVE);
				fileDialog.setText("Load sketch from XML file...");
				fileDialog.setFilterExtensions(new String[] { "*.xml" });
				String filePath = fileDialog.open();
				if (filePath != null) {
					File fromFile = new File(filePath);
					DOMInputSRL input = new DOMInputSRL();
					try {
						ISketch sketch = input.parseDocument(fromFile);
						if (sketch == null || sketch.getNumStrokes() == 0) {
							throw new Exception(
							        "The sketch is empty and contains no strokes");
						}
						
						clearCanvas();
						m_sketch = sketch;
						// draw each stroke
						drawSketch();
						
						MessageBox msgBox = new MessageBox(m_shell,
						        SWT.ICON_INFORMATION | SWT.OK);
						msgBox.setMessage("Sketch [" + fromFile.getName()
						                  + "] loaded");
						msgBox.open();
					}
					catch (Exception e) {
						MessageBox errMsg = new MessageBox(m_shell,
						        SWT.ICON_ERROR | SWT.OK);
						errMsg.setMessage("Loading sketch from file failed: "
						                  + e);
						errMsg.open();
					}
				}
			}
			
		});
	}
	
	private void drawSketch() {
		for (IStroke stroke : m_sketch.getStrokes()) {
			drawStroke(stroke);
		}
	}

	private void drawStroke(IStroke stroke) {
		IPoint lastPoint = null;
		for (IPoint point : stroke.getPoints()) {
			if (lastPoint == null) {
				lastPoint = point;
			}
			else {
				drawLineSegment((int) lastPoint.getX(), (int) lastPoint.getY(),
				        (int) point.getX(), (int) point.getY());
				lastPoint = point;
			}
		}
	}
	

	private void drawLineSegment(Point from, Point to) {
		if (m_drawPanelCanvas == null || m_drawPanelImage == null
		    || from == null || to == null) {
			return;
		}
		drawLineSegment(from.x, from.y, to.x, to.y);
	}
	

	private void drawLineSegment(int x1, int y1, int x2, int y2) {
		GC gc = new GC(m_drawPanelImage);
		gc.drawLine(x1, y1, x2, y2);
		gc.dispose();
		
		gc = new GC(m_drawPanelCanvas);
		gc.drawLine(x1, y1, x2, y2);
		gc.dispose();
	}
	

	private void clearCanvas() {
		if (m_drawPanelCanvas == null || m_drawPanelImage == null) {
			return;
		}
		
		m_sketch.clear();
		
		GC gc = new GC(m_drawPanelImage);
		Rectangle drawPanelBounds = m_drawPanelImage.getBounds();
		gc.fillRectangle(drawPanelBounds);
		gc.dispose();
		
		m_drawPanelCanvas.redraw();
	}
	

	private void addPointToCurrentStroke(Point p) {
		if (m_currentStroke != null) {
			m_currentStroke.addPoint(new org.ladder.core.sketch.Point(p.x, p.y,
			        System.currentTimeMillis()));
		}
	}
	

	public Shell getShell() {
		return m_shell;
	}
	

	public static void main(String[] args) {
		Display display = new Display();
		
		SWTDrawPanel test = new SWTDrawPanel(display);
		Shell shell = test.getShell();
		
		// while the shell ("window") is open
		while (!shell.isDisposed()) {
			// is there more work from the OS for us to do?
			if (!display.readAndDispatch()) {
				// if not we can sleep until the next event from the OS
				display.sleep();
			}
		}
		// window is closed, so let's get outta here
		shell.dispose();
		display.dispose();
	}
	
	class DrawPanelMouseListener implements MouseListener {
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseDoubleClick(MouseEvent e) {
			// nothing
		}
		

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseDown(MouseEvent e) {
			m_drawing = true;
			m_lastMousePoint = new Point(e.x, e.y);
			addPointToCurrentStroke(m_lastMousePoint);
			m_currentStroke = new Stroke();
		}
		

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseUp(MouseEvent e) {
			if (m_drawPanelCanvas == null) {
				return;
			}
			
			Point newPoint = new Point(e.x, e.y);
			drawLineSegment(m_lastMousePoint, newPoint);
			
			addPointToCurrentStroke(newPoint);
			m_sketch.addStroke(m_currentStroke);
			
			m_drawing = false;
			m_lastMousePoint = null;
			m_currentStroke = null;
		}
	}
	
	class DrawPanelMouseMoveListener implements MouseMoveListener {
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseMove(MouseEvent e) {
			if (m_drawing) {
				Point newPoint = new Point(e.x, e.y);
				addPointToCurrentStroke(newPoint);
				drawLineSegment(m_lastMousePoint, newPoint);
				m_lastMousePoint = newPoint;
			}
		}
	}
	
	class DrawPanelPaintListener implements PaintListener {
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
		 */
		@Override
		public void paintControl(PaintEvent e) {
//			e.gc.drawImage(m_drawPanelImage, e.x, e.y, e.width, e.height, e.x,
//			        e.y, e.width, e.height);
			drawSketch();
		}
	}
}
