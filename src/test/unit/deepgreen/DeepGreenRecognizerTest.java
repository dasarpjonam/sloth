package test.unit.deepgreen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.PatternSyntaxException;

import org.junit.Test;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.prior.ISymbolPrior;
import org.ladder.recognition.prior.SymbolPrior;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.IDeepGreenInterpretation;
import edu.tamu.deepGreen.recognition.IDeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.exceptions.LockedInterpretationException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchInterpretationException;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchStrokeException;

/**
 * JUnit test for our DeepGreenRecognizer class.
 * 
 * @author awolin
 */
public class DeepGreenRecognizerTest {
	
	/**
	 * Test that the initialization of the DeepGreenRecognizer creates non-null
	 * variables.
	 */
	@Test
	public void testDeepGreenRecognizer() throws IOException {
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Check that we have non-null variables
		assertNotNull(dgr.getUnlockedStrokes());
		assertNotNull(dgr.getLockedStrokes());
		
		assertNotNull(dgr.getUnlockedInterpretations());
		assertNotNull(dgr.getLockedInterpretations());
		
		assertNotNull(dgr.getSymbolPriors());
		
		// Check that we have emtpy lists
		assertEquals(0, dgr.getUnlockedStrokes().size());
		assertEquals(0, dgr.getLockedStrokes().size());
		
		assertEquals(0, dgr.getUnlockedInterpretations().size());
		assertEquals(0, dgr.getLockedInterpretations().size());
		
		assertEquals(0, dgr.getSymbolPriors().size());
	}
	

	/**
	 * Test adding an interpretation.
	 */
	@Test
	public void testAddInterpretation() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Strokes in DGR
		for (int i = 0; i < 10; i++) {
			IStroke stroke = new Stroke();
			stroke.addPoint(new Point(0, 0));
			
			dgr.addStroke(stroke);
			
			try {
				assertEquals(stroke, dgr.getStroke(stroke.getID()));
			}
			catch (NoSuchStrokeException nsse) {
				fail("Stroke was improperly added or gotten");
			}
		}
		
		// Strokes not in DGR
		List<IStroke> unadded = new ArrayList<IStroke>();
		
		for (int i = 0; i < 10; i++) {
			IStroke stroke = new Stroke();
			stroke.addPoint(new Point(0, 0));
			unadded.add(stroke);
		}
		
		// Test null pointer exceptions
		try {
			dgr.addInterpretation(null, DeepGreenSymbolPriorTest
			        .generateGoodSIDC());
			fail("Null stroke not caught");
		}
		catch (NullPointerException npe) {
			// Exception properly caught, continue...
		}
		catch (PatternSyntaxException e) {
			fail("Null strokes should be caught first");
		}
		catch (NoSuchStrokeException e) {
			fail("Null strokes should be caught first");
		}
		catch (LockedInterpretationException e) {
			fail("Null strokes should be caught first");
		}
		
		// Test null pointer exceptions
		try {
			dgr.addInterpretation(dgr.getUnlockedStrokes(), null);
			fail("Null SIDC not caught");
		}
		catch (NullPointerException npe) {
			// Exception properly caught, continue...
		}
		catch (PatternSyntaxException e) {
			fail("Null SIDC should be caught first");
		}
		catch (NoSuchStrokeException e) {
			fail("Null SIDC should be caught first");
		}
		catch (LockedInterpretationException e) {
			fail("Null SIDC should be caught first");
		}
		
