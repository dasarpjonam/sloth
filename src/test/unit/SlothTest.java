package test.unit;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.config.LadderConfig;
import org.ladder.core.sketch.Alias;
import org.ladder.core.sketch.Author;
import org.ladder.core.sketch.IBeautifiable;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISegmentation;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Pen;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Segmentation;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Sketch;
import org.ladder.core.sketch.Speech;
import org.ladder.core.sketch.Stroke;
import org.ladder.core.sketch.IBeautifiable.Type;
import org.ladder.core.sketch.Sketch.SpaceUnits;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.io.DomainDefinitionInputDOM;
import org.xml.sax.SAXException;

/**
 * JUnit testing class that other JUnits can extend from to grab some commonly
 * used funtions
 * 
 * @author Paul Corey, Brandon Paulson, Aaron Wolin
 */
public class SlothTest {
	
	/**
	 * Random number generator
	 */
	public static Random rand = new Random();
	
	/**
	 * Default delta value to use for comparisons
	 */
	public static final double S_DEFAULT_DELTA = Math.pow(10, -10);
	
	
	/**
	 * Default constructor
	 */
	public SlothTest() {
		// Do nothing
	}
	

	/**
	 * Create a Rectangle Shape
	 */
	public static IShape getRect(double x1, double y1, double x2, double y2) {
		List<IPoint> l = new ArrayList<IPoint>();
		Point p1, p2, p3, p4;
		p1 = new Point(x1, y1);
		p2 = new Point(x1, y2);
		p3 = new Point(x2, y2);
		p4 = new Point(x2, y1);
		l.add(p1);
		l.add(p2);
		l.add(p3);
		l.add(p4);
		Stroke s = new Stroke(l);
		List<IStroke> l2 = new ArrayList<IStroke>();
		l2.add(s);
		Shape shape = new Shape(l2, new ArrayList<IShape>());
		shape.setLabel("Rectangle");
		return shape;
	}
	

	/**
	 * Create a random Rectangle Shape
	 * 
	 * @return Random rectangle
	 */
	public static IShape getRect() {
		List<IPoint> l = new ArrayList<IPoint>();
		int xtranslate = (int) (10 * Math.random());
		int ytranslate = (int) (10 * Math.random());
		Point p1, p2, p3, p4;
		p1 = new Point(10 + xtranslate, 20 + ytranslate);
		p2 = new Point(20 + xtranslate, 20 + ytranslate);
		p3 = new Point(10 + xtranslate, 40 + ytranslate);
		p4 = new Point(20 + xtranslate, 40 + ytranslate);
		l.add(p1);
		l.add(p2);
		l.add(p3);
		l.add(p4);
		Stroke s = new Stroke(l);
		List<IStroke> l2 = new ArrayList<IStroke>();
		l2.add(s);
		Shape shape = new Shape(l2, new ArrayList<IShape>());
		shape.setLabel("Rectangle");
		return shape;
	}
	

	/**
	 * Get a line shape
	 * 
	 * @param x1
	 *            Point 1 x-coordinate
	 * @param y1
	 *            Point 1 y-coordinate
	 * @param d
	 *            Point 2 x-coordinate
	 * @param e
	 *            Point 2 y-coordinate
	 * 
	 * @return A line shape
	 */
	public static IShape getLine(double x1, double y1, double d, double e) {
		List<IPoint> l = new ArrayList<IPoint>();
		Point p1, p2;
		p1 = new Point(x1, y1);
		p2 = new Point(d, e);
		l.add(p1);
		l.add(p2);
		Stroke s = new Stroke(l);
		List<IStroke> l2 = new ArrayList<IStroke>();
		l2.add(s);
		Shape shape = new Shape(l2, null);
		shape.setLabel("Line");
		return shape;
	}
	

	/**
	 * Get a line, with one endpoint on the origin and the other on a circle of
	 * radius = 10, where the angle of the line to the positive x axis is the
	 * given angle (IN RADIANS).
	 * 
	 * @param angleRadians
	 *            The angle wrt the positive x axis
	 * @return A shape (line) with the given angle, with one endpoint at the
	 *         origin and the other on a circle with radius 10.
	 */
	public static IShape getRotatedLine(double angleRadians) {
		final double radius = 10.0;
		
		double x = radius * Math.cos(angleRadians);
		double y = radius * Math.sin(angleRadians);
		
		IPoint p1 = new Point(0, 0);
		IPoint p2 = new Point(x, y);
		IStroke stroke = new Stroke();
		stroke.addPoint(p1);
		stroke.addPoint(p2);
		IShape shape = new Shape();
		shape.addStroke(stroke);
		
		shape.setLabel("Line");
		
		return shape;
	}
	

