package test.unit.deepgreen;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.IDeepGreenRecognizer;

public class OverTimeThreadsTest {
	
	@Test
	public void checkThreads() throws IOException {
		
		// Create the strokes for the test.
		final double top = 100.0;
		final double bottom = 300.0;
		final double left = 100.0;
		final double right = 400.0;
		
		final IStroke rectStroke = new Stroke();
		rectStroke.addPoint(new Point(top, left));
		rectStroke.addPoint(new Point(bottom, left));
		rectStroke.addPoint(new Point(bottom, right));
		rectStroke.addPoint(new Point(top, right));
		rectStroke.addPoint(new Point(top, left));
		
		final IStroke lineStroke = new Stroke();
		lineStroke.addPoint(new Point(bottom, left));
		lineStroke.addPoint(new Point(top, right));
		
		final int initialActiveThreads = Thread.activeCount();
		
		// Create the recognizer.
		final IDeepGreenRecognizer recognizer = new DeepGreenRecognizer();
		
		int failedCount = 0;
		for (int i = 0; i < 100; ++i) {
			// Get ready for a fresh symbol.
			recognizer.reset();
			
			// Make the recognition time stupidly short in order to trigger
			// OverTimeExceptions. This is done here because I'm not sure if
			// reset
			// overrides this time.
			recognizer.setMaxTime(10);
			
			// Add the strokes.
			recognizer.addStroke(rectStroke);
			recognizer.addStroke(lineStroke);
			
			try {
				recognizer.recognizeSingleObject();
				// Somehow we recognized even within the timeout.
			}
			catch (final OverTimeException ote) {
				// We expect this because we made the timeout so short.
			}
			
			final int activeThreadsAfterRecognition = Thread.activeCount();
			// The first time through, this fails with one extra thread.
			// This fails, let's just print a message instead.
			// assertEquals(initialActiveThreads,
			// activeThreadsAfterRecognition);
			if (activeThreadsAfterRecognition != initialActiveThreads) {
				System.out.println("We had " + activeThreadsAfterRecognition
				                   + " instead of " + initialActiveThreads);
				++failedCount;
			}
		}
		
		// We should never have had an extra thread.
		assertEquals(0, failedCount);
	}
	
}
