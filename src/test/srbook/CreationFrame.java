package test.srbook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IStroke;
import org.ladder.loader.AbstractJFrame;
import org.ladder.recognition.rubine.RubineClassifier;
import org.ladder.recognition.rubine.RubineStroke;
import org.ladder.ui.UIInitializationException;
import org.ladder.ui.drawpanel.DrawPanelFrame;

public class CreationFrame extends AbstractJFrame {

	/**
	 * Our logger
	 */
	private static Logger log = LadderLogger.getLogger(DrawPanelFrame.class);

	/**
	 * Auto-gen UUID
	 */
	private static final long serialVersionUID = 1524917481224385100L;

	/**
	 * Drawing panel. Overrides the default IUI in AbstractJFrame to support
	 * more functions.
	 */
	private CreationDrawPanelUI m_drawPanel;

	/**
	 * Rubine classifier
	 */
	private RubineClassifier m_rubineClassifier = new RubineClassifier(
			RubineStroke.FeatureSet.Rubine);

	/**
	 * Default constructor
	 */
	public CreationFrame() {
		super();
		log.debug("Construct super AbstractJFrame");

		try {
			// Make a draw panel
			m_drawPanel = new CreationDrawPanelUI(this);
			log.debug("Constructed a draw panel");

			m_drawPanel.setEngine(m_engine);
			log.debug("Set the draw panel's engine");
		} catch (UIInitializationException uiie) {
			uiie.printStackTrace();
			System.exit(ERROR);
		}

		// Initialize the GUI components
		initializeFrame();
	}

