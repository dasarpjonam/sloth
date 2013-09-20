package org.ladder.io;

import java.io.File;
import java.io.FileFilter;

/**
 * DomainDirFilter is a FileFilter used by the ShapeDefinitionAccuracyTest, and
 * the DomainDefinitionAccuracyTest. It is used to filter for svn related files
 * when traversing the training data directory.
 * 
 * @author bde
 * 
 */
public class DomainDirFilter implements FileFilter {
	
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
		if (pathname.getName().startsWith(".DS")) {
			return false;
		}
		
		// if(!pathname.getName().contains("reconEngineer")) return false;
		
		return true;
	}
	
}
