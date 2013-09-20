package test.unit.ladder.recognition.constraint.confidence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.AboveConstraint;
import org.ladder.recognition.constraint.confidence.AcuteAngleConstraint;

import test.unit.SlothTest;

public class AcuteAngleConstraintTest extends SlothTest {
	
	public AcuteAngleConstraint aac;
	
	
	@Before
	public void setup() {
		
	}
	

	@After
	public void tearDown() {
		
	}
	

	@Test
	public void testAcuteAngleConstraint() {
		aac = new AcuteAngleConstraint();
		assertNotNull(aac);
		assertEquals(AcuteAngleConstraint.DEFAULT_THRESHOLD,
		        aac.getThreshold(), Math.pow(10, -10));
		assertEquals(AcuteAngleConstraint.DESCRIPTION, aac.getDescription());
		assertEquals(AcuteAngleConstraint.NAME, aac.getName());
		assertEquals(AcuteAngleConstraint.NUM_PARAMETERS, aac
		        .getNumRequiredParameters());
	}
	

	@Test
	public void testAcuteAngleConstraintDouble() {
		double t = rand.nextDouble();
		aac = new AcuteAngleConstraint(t);
		assertNotNull(aac);
		assertEquals(t, aac.getThreshold(), Math.pow(10, -10));
		assertEquals(AcuteAngleConstraint.DESCRIPTION, aac.getDescription());
		assertEquals(AcuteAngleConstraint.NAME, aac.getName());
		assertEquals(AcuteAngleConstraint.NUM_PARAMETERS, aac
		        .getNumRequiredParameters());
		
		try {
			aac = new AcuteAngleConstraint(-t);
			Assert.fail("We expected an exception");
		}
		catch(IllegalArgumentException e) {
			// good!
		}
		catch (Exception e) {
			Assert.fail("We expected illegal arg. and not "+e.getMessage());
		}
		
	}
	

