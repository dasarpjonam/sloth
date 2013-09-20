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
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.LargerSizeConstraint;

import test.unit.SlothTest;

/**
 * @author Priscus
 *
 */
public class LargerSizeConstraintTest extends SlothTest{

	LargerSizeConstraint lsc;
	List<IConstrainable> shapes;
	double prevConf;
	int distMax = 100;
	
	@Before
	public void setUp() {
		lsc= new LargerSizeConstraint();
		lsc.setScaleParameters(false);
		shapes = new ArrayList<IConstrainable>();
	}

	@After
	public void tearDown()  {
	}


	@Test
	public void testLargerSizeConstraint() {
		lsc=new LargerSizeConstraint();
		assertNotNull(lsc);
	}


	@Test
	public void testLargerSizeConstraintDouble() {
		double t = rand.nextDouble();
		lsc=new LargerSizeConstraint(t);
		assertNotNull(lsc);
		assertEquals(t,lsc.getThreshold(),Math.pow(10,-10));
		
		t = -t;
		try {
			lsc=new LargerSizeConstraint(t);
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
			lsc.setParameters(shapes);
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
			lsc.setParameters(shapes);
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
		lsc.setParameters(shapes);
		assertTrue(lsc.solve()>=0);
		assertTrue(lsc.solve()<=1);
	}

	@Test
	public void testLargerSizeForLines() {
		prevConf=-1;
		for(int length=0;length<=100;length+=10){
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,0,length)));
			shapes.add(new ConstrainableLine(getLine(0,0,0,50)));
			lsc.setParameters(shapes);
			double conf=lsc.solve();
			System.out.println("Length difference "+(50-length)+" Confidence: "+conf);
			if(prevConf!=-1)
					assertTrue(prevConf<=conf);
			prevConf=conf;
			
		}
	}
	
	@Test
	public void testLargerSizeForRectangles() {
		
		double[][] confs = new double[11][11];
		for (int width = 0; width <= 10; width++) {
			for (int height = 0; height <= 10; height++) {
				shapes.clear();
				shapes.add(new ConstrainableShape(getRect(0, 0, width * 10,
						height * 10)));
				shapes.add(new ConstrainableShape(getRect(0, 0, 50, 50)));
				lsc.setParameters(shapes);
				double conf = lsc.solve();
				// if(prevConf>0)
				// if(width<=100)
				// assertTrue(prevConf<conf);
				// else
				// assertTrue(prevConf>conf);
				confs[width][height] = conf;
				System.out.println("Width difference: " + (width * 10 - 50)
						+ "  Height difference: " + (height * 10 - 50));
				System.out.println("Confidence: " + conf);
				if (height > 0)
						assertTrue(confs[width][height - 1] <= confs[width][height]);
				if (width > 0)
						assertTrue(confs[width - 1][height] <= confs[width][height]);
			}
		}

		System.out.println("Confidences");
		System.out.print("  ");
		for(int w=0;w<11;w++)
			System.out.print("\t"+(w*10-50));
		System.out.println();
		for (int h = 0; h < 11; h++) {
			System.out.print(h*10-50);
			for (int w = 0; w < 11; w++)
				System.out.print("\t" + (int) (confs[w][h] * 100));
			System.out.println();
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
