package test.unit.ladder.recognition.constraint.confidence;

import java.util.List;
import java.util.ArrayList;

import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.constrainable.ConstrainablePoint;
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.BelowConstraint;

import test.unit.SlothTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BelowConstraintTest extends SlothTest {
	BelowConstraint bc;
	List<IConstrainable> shapes;
	double prevConf;
	int distMax = 100;

	@Before
	public void setup() {
		bc = new BelowConstraint();
		bc.setScaleParameters(false);
		shapes = new ArrayList<IConstrainable>();
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testBelowConstraint() {
		bc = new BelowConstraint();
		assertNotNull(bc);
		assertEquals(bc.getThreshold(), BelowConstraint.DEFAULT_THRESHOLD, Math
				.pow(10, -10));
		assertEquals(bc.getDescription(), BelowConstraint.DESCRIPTION);
		assertEquals(bc.getName(), BelowConstraint.NAME);
		assertEquals(bc.getNumRequiredParameters(),
				BelowConstraint.NUM_PARAMETERS);
	}

	@Test
	public void testBelowConstraintDouble() {
		double t = rand.nextDouble();
		bc = new BelowConstraint(t);
		assertNotNull(bc);
		assertEquals(bc.getThreshold(), t, Math.pow(10, -10));
		assertEquals(bc.getDescription(), BelowConstraint.DESCRIPTION);
		assertEquals(bc.getName(), BelowConstraint.NAME);

		t = -t;
		try {
			bc = new BelowConstraint(t);
			Assert.fail("We expected an exception");
		} catch (IllegalArgumentException e) {
			// good!
		} catch (Exception e) {
			Assert.fail("We expected illegal arg. and not " + e.getMessage());
		}
	}

	@Test
	public void testNoParameters() {
		// No parameters
		try {
			bc.setParameters(shapes);
			Assert.fail("We expected an exception");
		} catch (IllegalArgumentException e) {
			// good!
		} catch (Exception e) {
			Assert.fail("We expected illegal arg. and not " + e.getMessage());
		}
	}

	@Test
	public void testOneParameter() {
		shapes.add(new ConstrainableLine(getLine(0, 0, 10, 0)));
		try {
			bc.setParameters(shapes);
			Assert.fail("We expected an exception");
		} catch (IllegalArgumentException e) {
			// good!
		} catch (Exception e) {
			Assert.fail("We expected illegal arg. and not " + e.getMessage());
		}
	}

	@Test
	public void testTwoParametersReasonable() {
		shapes
				.add(new ConstrainableLine(getLine(
						(int) (rand.nextInt(10) * Math
								.signum(rand.nextDouble() - .5)), (int) (rand
								.nextInt(10) * Math
								.signum(rand.nextDouble() - .5)), (int) (rand
								.nextInt(10) * Math
								.signum(rand.nextDouble() - .5)), (int) (rand
								.nextInt(10) * Math
								.signum(rand.nextDouble() - .5)))));
		shapes
				.add(new ConstrainableLine(getLine(
						(int) (rand.nextInt(10) * Math
								.signum(rand.nextDouble() - .5)), (int) (rand
								.nextInt(10) * Math
								.signum(rand.nextDouble() - .5)), (int) (rand
								.nextInt(10) * Math
								.signum(rand.nextDouble() - .5)), (int) (rand
								.nextInt(10) * Math
								.signum(rand.nextDouble() - .5)))));
		System.out.println(shapes.size());
		bc.setParameters(shapes);
		assertTrue(bc.solve() >= 0);
		assertTrue(bc.solve() <= 1);
	}

	@Test
	public void testDirectlyAbove() {
		prevConf = -1;
		for (int i = 0; i < distMax; i++) {
			shapes.clear();
			shapes.add(new ConstrainablePoint(0, -i, null));
			shapes.add(new ConstrainablePoint(0, 0, null));
			bc.setParameters(shapes);
			System.out.println("Distance above: " + (i));
			double conf = bc.solve();
			System.out.println("Confidence: " + conf);
			if (prevConf >= 0)
				assertTrue(prevConf >= conf);
			prevConf = conf;
		}
	}

	@Test
	public void testDirectlyBelow() {
		prevConf = -1;
		for (int i = 0; i < distMax; i++) {
			shapes.clear();
			shapes.add(new ConstrainablePoint(0, distMax - i, null));
			shapes.add(new ConstrainablePoint(0, 0, null));
			bc.setParameters(shapes);
			System.out.println("Distance below: " + (i - distMax));
			double conf = bc.solve();
			System.out.println("Confidence: " + conf);
			if (prevConf >= 0)
				assertTrue(prevConf > conf);
			prevConf = conf;
		}
	}

	@Test
	public void testAboveOffsetX() {
		distMax = 100;
		for (int i = 0; i < distMax * 2 + 1; i++) {
			shapes.clear();
			shapes.add(new ConstrainablePoint(distMax - i, -50, null));
			shapes.add(new ConstrainablePoint(0, 0, null));
			bc.setParameters(shapes);
			System.out.println("Distance to side: " + (i - distMax));
			System.out.println("Confidence: " + bc.solve());
		}
	}

	@Test
	public void testBelowOffsetX() {
		distMax = 100;
		for (int i = 0; i < distMax * 2 + 1; i++) {
			shapes.clear();
			shapes.add(new ConstrainablePoint(distMax - i, 50, null));
			shapes.add(new ConstrainablePoint(0, 0, null));
			bc.setParameters(shapes);
		}
	}

	@Test
	public void testShapeHeight() {
		// Confidence with shape height
		// Above
		int maxHeight = 100;
		for (int i = 0; i < maxHeight; i++) {
			shapes.clear();
			shapes.add(new ConstrainableShape(getRect(0, 150, 100, i + 150)));
			shapes.add(new ConstrainableShape(getRect(0, 0, 100, 100)));
			bc.setParameters(shapes);
			System.out.println("Height: " + (i));
			System.out.println("Confidence: " + bc.solve());
		}
	}

	@Test
	public void testConfidenceThreshold() {
		assert (false);
	}

	@Test
	public void testScaling() {
		assert (false);
	}

}