	/**
	 * Initialize the frame's GUI parameters
	 */
	private void initializeFrame() {

		// set window size, layout, background color (draw panel itself is
		// transparent), etc.
		setSize(800, 600);
		setLayout(new BorderLayout());
		getContentPane().setBackground(Color.WHITE);

		// set panel components
		m_drawPanel.setStrokeWidth(4.5f);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// create bottom button panel
		JPanel bottomPanel = new JPanel();
		JButton clear = new JButton("Clear");
		clear.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				m_drawPanel.clear();
			}
		});
		bottomPanel.add(clear);

		// add components
		getContentPane().add(m_drawPanel, BorderLayout.CENTER);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		log.debug("Added all panel components");

		// Initialize the menus
		initMenu();
		log.debug("Initialized menus");

		setVisible(true);
		log.debug("DrawPanelFrame initialized");
	}

	/**
	 * Initialize the menus to use in the GUI
	 */
	private void initMenu() {

		// Create the menu bar.
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu recMenu = new JMenu("Recognizers");

		// Rubine
		JCheckBoxMenuItem rubineJMI = new JCheckBoxMenuItem("Rubine Recognizer");
		ActionListener rubineAction = new ActionListener() {

			public void actionPerformed(ActionEvent actionEvent) {
				m_drawPanel.toggleRubine();
			}
		};
		rubineJMI.addActionListener(rubineAction);

		recMenu.add(rubineJMI);

		menuBar.add(recMenu);

		setJMenuBar(menuBar);
	}

	public void outputRubineFeatures(IStroke stroke) {
		
		List<Double> features = m_rubineClassifier.getFeatures(stroke, false,
				true);
		List<String> featureLabels = m_rubineClassifier.getFeatureLabels();

		System.out.println("----------------------------------------------");
		System.out.println("Rubine Features");
		System.out.println("Stroke " + stroke.getID());
		System.out.println("Start = (" + stroke.getFirstPoint().getX() + ","
				+ stroke.getFirstPoint().getY() + ")");
		System.out.println("End = (" + stroke.getLastPoint().getX() + ","
				+ stroke.getLastPoint().getY() + ")");
		System.out.println("(x_min, y_min) = ("
				+ stroke.getBoundingBox().getMinX() + ","
				+ stroke.getBoundingBox().getMinY() + ")");
		System.out.println("(x_max, y_max) = ("
				+ stroke.getBoundingBox().getMaxX() + ","
				+ stroke.getBoundingBox().getMaxY() + ")");
		System.out.println();
		System.out.println("Normalized, y-swapped:");
		System.out.println("Start = ("
				+ (stroke.getFirstPoint().getX() - stroke.getBoundingBox()
						.getMinX())
				+ ","
				+ (stroke.getBoundingBox().getHeight() - (stroke
						.getFirstPoint().getY() - stroke.getBoundingBox()
						.getMinY())) + ")");
		System.out.println("End = ("
				+ (stroke.getLastPoint().getX() - stroke.getBoundingBox()
						.getMinX())
				+ ","
				+ (stroke.getBoundingBox().getHeight() - (stroke.getLastPoint()
						.getY() - stroke.getBoundingBox().getMinY())) + ")");
		System.out.println("(x_min, y_min) = ("
				+ (stroke.getBoundingBox().getMinX() - stroke.getBoundingBox()
						.getMinX())
				+ ","
				+ (stroke.getBoundingBox().getMinY() - stroke.getBoundingBox()
						.getMinY()) + ")");
		System.out.println("(x_max, y_max) = ("
				+ (stroke.getBoundingBox().getMaxX() - stroke.getBoundingBox()
						.getMinX())
				+ ","
				+ (stroke.getBoundingBox().getMaxY() - stroke.getBoundingBox()
						.getMinY()) + ")");
		System.out.println();

		for (int i = 0; i < features.size(); i++) {
			if (featureLabels.size() > i) {
				System.out.println(featureLabels.get(i) + ":   "
						+ features.get(i));
			} else {
				System.out.println((i + 1) + ":   " + features.get(i));
			}
		}

		// Output results for atan
		/*
		System.out.println();
		System.out.println("For atan graphs:");
		double[] xs = m_rubineClassifier.getRubineStroke().getXValues();
		double[] ys = m_rubineClassifier.getRubineStroke().getYValues();
		Point2D.Double[] deltas = m_rubineClassifier.getRubineStroke()
				.getDeltaValues();
		double[] angleDeltas = m_rubineClassifier.getRubineStroke()
				.getAngleDeltas();

		System.out.print("x = [");
		for (int i = 0; i < xs.length - 1; i++) {
			System.out.print(xs[i] + ",");
		}
		System.out.print(xs[xs.length - 1] + "];\n");

		System.out.print("y = [");
		for (int i = 0; i < ys.length - 1; i++) {
			System.out.print(ys[i] + ",");
		}
		System.out.print(ys[ys.length - 1] + "];\n");

		System.out.print("dx = [");
		for (int i = 0; i < deltas.length - 2; i++) {
			System.out.print(deltas[i].getX() + ",");
		}
		System.out.print(deltas[deltas.length - 2].getX() + "];\n");

		System.out.print("dy = [");
		for (int i = 0; i < deltas.length - 2; i++) {
			System.out.print(deltas[i].getY() + ",");
		}
		System.out.print(deltas[deltas.length - 2].getY() + "];\n");

		System.out.print("delta atan2 = [");
		for (int i = 0; i < deltas.length - 2; i++) {
			System.out.print(Math.atan2(deltas[i].getY(), deltas[i].getX())
					+ ",");
		}
		System.out.print(Math.atan2(deltas[deltas.length - 2].getY(),
				deltas[deltas.length - 2].getX())
				+ "];\n");

		System.out.print("angleDeltas = [");
		for (int i = 0; i < angleDeltas.length - 1; i++) {
			System.out.print(angleDeltas[i] + ",");
		}
		System.out.print(angleDeltas[angleDeltas.length - 1] + "];\n");
		*/
	}

	/**
	 * Create a new DrawPanelFrame
	 * 
	 * @param args
	 *            Nothing
	 */
	public static void main(String args[]) {

		@SuppressWarnings("unused")
		CreationFrame dpf = new CreationFrame();
	}

}
