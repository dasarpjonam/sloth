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
import org.ladder.recognition.constraint.confidence.SameSizeConstraint;

import test.unit.SlothTest;

public class SameSizeConstraintTest extends SlothTest {

	SameSizeConstraint ssc;
	List<IConstrainable> shapes;
	double prevConf;
	int distMax=100;

	@Before
	public void setup() {
		ssc = new SameSizeConstraint();
		ssc.setScaleParameters(false);
		shapes = new ArrayList<IConstrainable>();

	}

	@After
	public void tearDown() {
		ssc = null;
	}

	@Test
	public void testSameSizeConstraint() {
		ssc = new SameSizeConstraint();
		assertNotNull(ssc);
	}

	@Test
	public void testSameSizeConstraintDouble() {
		double t = rand.nextDouble();
		ssc = new SameSizeConstraint(t);
		assertNotNull(ssc);
		assertEquals(t, ssc.getThreshold(), Math.pow(10, -10));

		t = -t;
		try {
			ssc=new SameSizeConstraint(t);
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
			ssc.setParameters(shapes);
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
			ssc.setParameters(shapes);
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
		ssc.setParameters(shapes);
		assertTrue(ssc.solve()>=0);
		assertTrue(ssc.solve()<=1);
	}
	
	@Test
	public void testSameSizeForLines() {
		
		prevConf=-1;
		for(int length=0;length<=100;length+=10){
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,0,length)));
			shapes.add(new ConstrainableLine(getLine(0,0,0,50)));
			ssc.setParameters(shapes);
			double conf=ssc.solve();
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,0,50)));
			shapes.add(new ConstrainableLine(getLine(0,0,0,length)));
			ssc.setParameters(shapes);
			assertEquals(conf,ssc.solve(),Math.pow(10, -10));
			System.out.println("Length difference "+(50-length)+" Confidence: "+conf);
			if(prevConf!=-1)
				if(length<=50)
					assertTrue(prevConf<conf);
				else
					assertTrue(prevConf>conf);
			prevConf=conf;
		}
		
	}
	
	public void testSameSizeForRectangles() {
		
		double[][] confs = new double[11][11];
		for (int width = 0; width <= 10; width++)  {
			for (int height = 0; height <= 10; height++) {
				shapes.clear();
				shapes.add(new ConstrainableShape(getRect(0, 0, 50, 50)));
				shapes.add(new ConstrainableShape(getRect(0, 0, width * 10,
						height * 10)));
				ssc.setParameters(shapes);
				double conf = ssc.solve();
				// if(prevConf>0)
				// if(width<=100)
				// assertTrue(prevConf<conf);
				// else
				// assertTrue(prevConf>conf);
				confs[width][height] = conf;
				if (height > 0)
					if (height <= 5)
						assertTrue(confs[width][height - 1] < confs[width][height]);
					else
						assertTrue(confs[width][height - 1] > confs[width][height]);
				if (width > 0)
					if (width <= 5)
						assertTrue(confs[width - 1][height] < confs[width][height]);
					else
						assertTrue(confs[width - 1][height] > confs[width][height]);
				System.out.println("Width difference: " + (width * 10 - 50)
						+ "  Height difference: " + (height * 10 - 50));
				System.out.println("Confidence: " + conf);
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
