/**
 * ConvertOldSousaToNewLadder.java
 * 
 * Revision History:<br>
 * Nov 25, 2008 jbjohns - File created
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
package org.ladder.io;

import java.io.File;
import java.util.List;

import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Sketch;

/**
 * Class to convert SOUSA files in Johnston's directories into new SRL file formats.  You should use
 * 
 * @author jbjohns
 */
public class ConvertOldSousaToNewLadder {
	
	/**
	 * 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		File oldSousaDir = new File(
		        "/Users/rgraham/Documents/eclipse/Sloth/scripts/novXML");
		File newLadderDir = new File(
		        "/Users/rgraham/Documents/eclipse/Sloth/scripts/novXMLNew");
		
		// root dir of the old sousa data
		if (!oldSousaDir.isDirectory() || !oldSousaDir.canRead()) {
			System.err.println("Not a dir or cannot read: "
			                   + oldSousaDir.getPath());
			System.exit(-1);
		}
		
		// root dir for the new ladder data
		if (!newLadderDir.exists()) {
			try {
				newLadderDir.mkdirs();
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		
		// loop over sousa directory structure
		System.out.println("From sousa base dir: " + oldSousaDir.getPath());
		System.out.println("To new ladder dir: " + newLadderDir.getPath());
		
		File[] sousaDirs = oldSousaDir.listFiles();
		for (File sousaSubDir : sousaDirs) {
			System.out.println(sousaSubDir);
			if (sousaSubDir.isDirectory()) {
				System.out.println("Reading sousa dir: "
				                   + sousaSubDir.getName());
				File newSubDir = new File(newLadderDir.getPath() + "/"
				                          + sousaSubDir.getName());
				if (!newSubDir.exists()) {
					newSubDir.mkdir();
				}
				
				// loop over each file in this sousa dir
				for (File sousaFile : sousaSubDir
				        .listFiles(new XMLFileFilter())) {
					System.out.println("\tReading sousa file: "
					                   + sousaFile.getName());
					List<IStroke> strokes = SousaDataParser
					        .parseSousaFile(sousaFile);
					System.out.println(strokes.get(0).getNumPoints());
					ISketch sketch = new Sketch();
					sketch.setStrokes(strokes);
					
					File newLadderFile = new File(newSubDir.getPath() + "/"
					                              + sousaFile.getName());
					try {
						new DOMOutput().toFile(sketch, newLadderFile);
					}
					catch (Exception e) {
						System.err.println("Cannot write to new file format: "
						                   + e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}
	}
}
