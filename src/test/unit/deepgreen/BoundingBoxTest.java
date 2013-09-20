package test.unit.deepgreen;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.junit.Test;
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;

import edu.tamu.deepGreen.recognition.DeepGreenInterpretation;
import edu.tamu.deepGreen.recognition.IDeepGreenInterpretation;
import edu.tamu.deepGreen.recognition.exceptions.ControlPointNotSetException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchControlPointException;

public class BoundingBoxTest {
	
	/**
	 * This class exists only to allow us to create DeepGreenInterpretation
	 * instances for a unit test.
	 */
	private static class CreatableDeepGreenInterpretation extends
	        DeepGreenInterpretation {
		
		protected CreatableDeepGreenInterpretation(final List<IStroke> strokes,
		        final String sidc, final double confidence)
		        throws PatternSyntaxException, IllegalArgumentException,
		        NullPointerException {
			super(strokes, sidc, confidence);
		}
		
	}
	
	
	/**
	 * This checks that the bounding box is correct when a point is at 0.0.
	 * 
	 * @throws NoSuchControlPointException
	 * @throws ControlPointNotSetException
	 */
	@Test
	public void interpretationMethods() throws NoSuchControlPointException,
	        ControlPointNotSetException {
		final String expectedSidc = "SFGAUCATM-**--*";
		
		final Point expectedPoint1 = new Point();
		expectedPoint1.setX(0.0);
		expectedPoint1.setY(0.0);
		final Point expectedPoint2 = new Point();
		expectedPoint2.setX(10.0);
		expectedPoint2.setY(10.0);
		
		final IStroke stroke1 = new Stroke();
		stroke1.addPoint(expectedPoint1);
		stroke1.addPoint(expectedPoint2);
		
		final List<IStroke> expectedStrokes = new ArrayList<IStroke>();
		expectedStrokes.add(stroke1);
		
		final IDeepGreenInterpretation interp1 = new CreatableDeepGreenInterpretation(
		        expectedStrokes, expectedSidc, 1.0);
		
		final Rectangle2D.Double bb = interp1.getBoundingBox();
		assertNotNull(bb);
		assertTrue(bb.getMinX() == expectedPoint1.getX());
		assertTrue(bb.getMaxX() == expectedPoint2.getX());
	}
	

	@Test
	public void boundingBoxMethods() {
		
		final Point expectedPoint1 = new Point();
		expectedPoint1.setX(0.0);
		expectedPoint1.setY(0.0);
		final Point expectedPoint2 = new Point();
		expectedPoint2.setX(10.0);
		expectedPoint2.setY(10.0);
		
		final IStroke stroke1 = new Stroke();
		stroke1.addPoint(expectedPoint1);
		stroke1.addPoint(expectedPoint2);
		
		final List<IStroke> expectedStrokes1 = new ArrayList<IStroke>();
		expectedStrokes1.add(stroke1);
		
		final BoundingBox bb1 = new BoundingBox(expectedStrokes1);
		assertTrue(bb1.getMinX() == expectedPoint1.getX());
		assertTrue(bb1.getMaxX() == expectedPoint2.getX());
	}
	
}
