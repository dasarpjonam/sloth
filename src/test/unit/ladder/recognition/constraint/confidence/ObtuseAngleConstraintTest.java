package test.unit.ladder.recognition.constraint.confidence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ladder.recognition.constraint.constrainable.ConstrainableLine;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.ObtuseAngleConstraint;

import test.unit.SlothTest;
/*
 * @author Paul Corey
 */
public class ObtuseAngleConstraintTest extends SlothTest {

	public ObtuseAngleConstraint oac;
	
	@Before
	public void setup(){
		oac=new ObtuseAngleConstraint();
	}
	
	@After
	public void tearDown(){
		oac=null;
			
	}
	
	@Test
	public void testObtuseAngleConstraint() {
		oac = new ObtuseAngleConstraint();
		assertNotNull(oac);
		assertEquals(ObtuseAngleConstraint.DEFAULT_THRESHOLD,oac.getThreshold(),Math.pow(10,-10));
		assertEquals(ObtuseAngleConstraint.DESCRIPTION,oac.getDescription());
		assertEquals(ObtuseAngleConstraint.NAME,oac.getName());
		assertEquals(ObtuseAngleConstraint.NUM_PARAMETERS,oac.getNumRequiredParameters());
	}
	
	@Test
	public void testObtuseAngleConstraintDouble() {
		double t = rand.nextDouble();
		oac = new ObtuseAngleConstraint(t);
		assertNotNull(oac);
		assertEquals(t,oac.getThreshold(),Math.pow(10,-10));
		assertEquals(ObtuseAngleConstraint.DESCRIPTION,oac.getDescription());
		assertEquals(ObtuseAngleConstraint.NAME,oac.getName());
		assertEquals(ObtuseAngleConstraint.NUM_PARAMETERS,oac.getNumRequiredParameters());
		
		oac = new ObtuseAngleConstraint(-t);
		assertNotNull(oac);
		assertEquals(t,oac.getThreshold(),Math.pow(10,-10));
		assertEquals(ObtuseAngleConstraint.DESCRIPTION,oac.getDescription());
		assertEquals(ObtuseAngleConstraint.NAME,oac.getName());
		assertEquals(ObtuseAngleConstraint.NUM_PARAMETERS,oac.getNumRequiredParameters());
	}
	
	@Test
	public void testSolve() {
		List<IConstrainable> shapes = new ArrayList<IConstrainable>();
		oac = new ObtuseAngleConstraint();
		
		//No parameters
		oac.setParameters(shapes);
		assertEquals(0,oac.solve(),Math.pow(10,-10));
		
		//One parameter
		shapes.add(new ConstrainableLine(getLine(0,0,10,0)));
		oac.setParameters(shapes);
		assertEquals(0,oac.solve(),Math.pow(10,-10));
		
		//Two parameter
		//Reasonable range?
		shapes.add(new ConstrainableLine(getLine(0,0,(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)),(int)(rand.nextInt(10)*Math.signum(rand.nextDouble()-.5)))));
		oac.setParameters(shapes);
		assertTrue(oac.solve()>=0);
		assertTrue(oac.solve()<=1);
		
		double prevPosAngleConf=-1;
		double prevNegAngleConf=-1;

		//Lines have same first point, conceptually easy
		System.out.println("Same first point");
		for(int degrees=1;degrees<180;degrees++){
			if(degrees<90)
				System.out.println("Acute angle, should have relatively low confidence");
			if(degrees>90)
				System.out.println("Obtuse angle, should have relatively high confidence");
			if(degrees==90)
				System.out.println("At 90");
			shapes.clear();
			
			ConstrainableLine stableLine = new ConstrainableLine(getLine(0, 0,
			        100, 0));
			
			ConstrainableLine posDegLine = new ConstrainableLine(getLine(0, 0,
			        100 * Math.cos(degrees * Math.PI / 180), 100 * Math
			                .sin(-degrees * Math.PI / 180)));
			
			shapes.add(stableLine);
			shapes.add(posDegLine);
			
			oac.setParameters(shapes);
			double conf=oac.solve();
			System.out.println(stableLine.getAngleBetweenInDegrees(posDegLine) + " degree difference. Confidence: "+conf);
//			if(prevPosAngleConf>0)
//				assertTrue(prevPosAngleConf<conf);
			prevPosAngleConf=conf;
			
			ConstrainableLine negDegLine = new ConstrainableLine(getLine(0, 0,
			        100 * Math.cos(degrees * Math.PI / 180), 100 * Math
			                .sin(degrees * Math.PI / 180)));
			shapes.clear();
			shapes.add(stableLine);
			shapes.add(negDegLine);
			
			oac.setParameters(shapes);
			conf=oac.solve();
			System.out.println(-stableLine.getAngleBetweenInDegrees(negDegLine) + " degree difference. Confidence: "+conf);
//			if(prevNegAngleConf>0)
//				assertTrue(prevNegAngleConf<conf);
			prevNegAngleConf=conf;
			System.out.println();
		}
		
		/*
		 * Lines that don't intersect, you'll probably have to draw these to see
		 * I'm assuming that the angle we're measuring is the angle between
		 * the lines if we extend them to the point where they'd intersect
		 * Purposefully excluding cases where the intersection is on one
		 * of the lines
		 */
		System.out.println("Non-intersecting");
		prevNegAngleConf=-1;
		for(int degrees=-45;degrees>=-135;degrees--){
			if(degrees>-90){
				System.out.println("Acute angle, should have relatively low confidence");
				System.out.print(-degrees);
			}
			if(degrees<-90){
				System.out.println("Obtuse angle, should have relatively high confidence");
				System.out.print(-degrees);
			}
			if(degrees==-90){
				System.out.print("At 90");
			}
			shapes.clear();
			shapes.add(new ConstrainableLine(getLine(0,0,100,0)));
			shapes.add(new ConstrainableLine(getLine(200,0-100,200+100*Math.cos(degrees*Math.PI/180),100*Math.sin(degrees*Math.PI/180)-100)));
			oac.setParameters(shapes);
			double conf=oac.solve();
			System.out.println(" degree difference. Confidence: "+conf);
			if(prevPosAngleConf>0)
				assertTrue(prevPosAngleConf<conf);
			prevNegAngleConf=conf;
			System.out.println();
		}
		
		
	}

}
