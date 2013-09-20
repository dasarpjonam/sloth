package test.unit.ladder.recognition.constraint.confidence;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.ladder.recognition.constraint.constrainable.ConstrainablePoint;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.CloserConstraint;


import test.unit.SlothTest;

public class CloserConstraintTest extends SlothTest {

	CloserConstraint cc;
	List<IConstrainable> shapes;
	double prevConf=0;
	int distMax=100;
	
	@Before
	public void setup(){
		cc = new CloserConstraint();
		cc.setScaleParameters(false);
		shapes = new ArrayList<IConstrainable>();
	}
	
	@After
	public void tearDown(){
		cc=null;
	}
	
	@Test
	public void testCloserConstraint() {
		assertNotNull(cc);
		assertEquals(CloserConstraint.DEFAULT_THRESHOLD,cc.getThreshold(),Math.pow(10, -10));
		assertEquals(CloserConstraint.DESCRIPTION,cc.getDescription());
		assertEquals(CloserConstraint.NAME,cc.getName());
		assertEquals(CloserConstraint.NUM_PARAMETERS,cc.getNumRequiredParameters());
	}
	
	@Test
	public void testCloserConstraintDouble() {
		double t = rand.nextDouble();

		cc = new CloserConstraint(t);
		assertNotNull(cc);
		assertEquals(t,cc.getThreshold(),Math.pow(10, -10));
		assertEquals(CloserConstraint.DESCRIPTION,cc.getDescription());
		assertEquals(CloserConstraint.NAME,cc.getName());
		assertEquals(CloserConstraint.NUM_PARAMETERS,cc.getNumRequiredParameters());
		
		t = -t;
		try {
			cc=new CloserConstraint(t);
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
			cc.setParameters(shapes);
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
		shapes.add(new ConstrainablePoint(0,0, null));
		try {
			cc.setParameters(shapes);
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
	public void testTwoParameters() {
		shapes.add(new ConstrainablePoint(0,0, null));
		shapes.add(new ConstrainablePoint(0,0, null));
		try {
			cc.setParameters(shapes);
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
	public void testThreeParametersReasonable() {
		//Two parameter
		//Reasonable range?
		shapes.add(new ConstrainablePoint(0,0, null));
		shapes.add(new ConstrainablePoint(0,0, null));
		shapes.add(new ConstrainablePoint(0,0, null));
		cc.setParameters(shapes);
		assertTrue(cc.solve()>=0);
		assertTrue(cc.solve()<=1);
	}
	
	
	@Test
	public void testCloser() {
		prevConf=-1;
		for(int i=0;i<3*100;i++){
			shapes.clear();
			shapes.add(new ConstrainablePoint(0,0, null)); // POINT 1
			shapes.add(new ConstrainablePoint(0,i-distMax, null)); // CLOSER TO 2
			shapes.add(new ConstrainablePoint(0,distMax, null)); // THAN TO 3
			cc.setParameters(shapes);
			double conf = cc.solve();
			if(prevConf!=-1)
				if(i<=0){
					assertTrue(prevConf<conf);
				}
				else if(i>0&&i<=distMax)
					assertTrue(prevConf>conf);
				else if(i>distMax)
					assertTrue(prevConf<conf);
			if((i-distMax)<distMax/2)
				System.out.print("At "+(i-distMax)+" Closer to 0. Confidence: ");
			else
				System.out.print("At "+(i-distMax)+" Closer to "+distMax+". Confidence: ");
			System.out.println(conf);
		}
		
	}
	@Test
	public void testSolve() {
		ArrayList<IConstrainable> shapes = new ArrayList<IConstrainable>();
		// requires three shapes
		assertEquals(0,cc.solve(),Math.pow(10, -10));
		
		cc.setParameters(shapes);
		assertEquals(0,cc.solve(),Math.pow(10, -10));
		
		shapes.add(new ConstrainablePoint(randPoint(), null));
		cc.setParameters(shapes);
		assertEquals(0,cc.solve(),Math.pow(10, -10));
		
		
		shapes.add(new ConstrainablePoint(randPoint(), null));
		cc.setParameters(shapes);
		assertEquals(0,cc.solve(),Math.pow(10, -10));
		
		shapes.add(new ConstrainablePoint(randPoint(), null));
		cc.setParameters(shapes);
		assertTrue(0<=cc.solve());
		assertTrue(1>=cc.solve());
		
		int distMax=100;
		double prevConf=-1;
		for(int i=0;i<3*100;i++){
			shapes.clear();
			shapes.add(new ConstrainablePoint(0,0, null)); // POINT 1
			shapes.add(new ConstrainablePoint(0,i-distMax, null)); // CLOSER TO 2
			shapes.add(new ConstrainablePoint(0,distMax, null)); // THAN TO 3
			cc.setParameters(shapes);
			double conf = cc.solve();
			if(prevConf!=-1)
				if(i<=0){
					assertTrue(prevConf<conf);
				}
				else if(i>0&&i<=distMax)
					assertTrue(prevConf>conf);
				else if(i>distMax)
					assertTrue(prevConf<conf);
			if((i-distMax)<distMax/2)
				System.out.print("At "+(i-distMax)+" Closer to 0. Confidence: ");
			else
				System.out.print("At "+(i-distMax)+" Closer to "+distMax+". Confidence: ");
			System.out.println(conf);
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
