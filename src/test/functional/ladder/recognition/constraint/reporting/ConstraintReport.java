/**
 * ConstraintReport.java
 * 
 * Revision History:<br>
 * Dec 18, 2008 jbjohns - File created
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
package test.functional.ladder.recognition.constraint.reporting;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class holds the data for a constraint report that shows images of
 * sketches and the shapes used as parameters for various constraints.
 * 
 * @author jbjohns
 */
public class ConstraintReport {
	
	/**
	 * Map from constraint name to constraint column.
	 */
	Map<String, ConstraintColumn> m_columnMap = new HashMap<String, ConstraintColumn>();
	
	
	/**
	 * Add the given cell to the report. It is put into the proper column based
	 * on the name of the constraint in the cell.
	 * 
	 * @param cell
	 *            The cell, may not be null
	 */
	public void addCell(ConstraintReportCell cell) {
		if (cell == null) {
			throw new IllegalArgumentException("Cell may not be null");
		}
		// get the name of the constraint for this cell
		String constraintName = cell.getConstraintName();
		// get the column for this constraint name
		ConstraintColumn col = m_columnMap.get(constraintName);
		
		// if the col is null, no col for this constraint has been created.
		if (col == null) {
			// create the column for this constraint and add it to the map
			col = new ConstraintColumn(constraintName);
			m_columnMap.put(constraintName, col);
		}
		
		// add the cell to the column
		col.addCell(cell);
	}
	

	/**
	 * Clear the report of all its contents. This clears the columns of all
	 * cells, and then clears the report of all columns
	 */
	public void clear() {
		// clear all the columns of their cells
		for (ConstraintColumn col : m_columnMap.values()) {
			col.clear();
		}
		// clear the report of its columns
		m_columnMap.clear();
	}
	

	/**
	 * Write the contents of the report as images to the given destination
	 * directory. This method will attempt to make the dir if it does not exist
	 * 
	 * @param destinationDir
	 *            Destination directory to write the images to. May not be null
	 *            and must be a writeable directory according to
	 *            {@link File#isDirectory()} and {@link File#canWrite()}.
	 */
	public void writeReportToFile(File destinationDir) {
		
		if (destinationDir == null) {
			throw new NullPointerException(
			        "Desintation Dir File cannot be null");
		}
		
		System.out.println("Writing report to file...");
		
		if (!destinationDir.exists()) {
			destinationDir.mkdir();
		}
		if (!destinationDir.isDirectory() || !destinationDir.canWrite()) {
			throw new IllegalArgumentException(
			        destinationDir.getAbsolutePath()
			                + " is not a directory, or is not writeable");
		}
		
		// Get the columns, in order sorted by column name, stored in this
		// report
		SortedSet<String> columnNames = new TreeSet<String>(m_columnMap
		        .keySet());
		// loop over each column
		for (String colName : columnNames) {
			// create a directory for this column, which is the name of the
			// column
			File colDir = new File(destinationDir, colName);
			if (!colDir.exists()) {
				colDir.mkdir();
			}
			if (!colDir.isDirectory()) {
				System.err.println(colDir.getAbsolutePath()
				                   + " is not a directory");
				continue;
			}
			
			// get the actual column object and write it into the directory we
			// just created for it
			ConstraintColumn column = m_columnMap.get(colName);
			if (column != null) {
				column.writeColumnToFile(colDir);
			}
			else {
				System.out.println("Column " + colName + " is null");
			}
		}
	}
}
