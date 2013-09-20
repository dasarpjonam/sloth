package test.unit.ladder.recognition.constraint.confidence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.Point;
import org.ladder.recognition.constraint.constrainable.ConstrainablePoint;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.CoincidentConstraint;

import test.unit.SlothTest;

public class CoincidentConstraintTest extends SlothTest {


		CoincidentConstraint cc;
		
		@Before
		public void setup(){
			cc=null;
		}
		
		@Test
		public void testCoincidentSolveConstrainablePointConstrainablePoint() {
			CoincidentConstraint coincident = new CoincidentConstraint();
			CoincidentConstraint twoCoince = new CoincidentConstraint();
			
			double threshMult = 2;
			double thresh = twoCoince.getThreshold();
			thresh *= threshMult;
			twoCoince.setThreshold(thresh);
			
			IPoint origin = new Point(0, 0);
			ConstrainablePoint co = new ConstrainablePoint(origin, null);
			
			for (double x = 0; x < 50.0; x += 1) {
				IPoint other = new Point(x, 0);
				ConstrainablePoint cother = new ConstrainablePoint(other, null);
				
				double conf = coincident.solve(co, cother);
				double twoConf = twoCoince.solve(co, cother);
				
				System.out.println("Dists = " + other.distance(origin) + ", conf = "
				                 + conf + ", two conf = " + twoConf);
			}
		}
		
		@Test
		public void testCoincidentConstraint(){
			cc=new CoincidentConstraint();
			
			assertNotNull(cc);
			assertEquals(CoincidentConstraint.NAME,cc.getName());
			assertEquals(CoincidentConstraint.DEFAULT_THRESHOLD,cc.getThreshold(),0);
			assertEquals(CoincidentConstraint.DESCRIPTION,cc.getDescription());
			assertEquals(CoincidentConstraint.NUM_PARAMETERS,cc.getNumRequiredParameters());
		}
		
		@Test
		public void testSolve(){
			cc=new CoincidentConstraint();
			ArrayList<IConstrainable> shapes = new ArrayList<IConstrainable>();
			double confidence;
			
			//Range
			shapes.add(new ConstrainablePoint(0,0, null));
			shapes.add(new ConstrainablePoint(rand.nextDouble()*10,rand.nextDouble()*10, null));
			cc.setParameters(shapes);
			confidence=cc.solve();
			assertTrue(confidence>=0);
			System.out.println(confidence);
			assertTrue(confidence<=1);
			
			//Same Point
			shapes.clear();
			shapes.add(new ConstrainablePoint(0,0, null));
			shapes.add(new ConstrainablePoint(0,0, null));
			cc.setParameters(shapes);
			confidence=cc.solve();
			assertEquals(1,cc.solve(),.01);

		
			//Range of distances
			for(int i=1;i<=15;i++){
				shapes.clear();
				shapes.add(new ConstrainablePoint(0,0, null));
				shapes.add(new ConstrainablePoint(0,i, null));
				cc.setParameters(shapes);
				confidence=cc.solve();
				System.out.println("Distance "+i+" "+confidence);
				//			assertEquals(1,cc.solve(),.01);
			}
		}
	
	
}