	/**
	 * Get a point shape
	 * 
	 * @param x1
	 *            Point x-coordinate
	 * @param y1
	 *            Point y-coordinate
	 * 
	 * @return A point shape
	 */
	public static IShape getPoint(double x1, double y1) {
		List<IPoint> l = new ArrayList<IPoint>();
		Point p1;
		p1 = new Point(x1, y1);
		l.add(p1);
		Stroke s = new Stroke(l);
		List<IStroke> l2 = new ArrayList<IStroke>();
		l2.add(s);
		Shape shape = new Shape(l2, null);
		shape.setLabel("Point");
		return shape;
	}
	

	/**
	 * Generates a random string
	 * 
	 * @return random string
	 */
	public static String randString() {
		return "token" + Math.random();
	}
	

	/**
	 * Return a map of random attributes
	 * 
	 * @return map of random attributes
	 */
	public static Map<String, String> randAttributes() {
		int numAttrs = rand.nextInt(5) + 1;
		Map<String, String> attributes = new TreeMap<String, String>();
		for (int i = 0; i < numAttrs; i++)
			attributes.put(randString(), randString());
		return attributes;
	}
	

	/**
	 * Return a random author object
	 * 
	 * @return random author object
	 */
	public static Author randAuthor() {
		Author a = new Author();
		a.setDescription(randString());
		a.setDpi(rand.nextDouble(), rand.nextDouble());
		return a;
	}
	

	/**
	 * Generate a random set of authors
	 * 
	 * @return random set of authors
	 */
	public static Set<Author> randAuthorSet() {
		Set<Author> authorSet = new TreeSet<Author>();
		int num = rand.nextInt(5) + 1;
		for (int i = 0; i < num; i++)
			authorSet.add(randAuthor());
		return authorSet;
	}
	

	/**
	 * Return a random pen object
	 * 
	 * @return random pen object
	 */
	public static Pen randPen() {
		Pen p = new Pen();
		p.setBrand(randString());
		p.setDescription(randString());
		p.setPenID(randString());
		return p;
	}
	

	/**
	 * Generate a random set of pens
	 * 
	 * @return random set of pens
	 */
	public static Set<Pen> randPenSet() {
		Set<Pen> penSet = new TreeSet<Pen>();
		int num = rand.nextInt(5) + 1;
		for (int i = 0; i < num; i++)
			penSet.add(randPen());
		return penSet;
	}
	

	/**
	 * Return a random color
	 * 
	 * @return random color
	 */
	public static Color randColor() {
		return new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
	}
	

	/**
	 * Generate a random point
	 * 
	 * @return random point
	 */
	public static Point randPoint() {
		Point p = new Point(rand.nextDouble(), rand.nextDouble(), System
		        .currentTimeMillis());
		p.setAttributes(randAttributes());
		p.setName(randString());
		p.setPressure(rand.nextDouble());
		p.setTilt(rand.nextDouble(), rand.nextDouble());
		return p;
	}
	

	/**
	 * Generate a random list of points
	 * 
	 * @return random list of points
	 */
	public static List<IPoint> randPointList() {
		int numPoints = rand.nextInt(25) + 1;
		List<IPoint> list = new ArrayList<IPoint>();
		for (int i = 0; i < numPoints; i++)
			list.add(randPoint());
		return list;
	}
	

	/**
	 * Generate a random stroke
	 * 
	 * @return random stroke
	 */
	public static Stroke randStroke() {
		int numPoints = rand.nextInt(25) + 1;
		Stroke s = new Stroke();
		for (int i = 0; i < numPoints; i++)
			s.addPoint(randPoint());
		s.setAttributes(randAttributes());
		s.setAuthor(randAuthor());
		s.setColor(randColor());
		s.setLabel(randString());
		s.setPen(randPen());
		s.setVisible(rand.nextBoolean());
		return s;
	}
	

	/**
	 * Generate a random list of random strokes
	 * 
	 * @return random list of strokes
	 */
	public static List<IStroke> randStrokeList() {
		int numStrokes = rand.nextInt(10) + 1;
		List<IStroke> list = new ArrayList<IStroke>();
		for (int i = 0; i < numStrokes; i++)
			list.add(randStroke());
		return list;
	}
	