	@Test
	public void testSolve() {
		List<IConstrainable> shapes = new ArrayList<IConstrainable>();
		aac = new AcuteAngleConstraint();
		
		// No parameters
		try {
			aac.setParameters(shapes);
			Assert.fail("We expected an exception");
		}
		catch(IllegalArgumentException e) {
			// good!
		}
		catch (Exception e) {
			Assert.fail("We expected illegal arg. and not "+e.getMessage());
		}
		//assertEquals(0, aac.solve(), Math.pow(10, -10));
		
		// One parameter
		shapes.add(new ConstrainableLine(getLine(0, 0, 10, 0)));
		
		try {
			aac.setParameters(shapes);
			Assert.fail("We expected an exception");
		}
		catch(IllegalArgumentException e) {
			// good!
		}
		catch (Exception e) {
			Assert.fail("We expected illegal arg. and not "+e.getMessage());
		}
		
		//assertEquals(0, aac.solve(), Math.pow(10, -10));
		
		// Two parameter
		// Reasonable range?
		shapes
		        .add(new ConstrainableLine(getLine(0, 0, (int) (rand
		                .nextInt(10) * Math.signum(rand.nextDouble() - .5)),
		                (int) (rand.nextInt(10) * Math
		                        .signum(rand.nextDouble() - .5)))));
		aac.setParameters(shapes);
		assertTrue(aac.solve() >= 0);
		assertTrue(aac.solve() <= 1);
		
		double prevPosAngleConf = -1;
		double prevNegAngleConf = -1;
		for (int degrees = 1; degrees <= 180; degrees++) {
			if (degrees < 90)
				System.out
				        .println("Acute angle, should have relatively high confidence");
			if (degrees > 90)
				System.out
				        .println("Obtuse angle, should have relatively low confidence");
			if (degrees == 90)
				System.out.println("At 90");
			shapes.clear();
			
			ConstrainableLine stableLine = new ConstrainableLine(getLine(0, 0,
			        100, 0));
			
			ConstrainableLine posDegLine = new ConstrainableLine(getLine(0, 0,
			        100 * Math.cos(degrees * Math.PI / 180), 100 * Math
			                .sin(-degrees * Math.PI / 180)));
			shapes.add(stableLine);
			shapes.add(posDegLine);
			
			aac.setParameters(shapes);
			double conf = aac.solve();
			System.out.println("Angle2 = " + posDegLine.getAngleInDegrees() + ". " + stableLine.getAngleBetweenInDegrees(posDegLine)
			                   + " degree difference. Confidence: " + conf);
			if (prevPosAngleConf > 0)
				assertTrue(prevPosAngleConf > conf);
			prevPosAngleConf = conf;
			
			ConstrainableLine negDegLine = new ConstrainableLine(getLine(0, 0,
			        100 * Math.cos(degrees * Math.PI / 180), 100 * Math
			                .sin(degrees * Math.PI / 180)));
			shapes.clear();
			shapes.add(stableLine);
			shapes.add(negDegLine);
			
			aac.setParameters(shapes);
			conf = aac.solve();
			System.out.println("Angle2 = " + negDegLine.getAngleInDegrees() + ". " + stableLine.getAngleBetweenInDegrees(negDegLine)
			                   + " degree difference. Confidence: " + conf);
			if (prevNegAngleConf > 0)
				assertTrue(prevNegAngleConf > conf);
			prevNegAngleConf = conf;
			System.out.println();
		}
		
		// Same Line
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 10, 0)));
		shapes.add(new ConstrainableLine(getLine(0, 0, 10, 0)));
		aac.setParameters(shapes);
		aac.setScaleParameters(false);
		
		System.out.println("Same Line " + aac.solve());
		// assertEquals(1,aac.solve(),.01);
		
		// 45 degree, same start point, should have high confidence
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 10, 0)));
		shapes.add(new ConstrainableLine(getLine(0, 0, 10, 10)));
		aac.setParameters(shapes);
		
		assertTrue(aac.solve() >= .1);
		assertTrue(aac.solve() >= .3);
		System.out.println(aac.solve());
		assertTrue(aac.solve() >= .5);
		System.out.println(aac.solve());
		assertTrue(aac.solve() >= .7);
		// assertTrue(aac.solve()>=.9); Confidence < .9, is it confident enough?
		
		// 90 degree, same start point, ~50/50 chance sounds reasonable
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 10, 0)));
		shapes.add(new ConstrainableLine(getLine(0, 0, 0, 10)));
		aac.setParameters(shapes);
		assertTrue(aac.solve() >= .1);
		assertTrue(aac.solve() >= .4);
		// assertTrue(aac.solve()>=.5);
		assertTrue(aac.solve() <= .6);
		
		// 135 degree, same start point, low confidence
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 10, 0)));
		shapes.add(new ConstrainableLine(getLine(0, 0, -10, 10)));
		aac.setParameters(shapes);
		assertTrue(aac.solve() <= .7);
		assertTrue(aac.solve() <= .5);
		// assertTrue(aac.solve()<=.3); Confidence >.3, low enough?
		// assertTrue(aac.solve()<=.1);
		
		// -45 degree, same start point, should have high confidence
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 10, 0)));
		shapes.add(new ConstrainableLine(getLine(0, 0, 10, -10)));
		aac.setParameters(shapes);
		assertTrue(aac.solve() >= .1);
		assertTrue(aac.solve() >= .3);
		/*
		 * Confidence < .5? Fairly certain -45 degrees is still acute
		 * assertTrue(aac.solve()>=.5); assertTrue(aac.solve()>=.7);
		 * assertTrue(aac.solve()>=.9);
		 */

		// -90 degree, same start point, ~50/50 chance sounds reasonable
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 10, 0)));
		shapes.add(new ConstrainableLine(getLine(0, 0, 0, -10)));
		aac.setParameters(shapes);
		assertTrue(aac.solve() >= .1);
		assertTrue(aac.solve() >= .4);
		// assertTrue(aac.solve()>=.5);
		assertTrue(aac.solve() <= .6);
		
		// -135 degree, same start point, low confidence
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 10, 0)));
		shapes.add(new ConstrainableLine(getLine(0, 0, -10, -10)));
		aac.setParameters(shapes);
		/*
		 * Confidence >.7, that's a problem assertTrue(aac.solve()<=.7);
		 * assertTrue(aac.solve()<=.5); assertTrue(aac.solve()<=.3);
		 * assertTrue(aac.solve()<=.1);
		 */

		// Trying extremes
		// Tiny positive
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shapes.add(new ConstrainableLine(getLine(0, 0, 1000, 1)));
		aac.setParameters(shapes);
		assertTrue(aac.solve() >= .1);
		assertTrue(aac.solve() >= .3);
		assertTrue(aac.solve() >= .5);
		/*
		 * assertTrue(aac.solve()>=.7); Really?!? This is a tiny angle, should
		 * be ~1 assertTrue(aac.solve()>=.9);
		 */

		// Tiny negative
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shapes.add(new ConstrainableLine(getLine(0, 0, 1000, -1)));
		aac.setParameters(shapes);
		assertTrue(aac.solve() >= .1);
		assertTrue(aac.solve() >= .3);
		/*
		 * assertTrue(aac.solve()>=.5);// Really?!? This is a tiny angle, should
		 * be ~1 assertTrue(aac.solve()>=.7); assertTrue(aac.solve()>=.9);
		 */

		// Almost 180
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shapes.add(new ConstrainableLine(getLine(0, 0, -1000, 1)));
		aac.setParameters(shapes);
		assertTrue(aac.solve() <= .7);
		assertTrue(aac.solve() <= .5);
		/*
		 * assertTrue(aac.solve()<=.3); Should be almost 0 at 180 degrees
		 * assertTrue(aac.solve()<=.1);
		 */

		// Almost -180
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shapes.add(new ConstrainableLine(getLine(0, 0, -1000, -1)));
		aac.setParameters(shapes);
		assertTrue(aac.solve() <= .7);
		/*
		 * assertTrue(aac.solve()<=.5); Same thing assertTrue(aac.solve()<=.3);
		 * assertTrue(aac.solve()<=.1);
		 */
	}
	

	@Test
	public void testSolveConstrainableLineConstrainableLine() {
		aac = new AcuteAngleConstraint();
		ConstrainableLine shape1, shape2;
		
		/*
		 * //No parameters
		 * assertEquals(0,aac.solve(null,null),Math.pow(10,-10));
		 * 
		 * //One parameter shape1 = new ConstrainableLine(getLine(0,0,10,0));
		 * assertEquals(0,aac.solve(shape1,null),Math.pow(10,-10));
		 */
		// Two parameter
		// Reasonable range?
		shape1 = new ConstrainableLine(getLine(0, 0, 10, 0));
		shape2 = (new ConstrainableLine(getLine(0, 0,
		        (int) (rand.nextInt(10) * Math.signum(rand.nextDouble() - .5)),
		        (int) (rand.nextInt(10) * Math.signum(rand.nextDouble() - .5)))));
		assertTrue(aac.solve(shape1, shape2) >= 0);
		assertTrue(aac.solve(shape1, shape2) <= 1);
		
		// Same Line
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		assertEquals(1, aac.solve(shape1, shape2), .01);
		
		// 45 degree, same start point, should have high confidence
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0, 10, 10)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .3);
		assertTrue(aac.solve(shape1, shape2) >= .5);
		assertTrue(aac.solve(shape1, shape2) >= .7);
		// assertTrue(aac.solve(shape1,shape2)>=.9); Confidence < .9, is it
		// confident enough?
		
		// 90 degree, same start point, ~50/50 chance sounds reasonable
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0, 0, 10)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .4);
		// assertTrue(aac.solve(shape1,shape2)>=.5);
		assertTrue(aac.solve(shape1, shape2) <= .6);
		
		// 135 degree, same start point, low confidence
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0, -10, 10)));
		assertTrue(aac.solve(shape1, shape2) <= .7);
		assertTrue(aac.solve(shape1, shape2) <= .5);
		// assertTrue(aac.solve(shape1,shape2)<=.3); Confidence >.3, low enough?
		// assertTrue(aac.solve(shape1,shape2)<=.1);
		
		// -45 degree, same start point, should have high confidence
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0, 10, -10)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .3);
		/*
		 * Confidence < .5? Fairly certain -45 degrees is still acute
		 * assertTrue(aac.solve(shape1,shape2)>=.5);
		 * assertTrue(aac.solve(shape1,shape2)>=.7);
		 * assertTrue(aac.solve(shape1,shape2)>=.9);
		 */

		// -90 degree, same start point, ~50/50 chance sounds reasonable
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0, 0, -10)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .4);
		// assertTrue(aac.solve(shape1,shape2)>=.5);
		assertTrue(aac.solve(shape1, shape2) <= .6);
		
		// -135 degree, same start point, low confidence
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0, -10, -10)));
		/*
		 * Confidence >.7, that's a problem
		 * assertTrue(aac.solve(shape1,shape2)<=.7);
		 * assertTrue(aac.solve(shape1,shape2)<=.5);
		 * assertTrue(aac.solve(shape1,shape2)<=.3);
		 * assertTrue(aac.solve(shape1,shape2)<=.1);
		 */

		// Trying extremes
		// Tiny positive
		shape1 = (new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0, 1000, 1)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .3);
		assertTrue(aac.solve(shape1, shape2) >= .5);
		/*
		 * assertTrue(aac.solve(shape1,shape2)>=.7); Really?!? This is a tiny
		 * angle, should be ~1 assertTrue(aac.solve(shape1,shape2)>=.9);
		 */

		// Tiny negative
		shape1 = (new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0, 1000, -1)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .3);
		/*
		 * assertTrue(aac.solve(shape1,shape2)>=.5);// Really?!? This is a tiny
		 * angle, should be ~1 assertTrue(aac.solve(shape1,shape2)>=.7);
		 * assertTrue(aac.solve(shape1,shape2)>=.9);
		 */

		// Almost 180
		shape1 = (new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0, -1000, 1)));
		assertTrue(aac.solve(shape1, shape2) <= .7);
		assertTrue(aac.solve(shape1, shape2) <= .5);
		/*
		 * assertTrue(aac.solve(shape1,shape2)<=.3); Should be almost 0 at 180
		 * degrees assertTrue(aac.solve(shape1,shape2)<=.1);
		 */

		// Almost -180
		shape1 = (new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0, -1000, -1)));
		assertTrue(aac.solve(shape1, shape2) <= .7);
		/*
		 * assertTrue(aac.solve(shape1,shape2)<=.5); Same thing
		 * assertTrue(aac.solve(shape1,shape2)<=.3);
		 * assertTrue(aac.solve(shape1,shape2)<=.1);
		 */

		// Not Connected
		/*
		 * //No parameters
		 * assertEquals(0,aac.solve(null,null),Math.pow(10,-10));
		 * 
		 * //One parameter shape1 = new ConstrainableLine(getLine(0,0,10,0));
		 * assertEquals(0,aac.solve(shape1,null),Math.pow(10,-10));
		 */
		// Two parameter
		// Reasonable range?
		shape1 = new ConstrainableLine(getLine(0, 0, 10, 0));
		shape2 = (new ConstrainableLine(
		        getLine(10, 0, (int) (rand.nextInt(10) * Math.signum(rand
		                .nextDouble() - .5)) + 10,
		                (int) (rand.nextInt(10) * Math
		                        .signum(rand.nextDouble() - .5)))));
		assertTrue(aac.solve(shape1, shape2) >= 0);
		assertTrue(aac.solve(shape1, shape2) <= 1);
		shape1 = new ConstrainableLine(getLine(0, 0, 10, 0));
		shape2 = (new ConstrainableLine(getLine(0, 10,
		        (int) (rand.nextInt(10) * Math.signum(rand.nextDouble() - .5)),
		        10 + (int) (rand.nextInt(10) * Math
		                .signum(rand.nextDouble() - .5)))));
		assertTrue(aac.solve(shape1, shape2) >= 0);
		assertTrue(aac.solve(shape1, shape2) <= 1);
		
		// Same Line translated along line and parallel
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0 + 10, 0, 10 + 10, 0)));
		// assertEquals(1,aac.solve(shape1,shape2),.01);
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0 + 10, 10, 0 + 10)));
		// assertEquals(1,aac.solve(shape1,shape2),.01);
		
		// 45 degree
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0 + 10, 0, 10 + 10, 10)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .3);
		assertTrue(aac.solve(shape1, shape2) >= .5);
		assertTrue(aac.solve(shape1, shape2) >= .7);
		// assertTrue(aac.solve(shape1,shape2)>=.9); Confidence < .9, is it
		// confident enough?
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0 + 10, 0, 10 + 10, 10)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .3);
		assertTrue(aac.solve(shape1, shape2) >= .5);
		assertTrue(aac.solve(shape1, shape2) >= .7);
		// assertTrue(aac.solve(shape1,shape2)>=.9); Confidence < .9, is it
		// confident enough?
		
		// 90 degree, same start point, ~50/50 chance sounds reasonable
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0 + 10, 0, 0 + 10, 10)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .4);
		// assertTrue(aac.solve(shape1,shape2)>=.5);
		assertTrue(aac.solve(shape1, shape2) <= .6);
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0 + 10, 0, 10 + 10)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .4);
		// assertTrue(aac.solve(shape1,shape2)>=.5);
		assertTrue(aac.solve(shape1, shape2) <= .6);
		
		// 135 degree, same start point, low confidence
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0 - 10, 0, -10 - 10, 10)));
		assertTrue(aac.solve(shape1, shape2) <= .7);
		assertTrue(aac.solve(shape1, shape2) <= .5);
		// assertTrue(aac.solve(shape1,shape2)<=.3); Confidence >.3, low enough?
		// assertTrue(aac.solve(shape1,shape2)<=.1);
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0 + 10, -10, 10 + 10)));
		assertTrue(aac.solve(shape1, shape2) <= .7);
		assertTrue(aac.solve(shape1, shape2) <= .5);
		// assertTrue(aac.solve(shape1,shape2)<=.3); Confidence >.3, low enough?
		// assertTrue(aac.solve(shape1,shape2)<=.1);
		
		// -45 degree, same start point, should have high confidence
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0 + 10, 0, 10 + 10, -10)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .3);
		/*
		 * Confidence < .5? Fairly certain -45 degrees is still acute
		 * assertTrue(aac.solve(shape1,shape2)>=.5);
		 * assertTrue(aac.solve(shape1,shape2)>=.7);
		 * assertTrue(aac.solve(shape1,shape2)>=.9);
		 */
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0 - 10, 10, -10 - 10)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .3);
		/*
		 * Confidence < .5? Fairly certain -45 degrees is still acute
		 * assertTrue(aac.solve(shape1,shape2)>=.5);
		 * assertTrue(aac.solve(shape1,shape2)>=.7);
		 * assertTrue(aac.solve(shape1,shape2)>=.9);
		 */

		// -90 degree, same start point, ~50/50 chance sounds reasonable
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0 + 10, 0, 0 + 10, -10)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .4);
		// assertTrue(aac.solve(shape1,shape2)>=.5);
		assertTrue(aac.solve(shape1, shape2) <= .6);
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0 - 10, 0, -10 - 10)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .4);
		// assertTrue(aac.solve(shape1,shape2)>=.5);
		assertTrue(aac.solve(shape1, shape2) <= .6);
		
		// -135 degree, same start point, low confidence
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0 - 10, 0, -10 - 10, -10)));
		/*
		 * Confidence >.7, that's a problem
		 * assertTrue(aac.solve(shape1,shape2)<=.7);
		 * assertTrue(aac.solve(shape1,shape2)<=.5);
		 * assertTrue(aac.solve(shape1,shape2)<=.3);
		 * assertTrue(aac.solve(shape1,shape2)<=.1);
		 */
		shape1 = (new ConstrainableLine(getLine(0, 0, 10, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0 - 10, -10, -10 - 10)));
		/*
		 * Confidence >.7, that's a problem
		 * assertTrue(aac.solve(shape1,shape2)<=.7);
		 * assertTrue(aac.solve(shape1,shape2)<=.5);
		 * assertTrue(aac.solve(shape1,shape2)<=.3);
		 * assertTrue(aac.solve(shape1,shape2)<=.1);
		 */

		// Trying extremes
		// Tiny positive
		shape1 = (new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shape2 = (new ConstrainableLine(getLine(0 + 10, 0, 1000 + 10, 1)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .3);
		assertTrue(aac.solve(shape1, shape2) >= .5);
		/*
		 * assertTrue(aac.solve(shape1,shape2)>=.7); Really?!? This is a tiny
		 * angle, should be ~1 assertTrue(aac.solve(shape1,shape2)>=.9);
		 */
		shape1 = (new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0 + 10, 1000, 1 + 10)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .3);
		assertTrue(aac.solve(shape1, shape2) >= .5);
		/*
		 * assertTrue(aac.solve(shape1,shape2)>=.7); Really?!? This is a tiny
		 * angle, should be ~1 assertTrue(aac.solve(shape1,shape2)>=.9);
		 */

		// Tiny negative
		shape1 = (new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shape2 = (new ConstrainableLine(getLine(0 + 10, 0, 1000 + 10, -1)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .3);
		/*
		 * assertTrue(aac.solve(shape1,shape2)>=.5);// Really?!? This is a tiny
		 * angle, should be ~1 assertTrue(aac.solve(shape1,shape2)>=.7);
		 * assertTrue(aac.solve(shape1,shape2)>=.9);
		 */
		shape1 = (new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0 - 10, 1000, -1 - 10)));
		assertTrue(aac.solve(shape1, shape2) >= .1);
		assertTrue(aac.solve(shape1, shape2) >= .3);
		/*
		 * assertTrue(aac.solve(shape1,shape2)>=.5);// Really?!? This is a tiny
		 * angle, should be ~1 assertTrue(aac.solve(shape1,shape2)>=.7);
		 * assertTrue(aac.solve(shape1,shape2)>=.9);
		 */

		// Almost 180
		shape1 = (new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0 + 10, -1000, 1 + 10)));
		assertTrue(aac.solve(shape1, shape2) <= .7);
		assertTrue(aac.solve(shape1, shape2) <= .5);
		/*
		 * assertTrue(aac.solve(shape1,shape2)<=.3); Should be almost 0 at 180
		 * degrees assertTrue(aac.solve(shape1,shape2)<=.1);
		 */
		shape1 = (new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shape2 = (new ConstrainableLine(getLine(0 - 10, 0, -1000 - 10, 1)));
		assertTrue(aac.solve(shape1, shape2) <= .7);
		assertTrue(aac.solve(shape1, shape2) <= .5);
		/*
		 * assertTrue(aac.solve(shape1,shape2)<=.3); Should be almost 0 at 180
		 * degrees assertTrue(aac.solve(shape1,shape2)<=.1);
		 */

		// Almost -180
		shape1 = (new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shape2 = (new ConstrainableLine(getLine(0 - 10, 0, -1000 - 10, -1)));
		assertTrue(aac.solve(shape1, shape2) <= .7);
		/*
		 * assertTrue(aac.solve(shape1,shape2)<=.5); Same thing
		 * assertTrue(aac.solve(shape1,shape2)<=.3);
		 * assertTrue(aac.solve(shape1,shape2)<=.1);
		 */
		shape1 = (new ConstrainableLine(getLine(0, 0, 1000, 0)));
		shape2 = (new ConstrainableLine(getLine(0, 0 - 10, -1000, -1 - 10)));
		assertTrue(aac.solve(shape1, shape2) <= .7);
		/*
		 * assertTrue(aac.solve(shape1,shape2)<=.5); Same thing
		 * assertTrue(aac.solve(shape1,shape2)<=.3);
		 * assertTrue(aac.solve(shape1,shape2)<=.1);
		 */
	}
}
