package edu.tamu.deepGreen.test;

import java.io.File;
import javax.swing.filechooser.*;

import org.ladder.io.XMLFileFilter;

public class ImageFilter extends FileFilter 
{
	/**
	 * Many Image types
	 */
	private static final String[] S_UNDERSTOOD_FORMATS = { "png","jpg","jpeg","tif","tiff","gif" };

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
}
