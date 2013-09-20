/**
 * 
 */
package test.unit.ladder.recognition.constraint.confidence;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.PerpendicularConstraint;

import test.unit.SlothTest;

/**
 * @author Priscus
 *
 */
public class PerpendicularConstraintTest extends SlothTest{

	PerpendicularConstraint pc;
	List<IConstrainable> shapes;
	double prevConf;
	int distMax=100;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		pc=new PerpendicularConstraint();
		pc.setScaleParameters(false);
		shapes = new ArrayList<IConstrainable>();
	}


	@After
	public void tearDown() {
		pc = null;
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.PerpendicularConstraint#PerpendicularConstraint()}.
	 */
	@Test
	public void testPerpendicularConstraint() {
		pc = new PerpendicularConstraint();
		assertNotNull(pc);
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.PerpendicularConstraint#PerpendicularConstraint(double)}.
	 */
	@Test
	public void testPerpendicularConstraintDouble() {
		double t = rand.nextDouble();
		pc = new PerpendicularConstraint(t);
		assertNotNull(pc);
		assertEquals(t,pc.getThreshold(),0);
	}

	@Test
	public void testNoParameters() {
		try {
			pc.setParameters(shapes);
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
			pc.setParameters(shapes);
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
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		pc.setParameters(shapes);
		assertTrue(pc.solve()>=0);
		assertTrue(pc.solve()<=1);
	}
	
	@Test
	public void testPerpendicularOne() {
		
		prevConf=-1;
		for(double degrees=0;degrees<360;degrees++){
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,100,0)));
			shapes.add(new ConstrainableLine(getLine(0,0,100*Math.cos(degrees*Math.PI/180),100*Math.sin(degrees*Math.PI/180))));
			pc.setParameters(shapes);
			double conf = pc.solve();
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,100*Math.cos(degrees*Math.PI/180),100*Math.sin(degrees*Math.PI/180))));
			shapes.add(new ConstrainableLine(getLine(0,0,100,0)));
			pc.setParameters(shapes);
			assertEquals(conf,pc.solve(),0);
			System.out.println((360-degrees)+" angle difference.  Confidence: "+conf);
			if(prevConf!=-1)
				if(degrees<=90)
					assertTrue(prevConf<conf);
				else if(degrees<=180)
					assertTrue(prevConf>conf);
				else if(degrees<=270)
					assertTrue(prevConf<conf);
				else
					assertTrue(prevConf>conf);
		}
	}
	
	@Test
	public void testPerpendicularTwo() {
		prevConf=-1;
		for(double degrees=0;degrees<360;degrees++){
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,100,0)));
			shapes.add(new ConstrainableLine(getLine(150+0,150+0,150+100*Math.cos(degrees*Math.PI/180),150+100*Math.sin(degrees*Math.PI/180))));
			pc.setParameters(shapes);
			double conf = pc.solve();
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(150+0,150+0,150+100*Math.cos(degrees*Math.PI/180),150+100*Math.sin(degrees*Math.PI/180))));
			shapes.add(new ConstrainableLine(getLine(0,0,100,0)));
			pc.setParameters(shapes);
			assertEquals(conf,pc.solve(),0);
			System.out.println((360-degrees)+" angle difference.  Confidence: "+conf);
			if(prevConf!=-1)
				if(degrees<=90)
					assertTrue(prevConf<conf);
				else if(degrees<=180)
					assertTrue(prevConf>conf);
				else if(degrees<=270)
					assertTrue(prevConf<conf);
				else
					assertTrue(prevConf>conf);
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
