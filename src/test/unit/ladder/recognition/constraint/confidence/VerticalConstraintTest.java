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
import org.ladder.recognition.constraint.confidence.VerticalConstraint;

import test.unit.SlothTest;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Tests VerticalConstraint.  Mostly are the confidences consistent with
 * what I think they should be.
 * 
 * 						Confidence	Reason
 * Vertical Line		- 	~1		What's more vertical than vertical?
 * Horizontal Line		-	~0		Can't be any less vertical
 * 45 Degree PosSlope	-	~.5 	Could go either way
 * 45 Degree NegSlope	-	~.5		Ditto
 * 
 * @author Paul Corey
 *
 */
public class VerticalConstraintTest extends SlothTest {

	public VerticalConstraint vc;

	// public static double DEFAULT_THRESHOLD =
	// VerticalConstraint.DEFAULT_THRESHOLD;

	@Before
	public void setup() {
		vc = null;
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testVerticalConstraint() {
		vc = new VerticalConstraint();
		assertNotNull(vc);
		assertEquals(vc.getThreshold(),
				VerticalConstraint.DEFAULT_THRESHOLD, Math.pow(10, -10));
		assertEquals(vc.getDescription(), VerticalConstraint.DESCRIPTION);
		assertEquals(vc.getName(), VerticalConstraint.NAME);
	}

	@Test
	public void testVerticalConstraintDouble() {
		double t = 15;
		vc = new VerticalConstraint(t);
		assertNotNull(vc);
		assertEquals(vc.getThreshold(), t, Math.pow(10, -10));
		assertEquals(vc.getDescription(), VerticalConstraint.DESCRIPTION);
		assertEquals(vc.getName(), VerticalConstraint.NAME);

		t = 45;
		vc = new VerticalConstraint(t);
		assertNotNull(vc);
		assertEquals(vc.getThreshold(), t, Math.pow(10, -10));
		assertEquals(vc.getDescription(), VerticalConstraint.DESCRIPTION);
		assertEquals(vc.getName(), VerticalConstraint.NAME);
	}

	@Test
	public void testSolveList() {
		vc = new VerticalConstraint();

		// ArrayList
		// Empty list
		List<IConstrainable> shapes = new ArrayList<IConstrainable>();
		assertEquals(0, vc.solve(shapes), Math.pow(10, -10));

		// Non-line shape
		shapes.add(new ConstrainableShape(getRect()));
		assertEquals(0, vc.solve(shapes), Math.pow(10, -10));

		// Single Line
		// Ideal Positive Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 1, 0)));
		assertTrue(45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertTrue(vc.solve(shapes) > 0);
		assertEquals(.5, vc.solve(shapes), .01);

		// Ideal Negative Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 1)));
		assertTrue(45+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertEquals(.5, vc.solve(shapes), .01);

		// Horizontal
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 0)));
		assertTrue(45-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertTrue(vc.solve(shapes) > 0);
		assertEquals(0, vc.solve(shapes), .01);

		// Vertical
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 0, 0)));
		assertTrue(45+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertTrue(vc.solve(shapes) > 0);
		assertEquals(1, vc.solve(shapes), .01);

		// Multiple Lines
		double c;
		//Line first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		c = vc.solve(shapes);
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertEquals(c, vc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableShape(getRect()));
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertEquals(c, vc.solve(shapes), Math.pow(10, -10));

		//Other first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect()));
		c = vc.solve(shapes);
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertEquals(c, vc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertEquals(c, vc.solve(shapes), Math.pow(10, -10));

		// LinkedList
		// Empty list
		shapes = new LinkedList<IConstrainable>();
		assertEquals(0, vc.solve(shapes), Math.pow(10, -10));

		// Non-line shape
		shapes.add(new ConstrainableShape(getRect()));
		assertEquals(0, vc.solve(shapes), Math.pow(10, -10));

		// Single Line
		// Ideal Positive Slope
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 1, 0)));
		assertTrue(45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertTrue(vc.solve(shapes) > 0);
		assertEquals(.5, vc.solve(shapes), .01);

		// Ideal Negative Slope
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 1)));
		assertTrue(45+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertEquals(.5, vc.solve(shapes), .01);

		// Horizontal
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 0)));
		assertTrue(45-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertTrue(vc.solve(shapes) > 0);
		assertEquals(0, vc.solve(shapes), .01);

		// Vertical
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 0, 0)));
		assertTrue(45+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertTrue(vc.solve(shapes) > 0);
		assertEquals(1, vc.solve(shapes), .01);

		// Multiple Lines
		//Line first
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		c = vc.solve(shapes);
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertEquals(c, vc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableShape(getRect()));
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertEquals(c, vc.solve(shapes), Math.pow(10, -10));

		//Other first
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect()));
		c = vc.solve(shapes);
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertEquals(c, vc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		assertTrue(vc.solve(shapes) >= 0);
		assertTrue(vc.solve(shapes) <= 1);
		assertEquals(c, vc.solve(shapes), Math.pow(10, -10));
}

	@Test
	public void testSolveIConstrainable() {
		ConstrainableLine shape;
		vc = new VerticalConstraint();
		
		
		// Single Line
		// Ideal Positive Slope
		shape=new ConstrainableLine(getLine(0, 1, 1, 0));
		assertTrue(45==shape.getAngleInDegrees()||225==shape.getAngleInDegrees());
		assertTrue(vc.solve(shape) >= 0);
		assertTrue(vc.solve(shape) <= 1);
		assertTrue(vc.solve(shape) > 0);
		assertEquals(.5, vc.solve(shape), .01);

		// Ideal Negative Slope
		shape=(new ConstrainableLine(getLine(0, 0, 1, 1)));
		assertTrue(45+90==shape.getAngleInDegrees()||225+90==shape.getAngleInDegrees());
		assertTrue(vc.solve(shape) >= 0);
		assertTrue(vc.solve(shape) <= 1);
		assertEquals(.5, vc.solve(shape), .01);

		// Horizontal
		shape=(new ConstrainableLine(getLine(0, 0, 1, 0)));
		assertTrue(45-45==shape.getAngleInDegrees()||225-45==shape.getAngleInDegrees());
		assertTrue(vc.solve(shape) >= 0);
		assertTrue(vc.solve(shape) <= 1);
		assertTrue(vc.solve(shape) > 0);
		assertEquals(0, vc.solve(shape), .01);

		// Vertical
		shape=(new ConstrainableLine(getLine(0, 1, 0, 0)));
		assertTrue(45+45==shape.getAngleInDegrees()||225+45==shape.getAngleInDegrees());
		assertTrue(vc.solve(shape) >= 0);
		assertTrue(vc.solve(shape) <= 1);
		assertTrue(vc.solve(shape) > 0);
		assertEquals(1, vc.solve(shape), .01);
	}

	@Test
	public void testSolve(){
		vc = new VerticalConstraint();

		// ArrayList
		// Empty list
		List<IConstrainable> shapes = new ArrayList<IConstrainable>();
		assertEquals(0, vc.solve(shapes), Math.pow(10, -10));

		// Non-line shape
		shapes.add(new ConstrainableShape(getRect()));
		vc.setParameters(shapes);
		assertEquals(0, vc.solve(), Math.pow(10, -10));

		// Single Line
		// Ideal Positive Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 1, 0)));
		vc.setParameters(shapes);
		assertTrue(45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(vc.solve() >= 0);
		assertTrue(vc.solve() <= 1);
		assertTrue(vc.solve() > 0);
		assertEquals(.5, vc.solve(), .01);

		// Ideal Negative Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 1)));
		vc.setParameters(shapes);
		assertTrue(45+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(vc.solve() >= 0);
		assertTrue(vc.solve() <= 1);
		assertEquals(.5, vc.solve(), .01);

		// Horizontal
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 0)));
		vc.setParameters(shapes);
		assertTrue(45-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(vc.solve() >= 0);
		assertTrue(vc.solve() <= 1);
		assertTrue(vc.solve() > 0);
		assertEquals(0, vc.solve(), .01);

		// Vertical
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 0, 0)));
		vc.setParameters(shapes);
		assertTrue(45+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(vc.solve() >= 0);
		assertTrue(vc.solve() <= 1);
		assertTrue(vc.solve() > 0);
		assertEquals(1, vc.solve(), .01);

		// Multiple Lines
		double c;
		//Line first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		vc.setParameters(shapes);
		c = vc.solve();
		assertTrue(vc.solve() >= 0);
		assertTrue(vc.solve() <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		vc.setParameters(shapes);
		assertTrue(vc.solve() >= 0);
		assertTrue(vc.solve() <= 1);
		assertEquals(c, vc.solve(), Math.pow(10, -10));
		shapes.add(new ConstrainableShape(getRect()));
		vc.setParameters(shapes);
		assertTrue(vc.solve() >= 0);
		assertTrue(vc.solve() <= 1);
		assertEquals(c, vc.solve(), Math.pow(10, -10));

		//Other first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect()));
		vc.setParameters(shapes);
		c = vc.solve();
		assertTrue(vc.solve() >= 0);
		assertTrue(vc.solve() <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		vc.setParameters(shapes);
		assertTrue(vc.solve() >= 0);
		assertTrue(vc.solve() <= 1);
		assertEquals(c, vc.solve(), Math.pow(10, -10));
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		vc.setParameters(shapes);
		assertTrue(vc.solve() >= 0);
		assertTrue(vc.solve() <= 1);
		assertEquals(c, vc.solve(), Math.pow(10, -10));
	}
}
