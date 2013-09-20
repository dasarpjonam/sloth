/**
 * PaleoTest.java
 * 
 * Revision History:<br>
 * Aug 13, 2008 bpaulson - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
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
package test.functional.ladder.recognition.paleo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISegmentation;
import org.ladder.core.sketch.ISegmenter;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Segmentation;
import org.ladder.core.sketch.Sketch;
import org.ladder.core.sketch.Stroke;
import org.ladder.io.DOMInput;
import org.ladder.io.DOMOutput;
import org.ladder.io.PNGFileFilter;
import org.ladder.io.SousaDataParser;
import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.paleo.OrigPaleoSketchRecognizer;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;
import org.ladder.recognition.paleo.StrokeFeatures;
import org.ladder.recognition.paleo.multistroke.MultiStrokePaleoRecognizer;
import org.ladder.recognition.paleo.paleoNN.PaleoNNRecognizer;
import org.ladder.recognition.temporal.TemporalHistogram;
import org.ladder.recognition.temporal.TemporalHistogramRecognizer;
import org.ladder.segmentation.combination.FSSCombinationSegmenter;
import org.ladder.segmentation.combination.PolylineCombinationSegmenter;
import org.ladder.segmentation.douglaspeucker.DouglasPeuckerSegmenter;
import org.ladder.segmentation.mergecf.MergeCFSegmenter;
import org.ladder.segmentation.paleo.ComplexShapeSegmenter;
import org.ladder.segmentation.paleo.GullSegmenter;
import org.ladder.segmentation.paleo.PaleoSegmenter;
import org.ladder.segmentation.paleo.VSegmenter;
import org.ladder.segmentation.paleo.WaveSegmenter;
import org.ladder.segmentation.sezgin.SezginSegmenter;
import org.ladder.segmentation.shortstraw.ShortStrawSegmenter;
import org.ladder.tools.graph.Plot;
import org.ladder.ui.drawpanel.old.DrawPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Test draw panel for PaleoSketch
 * 
 * @author bpaulson
 */
public class PaleoTest extends JFrame {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 6143585773794727866L;

	/**
	 * Boolean that turns on/off printing direction and curvature values to
	 * console
	 */
	private static final boolean m_printValuesToConsole = true;

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
	private PaleoConfig m_paleoConfig = PaleoConfig.newDefault();

	/**
	 * Map of recognized strokes
	 */
	private HashMap<IStroke, Boolean> m_recognized = new HashMap<IStroke, Boolean>();

	/**
	 * File chooser
	 */
	private JFileChooser m_chooser = new JFileChooser();

	/**
	 * File chooser for images
	 */
	private JFileChooser m_imageChooser = new JFileChooser();

	/**
	 * Choices for graph combo box
	 */
	private String[] m_graphChoices = { "Direction", "Dir No Shift",
			"Curvature", "Paleo Corners", "ShortStraw Corners",
			"MergeCF Corners", "DP Corners", "Sezgin Corners", "Wave Corners",
			"Gull Corners", "V Corners", "Complex Corners",
			"Combination Corners", "FSS Corners" };

	/**
	 * Templates
	 */
	private List<TemporalHistogram> m_templates;

	/**
	 * PaleoSketch recognizer
	 */
	private OrigPaleoSketchRecognizer m_paleo;

	/**
	 * PaleoSketch (NN) recognizer
	 */
	private PaleoNNRecognizer m_paleoNN;

	/**
	 * Combined PaleoSketch recognizer
	 */
	private PaleoSketchRecognizer m_combined;

	/**
	 * Multi-stroke paleo
	 */
	private MultiStrokePaleoRecognizer m_mspr;

	/**
	 * Flag denoting whether or not to use the multi-stroke version of paleo
	 */
	private static final boolean USE_MULTISTROKE_PALEO = true;

	/**
	 * Default constructor
	 */
	public PaleoTest() {
		super();

		m_paleo = new OrigPaleoSketchRecognizer(m_paleoConfig);
		m_paleoNN = new PaleoNNRecognizer(m_paleoConfig);
		m_combined = new PaleoSketchRecognizer(m_paleoConfig);
		m_mspr = new MultiStrokePaleoRecognizer(m_paleoNN);

		// auckland config

		// m_paleoConfig = PaleoConfig.origPaleo();
		// m_paleoConfig.setComplexTestOn(false);

		m_drawPanel = new DrawPanel(m_sketch);

		// Initialize the GUI components
		initializeFrame();

		// loadTemplates();
	}

