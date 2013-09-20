/**
 * DeepGreenRecognizer_ExtraThreads.java
 * 
 * Revision History:<br>
 * Feb 28, 2009 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package test.unit.deepgreen;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.IDeepGreenRecognizer;

/**
 * 
 * @author jbjohns
 */
public class DeepGreenRecognizer_ExtraThreads {
	
	@Test
	public void checkThreads() throws IOException {
		
		// Create the strokes for the test.
		final double top = 100.0;
		final double bottom = 300.0;
		final double left = 100.0;
		final double right = 400.0;
		
		final IStroke rectStroke = new Stroke();
		rectStroke.addPoint(createPoint(top, left));
		rectStroke.addPoint(createPoint(bottom, left));
		rectStroke.addPoint(createPoint(bottom, right));
		rectStroke.addPoint(createPoint(top, right));
		rectStroke.addPoint(createPoint(top, left));
		
		final IStroke lineStroke = new Stroke();
		lineStroke.addPoint(createPoint(bottom, left));
		lineStroke.addPoint(createPoint(top, right));
		
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
			Assert.assertEquals(initialActiveThreads,
			        activeThreadsAfterRecognition);
			// if (activeThreadsAfterRecognition != initialActiveThreads) {
			// LOG.error("We had " + activeThreadsAfterRecognition
			// + " instead of " + initialActiveThreads);
			// ++failedCount;
			// }
		}
		
		// We should never have had an extra thread.
		Assert.assertEquals(0, failedCount);
	}
	

	private IPoint createPoint(double x, double y) {
		return new Point(x, y, System.currentTimeMillis());
	}
	
}
