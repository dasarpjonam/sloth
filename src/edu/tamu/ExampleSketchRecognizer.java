/**
 * ExampleSketchRecognizer.java
 * 
 * Revision History:<br>
 * Oct 11, 2010 jbjohns - File created
 * 
 * <p>
 * 
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
package edu.tamu;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.recognizer.AbstractSketchRecognizer;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;

import edu.tamu.deepGreen.recognition.IDeepGreenRecognizer;

/**
 * 
 * @author jbjohns
 */
public class ExampleSketchRecognizer extends AbstractSketchRecognizer {
	
	/**
	 * logger
	 */
	private static Logger log = LadderLogger
	        .getLogger(ExampleSketchRecognizer.class);
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.ITimedRecognizer#recognizeTimed(long)
	 */
	@Override
	public List<IRecognitionResult> recognizeTimed(long maxTime)
	        throws OverTimeException {
		
		// check to see if we're over time
		// we should do this often if we have a long algorithm...
		long startTime = System.currentTimeMillis();
		OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		
		// TODO meaningful sketch recognition, you should also assign the
		// correct strokes and subshapes to your recognized shapes
		
		IRecognitionResult results = new RecognitionResult();
		
		IShape shape1 = new Shape();
		// you should actually use the proper strokes or subshapes
		shape1.addStroke(super.m_sketch.getFirstStroke());
		shape1.setConfidence(0.99);
		// Your labels have to have a special format (the name of the directory)
		// if you use the DeepGreenGUI
		shape1.setLabel("999_F_X_X_X_ThisIsATest");
		// you need SIDCs to see stuff in the DeepGreenGUI, but you don't necc.
		// have to use them for your recognizer. This is a dummy SIDC that has
		// no meaning whatsoever
		shape1
		        .setAttribute(IDeepGreenRecognizer.S_ATTR_SIDC,
		                "SFGPAAAAAAAAAAA");
		
		IShape shape2 = new Shape();
		shape2.addStroke(super.m_sketch.getFirstStroke());
		shape2.setConfidence(0.33);
		shape2.setLabel("999_F_X_X_X_NBestList");
		shape2
		        .setAttribute(IDeepGreenRecognizer.S_ATTR_SIDC,
		                "SFGPAAAAAAAAAAA");
		
		IShape shape3 = new Shape();
		shape3.addStroke(super.m_sketch.getFirstStroke());
		shape3.setConfidence(0.10);
		shape3.setLabel("999_F_X_X_X_NoRecPerformed");
		shape3
		        .setAttribute(IDeepGreenRecognizer.S_ATTR_SIDC,
		                "SFGPAAAAAAAAAAA");
		
		results.addShapeToNBestList(shape1);
		results.addShapeToNBestList(shape2);
		results.addShapeToNBestList(shape3);
		
		results.sortNBestList();
		
		List<IRecognitionResult> resultList = new ArrayList<IRecognitionResult>();
		resultList.add(results);
		
		return resultList;
	}
}
