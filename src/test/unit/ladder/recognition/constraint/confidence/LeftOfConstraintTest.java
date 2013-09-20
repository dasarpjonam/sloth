package test.unit.ladder.recognition.constraint.confidence;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.constrainable.ConstrainablePoint;
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.constraint.confidence.LeftOfConstraint;

import test.unit.SlothTest;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class LeftOfConstraintTest extends SlothTest {
	LeftOfConstraint loc; 	
	
	@Before
	public void setup(){
		loc=null;
	}

	@After
	public void tearDown(){
		
	}
	
	@Test
	public void testLeftOfConstraint() {
		loc = new LeftOfConstraint();
		assertNotNull(loc);
		assertEquals(loc.getThreshold(),
				LeftOfConstraint.DEFAULT_THRESHOLD, Math.pow(10, -10));
		assertEquals(loc.getDescription(), LeftOfConstraint.DESCRIPTION);
		assertEquals(loc.getName(), LeftOfConstraint.NAME);
		assertEquals(loc.getNumRequiredParameters(),LeftOfConstraint.NUM_PARAMETERS);
	}
	


	@Test
	public void testLeftOfConstraintDouble() {
		double t = rand.nextDouble();
		loc = new LeftOfConstraint(t);
		assertNotNull(loc);
		assertEquals(loc.getThreshold(), t, Math.pow(10, -10));
		assertEquals(loc.getDescription(), LeftOfConstraint.DESCRIPTION);
		assertEquals(loc.getName(), LeftOfConstraint.NAME);

		t = -t;
		loc = new LeftOfConstraint(t);
		assertNotNull(loc);
		assertEquals(loc.getThreshold(), -t, Math.pow(10, -10));
		assertEquals(loc.getDescription(), LeftOfConstraint.DESCRIPTION);
		assertEquals(loc.getName(), LeftOfConstraint.NAME);
	}
	
	@Test
	public void testSolve() {
		List<IConstrainable> shapes = new ArrayList<IConstrainable>();
		double prevConf;
		loc = new LeftOfConstraint();

		//No parameters
		loc.setParameters(shapes);
		assertEquals(0,loc.solve(),Math.pow(10,-10));
		
		//One parameter
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		loc.setParameters(shapes);
		assertEquals(0,loc.solve(),Math.pow(10,-10));

		//Two parameter
		//Reasonable range?
		shapes.add(new ConstrainableLine(getLine((int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)))));
		loc.setParameters(shapes);
		assertTrue(loc.solve()>=0);
		assertTrue(loc.solve()<=1);

		//2 Points
		//Directly left
		int distMax=100;
		prevConf=-1;
		for(int i=0;i<distMax;i++){
			shapes.clear();
			shapes.add(new ConstrainablePoint(-i, 0,null));
			shapes.add(new ConstrainablePoint(0,0,null));
			loc.setParameters(shapes);
			System.out.println("Distance left: "+(i));
			double conf=loc.solve();
			System.out.println("Confidence: "+ conf);
			if(prevConf>=0)
				assertTrue(prevConf<conf);
			prevConf=conf;
		}

		//Directly right
		distMax=100;
		prevConf=-1;
		for(int i=0;i<distMax;i++){
			shapes.clear();
			shapes.add(new ConstrainablePoint(distMax-i, 0,null));
			shapes.add(new ConstrainablePoint(0,0,null));
			loc.setParameters(shapes);
			System.out.println("Distance right: "+(distMax-i));
			double conf=loc.solve();
			System.out.println("Confidence: "+ conf);
//			if(prevConf>=0)
//				assertTrue(prevConf>conf);
			prevConf=conf;
		}

		//Left and up/down
		distMax=100;
		for(int i=0;i<distMax*2+1;i++){
			shapes.clear();
			shapes.add(new ConstrainablePoint(-50, distMax-i,null));
			shapes.add(new ConstrainablePoint(0,0,null));
			loc.setParameters(shapes);
			System.out.println("Distance to side: "+(i-distMax));
			System.out.println("Confidence: "+loc.solve());
		}

		//Right and up/down
		distMax=100;
		for(int i=0;i<distMax*2+1;i++){
			shapes.clear();
			shapes.add(new ConstrainablePoint(distMax-i,50,null));
			shapes.add(new ConstrainablePoint(0,0,null));
			loc.setParameters(shapes);
		}
		
		//Confidence with shape width
		//Left of
		int maxHeight=100;
		for(int i=0;i<maxHeight;i++){
			shapes.clear();
			shapes.add(new ConstrainableShape(getRect(-50,0,i-50,100)));
			shapes.add(new ConstrainableShape(getRect(0,0,100,100)));
			loc.setParameters(shapes);
			System.out.println("Height: "+(i));
			System.out.println("Confidence: "+loc.solve());
		}
		
	}
}