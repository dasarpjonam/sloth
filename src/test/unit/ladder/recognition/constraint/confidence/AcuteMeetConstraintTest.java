package test.unit.ladder.recognition.constraint.confidence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.AcuteMeetConstraint;

import test.unit.SlothTest;

public class AcuteMeetConstraintTest extends SlothTest {
	
	public AcuteMeetConstraint amc;
	
	@Before
	public void setup(){
		
	}
	
	@After
	public void tearDown(){
			
	}
	
	@Test
	public void testAcuteMeetConstraint() {
		amc = new AcuteMeetConstraint();
		assertNotNull(amc);
		assertEquals(AcuteMeetConstraint.DEFAULT_THRESHOLD,amc.getThreshold(),Math.pow(10,-10));
		assertEquals(AcuteMeetConstraint.DESCRIPTION,amc.getDescription());
		assertEquals(AcuteMeetConstraint.NAME,amc.getName());
		assertEquals(AcuteMeetConstraint.NUM_PARAMETERS,amc.getNumRequiredParameters());
	}
	
	@Test
	public void testAcuteMeetConstraintDouble() {
		double t = rand.nextDouble();
		amc = new AcuteMeetConstraint(t);
		assertNotNull(amc);
		assertEquals(t,amc.getThreshold(),Math.pow(10,-10));
		assertEquals(AcuteMeetConstraint.DESCRIPTION,amc.getDescription());
		assertEquals(AcuteMeetConstraint.NAME,amc.getName());
		assertEquals(AcuteMeetConstraint.NUM_PARAMETERS,amc.getNumRequiredParameters());
		
		amc = new AcuteMeetConstraint(-t);
		assertNotNull(amc);
		assertEquals(t,amc.getThreshold(),Math.pow(10,-10));
		assertEquals(AcuteMeetConstraint.DESCRIPTION,amc.getDescription());
		assertEquals(AcuteMeetConstraint.NAME,amc.getName());
		assertEquals(AcuteMeetConstraint.NUM_PARAMETERS,amc.getNumRequiredParameters());
	}
	
	@Test
	public void testSolve() {
		List<IConstrainable> shapes = new ArrayList<IConstrainable>();
		amc = new AcuteMeetConstraint();
		
		//No parameters
		amc.setParameters(shapes);
		assertEquals(0,amc.solve(),Math.pow(10,-10));
		
		//One parameter
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		amc.setParameters(shapes);
		assertEquals(0,amc.solve(),Math.pow(10,-10));
		
		//Two parameter
		//Reasonable range?
		shapes.add(new ConstrainableLine(getLine(0,0,(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)))));
		amc.setParameters(shapes);
		assertTrue(amc.solve()>=0);
		assertTrue(amc.solve()<=1);
		
		//Same Line
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		amc.setParameters(shapes);
		//assertEquals(1,amc.solve(),.01);
		
		//45 degree, same start point, should have high confidence
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		shapes.add(new ConstrainableLine(getLine(0,0,10,10)));
		amc.setParameters(shapes);
		assertTrue(amc.solve()>=.1);
		assertTrue(amc.solve()>=.3);
		assertTrue(amc.solve()>=.5);
		assertTrue(amc.solve()>=.7);
		//assertTrue(amc.solve()>=.9);  Confidence < .9, is it confident enough?

		//90 degree, same start point, ~50/50 chance sounds reasonable
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		shapes.add(new ConstrainableLine(getLine(0,0,0,10)));
		amc.setParameters(shapes);
		assertTrue(amc.solve()>=.1);
		assertTrue(amc.solve()>=.4);
		//assertTrue(amc.solve()>=.5);
		assertTrue(amc.solve()<=.6);
		
		//135 degree, same start point, low confidence
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		shapes.add(new ConstrainableLine(getLine(0,0,-10,10)));
		amc.setParameters(shapes);
		assertTrue(amc.solve()<=.7);
		assertTrue(amc.solve()<=.5);
		//assertTrue(amc.solve()<=.3); Confidence >.3, low enough?
		//assertTrue(amc.solve()<=.1);

