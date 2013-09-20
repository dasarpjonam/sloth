/**
 * PaleoFilePrinter.java
 * 
 * Revision History:<br>
 * Nov 11, 2009 bpaulson - File created
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
package test.functional.ladder.recognition.paleo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ladder.core.config.LadderConfig;
import org.ladder.io.ShapeDirFilter;
import org.ladder.io.XMLFileFilter;


/**
 * 
 * @author bpaulson
 */
public class PaleoFilePrinter {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// get the list of individual shape directories
		File testData = new File(LadderConfig.getProperty("testData"));
		File[] shapeDirs = testData.listFiles(new ShapeDirFilter());
		List<String> fname = new ArrayList<String>();

		// loop through all shape directories
		for (File shapeDir : shapeDirs) {
			if (!shapeDir.getAbsolutePath().endsWith("2")) {
				File[] shapeFiles = shapeDir.listFiles(new XMLFileFilter());
				for (File f : shapeFiles)
					fname.add(f.getName());				
			}
		}
		
		Collections.sort(fname);
		for (String s : fname)
			System.out.println(s);		
	}
	
}
