/**
 * OverTimeExceptionTest.java
 * 
 * Revision History:<br>
 * Mar 6, 2009 jbjohns - File created
 *
 * <p>
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&M University 
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
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.IDeepGreenNBest;
import edu.tamu.deepGreen.recognition.IDeepGreenRecognizer;


/**
 * 
 * @author jbjohns
 */
public class OverTimeExceptionTest {
	
	
    @Test
    public void checkTimeout() throws IOException, NullPointerException,
                              UnknownSketchFileTypeException {
        final IDeepGreenRecognizer recognizer = new DeepGreenRecognizer();
        final File symbolFile = new File("testFiles/tai_cav.xml");
        recognizer.loadData(symbolFile);
        recognizer.setMaxTime(1000);
        final Date start = new Date();
        try {
            final IDeepGreenNBest nbest = recognizer.recognizeSingleObject();
            Assert.fail("If we get here, we got a recognition. There should not have been time.");
            Assert.assertNotNull(nbest);
        } catch (final OverTimeException ote) {
            // This is expected.
        }
        final Date stop = new Date();
        final long durationMs = stop.getTime() - start.getTime();
        System.out.println("The duration was: " + durationMs + " ms");
        Assert.assertTrue(2000 > durationMs);
    }

}