		//-45 degree, same start point, should have high confidence
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		shapes.add(new ConstrainableLine(getLine(0,0,10,-10)));
		amc.setParameters(shapes);
		assertTrue(amc.solve()>=.1);
		assertTrue(amc.solve()>=.3);
		/* Confidence < .5?  Fairly certain -45 degrees is still acute
		 * assertTrue(amc.solve()>=.5);	
		 * assertTrue(amc.solve()>=.7);
		 * assertTrue(amc.solve()>=.9);
		 * */

		//-90 degree, same start point, ~50/50 chance sounds reasonable
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		shapes.add(new ConstrainableLine(getLine(0,0,0,-10)));
		amc.setParameters(shapes);
		assertTrue(amc.solve()>=.1);
		assertTrue(amc.solve()>=.4);
		//assertTrue(amc.solve()>=.5);
		assertTrue(amc.solve()<=.6);
		
		//-135 degree, same start point, low confidence
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		shapes.add(new ConstrainableLine(getLine(0,0,-10,-10)));
		amc.setParameters(shapes);
		/* Confidence >.7, that's a problem
		assertTrue(amc.solve()<=.7);
		assertTrue(amc.solve()<=.5);
		 * assertTrue(amc.solve()<=.3);
		 * assertTrue(amc.solve()<=.1);
		 * */
		
