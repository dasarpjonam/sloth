/**
 * 
 */
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
import org.ladder.recognition.constraint.confidence.SameXConstraint;

import test.unit.SlothTest;

/**
 * Unit test to check the constraint for whether two object's center's
 * x-coordinates are the same
 * 
 * @author awolin
 */
public class SameXConstraintTest extends SlothTest {
	
	SameXConstraint sxc;
	List<IConstrainable> shapes;
	double prevConf;
	int distMax=100;
	

	@Before
	public void setUp(){
		sxc = new SameXConstraint();
		sxc.setScaleParameters(false);
		shapes = new ArrayList<IConstrainable>();
	}
	

	@After
	public void tearDown() {
		sxc=null;
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.confidence.SameXConstraint#SameXConstraint()}
	 * .
	 */
	@Test
	public void testSameXConstraint() {
		sxc = new SameXConstraint();
		assertNotNull(sxc);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.confidence.SameXConstraint#SameXConstraint(double)}
	 * .
	 */
	@Test
	public void testSameXConstraintDouble() {
		double t = rand.nextDouble();
		sxc = new SameXConstraint(t);
		assertNotNull(sxc);
		assertEquals(t, sxc.getThreshold(), S_DEFAULT_DELTA);
		
		t = -t;
		try {
			sxc=new SameXConstraint(t);
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
			sxc.setParameters(shapes);
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
			sxc.setParameters(shapes);
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
		shapes.add(new ConstrainableShape(getRect()));
		shapes.add(new ConstrainableShape(getRect()));
		sxc.setParameters(shapes);
		assertTrue(sxc.solve()>=0);
		assertTrue(sxc.solve()<=1);
	}
	

	@Test
	public void testSameXForRecInXDirection() {
		
		// Test for rectangles in the x direction. There should be no difference
		// between rectangles and other shapes. If there is, then it is a
		// problem with the shape gets and sets and not with SameXConstraint
		
		
		// Test for rectangles in the y direction. The confidence should remain
		// the same.
		
		prevConf = -1;
		
		for (int x = 0; x < distMax * 2; x++) {
			shapes.clear();
			assertEquals(shapes.size(), 0);
			
			shapes.add(new ConstrainableShape(getRect(x, 0, x + distMax,
					distMax)));
			shapes.add(new ConstrainableShape(getRect(distMax, 0,
					distMax * 2, distMax)));
			sxc.setParameters(shapes);
			
			double conf = sxc.solve();
			
			if (prevConf > 0) {
				if (x <= distMax)
					assertTrue(prevConf < conf);
				else
					assertTrue(prevConf > conf);
			}
			
			prevConf = conf;
			System.out.println("Rectangle x-difference: "
			                   + Math.abs(x - distMax) + ", Confidence: "
			                   + conf);
		}
		
	}
	
	@Test
	public void testSameXForRecInYDirection() {
		// Test for rectangles in the y direction. The confidence should remain
		// the same.
		
		prevConf = -1;
		for (int y = 0; y < distMax * 2; y++) {
			shapes.clear();
			assertEquals(shapes.size(), 0);
			
			shapes.add(new ConstrainableShape(getRect(0, y, distMax,
			        y + distMax)));
			shapes.add(new ConstrainableShape(getRect(0, distMax, distMax,
					distMax * 2)));
			sxc.setParameters(shapes);
			
			double conf = sxc.solve();
			
			if (prevConf > 0) {
				assertEquals(prevConf, conf, S_DEFAULT_DELTA);
			}
			
			prevConf = conf;
			System.out.println("Rectangle y-difference: "
			                   + Math.abs(y - distMax) + ", Confidence: "
			                   + conf);
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
