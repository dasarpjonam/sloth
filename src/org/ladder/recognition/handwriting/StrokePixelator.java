/**
 * StrokePixelator.java
 * 
 * Revision History:<br>
 * Jan 13, 2009 bde - File created
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

package org.ladder.recognition.handwriting;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;
import org.ladder.core.sketch.Stroke;

import weka.core.Instance;
import weka.core.Instances;

public class StrokePixelator {
	


	private static Logger log = LadderLogger
	.getLogger(StrokePixelator.class);


	/**
	 * Creates an int array pixel map of a sketch.
	 * 
	 * @param sk
	 * @param heightWidth
	 * @return
	 */

	
	public static int[][] pixelizeSketch(ISketch sk, int heightWidth) {

		int[][] pixels = new int[heightWidth + 1][heightWidth + 1];

		BoundingBox bb = sk.getBoundingBox();

		double skWidthHeight = Math.max(bb.height, bb.width);
		
		IPoint centerPoint = bb.getCenterPoint();

		double shiftX = 0 - (centerPoint.getX()-skWidthHeight/2);

		double shiftY = 0 - (centerPoint.getY()-skWidthHeight/2);

		double normalizeWidthHeight = heightWidth / skWidthHeight;

		for (IStroke st : sk.getStrokes()) {
			IStroke dbStroke = doublePoint(st);
			dbStroke = doublePoint(dbStroke);
			dbStroke = doublePoint(dbStroke);
			
			for (IPoint pt : dbStroke.getPoints()) {
				Double xValue = (pt.getX() + shiftX) * normalizeWidthHeight;
				Double yValue = (pt.getY() + shiftY) * normalizeWidthHeight;
				pixels[xValue.intValue()][yValue.intValue()] = 1;
			}
		}

		return pixels;
	}

	/**
	 * Returns a Weka instance of a sketch (using the pixelize function))
	 * 
	 * @param sk
	 * @param heightWidth
	 * @return
	 */
	public static Instance getInstance(ISketch sk, int heightWidth,
			Instances dataSet) {

		int[][] pixels = pixelizeSketch(sk, heightWidth);

		Instance inst = new Instance((int) (Math.pow((heightWidth + 1), 2) + 4));

		inst.setDataset(dataSet);

		inst.setValue(0, 0);

		inst.setValue(1,0);
		
		int count = 2;

		for (int i = 0; i < pixels.length; i++) {
			String holder = "";
			for (int j = 0; j < pixels.length; j++) {
					if(pixels[j][i] == 1) 
						inst.setValue(count, 1);
					else
						inst.setValue(count, -1);
				holder += String.valueOf(pixels[j][i]);
				count++;
			}
			log.debug(holder);
		}
		return inst;
	}
	
	private static IStroke doublePoint(IStroke sk) {
		IStroke doubledStroke = new Stroke();
		
		for(int i=0 ; i < sk.getNumPoints() - 1; i++){
			doubledStroke.addPoint(sk.getPoint(i));
			
			long timeOfNewPoint = (long) ((sk.getPoint(i).getTime() + sk.getPoint(i+1).getTime())/2.0); 
			
			double xValue = (sk.getPoint(i).getX() + sk.getPoint(i+1).getX())/2.0;
			
			double yValue = (sk.getPoint(i).getY() + sk.getPoint(i+1).getY())/2.0;
			
			IPoint pt = new Point(xValue, yValue, timeOfNewPoint);
			
			doubledStroke.addPoint(pt);
		}
		
		doubledStroke.addPoint(sk.getLastPoint());
		
		return doubledStroke;
	}

}
