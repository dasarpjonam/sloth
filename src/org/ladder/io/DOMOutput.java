/**
 * DOMOutput.java
 * 
 * Revision History:<br>
 * Jul 31, 2008 Aaron Wolin - File created
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
package org.ladder.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.srl.DOMOutputSRL;

/**
 * Output a sketch to an XML file. This file outputs the sketch in a known
 * format. Other format types can be added later via {@link SketchFileType} and
 * updating the methods in this class. New formats must also create their own
 * input and output parsing classes.
 * 
 * @author awolin
 */
public class DOMOutput implements IOutput {
	
	/**
	 * Default constructor
	 */
	public DOMOutput() {
		// Nothing to do
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.io.IOutput#toFile(org.ladder.core.sketch.ISketch,
	 * java.io.File)
	 */
	public void toFile(ISketch sketch, File file)
	        throws ParserConfigurationException, FileNotFoundException,
	        IOException {
		
		if (sketch instanceof Sketch) {
			toFile(sketch, SketchFileType.SRL, file);
		}
		else {
			// TODO - Not the best exception, but it catches the error
			System.err.println("Error: Unhandled file format");
			throw new IOException();
		}
	}
	

	/**
	 * Output the sketch to an XML file.
	 * 
	 * @param sketch
	 *            Sketch to output
	 * @param outputFileType
	 *            File type to output the sketch in, where the types are defined
	 *            in {@link SketchFileType}
	 * @param file
	 *            File to output the sketch to
	 */
	public void toFile(ISketch sketch, SketchFileType outputFileType, File file)
	        throws ParserConfigurationException, FileNotFoundException,
	        IOException {
		
		// Make the directories pointing to the file, if possible
		file.getParentFile().mkdirs();

		// Output the files
		switch (outputFileType) {
			case SRL:
				DOMOutputSRL outputFull = new DOMOutputSRL();
				outputFull.toFile(sketch, file);
				break;
			
			default:
				// TODO - Not the best exception, but it catches the error
				System.err.println("Error: Unhandled file format output");
				throw new IOException();
		}
	}
}
