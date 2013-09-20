/**
 * PaleoLabelerGUI.java
 * 
 * Revision History:<br>
 * Dec 9, 2008 bpaulson - File created
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
package org.ladder.tools.gui.paleo.labeler;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import org.ladder.core.config.LadderConfig;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.DOMInput;
import org.ladder.io.DOMOutput;
import org.ladder.io.ShapeDirFilter;
import org.ladder.io.SousaDataParser;
import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.paleo.Fit;
import org.ladder.recognition.paleo.OrigPaleoSketchRecognizer;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.tools.gui.event.KeyboardShortcutEvent;
import org.ladder.ui.drawpanel.old.DrawPanel;
import org.ladder.ui.drawpanel.old.DrawPanel.LabelInfo;

/**
 * GUI used to label low-level primitives in collected sketch files
 * 
 * @author bpaulson
 */
public class PaleoLabelerGUI extends JFrame implements ActionListener {
	
	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 8485897506738282235L;
	
	/**
	 * DrawPanelUI
	 */
	private DrawPanel m_drawPanel;
	
	/**
	 * Sketch object
	 */
	private ISketch m_sketch = new Sketch();
	
	/**
	 * Paleo configuration to use
	 */
	private PaleoConfig m_paleoConfig = PaleoConfig.deepGreenConfig();
	
	/**
	 * PaleoSketch recognizer
	 */
	private OrigPaleoSketchRecognizer m_paleoRecognizer;
	
	/**
	 * File chooser
	 */
	private JFileChooser m_chooser = new JFileChooser();
	
	/**
	 * Pop up menu
	 */
	private JPopupMenu m_popup = new JPopupMenu();
	
	/**
	 * Menu bar
	 */
	private JMenuBar m_menu = new JMenuBar();
	
	/**
	 * File menu
	 */
	private JMenu m_file = new JMenu("File");
	
	/**
	 * Start item
	 */
	private JMenuItem m_start = new JMenuItem("Start Batch");
	
	/**
	 * Bottom button panel
	 */
	private JPanel m_bottomPanel = new JPanel();
	
	/**
	 * Previous sketch button
	 */
	private JButton m_prev = new JButton("<-- Previous");
	
	/**
	 * Next sketch button
	 */
	private JButton m_next = new JButton("Next -->");
	
	/**
	 * List of files to parse through
	 */
	private List<File> m_files = new ArrayList<File>();
	
	/**
	 * Current file index
	 */
	private int m_fileIndex = -1;
	
	/**
	 * Test data directory
	 */
	private File m_testDataDir;
	
	/**
	 * History of files viewed
	 */
	private Set<String> m_history = new HashSet<String>();
	
	/**
	 * Flag used to specify whether pre-labeled files should be skipped in batch
	 * mode
	 */
	private static boolean m_skipPreLabeled = false;
	
	
	/**
	 * Default constructor
	 */
	public PaleoLabelerGUI() {
		super();
		// m_paleoConfig = PaleoConfig.deepGreenConfig();
		m_paleoRecognizer = new OrigPaleoSketchRecognizer(m_paleoConfig);
		m_drawPanel = new DrawPanel(m_sketch);
		m_drawPanel.setReadOnly(true);
		m_drawPanel.setShowFirstPoint(true);
		setJMenuBar(m_menu);
		
		// Initialize the GUI components
		initializeFrame();
		initializeMenu();
		initializePopup();
		initializeListeners();
		
		m_testDataDir = new File(LadderConfig.getProperty("testData"));
	}
	

