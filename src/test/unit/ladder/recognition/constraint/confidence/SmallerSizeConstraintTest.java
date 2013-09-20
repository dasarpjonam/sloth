/**
 * 
 */
package test.unit.ladder.recognition.constraint.confidence;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.SameSizeConstraint;
import org.ladder.recognition.constraint.confidence.SmallerSizeConstraint;

import test.unit.SlothTest;

/**
 * @author Priscus
 *
 */
public class SmallerSizeConstraintTest extends SlothTest{

	SmallerSizeConstraint ssc;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		ssc=null;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.SmallerSizeConstraint#SmallerSizeConstraint()}.
	 */
	@Test
	public void testSmallerSizeConstraint() {
		ssc=new SmallerSizeConstraint();
		assertNotNull(ssc);
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.SmallerSizeConstraint#SmallerSizeConstraint(double)}.
	 */
	@Test
	public void testSmallerSizeConstraintDouble() {
		double t = rand.nextDouble();
		ssc=new SmallerSizeConstraint(t);
		assertNotNull(ssc);
		assertEquals(t,ssc.getThreshold(),Math.pow(10,-10));
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.SmallerSizeConstraint#solve()}.
	 */
	@Test
	public void testSolve() {
		ssc = new SmallerSizeConstraint();
		ArrayList<IConstrainable> shapes = new ArrayList<IConstrainable>();

		ssc.setParameters(shapes);
		assertEquals(0, ssc.solve(), 0);

		shapes.add(new ConstrainableShape(getRect()));
		ssc.setParameters(shapes);
		assertEquals(0, ssc.solve(), 0);

		shapes.add(new ConstrainableShape(getRect()));
		ssc.setParameters(shapes);
		assertTrue(0 <= ssc.solve());
		assertTrue(1 >= ssc.solve());

		// Lines
		double prevConf=-1;
		for(int length=0;length<=100;length+=10){
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,0,length)));
			shapes.add(new ConstrainableLine(getLine(0,0,0,50)));
			ssc.setParameters(shapes);
			double conf=ssc.solve();
			System.out.println("Length difference "+(50-length)+" Confidence: "+conf);
			if(prevConf!=-1)
					assertTrue(prevConf>=conf);
			prevConf=conf;
			
		}
		
		// Changing Width/Height
		double[][] confs = new double[11][11];
		for (int width = 0; width <= 10; width++)
			for (int height = 0; height <= 10; height++) {
				shapes.clear();
				shapes.add(new ConstrainableShape(getRect(0, 0, width * 10,
						height * 10)));
				shapes.add(new ConstrainableShape(getRect(0, 0, 50, 50)));
				ssc.setParameters(shapes);
				double conf = ssc.solve();
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
						assertTrue(confs[width][height - 1] >= confs[width][height]);
				if (width > 0)
						assertTrue(confs[width - 1][height] >= confs[width][height]);
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

}
