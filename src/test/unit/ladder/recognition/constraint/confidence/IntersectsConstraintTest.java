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
import org.ladder.recognition.constraint.constrainable.ConstrainablePoint;
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.IntersectsConstraint;

import test.unit.SlothTest;

/**
 * @author pcorey
 *
 */
public class IntersectsConstraintTest extends SlothTest{

	IntersectsConstraint ic;
	
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
		ic=null;
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.IntersectsConstraint#IntersectsConstraint()}.
	 */
	@Test
	public void testIntersectsConstraint() {
		ic = new IntersectsConstraint();
		assertNotNull(ic);
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.IntersectsConstraint#IntersectsConstraint(double)}.
	 */
	@Test
	public void testIntersectsConstraintDouble() {
		double t = rand.nextDouble();
		ic = new IntersectsConstraint(t);
		assertNotNull(ic);
		assertEquals(t,ic.getThreshold(),0);
	}

	/**
	 * Test method for {@link org.ladder.recognition.constraint.confidence.IntersectsConstraint#solve()}.
	 */
	@Test
	public void testSolve() {
		ic = new IntersectsConstraint();
		ArrayList<IConstrainable> params = new ArrayList<IConstrainable>();
		
		ic.setParameters(params);
		assertEquals(0,ic.solve(),0);
		
		params.add(new ConstrainableShape(getRect()));
		ic.setParameters(params);
		assertEquals(0,ic.solve(),0);
		
		/*
		 * Point and Point
		 * Just calls CoincidentConstraint.  Run CoincidentConstraintTest to verify
		 * this functionality
		 */
		
		/*
		 * Point and Line
		 */
		double prevConf=-1;
		System.out.println("Along the line:");
		for(int i=-50;i<=150;i++){
			params.clear();
			params.add(new ConstrainablePoint(0,i,null));
			params.add(new ConstrainableLine(getLine(0,0,0,100)));
			ic.setParameters(params);
			double conf1=ic.solve();
			params.clear();
			params.add(new ConstrainableLine(getLine(0,0,0,100)));
			params.add(new ConstrainablePoint(0,i, null));
			ic.setParameters(params);
			double conf2=ic.solve();
			assertEquals(conf1,conf2,Math.pow(10,-10));
			if(i>0&&i<100)
				System.out.println("On the line.  Confidence: "+conf1);
			else if(i<=0)
				System.out.println(i+" from line.  Confidence: "+conf1);
			else
				System.out.println((i-100)+" from line.  Confidence: "+conf1);
			if(prevConf!=-1)
				if(i<=0)
					assertTrue(prevConf<conf1);
				else if(i<=100)
				assertEquals(prevConf,conf1,Math.pow(10, -5));
				else
					assertTrue(prevConf>conf1);
			prevConf=conf1;
		}

		prevConf=-1;
		System.out.println("Perpendicular to the line:");
		for(int i=-50;i<=50;i++){
			params.clear();
			params.add(new ConstrainablePoint(i,50, null));
			params.add(new ConstrainableLine(getLine(0,0,0,100)));
			ic.setParameters(params);
			double conf1=ic.solve();
			params.clear();
			params.add(new ConstrainableLine(getLine(0,0,0,100)));
			params.add(new ConstrainablePoint(i,50,null));
			ic.setParameters(params);
			double conf2=ic.solve();
			assertEquals(conf1,conf2,Math.pow(10,-10));
			System.out.println(i+" from line.  Confidence: "+conf1);
			if(prevConf!=-1)
				if(i<=0)
					assertTrue(prevConf<conf1);
				else
					assertTrue(prevConf>conf1);
			prevConf=conf1;
		}
		
		/*
		 * Point and Shape
		 */
		prevConf=-1;
		System.out.println("Vertically along shape:");
		for(int i=-50;i<=150;i++){
			params.clear();
			params.add(new ConstrainablePoint(0,i,null));
			params.add(new ConstrainableShape(getRect(0,0,0,100)));
			ic.setParameters(params);
			double conf1=ic.solve();
			params.clear();
			params.add(new ConstrainableShape(getRect(0,0,0,100)));
			params.add(new ConstrainablePoint(0,i,null));
			ic.setParameters(params);
			double conf2=ic.solve();
			assertEquals(conf1,conf2,Math.pow(10,-10));
			if(i>0&&i<100)
				System.out.println("On the shape.  Confidence: "+conf1);
			else if(i<=0)
				System.out.println(i+" from shape.  Confidence: "+conf1);
			else
				System.out.println((i-100)+" from shape.  Confidence: "+conf1);
			if(prevConf!=-1)
				if(i<=0)
					assertTrue(prevConf<conf1);
				else if(i<=100)
					assertEquals(prevConf,conf1,Math.pow(10, -5));
				else
					assertTrue(prevConf>conf1);
			prevConf=conf1;
		}

		prevConf=-1;
		System.out.println("Horizontally along shape:");
		for(int i=-50;i<=150;i++){
			params.clear();
			params.add(new ConstrainablePoint(i,0,null));
			params.add(new ConstrainableShape(getRect(0,0,0,100)));
			ic.setParameters(params);
			double conf1=ic.solve();
			params.clear();
			params.add(new ConstrainableShape(getRect(0,0,0,100)));
			params.add(new ConstrainablePoint(i,0,null));
			ic.setParameters(params);
			double conf2=ic.solve();
			assertEquals(conf1,conf2,Math.pow(10,-10));
			if(i>0&&i<100)
				System.out.println("On the shape.  Confidence: "+conf1);
			else if(i<=0)
				System.out.println(i+" from shape.  Confidence: "+conf1);
			else
				System.out.println((i-100)+" from shape.  Confidence: "+conf1);
			if(prevConf!=-1)
				if(i<=0)
					assertTrue(prevConf<conf1);
				else if(i<=100)
				assertEquals(prevConf,conf1,Math.pow(10, -5));
				else
					assertTrue(prevConf>conf1);
			prevConf=conf1;
		}
		
		/*
		 * Line and Line
		 */
		prevConf=-1;
		for(double degrees=0;degrees<360;degrees++){
			params.clear();
			params.add(new ConstrainableLine(getLine(0,0,100,0)));
			params.add(new ConstrainableLine(getLine(50,50,100*Math.cos(degrees*Math.PI/180)+50,100*Math.sin(degrees*Math.PI/180)+50)));
			ic.setParameters(params);
			double conf1=ic.solve();
			params.clear();
			params.add(new ConstrainableLine(getLine(50,50,100*Math.cos(degrees*Math.PI/180)+50,100*Math.sin(degrees*Math.PI/180)+50)));
			params.add(new ConstrainableLine(getLine(0,0,100,0)));
			ic.setParameters(params);
			double conf2=ic.solve();
			assertEquals(conf1,conf2,Math.pow(10,-10));
			if(degrees>=45+180&&degrees<=45+180+90)
				System.out.print("Intersects. ");
			System.out.println((360-degrees)+" degrees. Confidence: "+conf1);
			if(degrees>45+180&&degrees<=45+180+90)
				assertEquals(prevConf,conf1,.001);
			else if(degrees<=45+180)
				assertTrue(prevConf<=conf1);
			else
				assertTrue(prevConf>=conf1);
			prevConf=conf1;
		}
		
		/*
		 * Line and Shape
		 */
		prevConf=-1;
		for(double degrees=0;degrees<360;degrees++){
			params.clear();
			params.add(new ConstrainableShape(getRect(0,-100,100,0)));
			params.add(new ConstrainableLine(getLine(50,50,100*Math.cos(degrees*Math.PI/180)+50,100*Math.sin(degrees*Math.PI/180)+50)));
			ic.setParameters(params);
			double conf1=ic.solve();
			params.clear();
			params.add(new ConstrainableLine(getLine(50,50,100*Math.cos(degrees*Math.PI/180)+50,100*Math.sin(degrees*Math.PI/180)+50)));
			params.add(new ConstrainableShape(getRect(0,-100,100,0)));
			ic.setParameters(params);
			double conf2=ic.solve();
			assertEquals(conf1,conf2,Math.pow(10,-10));
			if(degrees>=45+180&&degrees<=45+180+90)
				System.out.print("Intersects. ");
			System.out.println((360-degrees)+" degrees. Confidence: "+conf1);
			if(degrees>45+180&&degrees<=45+180+90)
				assertEquals(prevConf,conf1,Math.pow(10, -5));
			else if(degrees<=45+180)
				assertTrue(prevConf<=conf1);
			else
				assertTrue(prevConf>=conf1);
			prevConf=conf1;
		}
		
		/*
		 * Shape and Shape
		 */
		prevConf=-1;
		for(double offset=-100;offset<=100;offset+=10){
			params.clear();
			params.add(new ConstrainableShape(getRect(0,0,50,50)));
			params.add(new ConstrainableShape(getRect(0+offset,0+offset,50+offset,50+offset)));
			ic.setParameters(params);
			double conf1=ic.solve();
			params.clear();
			params.add(new ConstrainableShape(getRect(0+offset,0+offset,50+offset,50+offset)));
			params.add(new ConstrainableShape(getRect(0,0,50,50)));
			ic.setParameters(params);
			double conf2=ic.solve();
			assertEquals(conf1,conf2,Math.pow(10,-10));
			if(Math.abs(offset)<=50)
				System.out.print("Intersects. ");
			System.out.println(offset+" offset.  Confidence: "+conf1);
			if(prevConf!=-1)
				if(offset<=0)
					assertTrue(prevConf<conf1);
				else
					assertTrue(prevConf>conf1);
		}
	}

}
