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
import org.ladder.recognition.constraint.constrainable.ConstrainableShape;
import org.ladder.recognition.constraint.IConstrainable;
import org.ladder.recognition.constraint.confidence.EqualAngleConstraint;

import test.unit.SlothTest;

/**
 * 
 * 
 * @author awolin
 */
public class EqualAngleConstraintTest extends SlothTest {
	
	EqualAngleConstraint eac;
	
	
	@Before
	public void setUp() throws Exception {
		eac = null;
	}
	

	@After
	public void tearDown() throws Exception {
	}
	

	@Test
	public void testEqualAngleConstraint() {
		eac = new EqualAngleConstraint();
		
		assertNotNull(eac);
		assertEquals(EqualAngleConstraint.DEFAULT_THRESHOLD,
		        eac.getThreshold(), S_DEFAULT_DELTA);
		assertEquals(EqualAngleConstraint.NAME, eac.getName());
		assertEquals(EqualAngleConstraint.DESCRIPTION, eac.getDescription());
		assertEquals(EqualAngleConstraint.NUM_PARAMETERS, eac
		        .getNumRequiredParameters());
	}
	

	@Test
	public void testEqualAngleConstraintDouble() {
		double testThreshold = rand.nextDouble();
		eac = new EqualAngleConstraint(testThreshold);
		
		assertNotNull(eac);
		assertEquals(testThreshold, eac.getThreshold(), S_DEFAULT_DELTA);
		assertEquals(EqualAngleConstraint.NAME, eac.getName());
		assertEquals(EqualAngleConstraint.DESCRIPTION, eac.getDescription());
		assertEquals(EqualAngleConstraint.NUM_PARAMETERS, eac
		        .getNumRequiredParameters());
	}
	

	@Test
	public void testSolve() {
		
		eac = new EqualAngleConstraint();
		assertNotNull(eac);
		
		List<IConstrainable> lines = new ArrayList<IConstrainable>();
		
		// 0 Parameters - Expect 0.0 confidence
		eac.setParameters(lines);
		assertEquals(0.0, eac.solve(), S_DEFAULT_DELTA);
		
		// 1 Parameters - Expect 0.0 confidence
		lines.add(new ConstrainableLine(getLine(0, 0, rand.nextDouble(), 0)));
		eac.setParameters(lines);
		assertEquals(0.0, eac.solve(), S_DEFAULT_DELTA);
		
		// 2 Parameters - Expect 0.0 confidence
		lines.add(new ConstrainableLine(getLine(0, 0, 0, rand.nextDouble())));
		eac.setParameters(lines);
		assertEquals(0.0, eac.solve(), S_DEFAULT_DELTA);
		
		// 3 Parameters - Expect 0.0 confidence
		lines.add(new ConstrainableLine(getLine(0, 0, rand.nextDouble(), 0)));
		eac.setParameters(lines);
		assertEquals(0.0, eac.solve(), S_DEFAULT_DELTA);
		
		// 4 Parameters, 1 not a line - Expect 0.0 confidence
		lines.add(new ConstrainableShape(getRect(0, 0, 10, 10)));
		assertEquals(EqualAngleConstraint.NUM_PARAMETERS, lines.size());
		eac.setParameters(lines);
		assertEquals(0.0, eac.solve(), S_DEFAULT_DELTA);
		
		// 4 Parameters, all lines
		lines.remove(lines.size() - 1);
		lines.add(new ConstrainableLine(getLine(0, 0, 0, rand.nextDouble())));
		assertEquals(EqualAngleConstraint.NUM_PARAMETERS, lines.size());
		eac.setParameters(lines);
		
		double confidence = eac.solve();
		assertTrue(confidence >= 0.0 && confidence <= 1.0);
		
		int numTrials = 1;
		
		// Loop through this test a few times to ensure no odd behavior
		for (int i = 0; i < numTrials; i++) {
			
			System.out.println("Trial #" + i + "/" + numTrials);
			
			double prevConfidence = -1.0;
			
			eac = new EqualAngleConstraint();
			assertNotNull(eac);
			
			lines.clear();
			assertTrue(lines.size() == 0);
			
			// First pair of lines. Random starts and random ends
			ConstrainableLine line1 = new ConstrainableLine(getLine(0, 0,
			        rand.nextDouble() - rand.nextDouble(), rand.nextDouble()
			                                               - rand.nextDouble()));
			ConstrainableLine line2 = new ConstrainableLine(getLine(0, 0,
			        rand.nextDouble() - rand.nextDouble(), rand.nextDouble()
			                                               - rand.nextDouble()));
			
			double degree1 = line1.getAngleBetweenInDegrees(line2);
			
			// Random start points for other lines
			double randX3 = rand.nextDouble() - rand.nextDouble();
			double randY3 = rand.nextDouble() - rand.nextDouble();
			double randX4 = rand.nextDouble() - rand.nextDouble();
			double randY4 = rand.nextDouble() - rand.nextDouble();
			
			double prevDiff = Double.MAX_VALUE;
			
			// Generate controlled ends for the second pair
			for (int d = 0; d < 360; d++) {
				
				lines.clear();
				lines.add(line1);
				lines.add(line2);
				
				double x4 = Math.cos((double) d * (Math.PI / 180.0));
				double y4 = -Math.sin((double) d * (Math.PI / 180.0));
				
				ConstrainableLine line3 = new ConstrainableLine(getLine(0, 0,
				        1, 0));
				ConstrainableLine line4 = new ConstrainableLine(getLine(0, 0,
				        x4, y4));
				
				// System.out.println("d = " + d + " | " + x4 + ", " + y4);
				
				lines.add(line3);
				lines.add(line4);
				
				double degree2 = line3.getAngleBetweenInDegrees(line4);
				
				eac.setParameters(lines);
				confidence = eac.solve();
				
				 System.out.println("Degree 1 = " + degree1 + ", Degree 2 = "
				 + degree2 + ", Confidence = " + confidence);
				
				double diff = Math.abs(degree2 - degree1);
				
				if (diff < prevDiff) {
					assertTrue(confidence >= prevConfidence);
					//System.out.println("Degree 1 = " + degree1
					//                   + ", Degree 2 = " + degree2
					//                   + ", Confidence = " + confidence);
				}
				else if (diff > prevDiff) {
					assertTrue(confidence <= prevConfidence);
				}
				
				prevConfidence = confidence;
				prevDiff = diff;
			}
		}
	}
}
