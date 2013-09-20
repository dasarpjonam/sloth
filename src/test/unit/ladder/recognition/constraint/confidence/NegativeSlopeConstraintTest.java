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
import org.ladder.recognition.constraint.confidence.NegativeSlopeConstraint;

import test.unit.SlothTest;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class NegativeSlopeConstraintTest extends SlothTest {

	public NegativeSlopeConstraint nsc;

	// public static double DEFAULT_THRESHOLD =
	// NegativeSlopeConstraint.DEFAULT_THRESHOLD;

	@Before
	public void setup() {
		nsc = null;
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testNegativeSlopeConstraint() {
		nsc = new NegativeSlopeConstraint();
		assertNotNull(nsc);
		assertEquals(nsc.getThreshold(),NegativeSlopeConstraint.DEFAULT_THRESHOLD, Math.pow(10, -10));
		assertEquals(nsc.getDescription(), NegativeSlopeConstraint.DESCRIPTION);
		assertEquals(nsc.getName(), NegativeSlopeConstraint.NAME);
	}

	@Test
	public void testNegativeSlopeConstraintDouble() {
		double t = 15;
		nsc = new NegativeSlopeConstraint(t);
		assertNotNull(nsc);
		assertEquals(nsc.getThreshold(), t, Math.pow(10, -10));
		assertEquals(nsc.getDescription(), NegativeSlopeConstraint.DESCRIPTION);
		assertEquals(nsc.getName(), NegativeSlopeConstraint.NAME);

		t = 45;
		nsc = new NegativeSlopeConstraint(t);
		assertNotNull(nsc);
		assertEquals(nsc.getThreshold(), t, Math.pow(10, -10));
		assertEquals(nsc.getDescription(), NegativeSlopeConstraint.DESCRIPTION);
		assertEquals(nsc.getName(), NegativeSlopeConstraint.NAME);
	}

	@Test
	public void testSolveList() {
		nsc = new NegativeSlopeConstraint();

		// ArrayList
		// Empty list
		List<IConstrainable> shapes = new ArrayList<IConstrainable>();
		assertEquals(0, nsc.solve(shapes), Math.pow(10, -10));

		// Non-line shape
		shapes.add(new ConstrainableShape(getRect()));
		assertEquals(0, nsc.solve(shapes), Math.pow(10, -10));

		// Single Line
		// Ideal Positive Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 1, 0)));
		assertTrue(45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertTrue(nsc.solve(shapes) > 0);
		assertEquals(0, nsc.solve(shapes), .01);

		// Ideal Negative Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 1)));
		assertTrue(45+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertEquals(1, nsc.solve(shapes), .01);

		// Horizontal
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 0)));
		assertTrue(45-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertTrue(nsc.solve(shapes) > 0);
		assertEquals(.5, nsc.solve(shapes), .01);

		// Vertical
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 0, 0)));
		assertTrue(45+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertTrue(nsc.solve(shapes) > 0);
		assertEquals(.5, nsc.solve(shapes), .01);

		// Multiple Lines
		double c;
		//Line first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		c = nsc.solve(shapes);
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertEquals(c, nsc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableShape(getRect()));
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertEquals(c, nsc.solve(shapes), Math.pow(10, -10));

		//Other first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect()));
		c = nsc.solve(shapes);
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertEquals(c, nsc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertEquals(c, nsc.solve(shapes), Math.pow(10, -10));

		// LinkedList
		// Empty list
		shapes = new LinkedList<IConstrainable>();
		assertEquals(0, nsc.solve(shapes), Math.pow(10, -10));

		// Non-line shape
		shapes.add(new ConstrainableShape(getRect()));
		assertEquals(0, nsc.solve(shapes), Math.pow(10, -10));

		// Single Line
		// Ideal Positive Slope
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 1, 0)));
		assertTrue(45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertTrue(nsc.solve(shapes) > 0);
		assertEquals(0, nsc.solve(shapes), .01);

		// Ideal Negative Slope
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 1)));
		assertTrue(45+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertEquals(1, nsc.solve(shapes), .01);

		// Horizontal
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 0)));
		assertTrue(45-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertTrue(nsc.solve(shapes) > 0);
		assertEquals(.5, nsc.solve(shapes), .01);

		// Vertical
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 0, 0)));
		assertTrue(45+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertTrue(nsc.solve(shapes) > 0);
		assertEquals(.5, nsc.solve(shapes), .01);

		// Multiple Lines
		//Line first
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		c = nsc.solve(shapes);
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertEquals(c, nsc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableShape(getRect()));
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertEquals(c, nsc.solve(shapes), Math.pow(10, -10));

		//Other first
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect()));
		c = nsc.solve(shapes);
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertEquals(c, nsc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		assertTrue(nsc.solve(shapes) >= 0);
		assertTrue(nsc.solve(shapes) <= 1);
		assertEquals(c, nsc.solve(shapes), Math.pow(10, -10));
}

	@Test
	public void testSolveIConstrainable() {
		ConstrainableLine shape;
		nsc = new NegativeSlopeConstraint();
		
		
		// Single Line
		// Ideal Positive Slope
		shape=new ConstrainableLine(getLine(0, 1, 1, 0));
		assertTrue(45==shape.getAngleInDegrees()||225==shape.getAngleInDegrees());
		assertTrue(nsc.solve(shape) >= 0);
		assertTrue(nsc.solve(shape) <= 1);
		assertTrue(nsc.solve(shape) > 0);
		assertEquals(0, nsc.solve(shape), .01);

		// Ideal Negative Slope
		shape=(new ConstrainableLine(getLine(0, 0, 1, 1)));
		assertTrue(45+90==shape.getAngleInDegrees()||225+90==shape.getAngleInDegrees());
		assertTrue(nsc.solve(shape) >= 0);
		assertTrue(nsc.solve(shape) <= 1);
		assertEquals(1, nsc.solve(shape), .01);

		// Horizontal
		shape=(new ConstrainableLine(getLine(0, 0, 1, 0)));
		assertTrue(45-45== shape.getAngleInDegrees()||225-45==shape.getAngleInDegrees());
		assertTrue(nsc.solve(shape) >= 0);
		assertTrue(nsc.solve(shape) <= 1);
		assertTrue(nsc.solve(shape) > 0);
		assertEquals(.5, nsc.solve(shape), .01);

		// Vertical
		shape=(new ConstrainableLine(getLine(0, 1, 0, 0)));
		assertTrue(45-45==shape.getAngleInDegrees()||225-45==shape.getAngleInDegrees());
		assertTrue(nsc.solve(shape) >= 0);
		assertTrue(nsc.solve(shape) <= 1);
		assertTrue(nsc.solve(shape) > 0);
		assertEquals(.5, nsc.solve(shape), .01);
	}

	@Test
	public void testSolve(){
		nsc = new NegativeSlopeConstraint();

		// ArrayList
		// Empty list
		List<IConstrainable> shapes = new ArrayList<IConstrainable>();
		assertEquals(0, nsc.solve(shapes), Math.pow(10, -10));

		// Non-line shape
		shapes.add(new ConstrainableShape(getRect()));
		nsc.setParameters(shapes);
		assertEquals(0, nsc.solve(), Math.pow(10, -10));

		// Single Line
		// Ideal Positive Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 1, 0)));
		nsc.setParameters(shapes);
		assertTrue(45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(nsc.solve() >= 0);
		assertTrue(nsc.solve() <= 1);
		assertTrue(nsc.solve() > 0);
		assertEquals(0, nsc.solve(), .01);

		// Ideal Negative Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 1)));
		nsc.setParameters(shapes);
		assertTrue(45+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(nsc.solve() >= 0);
		assertTrue(nsc.solve() <= 1);
		assertEquals(1, nsc.solve(), .01);

		// Horizontal
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 0)));
		nsc.setParameters(shapes);
		assertTrue(45-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(nsc.solve() >= 0);
		assertTrue(nsc.solve() <= 1);
		assertTrue(nsc.solve() > 0);
		assertEquals(.5, nsc.solve(), .01);

		// Vertical
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 0, 0)));
		nsc.setParameters(shapes);
		assertTrue(45+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(nsc.solve() >= 0);
		assertTrue(nsc.solve() <= 1);
		assertTrue(nsc.solve() > 0);
		assertEquals(.5, nsc.solve(), .01);

		// Multiple Lines
		double c;
		//Line first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		nsc.setParameters(shapes);
		c = nsc.solve();
		assertTrue(nsc.solve() >= 0);
		assertTrue(nsc.solve() <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		nsc.setParameters(shapes);
		assertTrue(nsc.solve() >= 0);
		assertTrue(nsc.solve() <= 1);
		assertEquals(c, nsc.solve(), Math.pow(10, -10));
		shapes.add(new ConstrainableShape(getRect()));
		nsc.setParameters(shapes);
		assertTrue(nsc.solve() >= 0);
		assertTrue(nsc.solve() <= 1);
		assertEquals(c, nsc.solve(), Math.pow(10, -10));

		//Other first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect()));
		nsc.setParameters(shapes);
		c = nsc.solve();
		assertTrue(nsc.solve() >= 0);
		assertTrue(nsc.solve() <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		nsc.setParameters(shapes);
		assertTrue(nsc.solve() >= 0);
		assertTrue(nsc.solve() <= 1);
		assertEquals(c, nsc.solve(), Math.pow(10, -10));
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		nsc.setParameters(shapes);
		assertTrue(nsc.solve() >= 0);
		assertTrue(nsc.solve() <= 1);
		assertEquals(c, nsc.solve(), Math.pow(10, -10));
	}
}
