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
import org.ladder.recognition.constraint.confidence.PositiveSlopeConstraint;

import test.unit.SlothTest;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class PositiveSlopeConstraintTest extends SlothTest {

	public PositiveSlopeConstraint psc;

	// public static double DEFAULT_THRESHOLD =
	// PositiveSlopeConstraint.DEFAULT_THRESHOLD;

	@Before
	public void setup() {
		psc = null;
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testPositiveSlopeConstraint() {
		psc = new PositiveSlopeConstraint();
		assertNotNull(psc);
		assertEquals(psc.getThreshold(),
				PositiveSlopeConstraint.DEFAULT_THRESHOLD, Math.pow(10, -10));
		assertEquals(psc.getDescription(), PositiveSlopeConstraint.DESCRIPTION);
		assertEquals(psc.getName(), PositiveSlopeConstraint.NAME);
	}

	@Test
	public void testPositiveSlopeConstraintDouble() {
		double t = 15;
		psc = new PositiveSlopeConstraint(t);
		assertNotNull(psc);
		assertEquals(psc.getThreshold(), t, Math.pow(10, -10));
		assertEquals(psc.getDescription(), PositiveSlopeConstraint.DESCRIPTION);
		assertEquals(psc.getName(), PositiveSlopeConstraint.NAME);

		t = 45;
		psc = new PositiveSlopeConstraint(t);
		assertNotNull(psc);
		assertEquals(psc.getThreshold(), t, Math.pow(10, -10));
		assertEquals(psc.getDescription(), PositiveSlopeConstraint.DESCRIPTION);
		assertEquals(psc.getName(), PositiveSlopeConstraint.NAME);
	}

	@Test
	public void testSolveList() {
		psc = new PositiveSlopeConstraint(45);
		System.out.println(PositiveSlopeConstraint.DEFAULT_THRESHOLD);
		// ArrayList
		// Empty list
		List<IConstrainable> shapes = new ArrayList<IConstrainable>();
		assertEquals(0, psc.solve(shapes), Math.pow(10, -10));

		// Non-line shape
		shapes.add(new ConstrainableShape(getRect()));
		assertEquals(0, psc.solve(shapes), Math.pow(10, -10));

		// Single Line
		// Ideal Positive Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 1, 0)));
		System.out.println(((ConstrainableLine)shapes.get(0)).getAngleInDegrees());
		assertTrue(45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertTrue(psc.solve(shapes) > 0);
		assertEquals(1, psc.solve(shapes), .01);

		// Ideal Negative Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 1)));
		System.out.println(((ConstrainableLine)shapes.get(0)).getAngleInDegrees());
		assertEquals(45+90,((ConstrainableLine) shapes.get(0)).getAngleInDegrees(),.001);
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertEquals(0, psc.solve(shapes), .05);

		// Horizontal
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 0)));
		assertTrue(45-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertTrue(psc.solve(shapes) > 0);
		assertEquals(.5, psc.solve(shapes), .01);

		// Vertical
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 0, 0)));
		assertTrue(45+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertTrue(psc.solve(shapes) > 0);
		assertEquals(.5, psc.solve(shapes), .01);

		// Multiple Lines
		double c;
		//Line first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		c = psc.solve(shapes);
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertEquals(c, psc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableShape(getRect()));
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertEquals(c, psc.solve(shapes), Math.pow(10, -10));

		//Other first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect()));
		c = psc.solve(shapes);
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertEquals(c, psc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertEquals(c, psc.solve(shapes), Math.pow(10, -10));

		// LinkedList
		// Empty list
		shapes = new LinkedList<IConstrainable>();
		assertEquals(0, psc.solve(shapes), Math.pow(10, -10));

		// Non-line shape
		shapes.add(new ConstrainableShape(getRect()));
		assertEquals(0, psc.solve(shapes), Math.pow(10, -10));

		// Single Line
		// Ideal Positive Slope
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 1, 0)));
		assertTrue(45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertTrue(psc.solve(shapes) > 0);
		assertEquals(1, psc.solve(shapes), .01);

		// Ideal Negative Slope
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 1)));
		assertTrue(45+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertEquals(0, psc.solve(shapes), .01);

		// Horizontal
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 0)));
		assertTrue(45-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertTrue(psc.solve(shapes) > 0);
		assertEquals(.5, psc.solve(shapes), .01);

		// Vertical
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 0, 0)));
		assertTrue(45+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertTrue(psc.solve(shapes) > 0);
		assertEquals(.5, psc.solve(shapes), .01);

		// Multiple Lines
		//Line first
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		c = psc.solve(shapes);
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertEquals(c, psc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableShape(getRect()));
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertEquals(c, psc.solve(shapes), Math.pow(10, -10));

		//Other first
		shapes = new LinkedList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect()));
		c = psc.solve(shapes);
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertEquals(c, psc.solve(shapes), Math.pow(10, -10));
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		assertTrue(psc.solve(shapes) >= 0);
		assertTrue(psc.solve(shapes) <= 1);
		assertEquals(c, psc.solve(shapes), Math.pow(10, -10));
}

	@Test
	public void testSolveIConstrainable() {
		ConstrainableLine shape;
		psc = new PositiveSlopeConstraint();
		
		
		// Single Line
		// Ideal Positive Slope
		shape=new ConstrainableLine(getLine(0, 1, 1, 0));
		assertTrue(45==shape.getAngleInDegrees()||225==shape.getAngleInDegrees());
		assertTrue(psc.solve(shape) >= 0);
		assertTrue(psc.solve(shape) <= 1);
		assertTrue(psc.solve(shape) > 0);
		assertEquals(0, psc.solve(shape), .01);

		// Ideal Negative Slope
		shape=(new ConstrainableLine(getLine(0, 0, 1, 1)));
		assertTrue(45+90==shape.getAngleInDegrees()||225+90==shape.getAngleInDegrees());
		assertTrue(psc.solve(shape) >= 0);
		assertTrue(psc.solve(shape) <= 1);
		assertEquals(1, psc.solve(shape), .01);

		// Horizontal
		shape=(new ConstrainableLine(getLine(0, 0, 1, 0)));
		assertTrue(45-45==shape.getAngleInDegrees()||225-45==shape.getAngleInDegrees());
		assertTrue(psc.solve(shape) >= 0);
		assertTrue(psc.solve(shape) <= 1);
		assertTrue(psc.solve(shape) > 0);
		assertEquals(.5, psc.solve(shape), .01);

		// Vertical
		shape=(new ConstrainableLine(getLine(0, 1, 0, 0)));
		assertTrue(45+45==shape.getAngleInDegrees()||225+45==shape.getAngleInDegrees());
		assertTrue(psc.solve(shape) >= 0);
		assertTrue(psc.solve(shape) <= 1);
		assertTrue(psc.solve(shape) > 0);
		assertEquals(.5, psc.solve(shape), .01);
	}

	@Test
	public void testSolve(){
		psc = new PositiveSlopeConstraint();

		// ArrayList
		// Empty list
		List<IConstrainable> shapes = new ArrayList<IConstrainable>();
		assertEquals(0, psc.solve(shapes), Math.pow(10, -10));

		// Non-line shape
		shapes.add(new ConstrainableShape(getRect()));
		psc.setParameters(shapes);
		assertEquals(0, psc.solve(), Math.pow(10, -10));

		// Single Line
		// Ideal Positive Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 1, 0)));
		psc.setParameters(shapes);
		assertTrue(45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(psc.solve() >= 0);
		assertTrue(psc.solve() <= 1);
		assertTrue(psc.solve() > 0);
		assertEquals(0, psc.solve(), .01);

		// Ideal Negative Slope
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 1)));
		psc.setParameters(shapes);
		assertTrue(45+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+90==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(psc.solve() >= 0);
		assertTrue(psc.solve() <= 1);
		assertEquals(1, psc.solve(), .01);

		// Horizontal
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1, 0)));
		psc.setParameters(shapes);
		assertTrue(45-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225-45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(psc.solve() >= 0);
		assertTrue(psc.solve() <= 1);
		assertTrue(psc.solve() > 0);
		assertEquals(.5, psc.solve(), .01);

		// Vertical
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 1, 0, 0)));
		psc.setParameters(shapes);
		assertTrue(45+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees()||225+45==((ConstrainableLine) shapes.get(0)).getAngleInDegrees());
		assertTrue(psc.solve() >= 0);
		assertTrue(psc.solve() <= 1);
		assertTrue(psc.solve() > 0);
		assertEquals(.5, psc.solve(), .01);

		// Multiple Lines
		double c;
		//Line first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		psc.setParameters(shapes);
		c = psc.solve();
		assertTrue(psc.solve() >= 0);
		assertTrue(psc.solve() <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		psc.setParameters(shapes);
		assertTrue(psc.solve() >= 0);
		assertTrue(psc.solve() <= 1);
		assertEquals(c, psc.solve(), Math.pow(10, -10));
		shapes.add(new ConstrainableShape(getRect()));
		psc.setParameters(shapes);
		assertTrue(psc.solve() >= 0);
		assertTrue(psc.solve() <= 1);
		assertEquals(c, psc.solve(), Math.pow(10, -10));

		//Other first
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect()));
		psc.setParameters(shapes);
		c = psc.solve();
		assertTrue(psc.solve() >= 0);
		assertTrue(psc.solve() <= 1);
		shapes.add(new ConstrainableLine(getLine(1, 1, 2, 1)));
		psc.setParameters(shapes);
		assertTrue(psc.solve() >= 0);
		assertTrue(psc.solve() <= 1);
		assertEquals(c, psc.solve(), Math.pow(10, -10));
		shapes.add(new ConstrainableLine(getLine(3, 4, 2, 1)));
		psc.setParameters(shapes);
		assertTrue(psc.solve() >= 0);
		assertTrue(psc.solve() <= 1);
		assertEquals(c, psc.solve(), Math.pow(10, -10));
	}
}
