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
import org.ladder.recognition.constraint.confidence.SameYConstraint;

import test.unit.SlothTest;

/**
 * Unit test to check the constraint for whether two object's center's
 * y-coordinates are the same
 * 
 * @author awolin
 */
public class SameYConstraintTest extends SlothTest {
	
	SameYConstraint syc;
	List<IConstrainable> shapes;
	double prevConf;
	int distMax=100;
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		syc = new SameYConstraint();
		syc.setScaleParameters(false);
		shapes = new ArrayList<IConstrainable>();
	}
	


	@After
	public void tearDown() {
		syc = null;
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.confidence.SameXConstraint#SameXConstraint()}
	 * .
	 */
	@Test
	public void testSameYConstraint() {
		syc = new SameYConstraint();
		assertNotNull(syc);
	}
	

	/**
	 * Test method for
	 * {@link org.ladder.recognition.constraint.confidence.SameXConstraint#SameXConstraint(double)}
	 * .
	 */
	@Test
	public void testSameYConstraintDouble() {
		double t = rand.nextDouble();
		syc = new SameYConstraint(t);
		assertNotNull(syc);
		assertEquals(t, syc.getThreshold(), S_DEFAULT_DELTA);
		
		t = -t;
		try {
			syc=new SameYConstraint(t);
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
			syc.setParameters(shapes);
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
			syc.setParameters(shapes);
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
		syc.setParameters(shapes);
		assertTrue(syc.solve()>=0);
		assertTrue(syc.solve()<=1);
	}
	
	
	@Test
	public void testSameYForRecInXDirection() {
		// Test for rectangles in the x direction. There should be no difference
		// between rectangles and other shapes. If there is, then it is a
		// problem with the shape gets and sets and not with SameYConstraint
		
		prevConf = -1;
		
		for (int x = 0; x < distMax * 2; x++) {
			shapes.clear();
			assertEquals(shapes.size(), 0);
			
			shapes.add(new ConstrainableShape(getRect(x, 0, x + distMax,
			        distMax)));
			shapes.add(new ConstrainableShape(getRect(distMax, 0,
			        distMax * 2, distMax)));
			syc.setParameters(shapes);
			
			double conf = syc.solve();
			
			if (prevConf > 0) {
				assertEquals(prevConf, conf, S_DEFAULT_DELTA);
			}
			
			prevConf = conf;
			System.out.println("Rectangle x-difference: "
			                   + Math.abs(x - distMax) + ", Confidence: "
			                   + conf);
		}
		
		
	}
	
	@Test
	public void testSameYForRecInYDirection() {
		
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
			syc.setParameters(shapes);
			
			double conf = syc.solve();
			
			if (prevConf > 0) {
				if (y <= distMax)
					assertTrue(prevConf < conf);
				else
					assertTrue(prevConf > conf);
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