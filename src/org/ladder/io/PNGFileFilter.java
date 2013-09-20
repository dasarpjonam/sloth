/**
 * PNGFileFilter.java
 * 
 * Revision History:<br>
 * Jan 11, 2010 bpaulson - File created
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
package org.ladder.io;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.filechooser.FileFilter;

/**
 * File filter that only allows PNG files to be displayed in a file chooser
 * 
 * @author bpaulson
 */
public class PNGFileFilter extends FileFilter implements FilenameFilter {
	
	/**
	 * PNG extension
	 */
	public static String S_PNG_EXTENSION = "png";
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File arg0) {
		if (arg0 == null)
			return false;
		if (arg0.isDirectory())
			return true;
		if (getExtension(arg0).compareTo(S_PNG_EXTENSION) == 0)
			return true;
		return false;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		return "PNG Files (*." + S_PNG_EXTENSION + ")";
	}
	

	/**
	 * Method used to parse the extension from a file
	 * 
	 * @param f
	 *            file to parse extension from
	 * @return file extension
	 */
	public static String getExtension(File f) {
		if (f == null)
			return "";
		String ext = "";
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	@Override
	public boolean accept(File dir, String name) {
		if (name.endsWith(".png") || name.endsWith(".PNG"))
			return true;
		return false;
	}
}