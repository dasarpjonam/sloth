/**
 * ShapeDirFilter.java
 * 
 * Revision History:<br>
 * Oct 6, 2008 bde - File created
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
import java.io.FileFilter;

/**
 * ShapeDirFilter is a FileFilter used by the ShapeDefinitionAccuracyTest, and
 * the DomainDefinitionAccuracyTest. It is used to filter for svn related files
 * when traversing the training data directory.
 * 
 * @author bde
 * 
 */
public class ShapeDirFilter implements FileFilter {
	
	/**
	 * Accepts the file path if it is not an SVN related directory.
	 * 
	 * @param pathname
	 *            File path name
	 * @return True if the path is not of .svn or .DS, false otherwise.
	 */
	public boolean accept(File pathname) {
		
		if (pathname.getName().endsWith(".svn")) {
			return false;
		}
		if (pathname.getName().endsWith(".project")) {
			return false;
		}
		if (pathname.getName().startsWith(".DS")) {
			return false;
		}
		if (pathname.getName().equals("images")) {
			return false;
		}
		
		return true;
	}
	
}
