package test.unit.ladder.recognition.constraint.confidence;

import java.util.List;
import java.util.ArrayList;


import junit.framework.Assert;



import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.constrainable.ConstrainablePoint;
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;

import org.ladder.recognition.constraint.IConstrainable;

import org.ladder.recognition.constraint.confidence.AboveConstraint;



import test.unit.SlothTest;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AboveConstraintTest extends SlothTest {
	AboveConstraint ac; 	
	List<IConstrainable> shapes;
	double prevConf;
	int distMax=100;
	
	@Before
	public void setup(){
		ac= new AboveConstraint();
		ac.setScaleParameters(false);
		shapes = new ArrayList<IConstrainable>();
	}

	@After
	public void tearDown(){
		
	}
	
	@Test
	public void testAboveConstraint() {
		ac = new AboveConstraint();
		assertNotNull(ac);
		assertEquals(ac.getThreshold(),
				AboveConstraint.DEFAULT_THRESHOLD, Math.pow(10, -10));
		assertEquals(ac.getDescription(), AboveConstraint.DESCRIPTION);
		assertEquals(ac.getName(), AboveConstraint.NAME);
		assertEquals(ac.getNumRequiredParameters(),AboveConstraint.NUM_PARAMS);
	}
	


	@Test
	public void testAboveConstraintDouble() {
		double t = rand.nextDouble();
		ac = new AboveConstraint(t);
		assertNotNull(ac);
		assertEquals(ac.getThreshold(), t, Math.pow(10, -10));
		assertEquals(ac.getDescription(), AboveConstraint.DESCRIPTION);
		assertEquals(ac.getName(), AboveConstraint.NAME);

		t = -t;
		try {
			ac = new AboveConstraint(t);
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
	public void testNoParameters() {
		//No parameters
		try {
			ac.setParameters(shapes);
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
	public void testOneParameter() {
		//One parameter
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		try {
			ac.setParameters(shapes);
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
	public void testTwoParametersReasonable() {
		//Two parameter
		//Reasonable range?
		shapes.add(new ConstrainableLine(getLine((int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)))));
		shapes.add(new ConstrainableLine(getLine((int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)))));
		ac.setParameters(shapes);
		assertTrue(ac.solve()>=0);
		assertTrue(ac.solve()<=1);
	}
	
	@Test
	public void testDirectlyAbove() {
		//2 Points
		//Directly above
		
		prevConf=-1;
		for(int i=0;i<distMax;i++){
			shapes.clear();
			shapes.add(new ConstrainablePoint(0,-i, null));
			shapes.add(new ConstrainablePoint(0,0, null));
			ac.setParameters(shapes);
			System.out.println("Distance above: "+(i));
			ac.setScaleParameters(false);
			double conf=ac.solve();
			System.out.println("Confidence: "+ conf);
			if(prevConf>=0)
				assertTrue(prevConf<conf);
			prevConf=conf;
		}
	}
	
	@Test
	public void testDirectlyBelow() {
		//Directly below
		distMax=100;
		prevConf=-1;
		for(int i=0;i<distMax;i++){
			shapes.clear();
			shapes.add(new ConstrainablePoint(0,distMax-i, null));
			shapes.add(new ConstrainablePoint(0,0, null));
			ac.setParameters(shapes);
			System.out.println("Distance below: "+(i-distMax));
			ac.setScaleParameters(false);
			double conf=ac.solve();
			System.out.println("Confidence: "+ conf);
			if(prevConf>=0)
				assertTrue(prevConf<=conf);
			prevConf=conf;
		}
	}
	
	@Test
	public void testAboveOffsetX() {
		//Above and to the sides
		distMax=100;
		for(int i=0;i<distMax*2+1;i++){
			shapes.clear();
			shapes.add(new ConstrainablePoint(distMax-i,-50, null));
			shapes.add(new ConstrainablePoint(0,0, null));
			ac.setParameters(shapes);
			System.out.println("Distance to side: "+(i-distMax));
			ac.setScaleParameters(false);
			System.out.println("Confidence: "+ac.solve());
		}
	}
	
	@Test
	public void testBelowOffsetX() {
		//Below and to the sides
		distMax=100;
		for(int i=0;i<distMax*2+1;i++){
			shapes.clear();
			shapes.add(new ConstrainablePoint(distMax-i,50, null));
			shapes.add(new ConstrainablePoint(0,0, null));
			ac.setParameters(shapes);
		}
	}
	
	@Test
	public void testShapeHeight() {
		//Confidence with shape height
		//Above
		int maxHeight=100;
		for(int i=0;i<maxHeight;i++){
			shapes.clear();
			shapes.add(new ConstrainableShape(getRect(0,-50,100,i-50)));
			shapes.add(new ConstrainableShape(getRect(0,0,100,100)));
			ac.setParameters(shapes);
			System.out.println("Height: "+(i));
			ac.setScaleParameters(false);
			System.out.println("Confidence: "+ac.solve());
		}
	}
	
	@Test
	public void testConfidenceThreshold() {
		assertTrue(false);
	}
	
	@Test
	public void testScaling() {
		assertTrue(false);
	}
	
}
