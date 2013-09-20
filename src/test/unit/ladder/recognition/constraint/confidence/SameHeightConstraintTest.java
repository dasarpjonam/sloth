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
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.SameHeightConstraint;

import test.unit.SlothTest;

public class SameHeightConstraintTest extends SlothTest {
	SameHeightConstraint shc;
	List<IConstrainable> shapes;
	double prevConf;
	int distMax=100;
	
	@Before
	public void setup(){
		shc = new SameHeightConstraint();
		shc.setScaleParameters(false);
		shapes = new ArrayList<IConstrainable>();
	}
	
	@After
	public void tearDown(){
		shc=null;
	}
	
	@Test
	public void testSameHeightConstraint(){
		shc=new SameHeightConstraint();
		assertNotNull(shc);
		/*
		 * Stopped caring if they have the right values, they all just call super
		 */
	}

	@Test
	public void testSameHeightConstraintDouble(){
		double t=rand.nextDouble();
		shc=new SameHeightConstraint(t);
		assertNotNull(shc);
		assertEquals(t,shc.getThreshold(),Math.pow(10, -10));
		
		t = -t;
		try {
			shc=new SameHeightConstraint(t);
			Assert.fail("We expected an exception");
		}
		catch(IllegalArgumentException e) {
			//good!
		}
		catch(Exception e) {
			Assert.fail("We expected illegal arg. and not "+e.getMessage());
		}
	}
	
	@Test
	public void testNoParameters() {
		try {
			shc.setParameters(shapes);
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
			shc.setParameters(shapes);
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
		shc.setParameters(shapes);
		assertTrue(shc.solve()>=0);
		assertTrue(shc.solve()<=1);
	}
	
	
	@Test
	public void testSameHeight(){
		
		double prevConf=-1;
		for(int height=0;height<200;height++){
			shapes.clear();
			shapes.add(new ConstrainableShape(getRect(0,0,100,100)));
			shapes.add(new ConstrainableShape(getRect(0,0,height,height)));
			shc.setParameters(shapes);
			double conf=shc.solve();
			if(prevConf>0)
				if(height<=100)
					assertTrue(prevConf<conf);
				else
					assertTrue(prevConf>conf);
			prevConf=conf;
			System.out.println("Height difference: "+Math.abs(height-100)+" Confidence: "+conf);
		}
	}
}
