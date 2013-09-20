package test.unit.ladder.recognition.constraint.confidence;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.constraint.confidence.HorizontalConstraint;

import test.unit.SlothTest;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Tests HorizontalConstraint.  Mostly are the confidences consistent with
 * what I think they should be.
 * 
 * 						Confidence	Reason
 * Horizontal Line		- 	~1		What's more horizontal than horizontal?
 * Vertical Line		-	~0		Can't be any less horizontal
 * 45 Degree PosSlope	-	~.5 	Could go either way
 * 45 Degree NegSlope	-	~.5		Ditto
 * 
 * @author Paul Corey
 *
 */
public class HorizontalConstraintTest extends SlothTest {

	public HorizontalConstraint hc;

	// public static double DEFAULT_THRESHOLD =
	// HorizontalConstraint.DEFAULT_THRESHOLD;

	@Before
	public void setup() {
		hc = null;
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testHorizontalConstraint() {
		hc = new HorizontalConstraint();
		assertNotNull(hc);
		assertEquals(hc.getThreshold(),
				HorizontalConstraint.DEFAULT_THRESHOLD, Math.pow(10, -10));
		assertEquals(hc.getDescription(), HorizontalConstraint.DESCRIPTION);
		assertEquals(hc.getName(), HorizontalConstraint.NAME);
	}

	@Test
	public void testHorizontalConstraintDouble() {
		double t = 15;
		hc = new HorizontalConstraint(t);
		assertNotNull(hc);
		assertEquals(hc.getThreshold(), t, Math.pow(10, -10));
		assertEquals(hc.getDescription(), HorizontalConstraint.DESCRIPTION);
		assertEquals(hc.getName(), HorizontalConstraint.NAME);

		t = 45;
		hc = new HorizontalConstraint(t);
		assertNotNull(hc);
		assertEquals(hc.getThreshold(), t, Math.pow(10, -10));
		assertEquals(hc.getDescription(), HorizontalConstraint.DESCRIPTION);
		assertEquals(hc.getName(), HorizontalConstraint.NAME);
	}

	@Test
	public void testSolveList() {
		hc = new HorizontalConstraint();

		// ArrayList
		// Empty list
		List<IConstrainable> shapes = new ArrayList<IConstrainable>();
		assertEquals(0, hc.solve(shapes), Math.pow(10, -10));

		// Non-line shape
		shapes.add(new ConstrainableShape(getRect()));
		assertEquals(0, hc.solve(shapes), Math.pow(10, -10));

		// Single Line
		// Ideal Positive Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 1, 0)));
		assertTrue(45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertTrue(hc.solve(shapes) > 0);
		assertEquals(.5, hc.solve(shapes), .01);

		// Ideal Negative Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 1)));
		assertTrue(45+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertEquals(.5, hc.solve(shapes), .01);

		// Horizontal
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 0)));
		assertTrue(45-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertTrue(hc.solve(shapes) > 0);
		assertEquals(1, hc.solve(shapes), .01);

		// Vertical
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 0, 0)));
		assertTrue(45+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertTrue(hc.solve(shapes) > 0);
		assertEquals(0, hc.solve(shapes), .01);

		// Multiple Lines
		double c;
		//Line first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		c = hc.solve(shapes);
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertEquals(c, hc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableShape(getRect()));
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertEquals(c, hc.solve(shapes), Math.pow(10, -10));

		//Other first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect()));
		c = hc.solve(shapes);
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertEquals(c, hc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertEquals(c, hc.solve(shapes), Math.pow(10, -10));

		// LinkedList
		// Empty list
		shapes = new LinkedList<IConstrainable>();
		assertEquals(0, hc.solve(shapes), Math.pow(10, -10));

		// Non-line shape
		shapes.add(new ConstrainableShape(getRect()));
		assertEquals(0, hc.solve(shapes), Math.pow(10, -10));

		// Single Line
		// Ideal Positive Slope
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 1, 0)));
		assertTrue(45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertTrue(hc.solve(shapes) > 0);
		assertEquals(.5, hc.solve(shapes), .01);

		// Ideal Negative Slope
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 1)));
		assertTrue(45+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertEquals(.5, hc.solve(shapes), .01);

		// Horizontal
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 0)));
		assertTrue(45-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertTrue(hc.solve(shapes) > 0);
		assertEquals(1, hc.solve(shapes), .01);

		// Vertical
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 0, 0)));
		assertTrue(45+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertTrue(hc.solve(shapes) > 0);
		assertEquals(0, hc.solve(shapes), .01);

		// Multiple Lines
		//Line first
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		c = hc.solve(shapes);
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertEquals(c, hc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableShape(getRect()));
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertEquals(c, hc.solve(shapes), Math.pow(10, -10));

		//Other first
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect()));
		c = hc.solve(shapes);
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertEquals(c, hc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		assertTrue(hc.solve(shapes) >= 0);
		assertTrue(hc.solve(shapes) <= 1);
		assertEquals(c, hc.solve(shapes), Math.pow(10, -10));
}

	@Test
	public void testSolveIConstrainable() {
		ConstrainableLine shape;
		hc = new HorizontalConstraint();
		
		
		// Single Line
		// Ideal Positive Slope
		shape=new ConstrainableLine(getLine(0, 1, 1, 0));
		assertTrue(45==shape.getAngleInDegrees()||225==shape.getAngleInDegrees());
		assertTrue(hc.solve(shape) >= 0);
		assertTrue(hc.solve(shape) <= 1);
		assertTrue(hc.solve(shape) > 0);
		assertEquals(.5, hc.solve(shape), .01);

		// Ideal Negative Slope
		shape=(new ConstrainableLine(getLine(0, 0, 1, 1)));
		assertTrue(45+90==shape.getAngleInDegrees()||225+90==shape.getAngleInDegrees());
		assertTrue(hc.solve(shape) >= 0);
		assertTrue(hc.solve(shape) <= 1);
		assertEquals(.5, hc.solve(shape), .01);

		// Horizontal
		shape=(new ConstrainableLine(getLine(0, 0, 1, 0)));
		assertTrue(45-45==shape.getAngleInDegrees()||225-45==shape.getAngleInDegrees());
		assertTrue(hc.solve(shape) >= 0);
		assertTrue(hc.solve(shape) <= 1);
		assertTrue(hc.solve(shape) > 0);
		assertEquals(1, hc.solve(shape), .01);

		// Vertical
		shape=(new ConstrainableLine(getLine(0, 1, 0, 0)));
		assertTrue(45+45==shape.getAngleInDegrees()||225+45==shape.getAngleInDegrees());
		assertTrue(hc.solve(shape) >= 0);
		assertTrue(hc.solve(shape) <= 1);
		assertTrue(hc.solve(shape) > 0);
		assertEquals(0, hc.solve(shape), .01);
	}

	@Test
	public void testSolve(){
		hc = new HorizontalConstraint();

		// ArrayList
		// Empty list
		List<IConstrainable> shapes = new ArrayList<IConstrainable>();
		assertEquals(0, hc.solve(shapes), Math.pow(10, -10));

		// Non-line shape
		shapes.add(new ConstrainableShape(getRect()));
		hc.setParameters(shapes);
		assertEquals(0, hc.solve(), Math.pow(10, -10));

		// Single Line
		// Ideal Positive Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 1, 0)));
		hc.setParameters(shapes);
		assertTrue(45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(hc.solve() >= 0);
		assertTrue(hc.solve() <= 1);
		assertTrue(hc.solve() > 0);
		assertEquals(.5, hc.solve(), .01);

		// Ideal Negative Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 1)));
		hc.setParameters(shapes);
		assertTrue(45+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(hc.solve() >= 0);
		assertTrue(hc.solve() <= 1);
		assertEquals(.5, hc.solve(), .01);

		// Horizontal
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 0)));
		hc.setParameters(shapes);
		assertTrue(45-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(hc.solve() >= 0);
		assertTrue(hc.solve() <= 1);
		assertTrue(hc.solve() > 0);
		assertEquals(1, hc.solve(), .01);

		// Vertical
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 0, 0)));
		hc.setParameters(shapes);
		assertTrue(45+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(hc.solve() >= 0);
		assertTrue(hc.solve() <= 1);
		assertTrue(hc.solve() > 0);
		assertEquals(0, hc.solve(), .01);

		// Multiple Lines
		double c;
		//Line first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		hc.setParameters(shapes);
		c = hc.solve();
		assertTrue(hc.solve() >= 0);
		assertTrue(hc.solve() <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		hc.setParameters(shapes);
		assertTrue(hc.solve() >= 0);
		assertTrue(hc.solve() <= 1);
		assertEquals(c, hc.solve(), Math.pow(10, -10));
		shapes.add(new ConstrainableShape(getRect()));
		hc.setParameters(shapes);
		assertTrue(hc.solve() >= 0);
		assertTrue(hc.solve() <= 1);
		assertEquals(c, hc.solve(), Math.pow(10, -10));

		//Other first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect()));
		hc.setParameters(shapes);
		c = hc.solve();
		assertTrue(hc.solve() >= 0);
		assertTrue(hc.solve() <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		hc.setParameters(shapes);
		assertTrue(hc.solve() >= 0);
		assertTrue(hc.solve() <= 1);
		assertEquals(c, hc.solve(), Math.pow(10, -10));
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		hc.setParameters(shapes);
		assertTrue(hc.solve() >= 0);
		assertTrue(hc.solve() <= 1);
		assertEquals(c, hc.solve(), Math.pow(10, -10));
	}
}