		// Test PatternSyntaxExceptions
		try {
			dgr.addInterpretation(dgr.getUnlockedStrokes(),
			        DeepGreenSymbolPriorTest.generateBadSIDC());
			fail("Improprerly formatted SIDC not caught");
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			// Exception properly caught, continue...
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
		catch (LockedInterpretationException e) {
			fail("Nothing should be locked");
		}
		
		// Test NoSuchStrokeExceptions
		try {
			dgr.addInterpretation(unadded, DeepGreenSymbolPriorTest
			        .generateGoodSIDC());
			fail("Interpretation created with unadded strokes");
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			// Exception properly caught, continue...
		}
		catch (LockedInterpretationException e) {
			fail("Nothing should be locked");
		}
		
		// Test that should complete
		IDeepGreenInterpretation added = null;
		
		try {
			added = dgr.addInterpretation(dgr.getUnlockedStrokes(),
			        DeepGreenSymbolPriorTest.generateGoodSIDC());
			assertEquals(added, dgr.getInterpretation(added.getID()));
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
		catch (LockedInterpretationException e) {
			fail("Nothing should be locked");
		}
		catch (NoSuchInterpretationException e) {
			fail("Interpretation should exist");
		}
		
		// Test LockedInterpretationsException
		try {
			dgr.lockInterpretation(added);
			dgr.addInterpretation(dgr.getLockedStrokes(),
			        DeepGreenSymbolPriorTest.generateGoodSIDC());
			fail("Interpretation created with locked strokes");
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
		catch (LockedInterpretationException e) {
			// Exception properly caught, continue...
		}
		catch (NoSuchInterpretationException e) {
			fail("Interpretation should exist");
		}
		
	}
	

	/**
	 * Test adding a stroke.
	 */
	@Test
	public void testAddStroke() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		try {
			dgr.addStroke(null);
			fail("Null stroke not caught");
		}
		catch (NullPointerException npe) {
			// Exception properly caught, continue...
		}
		
		try {
			dgr.addStroke(new Stroke());
			fail("Emtpy stroke not caught");
		}
		catch (NullPointerException npe) {
			// Exception properly caught, continue...
		}
		
		IStroke stroke = new Stroke();
		stroke.addPoint(new Point(0, 0));
		
		dgr.addStroke(stroke);
		
		try {
			assertEquals(stroke, dgr.getStroke(stroke.getID()));
		}
		catch (NoSuchStrokeException nsse) {
			fail("Stroke was improperly added or gotten");
		}
	}
	

	/**
	 * Test that adding the symbol prior puts the prior onto the list.
	 */
	@Test
	public void testAddSymbolPrior() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Check for a NullPointerException
		try {
			dgr.addSymbolPrior(null);
			fail("Null symbol prior not caught");
		}
		catch (NullPointerException npe) {
			// Exception properly caught, continue...
		}
		
		for (int i = 0; i < 1000; i++) {
			ISymbolPrior toAdd = new SymbolPrior(DeepGreenSymbolPriorTest
			        .generateGoodSIDC(), Math.random());
			dgr.addSymbolPrior(toAdd);
			
			assertEquals(i + 1, dgr.getSymbolPriors().size());
			assertEquals(toAdd, dgr.getSymbolPriors().get(i));
		}
	}
	

	/**
	 * Test getting an interpretation.
	 */
	@Test
	public void testGetInterpretation() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Strokes in DGR
		for (int i = 0; i < 10; i++) {
			IStroke stroke = new Stroke();
			stroke.addPoint(new Point(0, 0));
			dgr.addStroke(stroke);
		}
		
		try {
			IDeepGreenInterpretation added = dgr.addInterpretation(dgr
			        .getUnlockedStrokes(), DeepGreenSymbolPriorTest
			        .generateGoodSIDC());
			
			assertEquals(added, dgr.getInterpretation(added.getID()));
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
		catch (LockedInterpretationException e) {
			fail("Nothing should be locked");
		}
		catch (NoSuchInterpretationException e) {
			fail("Interpretation should exist");
		}
	}
	

	/**
	 * Test getting locked interpretations.
	 */
	@Test
	public void testGetLockedInterpretations() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Strokes in DGR
		for (int i = 0; i < 10; i++) {
			IStroke stroke = new Stroke();
			stroke.addPoint(new Point(0, 0));
			dgr.addStroke(stroke);
		}
		
