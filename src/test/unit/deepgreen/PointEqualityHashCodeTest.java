/**
 * PointEqualityHashCodeTest.java
 * 
 * Revision History:<br>
 * Mar 31, 2009 jbjohns - File created
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

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;


/**
 * 
 * @author jbjohns
 */
public class PointEqualityHashCodeTest {
	
	/**
     * Verify that two strokes are equals and have same hashcode when their
     * fields are equal.
     */
    @Test
    public void checkStrokeEquality() {

        final Stroke expectedStroke = new Stroke();
        expectedStroke.setID(UUID.randomUUID());

        final Stroke s1 = new Stroke();
        s1.setID(UUID.randomUUID());

        // Initially, the strokes have different IDs.
        Assert.assertFalse(expectedStroke.equals(s1));
        Assert.assertFalse(expectedStroke.hashCode() == s1.hashCode());

        // If we add a point, they are now both different.
        final IPoint expectedPoint = new Point(1.0, 2.0, 10);
        s1.addPoint(expectedPoint);

        // IDs still different, and that determines equals() and hash
        Assert.assertFalse(expectedStroke.equals(s1));
        Assert.assertFalse(expectedStroke.hashCode() == s1.hashCode());

        // Make the IDs the same, now they differ by points.
        s1.setID(expectedStroke.getID());

        // IDs same, determining equals and hash
        Assert.assertTrue(expectedStroke.equals(s1));
        Assert.assertTrue(expectedStroke.hashCode() == s1.hashCode());
    }
    
    /**
     * Verify that two points are equal and have same hashcode when their fields
     * are equal.
     */
    @Test
    public void checkPointEquality() {
        final IPoint expectedPoint = new Point(1.0, 2.0, 10);
        Assert.assertNotNull(expectedPoint.getID());

        final Point p1 = new Point(1.0, 2.0, 10);
        
        // not equal on ID
        Assert.assertFalse(expectedPoint.equals(p1));
        // not same hash code
        Assert.assertFalse(expectedPoint.hashCode() == p1.hashCode());

        // This makes them truly equal, so we expect both to pass.
        p1.setID(expectedPoint.getID());
        Assert.assertTrue(expectedPoint.equals(p1));
        Assert.assertTrue(expectedPoint.hashCode() == p1.hashCode());

        // Again, iff they are equal, the hashcodes should be the same.
        p1.setX(expectedPoint.getX() + 2.0);
        Assert.assertFalse(expectedPoint.equals(p1));
        Assert.assertFalse(expectedPoint.hashCode() == p1.hashCode());
    }


}