	/**
	 * Initialize the frame's GUI parameters
	 */
	public void initializeFrame() {

		setTitle("Paleo Test");
		setSize(1024, 768);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// create bottom button panel
		JPanel bottomPanel = new JPanel();

		// Load button
		JButton load = new JButton("Load");
		load.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				m_chooser.setFileFilter(new XMLFileFilter());
				m_chooser.setDialogTitle("Load Sketch from File...");
				int r = m_chooser.showOpenDialog(m_drawPanel);
				if (r == JFileChooser.APPROVE_OPTION) {
					File f = m_chooser.getSelectedFile();
					DOMInput inFile = new DOMInput();
					try {
						m_drawPanel.clear(true);
						m_sketch = inFile.parseDocument(f);
						/*
						 * m_sketch = new Sketch();
						 * m_sketch.setStrokes(readAucklandData(f
						 * .getAbsolutePath()));
						 */
						// remove shapes
						for (int i = m_sketch.getNumShapes() - 1; i >= 0; i--)
							m_sketch.removeShape(m_sketch.getShapes().get(i));
						m_drawPanel.setSketch(m_sketch);
						m_drawPanel.refresh();
					} catch (Exception e) {
						try {
							m_drawPanel.clear(true);
							m_sketch = new Sketch();
							m_sketch.setStrokes(SousaDataParser
									.parseSousaFile(f));
							m_drawPanel.setSketch(m_sketch);
							m_drawPanel.refresh();
						} catch (Exception e1) {
							System.err
									.println("Error loading sketch from file: "
											+ e.getMessage());
						}
					}
				}
			}
		});
		bottomPanel.add(load);

		// Save button
		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				m_chooser.setFileFilter(new XMLFileFilter());
				m_chooser.setDialogTitle("Save Sketch to File...");
				int r = m_chooser.showSaveDialog(m_drawPanel);
				if (r == JFileChooser.APPROVE_OPTION) {
					File f = m_chooser.getSelectedFile();
					DOMOutput outFile = new DOMOutput();
					try {
						if (!f.getName().endsWith(".xml"))
							f = new File(f.getAbsolutePath() + ".xml");
						outFile.toFile(m_sketch, f);
					} catch (Exception e) {
						System.err.println("Error writing sketch to file: "
								+ e.getMessage());
					}
				}
			}
		});
		bottomPanel.add(save);

		// clear button
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				m_drawPanel.clear(true);
				m_recognized = new HashMap<IStroke, Boolean>();
				m_mspr.clear();
			}
		});

		// recognize button
		JButton recognize = new JButton("Recognize");
		recognize.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (!USE_MULTISTROKE_PALEO) {
					for (int i = 0; i < m_sketch.getNumStrokes(); i++) {
						if (m_recognized.containsKey(m_sketch.getStrokes().get(
								i)))
							continue;
						m_paleo.setStroke(m_sketch.getStrokes().get(i));
						// m_paleoNN.setFeatures(m_paleo.getFeatures());
						m_paleoNN.submitForRecognition(m_sketch.getStrokes()
								.get(i));
						m_combined.setStroke(m_sketch.getStrokes().get(i));
						// IRecognitionResult shapes = m_paleo.recognize();
						IRecognitionResult shapes2 = m_paleoNN.recognize();
						// IRecognitionResult shapes3 = m_combined.recognize();
						m_recognized.put(m_sketch.getStrokes().get(i), true);
						// System.out.println("");
						// System.out.println("*** ORIG PALEO ***");
						// for (IShape s : shapes.getNBestList())
						// System.out.println(s.getLabel() + " "
						// + s.getConfidence());
						// System.out.println("");
						System.out.println("*** PALEO NN ***");
						shapes2.sortNBestList();
						for (IShape s : shapes2.getNBestList())
							System.out.println(s.getLabel() + " "
									+ s.getConfidence());
						System.out.println("");
						// System.out.println("*** PALEO COMBINED ***");
						// shapes3.sortNBestList();
						// for (IShape s : shapes3.getNBestList())
						// System.out.println(s.getLabel() + " "
						// + s.getConfidence());
						// System.out.println("");
						m_sketch.addShape(shapes2.getBestShape());
					}
					m_drawPanel.refresh();

					// dash line testing
					// System.out.println("SENDING TO DASH...");
					// DashRecognizer dr = new
					// DashRecognizer(m_sketch.getShapes());
					// List<IShape> shapes = dr.recognize();
					// for (IShape s : shapes) {
					// if (s.getSubShapes().size() > 0)
					// System.out.println(s.getLabel() + " ("
					// + s.getSubShapes().size() + ")");
					// else
					// System.out.println(s.getLabel());
					// }

				} else {
					List<IStroke> unrecognized = new ArrayList<IStroke>();
					for (int i = 0; i < m_sketch.getNumStrokes(); i++) {
						if (!m_recognized.containsKey(m_sketch.getStrokes()
								.get(i)))
							unrecognized.add(m_sketch.getStrokes().get(i));
					}

					// multi-stroke paleo
					System.out.println("====== START ========");
					m_mspr.submitForRecognition(unrecognized);
					List<IRecognitionResult> results = m_mspr.recognize();
					for (IStroke s : unrecognized)
						m_recognized.put(s, true);
					m_sketch.getShapes().clear();
					for (IRecognitionResult r : results) {
						System.out.println("");
						System.out.println("*** MULTI-STROKE PALEO ***");
						for (IShape s : r.getNBestList())
							System.out.println(s.getLabel() + " "
									+ s.getConfidence());
						System.out.println("");
						m_sketch.addShape(r.getBestShape());
					}
					m_drawPanel.refresh();
					System.out.println("====== END ========");

				}
			}
		});
		bottomPanel.add(clear);
		bottomPanel.add(recognize);

		// graph selection view
		final JComboBox graphChoice = new JComboBox(m_graphChoices);
		JButton plot = new JButton("Plot");
		plot.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (m_sketch.getNumStrokes() == 0)
					return;
				if (graphChoice.getSelectedItem().toString()
						.compareToIgnoreCase("Curvature") == 0)
					plotCurvature();
				else if (graphChoice.getSelectedItem().toString()
						.compareToIgnoreCase("Direction") == 0)
					plotDirection(true);
				else if (graphChoice.getSelectedItem().toString()
						.compareToIgnoreCase("Dir No Shift") == 0)
					plotDirection(false);
				else if (graphChoice.getSelectedItem().toString()
						.compareToIgnoreCase("Paleo Corners") == 0)
					plotCorners(new PaleoSegmenter(m_paleoConfig
							.getHeuristics().FILTER_DIR_GRAPH));
				else if (graphChoice.getSelectedItem().toString()
						.compareToIgnoreCase("ShortStraw Corners") == 0)
					plotCorners(new ShortStrawSegmenter());
				else if (graphChoice.getSelectedItem().toString()
						.compareToIgnoreCase("MergeCF Corners") == 0)
					plotCorners(new MergeCFSegmenter());
				else if (graphChoice.getSelectedItem().toString()
						.compareToIgnoreCase("DP Corners") == 0)
					plotCorners(new DouglasPeuckerSegmenter());
				else if (graphChoice.getSelectedItem().toString()
						.compareToIgnoreCase("Sezgin Corners") == 0)
					plotCorners(new SezginSegmenter());
				else if (graphChoice.getSelectedItem().toString()
						.compareToIgnoreCase("Wave Corners") == 0)
					plotCorners(new WaveSegmenter());
				else if (graphChoice.getSelectedItem().toString()
						.compareToIgnoreCase("Gull Corners") == 0)
					plotCorners(new GullSegmenter());
				else if (graphChoice.getSelectedItem().toString()
						.compareToIgnoreCase("V Corners") == 0)
					plotCorners(new VSegmenter());
				else if (graphChoice.getSelectedItem().toString()
						.compareToIgnoreCase("Complex Corners") == 0)
					plotCorners(new ComplexShapeSegmenter());
				else if (graphChoice.getSelectedItem().toString()
						.compareToIgnoreCase("Combination Corners") == 0) {
					plotCorners(new PolylineCombinationSegmenter(m_paleoConfig
							.getHeuristics().FILTER_DIR_GRAPH));
				} else if (graphChoice.getSelectedItem().toString()
						.compareToIgnoreCase("FSS Corners") == 0) {
					plotCorners(new FSSCombinationSegmenter(m_paleoConfig
							.getHeuristics().FILTER_DIR_GRAPH));
				}
			}
		});
		bottomPanel.add(graphChoice);
		bottomPanel.add(plot);

		// label button
		JButton label = new JButton("Label");
		label.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				for (int i = 0; i < m_sketch.getNumStrokes(); i++) {
					if (m_sketch.getStrokes().get(i).getLabel() == null
							|| m_sketch.getStrokes().get(i).getLabel() == "")
						m_drawPanel.addLabel(Integer.toString(i), m_sketch
								.getStrokes().get(i), false);
					else
						m_drawPanel.addLabel(m_sketch.getStrokes().get(i)
								.getLabel(), m_sketch.getStrokes().get(i),
								false);
				}
				m_drawPanel.refresh();
			}
		});
		bottomPanel.add(label);

		JButton histogram = new JButton("Histogram");
		histogram.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				TemporalHistogram hist = new TemporalHistogram(m_drawPanel
						.getSketch().getStrokes(), null);
				hist.printHistogram();
				TemporalHistogramRecognizer r = new TemporalHistogramRecognizer(
						m_templates);
				r.setQuery(hist);
				IRecognitionResult result = r.recognize();
				System.out.println("");
				for (IShape s : result.getNBestList()) {
					System.out.println(s.getLabel() + " " + s.getConfidence());
				}
				System.out.println("");
			}

		});
		// bottomPanel.add(histogram);

		// to image button
		JButton image = new JButton("Save To Image");
		image.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				m_imageChooser.setFileFilter(new PNGFileFilter());
				int r = m_imageChooser.showSaveDialog(null);
				if (r == JFileChooser.APPROVE_OPTION) {
					File f = m_imageChooser.getSelectedFile();
					if (!f.getName().endsWith(".png")
							|| !f.getName().endsWith(".PNG"))
						f = new File(f.getAbsolutePath() + ".png");
					writeImage(m_drawPanel.getSketch(), f);
				}
			}

		});
		bottomPanel.add(image);

		// add components
		getContentPane().add(m_drawPanel, BorderLayout.CENTER);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);

		setVisible(true);
	}

	/**
	 * Plot curvature graph of strokes
	 */
	public void plotCurvature() {
		for (int i = m_sketch.getNumStrokes() - 1; i >= 0; i--) {
			StrokeFeatures sf = new StrokeFeatures(
					m_sketch.getStrokes().get(i),
					m_paleoConfig.getHeuristics().FILTER_DIR_GRAPH);
			Plot plot = new Plot("Stroke " + i + " Curvature");
			plot
					.addLine(sf.getLengthSoFar2nd(), sf.getCurvature(),
							Color.black);
			if (m_printValuesToConsole) {
				for (int j = 0; j < sf.getCurvature().length - 1; j++)
					System.out.print(sf.getCurvature()[j] + ", ");
				System.out
						.println(sf.getCurvature()[sf.getCurvature().length - 1]);
			}
			plot.plot();
		}
	}

	/**
	 * Plot speed graph of strokes
	 */
	public void plotDirection(boolean useShiftedGraph) {
		for (int i = m_sketch.getNumStrokes() - 1; i >= 0; i--) {
			StrokeFeatures sf = new StrokeFeatures(
					m_sketch.getStrokes().get(i),
					m_paleoConfig.getHeuristics().FILTER_DIR_GRAPH);
			Plot plot = new Plot("Stroke " + i + " Direction");
			double[] dir;
			if (useShiftedGraph)
				dir = sf.getDir();
			else
				dir = sf.getDirNoShift();
			plot.addLine(sf.getLengthSoFar(), dir, Color.black);
			if (m_printValuesToConsole && dir != null) {
				for (int j = 0; j < dir.length - 1; j++)
					System.out.print(dir[j] + ", ");
				System.out.println(dir[dir.length - 1]);
			}
			plot.plot();
		}
	}

	/**
	 * Plot corners of strokes
	 * 
	 * @param seg
	 *            segmenter to use for corners
	 */
	public void plotCorners(ISegmenter seg) {
		for (int i = m_sketch.getNumStrokes() - 1; i >= 0; i--) {
			StrokeFeatures sf = new StrokeFeatures(
					m_sketch.getStrokes().get(i),
					m_paleoConfig.getHeuristics().FILTER_DIR_GRAPH);
			seg.setStroke(m_sketch.getStrokes().get(i));
			try {
				ISegmentation segmentation = seg.getSegmentations().get(0);
				List<IPoint> corners = sf.getCorners(segmentation);
				double[] x = new double[corners.size()];
				double[] y = new double[corners.size()];
				for (int j = 0; j < corners.size(); j++) {
					x[j] = corners.get(j).getX();
					y[j] = corners.get(j).getY();
				}
				String title = "Stroke " + i + " Corners (" + seg.getName()
						+ ")";
				if (((Segmentation) segmentation).getLabel() != null)
					title += " " + ((Segmentation) segmentation).getLabel();
				Plot plot = new Plot(title);
				plot.addLine(sf.getOrigPoints(), Color.red);
				plot.addLine(x, y, Color.black);
				plot.setKeepdim(true);
				plot.plot();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null,
						"Error: unable to plot corners for stroke " + i
								+ " and corner finder " + seg.getName() + "\n"
								+ e.getMessage());
			}
		}
	}

	/**
	 * Load templates (for testing temporal recognizer)
	 */
	public void loadTemplates() {
		// load main directory
		File dir;
		JFileChooser c = new JFileChooser();
		c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int r = c.showOpenDialog(null);
		if (r == JFileChooser.APPROVE_OPTION)
			dir = c.getSelectedFile();
		else
			return;
		m_templates = new ArrayList<TemporalHistogram>();
		for (int i = 0; i < dir.listFiles().length; i++) {
			try {
				ISketch s = new DOMInput().parseDocument(dir.listFiles()[i]);
				TemporalHistogram hist = new TemporalHistogram(s.getStrokes(),
						dir.listFiles()[i].getName());
				m_templates.add(hist);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Reads the data from University of Auckland
	 * 
	 * @param PATH
	 *            file path
	 * @return list of parsed strokes
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static List<IStroke> readAucklandData(String PATH)
			throws ParserConfigurationException, SAXException, IOException {
		List<IStroke> strokes = new ArrayList<IStroke>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document document = builder.parse(PATH);
		NodeList strokesNodeList = document.getDocumentElement()
				.getChildNodes();

		for (int i = 0; i < strokesNodeList.getLength(); i++) {
			Node strokeNode = strokesNodeList.item(i);
			if (strokeNode.getNodeType() == Node.ELEMENT_NODE
					&& ((Element) strokeNode).getTagName().equals("Stroke")) {
				NodeList pointNodeList = strokeNode.getChildNodes();

				Stroke stroke = new Stroke();

				for (int j = 0; j < pointNodeList.getLength(); j++) {
					Node pointNode = pointNodeList.item(j);
					if (pointNode.getNodeType() == Node.ELEMENT_NODE
							&& ((Element) pointNode).getTagName().equals(
									"Point")) {

						if (pointNode.hasAttributes()) {
							NamedNodeMap attributes = pointNode.getAttributes();

							stroke.addPoint(new Point(Double
									.parseDouble(attributes.getNamedItem("X")
											.getNodeValue()), Double
									.parseDouble(attributes.getNamedItem("Y")
											.getNodeValue()), Long
									.parseLong(attributes.getNamedItem("Time")
											.getNodeValue())));

							// System.out.println(pointNode.getNodeName()+ " " +
							// attributes.getNamedItem("X").getNodeValue());

						}
					}
				}
				strokes.add(stroke);
			}
		}
		return strokes;
	}

	/**
	 * Write the sketch (as an image) out to file
	 * 
	 * @param sketch
	 *            sketch to write out
	 * @param f
	 *            file to write to
	 */
	private static void writeImage(ISketch sketch, File f) {

		// generate buffered image
		BoundingBox bb = sketch.getBoundingBox();
		BufferedImage img = new BufferedImage((int) bb.width + 6,
				(int) bb.height + 6, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) img.getGraphics();
		g2.setStroke(new java.awt.BasicStroke(2.0f));
		g2.setBackground(Color.white);
		g2.setColor(Color.black);
		for (IStroke stroke : sketch.getStrokes()) {
			for (int i = 0; i < stroke.getNumPoints() - 1; i++) {
				IPoint p1 = stroke.getPoint(i);
				IPoint p2 = stroke.getPoint(i + 1);
				g2.drawLine((int) (p1.getX() - bb.getMinX() + 3), (int) (p1
						.getY()
						- bb.getMinY() + 3),
						(int) (p2.getX() - bb.getMinX() + 3), (int) (p2.getY()
								- bb.getMinY() + 3));
			}
		}

		// write image out to file
		try {
			ImageIO.write(img, "PNG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a new PaleoTest
	 * 
	 * @param args
	 *            Nothing
	 */
	public static void main(String args[]) {

		@SuppressWarnings("unused")
		PaleoTest pTest = new PaleoTest();
	}
}