		try {
			IDeepGreenInterpretation locked = dgr.addInterpretation(dgr
			        .getUnlockedStrokes(), DeepGreenSymbolPriorTest
			        .generateGoodSIDC());
			dgr.lockInterpretation(locked);
			
			assertEquals(locked, dgr.getLockedInterpretations().get(0));
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
		catch (LockedInterpretationException e) {
			fail("Nothing should be locked");
		}
		catch (NoSuchInterpretationException e) {
			fail("Interpretation should exist");
		}
	}
	

	/**
	 * Test getting locked strokes.
	 */
	@Test
	public void testGetLockedStrokes() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Strokes in DGR
		for (int i = 0; i < 10; i++) {
			IStroke stroke = new Stroke();
			stroke.addPoint(new Point(0, 0));
			dgr.addStroke(stroke);
		}
		
		try {
			IDeepGreenInterpretation locked = dgr.addInterpretation(dgr
			        .getUnlockedStrokes(), DeepGreenSymbolPriorTest
			        .generateGoodSIDC());
			dgr.lockInterpretation(locked);
			
			assertEquals(locked.getStrokes(), dgr.getLockedStrokes());
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
		catch (LockedInterpretationException e) {
			fail("Nothing should be locked");
		}
		catch (NoSuchInterpretationException e) {
			fail("Interpretation should exist");
		}
	}
	