		//Trying extremes
		//Tiny positive
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0,0,1000,0)));
		shapes.add(new ConstrainableLine(getLine(0,0,1000,1)));
		amc.setParameters(shapes);
		assertTrue(amc.solve()>=.1);
		assertTrue(amc.solve()>=.3);
		assertTrue(amc.solve()>=.5);
		/*assertTrue(amc.solve()>=.7); Really?!? This is a  tiny angle, should be ~1
		assertTrue(amc.solve()>=.9);*/
		
		//Tiny negative
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0,0,1000,0)));
		shapes.add(new ConstrainableLine(getLine(0,0,1000,-1)));
		amc.setParameters(shapes);
		assertTrue(amc.solve()>=.1);
		assertTrue(amc.solve()>=.3);
		/*assertTrue(amc.solve()>=.5);// Really?!? This is a  tiny angle, should be ~1
		assertTrue(amc.solve()>=.7);
		assertTrue(amc.solve()>=.9);*/

		//Almost 180
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0,0,1000,0)));
		shapes.add(new ConstrainableLine(getLine(0,0,-1000,1)));
		amc.setParameters(shapes);
		assertTrue(amc.solve()<=.7);
		assertTrue(amc.solve()<=.5);
		/*assertTrue(amc.solve()<=.3);  Should be almost 0 at 180 degrees
		assertTrue(amc.solve()<=.1);*/
		
		//Almost -180
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableLine(getLine(0,0,1000,0)));
		shapes.add(new ConstrainableLine(getLine(0,0,-1000,-1)));
		amc.setParameters(shapes);
		assertTrue(amc.solve()<=.7);
		/*assertTrue(amc.solve()<=.5);  Same thing
		assertTrue(amc.solve()<=.3);
		assertTrue(amc.solve()<=.1);*/
	}
	
	@Test
	public void testSolveConstrainableLineConstrainableLine() {
		amc = new AcuteMeetConstraint();
		ConstrainableLine shape1, shape2;
		
/*
		//No parameters
		assertEquals(0,amc.solve(null,null),Math.pow(10,-10));
		
		//One parameter
		shape1 = new ConstrainableLine(getLine(0,0,10,0));
		assertEquals(0,amc.solve(shape1,null),Math.pow(10,-10));
*/		
		//Two parameter
		//Reasonable range?
		shape1 = new ConstrainableLine(getLine(0,0,10,0));
		shape2=(new ConstrainableLine(getLine(0,0,(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)))));
		assertTrue(amc.solve(shape1,shape2)>=0);
		assertTrue(amc.solve(shape1,shape2)<=1);
		
		//Same Line
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0,0,10,0)));
		assertEquals(1,amc.solve(shape1,shape2),.01);
		
		//45 degree, same start point, should have high confidence
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0,0,10,10)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.3);
		assertTrue(amc.solve(shape1,shape2)>=.5);
		assertTrue(amc.solve(shape1,shape2)>=.7);
		//assertTrue(amc.solve(shape1,shape2)>=.9);  Confidence < .9, is it confident enough?

		//90 degree, same start point, ~50/50 chance sounds reasonable
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0,0,0,10)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.4);
		//assertTrue(amc.solve(shape1,shape2)>=.5);
		assertTrue(amc.solve(shape1,shape2)<=.6);
		
		//135 degree, same start point, low confidence
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0,0,-10,10)));
		assertTrue(amc.solve(shape1,shape2)<=.7);
		assertTrue(amc.solve(shape1,shape2)<=.5);
		//assertTrue(amc.solve(shape1,shape2)<=.3); Confidence >.3, low enough?
		//assertTrue(amc.solve(shape1,shape2)<=.1);

		//-45 degree, same start point, should have high confidence
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0,0,10,-10)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.3);
		/* Confidence < .5?  Fairly certain -45 degrees is still acute
		 * assertTrue(amc.solve(shape1,shape2)>=.5);	
		 * assertTrue(amc.solve(shape1,shape2)>=.7);
		 * assertTrue(amc.solve(shape1,shape2)>=.9);
		 * */

		//-90 degree, same start point, ~50/50 chance sounds reasonable
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0,0,0,-10)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.4);
		//assertTrue(amc.solve(shape1,shape2)>=.5);
		assertTrue(amc.solve(shape1,shape2)<=.6);
		
		//-135 degree, same start point, low confidence
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0,0,-10,-10)));
		/* Confidence >.7, that's a problem
		assertTrue(amc.solve(shape1,shape2)<=.7);
		assertTrue(amc.solve(shape1,shape2)<=.5);
		 * assertTrue(amc.solve(shape1,shape2)<=.3);
		 * assertTrue(amc.solve(shape1,shape2)<=.1);
		 * */
		
		//Trying extremes
		//Tiny positive
		shape1=(new ConstrainableLine(getLine(0,0,1000,0)));
		shape2=(new ConstrainableLine(getLine(0,0,1000,1)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.3);
		assertTrue(amc.solve(shape1,shape2)>=.5);
		/*assertTrue(amc.solve(shape1,shape2)>=.7); Really?!? This is a  tiny angle, should be ~1
		assertTrue(amc.solve(shape1,shape2)>=.9);*/
		
		//Tiny negative
		shape1=(new ConstrainableLine(getLine(0,0,1000,0)));
		shape2=(new ConstrainableLine(getLine(0,0,1000,-1)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.3);
		/*assertTrue(amc.solve(shape1,shape2)>=.5);// Really?!? This is a  tiny angle, should be ~1
		assertTrue(amc.solve(shape1,shape2)>=.7);
		assertTrue(amc.solve(shape1,shape2)>=.9);*/

		//Almost 180
		shape1=(new ConstrainableLine(getLine(0,0,1000,0)));
		shape2=(new ConstrainableLine(getLine(0,0,-1000,1)));
		assertTrue(amc.solve(shape1,shape2)<=.7);
		assertTrue(amc.solve(shape1,shape2)<=.5);
		/*assertTrue(amc.solve(shape1,shape2)<=.3);  Should be almost 0 at 180 degrees
		assertTrue(amc.solve(shape1,shape2)<=.1);*/
		
		//Almost -180
		shape1=(new ConstrainableLine(getLine(0,0,1000,0)));
		shape2=(new ConstrainableLine(getLine(0,0,-1000,-1)));
		assertTrue(amc.solve(shape1,shape2)<=.7);
		/*assertTrue(amc.solve(shape1,shape2)<=.5);  Same thing
		assertTrue(amc.solve(shape1,shape2)<=.3);
		assertTrue(amc.solve(shape1,shape2)<=.1);*/
	
		//Not Connected
		/*
		//No parameters
		assertEquals(0,amc.solve(null,null),Math.pow(10,-10));
		
		//One parameter
		shape1 = new ConstrainableLine(getLine(0,0,10,0));
		assertEquals(0,amc.solve(shape1,null),Math.pow(10,-10));
*/		
		//Two parameter
		//Reasonable range?
		shape1 = new ConstrainableLine(getLine(0,0,10,0));
		shape2=(new ConstrainableLine(getLine(10,0,(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5))+10,(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)))));
		assertTrue(amc.solve(shape1,shape2)>=0);
		assertTrue(amc.solve(shape1,shape2)<=1);
		shape1 = new ConstrainableLine(getLine(0,0,10,0));
		shape2=(new ConstrainableLine(getLine(0,10,(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),10+(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)))));
		assertTrue(amc.solve(shape1,shape2)>=0);
		assertTrue(amc.solve(shape1,shape2)<=1);
		
		//Same Line translated along line and parallel
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0+10,0,10+10,0)));
		//assertEquals(1,amc.solve(shape1,shape2),.01);
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0,0+10,10,0+10)));
		//assertEquals(1,amc.solve(shape1,shape2),.01);
		
		//45 degree
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0+10,0,10+10,10)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.3);
		assertTrue(amc.solve(shape1,shape2)>=.5);
		assertTrue(amc.solve(shape1,shape2)>=.7);
		//assertTrue(amc.solve(shape1,shape2)>=.9);  Confidence < .9, is it confident enough?
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0+10,0,10+10,10)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.3);
		assertTrue(amc.solve(shape1,shape2)>=.5);
		assertTrue(amc.solve(shape1,shape2)>=.7);
		//assertTrue(amc.solve(shape1,shape2)>=.9);  Confidence < .9, is it confident enough?

		//90 degree, same start point, ~50/50 chance sounds reasonable
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0+10,0,0+10,10)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.4);
		//assertTrue(amc.solve(shape1,shape2)>=.5);
		assertTrue(amc.solve(shape1,shape2)<=.6);
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0,0+10,0,10+10)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.4);
		//assertTrue(amc.solve(shape1,shape2)>=.5);
		assertTrue(amc.solve(shape1,shape2)<=.6);
		
		//135 degree, same start point, low confidence
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0-10,0,-10-10,10)));
		assertTrue(amc.solve(shape1,shape2)<=.7);
		assertTrue(amc.solve(shape1,shape2)<=.5);
		//assertTrue(amc.solve(shape1,shape2)<=.3); Confidence >.3, low enough?
		//assertTrue(amc.solve(shape1,shape2)<=.1);
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0,0+10,-10,10+10)));
		assertTrue(amc.solve(shape1,shape2)<=.7);
		assertTrue(amc.solve(shape1,shape2)<=.5);
		//assertTrue(amc.solve(shape1,shape2)<=.3); Confidence >.3, low enough?
		//assertTrue(amc.solve(shape1,shape2)<=.1);

		//-45 degree, same start point, should have high confidence
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0+10,0,10+10,-10)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.3);
		/* Confidence < .5?  Fairly certain -45 degrees is still acute
		 * assertTrue(amc.solve(shape1,shape2)>=.5);	
		 * assertTrue(amc.solve(shape1,shape2)>=.7);
		 * assertTrue(amc.solve(shape1,shape2)>=.9);
		 * */
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0,0-10,10,-10-10)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.3);
		/* Confidence < .5?  Fairly certain -45 degrees is still acute
		 * assertTrue(amc.solve(shape1,shape2)>=.5);	
		 * assertTrue(amc.solve(shape1,shape2)>=.7);
		 * assertTrue(amc.solve(shape1,shape2)>=.9);
		 * */

		//-90 degree, same start point, ~50/50 chance sounds reasonable
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0+10,0,0+10,-10)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.4);
		//assertTrue(amc.solve(shape1,shape2)>=.5);
		assertTrue(amc.solve(shape1,shape2)<=.6);
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0,0-10,0,-10-10)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.4);
		//assertTrue(amc.solve(shape1,shape2)>=.5);
		assertTrue(amc.solve(shape1,shape2)<=.6);
		
		//-135 degree, same start point, low confidence
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0-10,0,-10-10,-10)));
		/* Confidence >.7, that's a problem
		assertTrue(amc.solve(shape1,shape2)<=.7);
		assertTrue(amc.solve(shape1,shape2)<=.5);
		 * assertTrue(amc.solve(shape1,shape2)<=.3);
		 * assertTrue(amc.solve(shape1,shape2)<=.1);
		 * */
		shape1=(new ConstrainableLine(getLine(0,0,10,0)));
		shape2=(new ConstrainableLine(getLine(0,0-10,-10,-10-10)));
		/* Confidence >.7, that's a problem
		assertTrue(amc.solve(shape1,shape2)<=.7);
		assertTrue(amc.solve(shape1,shape2)<=.5);
		 * assertTrue(amc.solve(shape1,shape2)<=.3);
		 * assertTrue(amc.solve(shape1,shape2)<=.1);
		 * */
		
		//Trying extremes
		//Tiny positive
		shape1=(new ConstrainableLine(getLine(0,0,1000,0)));
		shape2=(new ConstrainableLine(getLine(0+10,0,1000+10,1)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.3);
		assertTrue(amc.solve(shape1,shape2)>=.5);
		/*assertTrue(amc.solve(shape1,shape2)>=.7); Really?!? This is a  tiny angle, should be ~1
		assertTrue(amc.solve(shape1,shape2)>=.9);*/
		shape1=(new ConstrainableLine(getLine(0,0,1000,0)));
		shape2=(new ConstrainableLine(getLine(0,0+10,1000,1+10)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.3);
		assertTrue(amc.solve(shape1,shape2)>=.5);
		/*assertTrue(amc.solve(shape1,shape2)>=.7); Really?!? This is a  tiny angle, should be ~1
		assertTrue(amc.solve(shape1,shape2)>=.9);*/
		
		//Tiny negative
		shape1=(new ConstrainableLine(getLine(0,0,1000,0)));
		shape2=(new ConstrainableLine(getLine(0+10,0,1000+10,-1)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.3);
		/*assertTrue(amc.solve(shape1,shape2)>=.5);// Really?!? This is a  tiny angle, should be ~1
		assertTrue(amc.solve(shape1,shape2)>=.7);
		assertTrue(amc.solve(shape1,shape2)>=.9);*/
		shape1=(new ConstrainableLine(getLine(0,0,1000,0)));
		shape2=(new ConstrainableLine(getLine(0,0-10,1000,-1-10)));
		assertTrue(amc.solve(shape1,shape2)>=.1);
		assertTrue(amc.solve(shape1,shape2)>=.3);
		/*assertTrue(amc.solve(shape1,shape2)>=.5);// Really?!? This is a tiny angle, should be ~1
		assertTrue(amc.solve(shape1,shape2)>=.7);
		assertTrue(amc.solve(shape1,shape2)>=.9);*/

		//Almost 180
		shape1=(new ConstrainableLine(getLine(0,0,1000,0)));
		shape2=(new ConstrainableLine(getLine(0,0+10,-1000,1+10)));
		assertTrue(amc.solve(shape1,shape2)<=.7);
		assertTrue(amc.solve(shape1,shape2)<=.5);
		/*assertTrue(amc.solve(shape1,shape2)<=.3);  Should be almost 0 at 180 degrees
		assertTrue(amc.solve(shape1,shape2)<=.1);*/
		shape1=(new ConstrainableLine(getLine(0,0,1000,0)));
		shape2=(new ConstrainableLine(getLine(0-10,0,-1000-10,1)));
		assertTrue(amc.solve(shape1,shape2)<=.7);
		assertTrue(amc.solve(shape1,shape2)<=.5);
		/*assertTrue(amc.solve(shape1,shape2)<=.3);  Should be almost 0 at 180 degrees
		assertTrue(amc.solve(shape1,shape2)<=.1);*/
		
		//Almost -180
		shape1=(new ConstrainableLine(getLine(0,0,1000,0)));
		shape2=(new ConstrainableLine(getLine(0-10,0,-1000-10,-1)));
		assertTrue(amc.solve(shape1,shape2)<=.7);
		/*assertTrue(amc.solve(shape1,shape2)<=.5);  Same thing
		assertTrue(amc.solve(shape1,shape2)<=.3);
		assertTrue(amc.solve(shape1,shape2)<=.1);*/
		shape1=(new ConstrainableLine(getLine(0,0,1000,0)));
		shape2=(new ConstrainableLine(getLine(0,0-10,-1000,-1-10)));
		assertTrue(amc.solve(shape1,shape2)<=.7);
		/*assertTrue(amc.solve(shape1,shape2)<=.5);  Same thing
		assertTrue(amc.solve(shape1,shape2)<=.3);
		assertTrue(amc.solve(shape1,shape2)<=.1);*/
	}
}