	/**
	 * Generate a random segmentation
	 * 
	 * @return random segmentation
	 */
	public static Segmentation randSegmentation() {
		Segmentation s = new Segmentation();
		s.setAttributes(randAttributes());
		s.setConfidence(rand.nextDouble());
		s.setLabel(randString());
		s.setSegmentedStrokes(randStrokeList());
		s.setSegmenterName(randString());
		return s;
	}
	

	/**
	 * Generate random list of segmentations
	 * 
	 * @return random list of segmentations
	 */
	public static List<ISegmentation> randSegmentationList() {
		List<ISegmentation> list = new ArrayList<ISegmentation>();
		int numSegs = rand.nextInt(5) + 1;
		for (int i = 0; i < numSegs; i++)
			list.add(randSegmentation());
		return list;
	}
	

	/**
	 * Generate a random shape
	 * 
	 * @return random shape
	 */
	public static Shape randShape() {
		Shape s = new Shape();
		s.setAttributes(randAttributes());
		s.setColor(randColor());
		s.setConfidence(rand.nextDouble());
		s.setDescription(randString());
		s.setLabel(randString());
		s.setOrientation(rand.nextDouble());
		s.setRecognizer(randString());
		s.setStrokes(randStrokeList());
		s.setVisible(rand.nextBoolean());
		return s;
	}
	

	/**
	 * Generate a random list of shapes
	 * 
	 * @return random list of shapes
	 */
	public static List<IShape> randShapeList() {
		int numShapes = rand.nextInt(20) + 1;
		List<IShape> list = new ArrayList<IShape>();
		for (int i = 0; i < numShapes; i++)
			list.add(randShape());
		return list;
	}
	

	/**
	 * Generate random speech object
	 * 
	 * @return random speech object
	 */
	public static Speech randSpeech() {
		Speech s = new Speech();
		s.setDescription(randString());
		s.setPath(randString());
		s.setStartTime(System.currentTimeMillis());
		s.setStopTime(System.currentTimeMillis() + rand.nextLong());
		return s;
	}
	

	/**
	 * Generate a random sketch object
	 * 
	 * @return random sketch
	 */
	public static Sketch randSketch() {
		Sketch s = new Sketch(randStrokeList(), randShapeList());
		s.setAttributes(randAttributes());
		s.setAuthors(randAuthorSet());
		s.setDomain(randString());
		s.setPens(randPenSet());
		s.setSpeech(randSpeech());
		s.setStudy(randString());
		if (rand.nextBoolean())
			s.setUnits(SpaceUnits.HIMETRIC);
		else
			s.setUnits(SpaceUnits.PIXEL);
		return s;
	}
	

	/**
	 * Return a random beautifiable type
	 * 
	 * @return random beautifiable type
	 */
	public static IBeautifiable.Type randType() {
		int x = rand.nextInt(3);
		if (x == 0)
			return Type.NONE;
		if (x == 1)
			return Type.IMAGE;
		return Type.SHAPE;
	}
	

	/**
	 * Generate a random alias
	 * 
	 * @return random alias
	 */
	public static Alias randAlias() {
		return new Alias(randString(), randPoint());
	}
	

	/**
	 * Compares two maps of attributes to determine if they contain the same
	 * keys and values
	 * 
	 * @param map1
	 *            map 1
	 * @param map2
	 *            map 2
	 * @return true if maps contain the same key/value pairs; else false
	 */
	public static boolean equalAttributes(Map<String, String> map1,
	        Map<String, String> map2) {
		boolean same = true;
		Iterator<String> iter = map1.keySet().iterator();
		while (same && iter.hasNext()) {
			String key = iter.next();
			if (!map1.get(key).equals(map2.get(key)))
				same = false;
		}
		return same;
	}
	

	/**
	 * Load the default domain specified in LadderConfig and return it.
	 * 
	 * @return The domain definition for the default domain specified in ladder
	 *         config
	 * @throws ParserConfigurationException
	 *             Thrown by Domain DOM
	 * @throws SAXException
	 *             Thrown by Domain DOM
	 * @throws IOException
	 *             Thrown by Domain DOM
	 */
	public static DomainDefinition loadDefaultDomain()
	        throws ParserConfigurationException, SAXException, IOException {
		DomainDefinition domain = new DomainDefinitionInputDOM()
		        .readDomainDefinitionFromFile(LadderConfig
		                .getProperty(LadderConfig.DOMAIN_DESC_LOC_KEY)
		                                      + LadderConfig
		                                              .getProperty(LadderConfig.DEFAULT_LOAD_DOMAIN_KEY));
		
		return domain;
	}
}
