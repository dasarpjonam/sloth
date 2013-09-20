/**
 * InterpretationSetJeffRyeTest.java
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.PatternSyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.ladder.core.sketch.IStroke;

import edu.tamu.deepGreen.recognition.DeepGreenInterpretation;
import edu.tamu.deepGreen.recognition.IDeepGreenInterpretation;

/**
 * 
 * @author jbjohns
 */
public class InterpretationSetJeffRyeTest {
	
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
	
	
	@Test
	public void checkSetMembership() {
		// Create two different interpretations.
		final List<IStroke> strokes = new ArrayList<IStroke>();
		final IDeepGreenInterpretation interp1 = new CreatableDeepGreenInterpretation(
		        strokes, "SFGPUCI----*--*", 0.5);
		final IDeepGreenInterpretation interp2 = new CreatableDeepGreenInterpretation(
		        strokes, "SFGPUCR----*--*", 0.5);
		
		// Make sure that they are not equal and do not have the same hashcode.
		Assert.assertFalse(interp1.equals(interp2));
		Assert.assertFalse(interp1.hashCode() == interp2.hashCode());
		
		// Add one to the set.
		final Set<IDeepGreenInterpretation> set = new TreeSet<IDeepGreenInterpretation>();
		set.add(interp1);
		
		// Verify that only the one is in the set.
		Assert.assertEquals(1, set.size());
		Assert.assertTrue(set.contains(interp1));
		// The second one is not yet in the set
		Assert.assertFalse(set.contains(interp2));
		
		// Try to add the second to the set.
		set.add(interp2);
		
		// Now the second one is in the set and no longer conflicts with the
		// first
		Assert.assertEquals(2, set.size());
		Assert.assertTrue(set.contains(interp1));
		Assert.assertTrue(set.contains(interp2));
	}
	
}
