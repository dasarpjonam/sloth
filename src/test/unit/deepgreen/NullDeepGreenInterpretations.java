/**
 * NullDeepGreenInterpretations.java
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.DOMOutput;
import org.ladder.recognition.recognizer.OverTimeException;

import test.unit.SlothTest;
import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.IDeepGreenNBest;
import edu.tamu.deepGreen.recognition.IDeepGreenRecognizer;

/**
 * Test to see if we ever get a null interpretation back from
 * {@link IDeepGreenRecognizer#recognize()}
 * 
 * @author jbjohns
 */
public class NullDeepGreenInterpretations {
	
	@Test
	public void nullDeepGreenInterpretationsTest() throws IOException,
	        NullPointerException, OverTimeException,
	        ParserConfigurationException {
		// The error is that the new API recognize() method was returning
		// null for null interpretations rather than an empty DeepGreenNBest
		
		final IDeepGreenRecognizer rec = new DeepGreenRecognizer();
		final int numReps = 500;
		
		for (int i = 0; i < numReps; i++) {
			// make up some random strokes, between 1...5 of them
			int numRandomStrokes = SlothTest.rand.nextInt(5) + 1;
			List<IStroke> randomStrokes = new ArrayList<IStroke>();
			for (int n = 0; n < numRandomStrokes; n++) {
				randomStrokes.add(SlothTest.randStroke());
			}
			
			// recognize these strokes! This should not recognize anything in
			// the COA since the strokes are random. If it does, it's a miracle.
			// This is meant to test if anything that is NOT recognized does
			// NOT come back null.
			try {
				IDeepGreenNBest nbest = rec
				        .recognizeSingleObject(randomStrokes);
				Assert.assertNotNull(nbest);
			}
			catch (NullPointerException e) {
				String fileName = "/Users/jbjohns/Desktop/npeShapes_" + i
				                  + ".xml";
				ISketch sketch = new Sketch();
				sketch.setStrokes(randomStrokes);
				new DOMOutput().toFile(sketch, new File(fileName));
			}
		}
	}
}
