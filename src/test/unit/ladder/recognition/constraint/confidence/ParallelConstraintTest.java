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
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.ParallelConstraint;

import test.unit.SlothTest;

/**
 * @author Priscus
 *
 */
public class ParallelConstraintTest extends SlothTest{

	ParallelConstraint pc;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		pc=null;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.ParallelConstraint#ParallelConstraint()}.
	 */
	@Test
	public void testParallelConstraint() {
		pc = new ParallelConstraint();
		assertNotNull(pc);
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.ParallelConstraint#ParallelConstraint(double)}.
	 */
	@Test
	public void testParallelConstraintDouble() {
		double t = rand.nextDouble();
		pc = new ParallelConstraint(t);
		assertNotNull(pc);
		assertEquals(t,pc.getThreshold(),0);
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.ParallelConstraint#solve()}.
	 */
	@Test
	public void testSolve() {
		
		pc = new ParallelConstraint();
		
		ArrayList<IConstrainable> params = new ArrayList<IConstrainable>();
		
		pc.setParameters(params);
		assertEquals(0,pc.solve(),0);
		
		params.add(new ConstrainableLine(getLine(0,0,0,10)));
		pc.setParameters(params);
		assertEquals(0,pc.solve(),0);
		
		double prevConf=-1;
		for(double degrees=0;degrees<360;degrees++){
			params.clear();
			params.add(new ConstrainableLine(getLine(0,0,100,0)));
			params.add(new ConstrainableLine(getLine(0,0,100*Math.cos(degrees*Math.PI/180),100*Math.sin(degrees*Math.PI/180))));
			pc.setParameters(params);
			double conf = pc.solve();
			params.clear();
			params.add(new ConstrainableLine(getLine(0,0,100*Math.cos(degrees*Math.PI/180),100*Math.sin(degrees*Math.PI/180))));
			params.add(new ConstrainableLine(getLine(0,0,100,0)));
			pc.setParameters(params);
			assertEquals(conf,pc.solve(),0);
			System.out.println((360-degrees)+" angle difference.  Confidence: "+conf);
			if(prevConf!=-1)
				if(degrees<=90)
					assertTrue(prevConf>conf);
				else if(degrees<=180)
					assertTrue(prevConf<conf);
				else if(degrees<=270)
					assertTrue(prevConf>conf);
				else
					assertTrue(prevConf<conf);
		}

		prevConf=-1;
		for(double degrees=0;degrees<360;degrees++){
			params.clear();
			params.add(new ConstrainableLine(getLine(0,0,100,0)));
			params.add(new ConstrainableLine(getLine(150+0,150+0,150+100*Math.cos(degrees*Math.PI/180),150+100*Math.sin(degrees*Math.PI/180))));
			pc.setParameters(params);
			double conf = pc.solve();
			params.clear();
			params.add(new ConstrainableLine(getLine(150+0,150+0,150+100*Math.cos(degrees*Math.PI/180),150+100*Math.sin(degrees*Math.PI/180))));
			params.add(new ConstrainableLine(getLine(0,0,100,0)));
			pc.setParameters(params);
			assertEquals(conf,pc.solve(),0);
			System.out.println((360-degrees)+" angle difference.  Confidence: "+conf);
			if(prevConf!=-1)
				if(degrees<=90)
					assertTrue(prevConf>conf);
				else if(degrees<=180)
					assertTrue(prevConf<conf);
				else if(degrees<=270)
					assertTrue(prevConf>conf);
				else
					assertTrue(prevConf<conf);
		}
	}

}
