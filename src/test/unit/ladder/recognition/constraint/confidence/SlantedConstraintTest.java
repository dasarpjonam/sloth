/**
 * 
 */
package test.unit.ladder.recognition.constraint.confidence;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.unit.SlothTest;

import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.SlantedConstraint;

/**
 * @author Priscus
 *
 */
public class SlantedConstraintTest extends SlothTest {

	SlantedConstraint sc;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		sc = null;
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.SlantedConstraint#SlantedConstraint()}.
	 */
	@Test
	public void testSlantedConstraint() {
		sc = new SlantedConstraint();
		assertNotNull(sc);
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.SlantedConstraint#SlantedConstraint(double)}.
	 */
	@Test
	public void testSlantedConstraintDouble() {
		double t = rand.nextDouble();
		sc = new SlantedConstraint(t);
		assertNotNull(sc);
		assertEquals(t,sc.getThreshold(),0);
		
		sc = new SlantedConstraint(-t);
		assertNotNull(sc);
		assertEquals(t,sc.getThreshold(),0);		
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.SlantedConstraint#solve()}.
	 */
	@Test
	public void testSolve() {
		sc = new SlantedConstraint();
		ArrayList<IConstrainable> shapes = new ArrayList<IConstrainable>();
		
		sc.setParameters(shapes);
		assertEquals(0,sc.solve(),0);
		
		double prevPosConf=-1;
		double prevNegConf=-1;
		for(double degrees=0;degrees<=45;degrees++){
			System.out.println("+/-"+degrees+" degree line");
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,100*Math.cos(degrees*Math.PI/180),100*Math.sin(degrees*Math.PI/180))));
			sc.setParameters(shapes);
			double conf = sc.solve();
			System.out.println("+ confidence: "+conf);
			if(prevPosConf!=-1)
				assertTrue(conf>prevPosConf);
			prevPosConf=conf;
			
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,100*Math.cos(-degrees*Math.PI/180),100*Math.sin(-degrees*Math.PI/180))));
			sc.setParameters(shapes);
			conf = sc.solve();
			System.out.println("- confidence: "+conf);
			if(prevNegConf!=-1)
				assertTrue(conf>prevNegConf);
			prevNegConf=conf;
			System.out.println();
		}

		prevPosConf=-1;
		prevNegConf=-1;
		for(double degrees=45;degrees<=90;degrees++){
			System.out.println("+/-"+degrees+" degree line");
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,100*Math.cos(degrees*Math.PI/180),100*Math.sin(degrees*Math.PI/180))));
			sc.setParameters(shapes);
			double conf = sc.solve();
			if(prevPosConf!=-1)
				assertTrue(conf<prevPosConf);
			prevPosConf=conf;
			System.out.println("+ confidence: "+conf);
			
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,100*Math.cos(-degrees*Math.PI/180),100*Math.sin(-degrees*Math.PI/180))));
			sc.setParameters(shapes);
			conf = sc.solve();
			if(prevNegConf!=-1)
				assertTrue(conf<prevNegConf);
			prevNegConf=conf;
			System.out.println("- confidence: "+conf);
			System.out.println();
		}
		
		prevPosConf=-1;
		prevNegConf=-1;
		for(double degrees=90;degrees<=135;degrees++){
			System.out.println("+/-"+degrees+" degree line");
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,100*Math.cos(degrees*Math.PI/180),100*Math.sin(degrees*Math.PI/180))));
			sc.setParameters(shapes);
			double conf = sc.solve();
			System.out.println("+ confidence: "+conf);
			if(prevPosConf!=-1)
				assertTrue(conf>prevPosConf);
			prevPosConf=conf;
			
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,100*Math.cos(-degrees*Math.PI/180),100*Math.sin(-degrees*Math.PI/180))));
			sc.setParameters(shapes);
			conf = sc.solve();
			System.out.println("- confidence: "+conf);
			if(prevNegConf!=-1)
				assertTrue(conf>prevNegConf);
			prevNegConf=conf;
			System.out.println();
		}

		prevPosConf=-1;
		prevNegConf=-1;
		for(double degrees=135;degrees<=180;degrees++){
			System.out.println("+/-"+degrees+" degree line");
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,100*Math.cos(degrees*Math.PI/180),100*Math.sin(degrees*Math.PI/180))));
			sc.setParameters(shapes);
			double conf = sc.solve();
			if(prevPosConf!=-1)
				assertTrue(conf<prevPosConf);
			prevPosConf=conf;
			System.out.println("+ confidence: "+conf);
			
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,100*Math.cos(-degrees*Math.PI/180),100*Math.sin(-degrees*Math.PI/180))));
			sc.setParameters(shapes);
			conf = sc.solve();
			if(prevNegConf!=-1)
				assertTrue(conf<prevNegConf);
			prevNegConf=conf;
			System.out.println("- confidence: "+conf);
			System.out.println();
		}
	}

}
