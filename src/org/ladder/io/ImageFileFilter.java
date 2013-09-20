/**
 * ImageFileFilter.java
 * 
 * Revision History:<br>
 * Oct 28, 2008 jbjohns - File created
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

import javax.swing.filechooser.FileFilter;

/**
 * File filter for accepted images that we can understand. Current formats
 * include: .png
 * 
 * @author jbjohns
 */
public class ImageFileFilter extends FileFilter {
	
	/**
	 * We can write to PNG, since it's the only file format that doesn't get
	 * screwed up using DrawPanel's buffered image
	 */
	private static final String[] S_UNDERSTOOD_FORMATS = { "png" };
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File f) {
		if (f == null) {
			return false;
		}
		if (f.isDirectory()) {
			return true;
		}
		
		String ext = getExtension(f);
		for (String format : S_UNDERSTOOD_FORMATS) {
			if (format.equals(ext)) {
				return true;
			}
		}
		
		return false;
	}
	

	/**
	 * We cheat and use {@link XMLFileFilter#getExtension(File)}
	 * 
	 * @param f
	 *            The file to get the extension of
	 * @return The extension of the file
	 */
	public static String getExtension(File f) {
		return XMLFileFilter.getExtension(f);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Image file (");
		for (int i = 0; i < S_UNDERSTOOD_FORMATS.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(S_UNDERSTOOD_FORMATS[i]);
		}
		sb.append(')');
		
		return sb.toString();
	}
	
}