	/**
	 * Initialize file menu
	 */
	public void initializeMenu() {
		m_menu.add(m_file);
		m_file.add(m_start);
		m_start.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				startBatch();
			}
		});
	}
	

	/**
	 * Initialize pop up menu
	 */
	public void initializePopup() {
		// add special "split" flag
		JMenuItem split = new JMenuItem("Split");
		split.addActionListener(this);
		m_popup.add(split);
		
		// add special "unintentional" flag
		JMenuItem uitem = new JMenuItem("Unintentional");
		uitem.addActionListener(this);
		m_popup.add(uitem);
		
		// add text flag
		JMenuItem text = new JMenuItem("Text");
		text.setMnemonic(KeyEvent.VK_T);
		text.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0));
		text.addActionListener(this);
		m_popup.add(text);
		
		for (String s : m_paleoConfig.getShapesTurnedOn()) {
			// special case
			if (s == Fit.POLYLINE || s == Fit.POLYGON) {
				JMenu poly = new JMenu(s);
				for (int i = 2; i <= 15; i++) {
					JMenuItem item = new JMenuItem(s + " (" + i + ")");
					item.addActionListener(this);
					poly.add(item);
				}
				m_popup.add(poly);
			}
			else {
				JMenuItem item = new JMenuItem(s);
				item.addActionListener(this);
				m_popup.add(item);
			}
		}
	}
	

	/**
	 * Initialize frame listeners
	 */
	public void initializeListeners() {
		addComponentListener(new ComponentListener() {
			
			@Override
			public void componentHidden(ComponentEvent e) {
			}
			

			@Override
			public void componentMoved(ComponentEvent e) {
			}
			

			@Override
			public void componentResized(ComponentEvent e) {
				m_drawPanel.refresh();
			}
			

			@Override
			public void componentShown(ComponentEvent e) {
			}
			
		});
		m_drawPanel.addMouseListener(new MouseListener() {
			
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
				LabelInfo closest = null;
				double dis = Double.MAX_VALUE;
				for (int i = 0; i < m_drawPanel.getLabels().size(); i++) {
					LabelInfo label = m_drawPanel.getLabels().get(i);
					double d = e.getPoint().distance(label.x, label.y);
					if (d < dis) {
						dis = d;
						closest = label;
					}
				}
				if (dis < 50 && closest.text != "") {
					int tmp = e.getModifiers();
					
					// shift click
					if (tmp == 5 || tmp == 17) {
						m_drawPanel.addHighlightIndex(closest.stroke);
					}
					else if (e.getButton() == MouseEvent.BUTTON1
					         || (e.getButton() == MouseEvent.BUTTON3
					             && tmp == 4 && m_drawPanel.getHighlightIndex()
					                 .size() <= 1)) {
						m_drawPanel.clearHighlight();
						m_drawPanel.addHighlightIndex(closest.stroke);
					}
					m_drawPanel.refresh();
				}
				else {
					m_drawPanel.clearHighlight();
					m_drawPanel.refresh();
				}
				if (m_drawPanel.getHighlightIndex().size() > 0
				    && e.getButton() == MouseEvent.BUTTON3) {
					m_popup.getComponent(0).setEnabled(false);
					for (IShape s : m_sketch.getShapes()) {
						if (s.containsStroke(closest.stroke)) {
							m_popup.getComponent(0).setEnabled(true);
							break;
						}
					}
					m_popup.show(m_drawPanel, e.getX(), e.getY());
				}
			}
			

			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
		});
	}
	

	/**
	 * Initialize the frame's GUI parameters
	 */
	public void initializeFrame() {
		
		setTitle("Paleo Labeler");
		setSize(1024, 768);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Load button
		JButton load = new JButton("Load");
		load.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				m_chooser.setFileFilter(new XMLFileFilter());
				m_chooser.setDialogTitle("Load Sketch from File...");
				int r = m_chooser.showOpenDialog(m_drawPanel);
				if (r == JFileChooser.APPROVE_OPTION) {
					File f = m_chooser.getSelectedFile();
					loadFile(f);
				}
			}
		});
		m_bottomPanel.add(load);
		
		// Save button
		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				m_chooser.setFileFilter(new XMLFileFilter());
				m_chooser.setDialogTitle("Save Sketch to File...");
				int r = m_chooser.showSaveDialog(m_drawPanel);
				if (r == JFileChooser.APPROVE_OPTION) {
					File f = m_chooser.getSelectedFile();
					saveFile(f);
				}
			}
		});
		m_bottomPanel.add(save);
		
		// add components
		getContentPane().add(m_drawPanel, BorderLayout.CENTER);
		getContentPane().add(m_bottomPanel, BorderLayout.SOUTH);
	}
	

	/**
	 * Label the strokes on the screen
	 * 
	 * @return true if strokes were already labeled; else false
	 */
	public boolean labelStrokes() {
		boolean preLabeled = true;
		List<IStroke> labeled = new ArrayList<IStroke>();
		for (IShape sh : m_drawPanel.getSketch().getShapes()) {
			String label = sh.getLabel() + " - ";
			for (int i = 0; i < sh.getStrokes().size(); i++) {
				label += sh.getStrokes().get(i).getLabel() + " ";
				labeled.add(sh.getStroke(i));
			}
			m_drawPanel.addLabel(label, sh.getLastStroke(), true);
			preLabeled = true;
		}
		for (IStroke str : m_drawPanel.getSketch().getStrokes()) {
			if (labeled.contains(str))
				continue;
			String label = str.getLabel();
			if (str.getLabel() == null || str.getLabel() == "") {
				m_paleoRecognizer.setStroke(str);
				IRecognitionResult r = m_paleoRecognizer.recognize();
				r.sortNBestList();
				List<IShape> results = r.getNBestList();
				label = results.get(0).getLabel();
				str.setLabel(label);
				preLabeled = false;
			}
			m_drawPanel.addLabel(label, str, true);
		}
		return preLabeled;
	}
	

	/**
	 * Start batch program
	 */
	public void startBatch() {
		int r = JOptionPane
		        .showConfirmDialog(
		                this,
		                "This will begin batch labeling of items in your data directory (as specified in the ladder.conf file)\n\n"
		                        + "Your directory is currently set to:\n"
		                        + m_testDataDir.getAbsolutePath()
		                        + "\n\n"
		                        + "Do you wish to continue?");
		if (r != JOptionPane.YES_OPTION)
			return;
		m_prev.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				displayPrevFile();
			}
			
		});
		
		m_next.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				displayNextFile();
			}
			
		});
		m_bottomPanel.removeAll();
		
		// Added LEFT-Arrow and RIGHT-Arrow for PREV and NEXT
		InputMap prevInputMap = m_prev
		        .getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW);
		KeyStroke prev = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
		prevInputMap.put(prev, "PREV");
		m_prev.getActionMap().put("PREV", new KeyboardShortcutEvent(m_prev));
		m_bottomPanel.add(m_prev);
		
		m_prev.setEnabled(false);
		
		InputMap nextInputMap = m_next
		        .getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW);
		KeyStroke next = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
		nextInputMap.put(next, "NEXT");
		m_next.getActionMap().put("NEXT", new KeyboardShortcutEvent(m_next));
		m_bottomPanel.add(m_next);
		
		loadFiles();
		displayNextFile();
		validate();
		repaint();
	}
	

	/**
	 * Load all files to verify into list
	 */
	private void loadFiles() {
		File[] shapeDirs = m_testDataDir.listFiles(new ShapeDirFilter());
		for (File shapeDir : shapeDirs) {
			File[] files = shapeDir.listFiles(new XMLFileFilter());
			for (File f : files)
				m_files.add(f);
		}
	}
	

	/**
	 * Display the next file in the list
	 */
	private void displayNextFile() {
		boolean preLabeled;
		if (m_fileIndex >= 0 && m_fileIndex < m_files.size())
			saveFile(m_files.get(m_fileIndex));
		m_fileIndex++;
		if (m_fileIndex == m_files.size())
			m_next.setEnabled(false);
		else
			m_next.setEnabled(true);
		if (m_fileIndex > 0)
			m_prev.setEnabled(true);
		if (m_fileIndex < m_files.size()) {
			preLabeled = loadFile(m_files.get(m_fileIndex));
			if (preLabeled
			    && m_skipPreLabeled
			    && !m_history.contains(m_files.get(m_fileIndex)
			            .getAbsolutePath()))
				displayNextFile();
		}
		else {
			m_drawPanel.clear(true);
			setTitle("Paleo Labeler - DONE!");
		}
	}
	

	/**
	 * Display the previous file in the list
	 */
	private void displayPrevFile() {
		if (m_fileIndex >= 0 && m_fileIndex < m_files.size())
			saveFile(m_files.get(m_fileIndex));
		m_fileIndex--;
		if (m_fileIndex == 0)
			m_prev.setEnabled(false);
		else
			m_prev.setEnabled(true);
		m_next.setEnabled(true);
		boolean preLabeled = loadFile(m_files.get(m_fileIndex));
		if (preLabeled && m_skipPreLabeled
		    && !m_history.contains(m_files.get(m_fileIndex).getAbsolutePath()))
			displayPrevFile();
	}
	

	/**
	 * Load file into draw panel
	 * 
	 * @param f
	 *            file to load
	 * @return true if file has been labeled already; else false
	 */
	private boolean loadFile(File f) {
		boolean preLabeled = false;
		DOMInput inFile = new DOMInput();
		try {
			m_drawPanel.clear(true);
			m_sketch = inFile.parseDocument(f);
			m_drawPanel.setSketch(m_sketch);
			preLabeled = labelStrokes();
			setTitle("Paleo Labeler - " + f.getName());
			m_drawPanel.refresh();
			if (!preLabeled && m_skipPreLabeled)
				m_history.add(f.getAbsolutePath());
		}
		catch (Exception e) {
			try {
				m_drawPanel.clear(true);
				m_sketch = new Sketch();
				m_sketch.setStrokes(SousaDataParser.parseSousaFile(f));
				m_drawPanel.setSketch(m_sketch);
				preLabeled = labelStrokes();
				m_drawPanel.refresh();
				if (!preLabeled && m_skipPreLabeled)
					m_history.add(f.getAbsolutePath());
			}
			catch (Exception e1) {
				System.err.println("Error loading sketch from file: "
				                   + e.getMessage());
			}
		}
		return preLabeled;
	}
	

	/**
	 * Save sketch to file
	 * 
	 * @param f
	 *            file to save to
	 */
	private void saveFile(File f) {
		DOMOutput outFile = new DOMOutput();
		try {
			if (!f.getName().endsWith(".xml"))
				f = new File(f.getAbsolutePath() + ".xml");
			outFile.toFile(m_sketch, f);
		}
		catch (Exception e) {
			System.err.println("Error writing sketch to file: "
			                   + e.getMessage());
		}
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PaleoLabelerGUI gui = new PaleoLabelerGUI();
		gui.setVisible(true);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem item = (JMenuItem) e.getSource();
		String label = item.getText();
		if (label.equals("Complex")) {
			String x = JOptionPane
			        .showInputDialog("Please enter complex shapes; each should be separated with a comma");
			label = label + " (" + x + ")";
		}
		if (label.equals("Split")) {
			for (IStroke str : m_drawPanel.getHighlightIndex()) {
				IShape sh = m_drawPanel.shapeContains(str);
				m_sketch.removeShape(sh);
				m_drawPanel.removeLabel(str);
			}
			m_drawPanel.clearHighlight();
			labelStrokes();
		}
		else {
			if (m_drawPanel.getHighlightIndex().size() == 1) {
				IStroke selected = m_drawPanel.getHighlightIndex().get(0);
				selected.setLabel(label);
				m_drawPanel.getLabel(selected).text = label;
			}
			else {
				IShape shape = new Shape();
				String tmpLabel = label + " - ";
				for (IStroke str : m_drawPanel.getHighlightIndex()) {
					if (!shape.containsStroke(str))
						shape.addStroke(str);
					tmpLabel += str.getLabel() + " ";
					if (str == m_drawPanel.getHighlightIndex().get(
					        m_drawPanel.getHighlightIndex().size() - 1))
						m_drawPanel.getLabel(str).text = tmpLabel;
					else
						m_drawPanel.removeLabel(str);
					removeOldShapes(str);
				}
				shape.setLabel(label);
				m_sketch.addShape(shape);
			}
		}
		m_drawPanel.refresh();
	}
	

	/**
	 * Removes shapes containing the given stroke
	 * 
	 * @param str
	 *            stroke
	 */
	private void removeOldShapes(IStroke str) {
		for (int i = m_sketch.getShapes().size() - 1; i >= 0; i--)
			if (m_sketch.getShapes().get(i).containsStroke(str))
				m_sketch.removeShape(m_sketch.getShapes().get(i));
	}
	
}
