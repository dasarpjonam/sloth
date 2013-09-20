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
import org.ladder.recognition.constraint.confidence.RightOfConstraint;

import test.unit.SlothTest;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class RightOfConstraintTest extends SlothTest {
	RightOfConstraint roc; 	
	
	@Before
	public void setup(){
		roc=null;
	}

	@After
	public void tearDown(){
		
	}
	
	@Test
	public void testRightOfConstraint() {
		roc = new RightOfConstraint();
		assertNotNull(roc);
		assertEquals(roc.getThreshold(),
				RightOfConstraint.DEFAULT_THRESHOLD, Math.pow(10, -10));
		assertEquals(roc.getDescription(), RightOfConstraint.DESCRIPTION);
		assertEquals(roc.getName(), RightOfConstraint.NAME);
		assertEquals(roc.getNumRequiredParameters(),RightOfConstraint.NUM_PARAMETERS);
	}
	


	@Test
	public void testRightOfConstraintDouble() {
		double t = rand.nextDouble();
		roc = new RightOfConstraint(t);
		assertNotNull(roc);
		assertEquals(roc.getThreshold(), t, Math.pow(10, -10));
		assertEquals(roc.getDescription(), RightOfConstraint.DESCRIPTION);
		assertEquals(roc.getName(), RightOfConstraint.NAME);

		t = -t;
		roc = new RightOfConstraint(t);
		assertNotNull(roc);
		assertEquals(roc.getThreshold(), -t, Math.pow(10, -10));
		assertEquals(roc.getDescription(), RightOfConstraint.DESCRIPTION);
		assertEquals(roc.getName(), RightOfConstraint.NAME);
	}
	
	@Test
	public void testSolve() {
		List<IConstrainable> shapes = new ArrayList<IConstrainable>();
		double prevConf;
		roc = new RightOfConstraint();

		//No parameters
		roc.setParameters(shapes);
		assertEquals(0,roc.solve(),Math.pow(10,-10));
		
		//One parameter
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		roc.setParameters(shapes);
		assertEquals(0,roc.solve(),Math.pow(10,-10));

		//Two parameter
		//Reasonable range?
		shapes.add(new ConstrainableLine(getLine((int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)))));
		roc.setParameters(shapes);
		assertTrue(roc.solve()>=0);
		assertTrue(roc.solve()<=1);

		//2 Points
		//Directly left
		int distMax=100;
		prevConf=-1;
		for(int i=0;i<distMax;i++){
			shapes.clear();
			shapes.add(new ConstrainablePoint(-i, 0, null));
			shapes.add(new ConstrainablePoint(0,0, null));
			roc.setParameters(shapes);
			System.out.println("Distance left: "+(i));
			double conf=roc.solve();
			System.out.println("Confidence: "+ conf);
//			if(prevConf>=0)
//				assertTrue(prevConf<conf);
			prevConf=conf;
		}

		//Directly right
		distMax=100;
		prevConf=-1;
		for(int i=0;i<distMax;i++){
			shapes.clear();
			shapes.add(new ConstrainablePoint(distMax-i, 0,null));
			shapes.add(new ConstrainablePoint(0,0,null));
			roc.setParameters(shapes);
			System.out.println("Distance right: "+(distMax-i));
			double conf=roc.solve();
			System.out.println("Confidence: "+ conf);
			if(prevConf>=0)
				assertTrue(prevConf>conf);
			prevConf=conf;
		}

		//Left and up/down
		distMax=100;
		for(int i=0;i<distMax*2+1;i++){
			shapes.clear();
			shapes.add(new ConstrainablePoint(-50, distMax-i,null));
			shapes.add(new ConstrainablePoint(0,0,null));
			roc.setParameters(shapes);
			System.out.println("Distance to side: "+(i-distMax));
			System.out.println("Confidence: "+roc.solve());
		}

		//Right and up/down
		distMax=100;
		for(int i=0;i<distMax*2+1;i++){
			shapes.clear();
			shapes.add(new ConstrainablePoint(distMax-i,50,null));
			shapes.add(new ConstrainablePoint(0,0,null));
			roc.setParameters(shapes);
		}
		
		//Confidence with shape width
		//Left of
		int maxHeight=100;
		for(int i=0;i<maxHeight;i++){
			shapes.clear();
			shapes.add(new ConstrainableShape(getRect(150,0,i+150,100)));
			shapes.add(new ConstrainableShape(getRect(0,0,100,100)));
			roc.setParameters(shapes);
			System.out.println("Height: "+(i));
			System.out.println("Confidence: "+roc.solve());
		}
		
	}
}