	/**
	 * Test getting a stroke.
	 */
	@Test
	public void testGetStroke() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		for (int i = 0; i < 1000; i++) {
			IStroke stroke = new Stroke();
			stroke.addPoint(new Point(0, 0));
			
			dgr.addStroke(stroke);
			
			try {
				assertEquals(stroke, dgr.getStroke(stroke.getID()));
			}
			catch (NoSuchStrokeException nsse) {
				fail("Stroke was improperly added or gotten");
			}
		}
	}
	

	/**
	 * Test that the prior list is updating correctly and that the get function
	 * is working.
	 */
	@Test
	public void testGetSymbolPriors() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		List<ISymbolPrior> addedPriors = dgr.getSymbolPriors();
		
		for (int i = 0; i < 1000; i++) {
			ISymbolPrior toAdd = new SymbolPrior(DeepGreenSymbolPriorTest
			        .generateGoodSIDC(), Math.random());
			dgr.addSymbolPrior(toAdd);
		}
		
		assertEquals(dgr.getSymbolPriors(), addedPriors);
	}
	

	/**
	 * Test getting unlocked interpretations.
	 */
	@Test
	public void testGetUnlockedInterpretations() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Strokes in DGR
		for (int i = 0; i < 10; i++) {
			IStroke stroke = new Stroke();
			stroke.addPoint(new Point(0, 0));
			dgr.addStroke(stroke);
		}
		
		try {
			IDeepGreenInterpretation unlocked = dgr.addInterpretation(dgr
			        .getUnlockedStrokes(), DeepGreenSymbolPriorTest
			        .generateGoodSIDC());
			
			assertEquals(unlocked, dgr.getUnlockedInterpretations().get(0));
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
		catch (LockedInterpretationException e) {
			fail("Nothing should be locked");
		}
	}
	

	/**
	 * Test getting unlocked strokes.
	 */
	@Test
	public void testGetUnlockedStrokes() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Strokes in DGR
		for (int i = 0; i < 10; i++) {
			IStroke stroke = new Stroke();
			stroke.addPoint(new Point(0, 0));
			dgr.addStroke(stroke);
		}
		
		try {
			IDeepGreenInterpretation unlocked = dgr.addInterpretation(dgr
			        .getUnlockedStrokes(), DeepGreenSymbolPriorTest
			        .generateGoodSIDC());
			
			assertEquals(unlocked.getStrokes(), dgr.getUnlockedStrokes());
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
		catch (LockedInterpretationException e) {
			fail("Nothing should be locked");
		}
	}
	

	/**
	 * Test locking an interpretation.
	 */
	@Test
	public void testLockInterpretationIDeepGreenInterpretation()
	        throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Strokes in DGR
		for (int i = 0; i < 10; i++) {
			IStroke stroke = new Stroke();
			stroke.addPoint(new Point(0, 0));
			dgr.addStroke(stroke);
		}
		
		try {
			IDeepGreenInterpretation locked = dgr.addInterpretation(dgr
			        .getUnlockedStrokes(), DeepGreenSymbolPriorTest
			        .generateGoodSIDC());
			dgr.lockInterpretation(locked);
			
			assertEquals(true, locked.isLocked());
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
		catch (LockedInterpretationException e) {
			fail("Nothing else should be locked");
		}
		catch (NoSuchInterpretationException e) {
			fail("Interpretation should exist");
		}
	}
	

	/**
	 * Test locking an interpretation.
	 */
	@Test
	public void testLockInterpretationUUID() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Strokes in DGR
		for (int i = 0; i < 10; i++) {
			IStroke stroke = new Stroke();
			stroke.addPoint(new Point(0, 0));
			dgr.addStroke(stroke);
		}
		
		IDeepGreenInterpretation locked = null;
		IDeepGreenInterpretation toLock = null;
		
		// Test NoSuchInterpretationException
		try {
			locked = dgr.addInterpretation(dgr.getUnlockedStrokes().subList(0,
			        1), DeepGreenSymbolPriorTest.generateGoodSIDC());
			toLock = dgr.addInterpretation(dgr.getUnlockedStrokes(),
			        DeepGreenSymbolPriorTest.generateGoodSIDC());
			
			dgr.lockInterpretation(UUID.randomUUID());
			
			fail("Interpretation shouldn't exist");
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Stroke should exist");
		}
		catch (LockedInterpretationException e) {
			fail("Nothing else should be locked");
		}
		catch (NoSuchInterpretationException e) {
			// Exception properly caught, continue...
		}
		
		// Test LockedInterpetationException and the lockInterpretation method
		try {
			dgr.lockInterpretation(locked.getID());
			dgr.lockInterpretation(toLock.getID());
			
			fail("Interpretation shouldn't lock");
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (LockedInterpretationException e) {
			// Exception properly caught, continue...
		}
		catch (NoSuchInterpretationException e) {
			fail("Interpretation should exist");
		}
	}
	

	/**
	 * Test partOf.
	 */
	@Test
	public void testPartOfIStroke() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Strokes in DGR
		List<IStroke> strokesToAdd = new ArrayList<IStroke>();
		
		for (int i = 0; i < 10; i++) {
			IStroke stroke = new Stroke();
			stroke.addPoint(new Point(0, 0));
			strokesToAdd.add(stroke);
			dgr.addStroke(stroke);
		}
		
		IDeepGreenInterpretation unlocked = null;
		
		// Add the interpretation
		try {
			unlocked = dgr.addInterpretation(dgr.getUnlockedStrokes(),
			        DeepGreenSymbolPriorTest.generateGoodSIDC());
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
		catch (LockedInterpretationException e) {
			fail("Nothing should be locked");
		}
		
		// Check for NoSuchStrokeExceptions
		try {
			@SuppressWarnings("unused")
			List<IDeepGreenInterpretation> partOfInterps = dgr
			        .partOf(new Stroke());
			
			fail("Stroke should not be known to the DGR");
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (NoSuchStrokeException e) {
			// Exception properly caught, continue...
		}
		
		// Test should complete
		try {
			List<IDeepGreenInterpretation> partOfInterps = dgr
			        .partOf(strokesToAdd.get(0));
			
			assertEquals(true, partOfInterps.contains(unlocked));
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
	}
	

	/**
	 * Test partOf.
	 */
	@Test
	public void testPartOfUUID() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Strokes in DGR
		List<IStroke> strokesToAdd = new ArrayList<IStroke>();
		
		for (int i = 0; i < 10; i++) {
			IStroke stroke = new Stroke();
			stroke.addPoint(new Point(0, 0));
			strokesToAdd.add(stroke);
			dgr.addStroke(stroke);
		}
		
		IDeepGreenInterpretation unlocked = null;
		
		// Add the interpretation
		try {
			unlocked = dgr.addInterpretation(dgr.getUnlockedStrokes(),
			        DeepGreenSymbolPriorTest.generateGoodSIDC());
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
		catch (LockedInterpretationException e) {
			fail("Nothing should be locked");
		}
		
		// Check for NoSuchStrokeExceptions
		try {
			@SuppressWarnings("unused")
			List<IDeepGreenInterpretation> partOfInterps = dgr
			        .partOf(new Stroke().getID());
			
			fail("Stroke should not be known to the DGR");
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (NoSuchStrokeException e) {
			// Exception properly caught, continue...
		}
		
		// Test should complete
		try {
			List<IDeepGreenInterpretation> partOfInterps = dgr
			        .partOf(strokesToAdd.get(0).getID());
			
			assertEquals(true, partOfInterps.contains(unlocked));
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
	}
	

	@Test
	public void testRecognize() {
		fail("Not yet implemented");
	}
	

	@Test
	public void testRecognizeListOfIStroke() {
		fail("Not yet implemented");
	}
	

	@Test
	public void testRecognizeSingleObject() {
		fail("Not yet implemented");
	}
	

	@Test
	public void testRecognizeSingleObjectListOfIStroke() {
		fail("Not yet implemented");
	}
	

	@Test
	public void testRemoveInterpretationUUID() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Test NoSuchStrokeExceptions
		try {
			dgr.removeInterpretation(UUID.randomUUID());
			fail("No interpretation should exist");
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (NoSuchInterpretationException e) {
			// Exception properly caught, continue...
		}
		catch (LockedInterpretationException e) {
			fail("Nothing should be locked");
		}
		
		IDeepGreenInterpretation interpretation = null;
		
		// Test LockedInterpretationException
		try {
			// Strokes in DGR
			for (int i = 0; i < 10; i++) {
				IStroke stroke = new Stroke();
				stroke.addPoint(new Point(0, 0));
				dgr.addStroke(stroke);
			}
			
			interpretation = dgr.addInterpretation(dgr.getUnlockedStrokes(),
			        DeepGreenSymbolPriorTest.generateGoodSIDC());
			
			dgr.lockInterpretation(interpretation);
			dgr.removeInterpretation(interpretation);
			
			fail("The interpretation should be locked");
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (NoSuchStrokeException e) {
			fail("The stroke should exist");
		}
		catch (LockedInterpretationException e) {
			// Exception properly caught, continue...
		}
		catch (NoSuchInterpretationException e) {
			fail("Interpretation should exist");
		}
		
		// Test should complete
		try {
			dgr.unlockInterpretation(interpretation);
			IDeepGreenInterpretation removed = dgr
			        .removeInterpretation(interpretation.getID());
			
			assertEquals(interpretation, removed);
			assertEquals(0, dgr.getUnlockedInterpretations().size());
			
			dgr.getInterpretation(removed.getID());
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (NoSuchInterpretationException e) {
			// Exception properly caught, continue...
		}
		catch (LockedInterpretationException e) {
			fail("Nothing should be locked");
		}
	}
	

	@Test
	public void testRemoveStrokeUUID() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Test NoSuchStrokeExceptions
		try {
			dgr.removeStroke(UUID.randomUUID());
			fail("No stroke should exist");
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (NoSuchStrokeException e) {
			// Exception properly caught, continue...
		}
		catch (LockedInterpretationException e) {
			fail("Nothing should be locked");
		}
		
		IDeepGreenInterpretation interpretation = null;
		
		// Test LockedInterpretationException
		try {
			// Strokes in DGR
			for (int i = 0; i < 10; i++) {
				IStroke stroke = new Stroke();
				stroke.addPoint(new Point(0, 0));
				dgr.addStroke(stroke);
			}
			
			interpretation = dgr.addInterpretation(dgr.getUnlockedStrokes(),
			        DeepGreenSymbolPriorTest.generateGoodSIDC());
			
			dgr.lockInterpretation(interpretation);
			
			dgr.removeStroke(dgr.getLockedStrokes().get(0));
			
			fail("The strokes should be locked");
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (NoSuchStrokeException e) {
			fail("The stroke should exist");
		}
		catch (LockedInterpretationException e) {
			// Exception properly caught, continue...
		}
		catch (NoSuchInterpretationException e) {
			fail("Interpretation should exist");
		}
		
		// Test should complete
		try {
			dgr.unlockInterpretation(interpretation);
			IStroke toRemove = dgr.getUnlockedStrokes().get(0);
			
			IStroke removed = dgr.removeStroke(toRemove.getID());
			
			assertEquals(toRemove, removed);
			assertEquals(0, dgr.getUnlockedInterpretations().size());
			
			dgr.getStroke(removed.getID());
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (NoSuchStrokeException e) {
			// Exception properly caught, continue...
		}
		catch (LockedInterpretationException e) {
			fail("Nothing should be locked");
		}
		catch (NoSuchInterpretationException e) {
			fail("Interpretation should exist");
		}
	}
	

	/**
	 * Test that the variables are being reset.
	 */
	@Test
	public void testReset() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		dgr.reset();
		
		// Check that we have non-null variables
		assertNotNull(dgr.getUnlockedStrokes());
		assertNotNull(dgr.getLockedStrokes());
		
		assertNotNull(dgr.getUnlockedInterpretations());
		assertNotNull(dgr.getLockedInterpretations());
		
		assertNotNull(dgr.getSymbolPriors());
		
		// Check that we have emtpy lists
		assertEquals(0, dgr.getUnlockedStrokes().size());
		assertEquals(0, dgr.getLockedStrokes().size());
		
		assertEquals(0, dgr.getUnlockedInterpretations().size());
		assertEquals(0, dgr.getLockedInterpretations().size());
		
		assertEquals(0, dgr.getSymbolPriors().size());
	}
	

	/**
	 * No test available, since the methods for scale are private.
	 */
	@Test
	public void testResetScale() {
		// Do nothing
	}
	

	/**
	 * Test that the symbol priors are being reset
	 */
	@Test
	public void testResetSymbolPriors() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Add 1000 priors
		for (int i = 0; i < 1000; i++) {
			ISymbolPrior toAdd = new SymbolPrior(DeepGreenSymbolPriorTest
			        .generateGoodSIDC(), Math.random());
			dgr.addSymbolPrior(toAdd);
		}
		
		assertEquals(1000, dgr.getSymbolPriors().size());
		
		// Reset the priors
		dgr.resetSymbolPriors();
		
		assertNotNull(dgr.getSymbolPriors());
		assertEquals(0, dgr.getSymbolPriors().size());
	}
	

	/**
	 * Test the exceptions that can be thrown. Cannot verify that the max time
	 * has been set correctly, since no getTime method is public.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSetMaxTime() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		int negInteger = (new Random().nextInt(Integer.MAX_VALUE - 1) + 1) * -1;
		dgr.setMaxTime(negInteger);
	}
	

	/**
	 * Test setting the scale. Can only test the exceptions, since there are no
	 * public methods to get the scale.
	 */
	@Test
	public void testSetScale() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		double minX = Math.random() * 100.0;
		double maxX = 200.0 + (Math.random() * 100.0);
		
		double minY = Math.random() * 100.0;
		double maxY = 200.0 + (Math.random() * 100.0);
		
		int panelWidth = 100;
		int panelHeight = 100;
		
		// Negative values testing
		try {
			dgr.setScale(minX * -1.0 - 1, minY, maxX, maxY, panelWidth,
			        panelHeight);
			fail("minX should be negative");
		}
		catch (IllegalArgumentException iae) {
			// Exception properly caught, continue...
		}
		
		try {
			dgr.setScale(minX, minY * -1.0 - 1, maxX, maxY, panelWidth,
			        panelHeight);
			fail("minY should be negative");
		}
		catch (IllegalArgumentException iae) {
			// Exception properly caught, continue...
		}
		
		try {
			dgr.setScale(minX, minY, maxX * -1.0 - 1, maxY, panelWidth,
			        panelHeight);
			fail("maxX should be negative");
		}
		catch (IllegalArgumentException iae) {
			// Exception properly caught, continue...
		}
		
		try {
			dgr.setScale(minX, minY, maxX, maxY * -1.0 - 1, panelWidth,
			        panelHeight);
			fail("maxY should be negative");
		}
		catch (IllegalArgumentException iae) {
			// Exception properly caught, continue...
		}
		
		// Swapped min/max testing
		try {
			dgr.setScale(maxX, minY, minX, maxY, panelWidth, panelHeight);
			fail("maxY should be greater than minX");
		}
		catch (IllegalArgumentException iae) {
			// Exception properly caught, continue...
		}
		
		try {
			dgr.setScale(minX, maxY, maxX, minY, panelWidth, panelHeight);
			fail("maxY should be negative");
		}
		catch (IllegalArgumentException iae) {
			// Exception properly caught, continue...
		}
		
		// Test should pass
		try {
			dgr.setScale(minX, minY, maxX, maxY, panelWidth, panelHeight);
		}
		catch (IllegalArgumentException iae) {
			fail("Nothing should be illegal");
		}
	}
	

	/**
	 * Test unlocking an interpretation.
	 */
	@Test
	public void testUnlockInterpretationIDeepGreenInterpretation()
	        throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Strokes in DGR
		for (int i = 0; i < 10; i++) {
			IStroke stroke = new Stroke();
			stroke.addPoint(new Point(0, 0));
			dgr.addStroke(stroke);
		}
		
		try {
			IDeepGreenInterpretation interpretation = dgr.addInterpretation(dgr
			        .getUnlockedStrokes(), DeepGreenSymbolPriorTest
			        .generateGoodSIDC());
			
			dgr.lockInterpretation(interpretation);
			assertEquals(true, interpretation.isLocked());
			
			dgr.unlockInterpretation(interpretation);
			assertEquals(false, interpretation.isLocked());
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
		catch (LockedInterpretationException e) {
			fail("Nothing else should be locked");
		}
		catch (NoSuchInterpretationException e) {
			fail("Interpretation should exist");
		}
	}
	

	/**
	 * Test unlocking an interpretation.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testUnlockInterpretationUUID() throws IOException {
		
		IDeepGreenRecognizer dgr = new DeepGreenRecognizer();
		
		// Strokes in DGR
		for (int i = 0; i < 10; i++) {
			IStroke stroke = new Stroke();
			stroke.addPoint(new Point(0, 0));
			dgr.addStroke(stroke);
		}
		
		try {
			IDeepGreenInterpretation interpretation = dgr.addInterpretation(dgr
			        .getUnlockedStrokes(), DeepGreenSymbolPriorTest
			        .generateGoodSIDC());
			
			dgr.lockInterpretation(interpretation.getID());
			assertEquals(true, interpretation.isLocked());
			
			dgr.unlockInterpretation(interpretation.getID());
			assertEquals(false, interpretation.isLocked());
		}
		catch (NullPointerException npe) {
			fail("Nothing should be null");
		}
		catch (PatternSyntaxException e) {
			fail("SIDC should be properly formatted");
		}
		catch (NoSuchStrokeException e) {
			fail("Interpretation strokes should be in the DGR");
		}
		catch (LockedInterpretationException e) {
			fail("Nothing else should be locked");
		}
		catch (NoSuchInterpretationException e) {
			fail("Interpretation should exist");
		}
	}
	
}
