package test.unit.ladder.recognition.constraint.confidence;

import java.util.List;
import java.util.ArrayList;

import org.ladder.recognition.constraint.confidence.ContainsConstraint;
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;
import org.ladder.recognition.constraint.IConstrainable;

import test.unit.SlothTest;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class ContainsConstraintTest extends SlothTest {
	
	public ContainsConstraint cc;

	@Before
	public void setup(){
		cc=null;
	}
	
	@After
	public void tearDown(){
		
	}
	
	@Test
	public void testContainsConstraint() {
		cc = new ContainsConstraint();
		assertNotNull(cc);
		assertEquals(ContainsConstraint.DEFAULT_THRESHOLD,cc.getThreshold(),Math.pow(10,-10));
		assertEquals(ContainsConstraint.DESCRIPTION,cc.getDescription());
		assertEquals(ContainsConstraint.NAME,cc.getName());
	}

	@Test
	public void testContainsConstraintDouble() {
		double t = 10;
		cc = new ContainsConstraint(t);
		assertNotNull(cc);
		assertEquals(t,cc.getThreshold(),Math.pow(10,-10));
		assertEquals(ContainsConstraint.DESCRIPTION,cc.getDescription());
		assertEquals(ContainsConstraint.NAME,cc.getName());

		t = 640;
		cc = new ContainsConstraint(t);
		assertNotNull(cc);
		assertEquals(t,cc.getThreshold(),Math.pow(10,-10));
		assertEquals(ContainsConstraint.DESCRIPTION,cc.getDescription());
		assertEquals(ContainsConstraint.NAME,cc.getName());
	}

	@Test
	public void testSolveListShape() {
		cc = new ContainsConstraint();
		List<IConstrainable> shapes;
		
		// ArrayList
		// Empty list
		shapes = new ArrayList<IConstrainable>();
		assertEquals(0, cc.solve(shapes), Math.pow(10, -10));
		
		//Single Shape
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		assertEquals(0, cc.solve(shapes), Math.pow(10, -10));

		//Two Shapes
		//Definitely contained, should have high confidence
		shapes.add(new ConstrainableShape(getRect(10,10,20,20)));
		assertTrue(cc.solve(shapes) >= 0);
		assertTrue(cc.solve(shapes) <= 1);
		assertTrue(cc.solve(shapes) >= .2);
		assertTrue(cc.solve(shapes) >= .4);
		assertTrue(cc.solve(shapes) >= .6);
		assertTrue(cc.solve(shapes) >= .8);
		assertEquals(1,cc.solve(shapes),.01);
		double c = cc.solve(shapes);
		
		//Shared edges
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(10,10,300,20)));
		assertTrue(cc.solve(shapes) >= 0);
		assertTrue(cc.solve(shapes) <= 1);
		assertTrue(cc.solve(shapes) >= .2);
		assertTrue(cc.solve(shapes) >= .4);
		assertTrue(cc.solve(shapes) >= .6);
		assertTrue(cc.solve(shapes) >= .8);
		assertTrue(cc.solve(shapes)<=c);
		c = cc.solve(shapes);
		
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(10,10,300,300)));
		assertTrue(cc.solve(shapes) >= 0);
		assertTrue(cc.solve(shapes) <= 1);
		assertTrue(cc.solve(shapes) >= .2);
		assertTrue(cc.solve(shapes) >= .4);
		assertTrue(cc.solve(shapes) >= .6);
		assertTrue(cc.solve(shapes) >= .8);
		assertTrue(cc.solve(shapes)<=c);
		c = cc.solve(shapes);

		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(10,0,300,300)));
		assertTrue(cc.solve(shapes) >= 0);
		assertTrue(cc.solve(shapes) <= 1);
		assertTrue(cc.solve(shapes) >= .2);
		assertTrue(cc.solve(shapes) >= .4);
		assertTrue(cc.solve(shapes) >= .6);
		assertTrue(cc.solve(shapes) >= .8);
		assertTrue(cc.solve(shapes)<=c);
		c = cc.solve(shapes);

		//Same shape
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		assertTrue(cc.solve(shapes) >= 0);
		assertTrue(cc.solve(shapes) <= 1);
		assertTrue(cc.solve(shapes) >= .2);
		assertTrue(cc.solve(shapes) >= .4);
		assertTrue(cc.solve(shapes) >= .6);
		assertTrue(cc.solve(shapes)<=c);
		c = cc.solve(shapes);

		//Extends past
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(0,0,300,330)));
		assertTrue(cc.solve(shapes) >= 0);
		assertTrue(cc.solve(shapes) <= 1);
		assertTrue(cc.solve(shapes) <= .8);
		//assertTrue(cc.solve(shapes) <= .6);
		//assertTrue(cc.solve(shapes) <= .4);
		assertTrue(cc.solve(shapes)<c);
		c = cc.solve(shapes);

		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(0,0,330,330)));
		assertTrue(cc.solve(shapes) >= 0);
		assertTrue(cc.solve(shapes) <= 1);
		assertTrue(cc.solve(shapes) <= .8);
		assertTrue(cc.solve(shapes) <= .6);
		//assertTrue(cc.solve(shapes) <= .4);
		assertTrue(cc.solve(shapes)<c);
		c= cc.solve(shapes);

		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(0,-10,330,330)));
		assertTrue(cc.solve(shapes) >= 0);
		assertTrue(cc.solve(shapes) <= 1);
		assertTrue(cc.solve(shapes) <= .8);
		assertTrue(cc.solve(shapes) <= .6);
		assertTrue(cc.solve(shapes) <= .4);
		//assertTrue(cc.solve(shapes) <= .2);
		assertTrue(cc.solve(shapes)<c);
		c= cc.solve(shapes);

		//2 encloses 1
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(-10,-10,330,330)));
		assertTrue(cc.solve(shapes) >= 0);
		assertTrue(cc.solve(shapes) <= 1);
		assertTrue(cc.solve(shapes) <= .8);
		assertTrue(cc.solve(shapes) <= .6);
		assertTrue(cc.solve(shapes) <= .4);
		//assertTrue(cc.solve(shapes) <= .2);
		//assertEquals(0,cc.solve(shapes), .01);
		double c1 = cc.solve(shapes);
		
		//2 outside 1
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(330,330,640,640)));
		assertTrue(cc.solve(shapes) >= 0);
		assertTrue(cc.solve(shapes) <= 1);
		assertTrue(cc.solve(shapes) <= .8);
		assertTrue(cc.solve(shapes) <= .6);
		assertTrue(cc.solve(shapes) <= .4);
		assertTrue(cc.solve(shapes) <= .2);
		assertEquals(0,cc.solve(shapes), .01);
	}

	@Test
	public void testSolve() {
		cc = new ContainsConstraint();
		List<IConstrainable> shapes;
		
		// ArrayList
		// Empty list
		shapes = new ArrayList<IConstrainable>();
		cc.setParameters(shapes);
		assertEquals(0, cc.solve(), Math.pow(10, -10));
		
		System.out.println("Same center, small grows out");
		double prevConf=-1;
		for(double size=0;size<=100;size+=10){
			shapes.clear();
			shapes.add(new ConstrainableShape(getRect(0,0,50,50)));
			shapes.add(new ConstrainableShape(getRect(25-size/2,25-size/2,25+size/2,25+size/2)));
			cc.setParameters(shapes);
			double conf=cc.solve();
			if(prevConf!=-1)
				assertTrue(prevConf<=conf);
			System.out.println(size+" size, same center.  Confidence: "+conf);
		}
		
		System.out.println("Same upper left, small grows out");
		prevConf=-1;
		for(double size=0;size<=100;size+=10){
			shapes.clear();
			shapes.add(new ConstrainableShape(getRect(0,0,50,50)));
			shapes.add(new ConstrainableShape(getRect(0,0,size,size)));
			cc.setParameters(shapes);
			double conf=cc.solve();
			if(prevConf!=-1)
				assertTrue(prevConf<=conf);
			System.out.println(size+" size.  Confidence: "+conf);
		}

		System.out.println("Same top, small grows out");
		prevConf=-1;
		for(double size=0;size<=100;size+=10){
			shapes.clear();
			shapes.add(new ConstrainableShape(getRect(0,0,50,50)));
			shapes.add(new ConstrainableShape(getRect(0,0,50,size)));
			cc.setParameters(shapes);
			double conf=cc.solve();
			if(prevConf!=-1)
				assertTrue(prevConf<=conf);
			System.out.println(size+" size. Confidence: "+conf);
		}
		shapes.clear();
		//Single Shape
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		cc.setParameters(shapes);
		assertEquals(0, cc.solve(), Math.pow(10, -10));
		
		//Two Shapes
		//Definitely contained, should have high confidence
		shapes.add(new ConstrainableShape(getRect(10,10,20,20)));
		cc.setParameters(shapes);
		assertTrue(cc.solve() >= 0);
		assertTrue(cc.solve() <= 1);
		System.out.println(cc.solve());
		assertTrue(cc.solve() >= .2);
		assertTrue(cc.solve() >= .4);
		assertTrue(cc.solve() >= .6);
		assertTrue(cc.solve() >= .8);
		assertEquals(1,cc.solve(),.01);
		double c = cc.solve();
		
		//Shared edges
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(10,10,300,20)));
		cc.setParameters(shapes);
		assertTrue(cc.solve() >= 0);
		assertTrue(cc.solve() <= 1);
		assertTrue(cc.solve() >= .2);
		assertTrue(cc.solve() >= .4);
		assertTrue(cc.solve() >= .6);
		assertTrue(cc.solve() >= .8);
		assertTrue(cc.solve()<=c);
		c = cc.solve();
		
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(10,10,300,300)));
		cc.setParameters(shapes);
		assertTrue(cc.solve() >= 0);
		assertTrue(cc.solve() <= 1);
		assertTrue(cc.solve() >= .2);
		assertTrue(cc.solve() >= .4);
		assertTrue(cc.solve() >= .6);
		assertTrue(cc.solve() >= .8);
		assertTrue(cc.solve()<=c);
		c = cc.solve();

		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(10,0,300,300)));
		cc.setParameters(shapes);
		assertTrue(cc.solve() >= 0);
		assertTrue(cc.solve() <= 1);
		assertTrue(cc.solve() >= .2);
		assertTrue(cc.solve() >= .4);
		assertTrue(cc.solve() >= .6);
		assertTrue(cc.solve() >= .8);
		assertTrue(cc.solve()<=c);
		c = cc.solve();

		//Same shape
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		cc.setParameters(shapes);
		assertTrue(cc.solve() >= 0);
		assertTrue(cc.solve() <= 1);
		assertTrue(cc.solve() >= .2);
		assertTrue(cc.solve() >= .4);
		assertTrue(cc.solve() >= .6);
		assertTrue(cc.solve()<=c);
		c = cc.solve();

		//Extends past
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(0,0,300,330)));
		cc.setParameters(shapes);
		assertTrue(cc.solve() >= 0);
		assertTrue(cc.solve() <= 1);
		assertTrue(cc.solve() <= .8);
		//assertTrue(cc.solve() <= .6);
		//assertTrue(cc.solve() <= .4);
		assertTrue(cc.solve()<c);
		c = cc.solve();

		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(0,0,330,330)));
		cc.setParameters(shapes);
		assertTrue(cc.solve() >= 0);
		assertTrue(cc.solve() <= 1);
		assertTrue(cc.solve() <= .8);
		assertTrue(cc.solve() <= .6);
		//assertTrue(cc.solve() <= .4);
		assertTrue(cc.solve()<c);
		c= cc.solve();

		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(0,-10,330,330)));
		cc.setParameters(shapes);
		assertTrue(cc.solve() >= 0);
		assertTrue(cc.solve() <= 1);
		assertTrue(cc.solve() <= .8);
		assertTrue(cc.solve() <= .6);
		assertTrue(cc.solve() <= .4);
		//assertTrue(cc.solve() <= .2);
		assertTrue(cc.solve()<c);
		c= cc.solve();

		//2 encloses 1
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(-10,-10,330,330)));
		cc.setParameters(shapes);
		assertTrue(cc.solve() >= 0);
		assertTrue(cc.solve() <= 1);
		assertTrue(cc.solve() <= .8);
		assertTrue(cc.solve() <= .6);
		assertTrue(cc.solve() <= .4);
		//assertTrue(cc.solve() <= .2);
		//assertEquals(0,cc.solve(), .01);
		double c1 = cc.solve();
		
		//2 outside 1
		shapes = new ArrayList<IConstrainable>();
		shapes.add(new ConstrainableShape(getRect(0,0,300,300)));
		shapes.add(new ConstrainableShape(getRect(330,330,640,640)));
		cc.setParameters(shapes);
		assertTrue(cc.solve() >= 0);
		assertTrue(cc.solve() <= 1);
		assertTrue(cc.solve() <= .8);
		assertTrue(cc.solve() <= .6);
		assertTrue(cc.solve() <= .4);
		assertTrue(cc.solve() <= .2);
		assertEquals(0,cc.solve(), .01);
	}

	@Test
	public void solveIConstrainableIConstrainable() {
		cc = new ContainsConstraint();
		double c;
		ConstrainableShape shape1,shape2;
		
		//Definitely contained, should have high confidence
		shape1=(new ConstrainableShape(getRect(0,0,300,300)));
		shape2=(new ConstrainableShape(getRect(10,10,20,20)));
		assertTrue(cc.solve(shape1, shape2) >= 0);
		assertTrue(cc.solve(shape1, shape2) <= 1);
		assertTrue(cc.solve(shape1, shape2) >= .2);
		assertTrue(cc.solve(shape1, shape2) >= .4);
		assertTrue(cc.solve(shape1, shape2) >= .6);
		assertTrue(cc.solve(shape1, shape2) >= .8);
		
		//Shared edges, confidence should be somewhat lower
		shape1=(new ConstrainableShape(getRect(0,0,300,300)));
		shape2=(new ConstrainableShape(getRect(10,10,300,20)));
		assertTrue(cc.solve(shape1, shape2) >= 0);
		assertTrue(cc.solve(shape1, shape2) <= 1);
		assertTrue(cc.solve(shape1, shape2) >= .2);
		assertTrue(cc.solve(shape1, shape2) >= .4);
		assertTrue(cc.solve(shape1, shape2) >= .6);
		
		shape1=(new ConstrainableShape(getRect(0,0,300,300)));
		shape2=(new ConstrainableShape(getRect(10,10,300,300)));
		assertTrue(cc.solve(shape1, shape2) >= 0);
		assertTrue(cc.solve(shape1, shape2) <= 1);
		assertTrue(cc.solve(shape1, shape2) >= .2);
		assertTrue(cc.solve(shape1, shape2) >= .4);
		assertTrue(cc.solve(shape1, shape2) >= .6);

		shape1=(new ConstrainableShape(getRect(0,0,300,300)));
		shape2=(new ConstrainableShape(getRect(10,0,300,300)));
		assertTrue(cc.solve(shape1, shape2) >= 0);
		assertTrue(cc.solve(shape1, shape2) <= 1);
		assertTrue(cc.solve(shape1, shape2) >= .2);
		assertTrue(cc.solve(shape1, shape2) >= .4);
		assertTrue(cc.solve(shape1, shape2) >= .6);

		//Same shape
		shape1=(new ConstrainableShape(getRect(0,0,300,300)));
		shape2=(new ConstrainableShape(getRect(0,0,300,300)));
		assertTrue(cc.solve(shape1, shape2) >= 0);
		assertTrue(cc.solve(shape1, shape2) <= 1);
		assertTrue(cc.solve(shape1, shape2) >= .2);
		assertTrue(cc.solve(shape1, shape2) >= .4);

		//Extends past
		shape1=(new ConstrainableShape(getRect(0,0,300,300)));
		shape2=(new ConstrainableShape(getRect(0,0,300,330)));
		assertTrue(cc.solve(shape1, shape2) >= 0);
		assertTrue(cc.solve(shape1, shape2) <= 1);
		//assertTrue(cc.solve(shape1, shape2) <= .6);
		//assertTrue(cc.solve(shape1, shape2) <= .4);

		shape1=(new ConstrainableShape(getRect(0,0,300,300)));
		shape2=(new ConstrainableShape(getRect(0,0,330,330)));
		assertTrue(cc.solve(shape1, shape2) >= 0);
		assertTrue(cc.solve(shape1, shape2) <= 1);
		assertTrue(cc.solve(shape1, shape2) <= .6);
		//assertTrue(cc.solve(shape1, shape2) <= .4);

		shape1=(new ConstrainableShape(getRect(0,0,300,300)));
		shape2=(new ConstrainableShape(getRect(0,-10,330,330)));
		assertTrue(cc.solve(shape1, shape2) >= 0);
		assertTrue(cc.solve(shape1, shape2) <= 1);
		assertTrue(cc.solve(shape1, shape2) <= .6);
		assertTrue(cc.solve(shape1, shape2) <= .4);
		//assertTrue(cc.solve(shape1, shape2) <= .2);

		//2 encloses 1
		shape1=(new ConstrainableShape(getRect(0,0,300,300)));
		shape2=(new ConstrainableShape(getRect(-10,-10,330,330)));
		assertTrue(cc.solve(shape1, shape2) >= 0);
		assertTrue(cc.solve(shape1, shape2) <= 1);
		assertTrue(cc.solve(shape1, shape2) <= .6);
		assertTrue(cc.solve(shape1, shape2) <= .4);
		//assertTrue(cc.solve(shape1, shape2) <= .2);
		//assertEquals(0,cc.solve(shape1, shape2), .01);
		
		shape1=(new ConstrainableShape(getRect(0,0,300,300)));
		shape2=(new ConstrainableShape(getRect(-1,-1,301,301)));
		assertTrue(cc.solve(shape1, shape2) >= 0);
		assertTrue(cc.solve(shape1, shape2) <= 1);
		assertTrue(cc.solve(shape1, shape2) <= .6);
		//assertTrue(cc.solve(shape1, shape2) <= .4);
		//assertTrue(cc.solve(shape1, shape2) <= .2);
		//assertEquals(0,cc.solve(shape1, shape2), .01);

		//2 outside 1
		shape1 =(new ConstrainableShape(getRect(0,0,300,300)));
		shape2 = (new ConstrainableShape(getRect(330,330,640,640)));
		assertTrue(cc.solve(shape1, shape2) >= 0);
		assertTrue(cc.solve(shape1, shape2) <= 1);
		assertTrue(cc.solve(shape1, shape2) <= .6);
		assertTrue(cc.solve(shape1, shape2) <= .4);
		assertTrue(cc.solve(shape1, shape2) <= .2);
		assertEquals(0,cc.solve(shape1, shape2), .01);

		shape1 =(new ConstrainableShape(getRect(0,0,300,300)));
		shape2 = (new ConstrainableShape(getRect(30,330,230,640)));
		assertTrue(cc.solve(shape1, shape2) >= 0);
		assertTrue(cc.solve(shape1, shape2) <= 1);
		assertTrue(cc.solve(shape1, shape2) <= .6);
		assertTrue(cc.solve(shape1, shape2) <= .4);
		assertTrue(cc.solve(shape1, shape2) <= .2);
		assertEquals(0,cc.solve(shape1, shape2), .01);
	}
}
