/**
 * 
 */
package test.unit.ladder.recognition.constraint.confidence;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ladder.core.sketch.Point;
import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.constrainable.ConstrainablePoint;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.BisectsConstraint;

import test.unit.SlothTest;

/**
 * @author pfc9416
 *
 */
public class BisectsConstraintTest extends SlothTest{

	BisectsConstraint bc;
	
	/**
	 * 
	 */
	@Before
	public void setUp(){
	}

	/**
	 * 
	 */
	@After
	public void tearDown(){
		bc=null;
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.BisectsConstraint#BisectsConstraint()}.
	 */
	@Test
	public void testBisectsConstraint() {
		bc=new BisectsConstraint();
		assertNotNull(bc);
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.BisectsConstraint#BisectsConstraint(double)}.
	 */
	@Test
	public void testBisectsConstraintDouble() {
		double t = rand.nextDouble();
		bc=new BisectsConstraint(t);
		assertNotNull(bc);
		assertEquals(t,bc.getThreshold(),0);
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.BisectsConstraint#solve()}.
	 */
	@Test
	public void testSolve() {
		bc=new BisectsConstraint();
		
		ArrayList<IConstrainable> params = new ArrayList<IConstrainable>();
		
		bc.setParameters(params);
		assertEquals(0,bc.solve(),0);
		
		params.add(new ConstrainableLine(getLine(0, 0, 0, 100)));
		bc.setParameters(params);
		assertEquals(0,bc.solve(),0);
		
		double prevConf=-1;
		for(int i=-50;i<=150;i++){
			params.clear();
			params.add(new ConstrainablePoint(0,i, null));
			params.add(new ConstrainableLine(getLine(0, 0, 0, 100)));
			bc.setParameters(params);
			double conf=bc.solve();
			System.out.println((50-i)+" distance from midpoint.");
			System.out.println("Confidence: "+conf);
//			if(prevConf>-1)
//				if(i>100)
//					assertTrue(prevConf<conf);
//				else if(i>50)
//					assertTrue(prevConf>conf);
//				else if(i>0)
//					assertTrue(prevConf<conf);
//				else
//					assertTrue(prevConf>conf);
			prevConf=conf;
		}
	}

}
