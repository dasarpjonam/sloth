/**
 * IBeautifiable.java
 *
 * Revision History: <br>
 * (5/26/08) bpaulson - interface created <br>
 *
 * <p>
 *
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 *
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
package org.ladder.core.sketch;

import java.awt.Image;


/**
 * Interface specifying that an element can be beautified
 *
 * @author bpaulson
 *
 */
public interface IBeautifiable {

	/**
	 * Enumerated type used to specify the mode of beautification. Either NONE
	 * (not currently beautified), SHAPE (beautified using a Java2D Shape
	 * object), or IMAGE (beautified using image replacement)
	 *
	 * @author bpaulson
	 *
	 */
	public enum Type {
		/**
		 * No beautification used
		 */
		NONE,
		/**
		 * Beautified shape
		 */
		SHAPE,
		/**
		 * Image replacing the shape
		 */
		IMAGE
	};


	/**
	 * Get the beautification type
	 *
	 * @return beautification type
	 */
	public Type getBeautificationType();


	/**
	 * Get the beautified Java2D Shape object
	 *
	 * @return beautified Java2D Shape object
	 */
	public java.awt.Shape getBeautifiedShape();


	/**
	 * If the shape requires special painting instructions (like rotation,
	 * scaling, etc.) then a shape painter should be specified.
	 *
	 * @return the shape painter for the beautified shape
	 */
	public IShapePainter getBeautifiedShapePainter();


	/**
	 * Get the beautified Image object
	 *
	 * @return beautified Image object
	 */
	public Image getBeautifiedImage();


	/**
	 * Get the bounding box in which the beautified image should be displayed
	 *
	 * @return bounding box of beautified image
	 */
	public BoundingBox getBeautifiedImageBoundingBox();


	/**
	 * Set the beautification type
	 *
	 * @param type
	 *            beautification type
	 */
	public void setBeautificationType(Type type);


	/**
	 * Set the beautified shape object
	 *
	 * @param shape
	 *            beautified shape object
	 */
	public void setBeautifiedShape(java.awt.Shape shape);


	/**
	 * If the shape requires special painting instructions (like rotation,
	 * scaling, etc.) then a shape painter should be specified.
	 *
	 * @param shapePainter
	 *            shape painter to use when painting a shape object
	 */
	public void setBeautifiedShapePainter(IShapePainter shapePainter);


	/**
	 * Set the beautified image object
	 *
	 * @param image
	 *            beautified image object
	 * @param boundingBox
	 *            bounding box that the image should be displayed in
	 */
	public void setBeautifiedImage(Image image, BoundingBox boundingBox);
}
