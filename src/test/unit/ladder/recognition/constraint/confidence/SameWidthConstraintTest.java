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
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.SameWidthConstraint;

import test.unit.SlothTest;

public class SameWidthConstraintTest extends SlothTest {

	SameWidthConstraint swc;
	List<IConstrainable> shapes;
	double prevConf;
	int distMax=100;
	
	@Before
	public void setup(){
		swc = new SameWidthConstraint();
		swc.setScaleParameters(false);
		shapes = new ArrayList<IConstrainable>();
	}
	
	@After
	public void tearDown(){
		swc=null;
	}
	
	@Test
	public void testSameWidthConstraint(){
		swc=new SameWidthConstraint();
		assertNotNull(swc);
		/*
		 * Stopped caring if they have the right values, they all just call super
		 */
	}

	@Test
	public void testSameWidthConstraintDouble(){
		double t=rand.nextDouble();
		swc=new SameWidthConstraint(t);
		assertNotNull(swc);
		assertEquals(t,swc.getThreshold(),Math.pow(10, -10));
		
		t = -t;
		try {
			swc=new SameWidthConstraint(t);
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
			swc.setParameters(shapes);
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
		shapes.add(new ConstrainableShape(getRect()));
		try {
			swc.setParameters(shapes);
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
		shapes.add(new ConstrainableShape(getRect()));
		shapes.add(new ConstrainableShape(getRect()));
		swc.setParameters(shapes);
		assertTrue(0<=swc.solve());
		assertTrue(1>=swc.solve());
	}
	
	@Test
	public void testSameWidthOf(){
		double prevConf=-1;
		for(int width=0;width<200;width++){
			shapes.clear();
			shapes.add(new ConstrainableShape(getRect(0,0,100,100)));
			shapes.add(new ConstrainableShape(getRect(0,0,width,width)));
			swc.setParameters(shapes);
			double conf=swc.solve();
			if(prevConf>0)
				if(width<=100)
					assertTrue(prevConf<conf);
				else
					assertTrue(prevConf>conf);
			prevConf=conf;
			System.out.println("Width difference: "+Math.abs(width-100)+" Confidence: "+conf);
		}
	}
	
	@Test
	public void testConfidenceThreshold() {
		assert(false);
	}
	
	@Test
	public void testScaling() {
		assert(false);
	}
}
