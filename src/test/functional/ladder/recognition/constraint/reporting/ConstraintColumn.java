/**
 * ConstraintColumn.java
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class is one column in a report, which represents one constraint and
 * holds a set of cells, grouped by confidence rounded to the nearest 1 /
 * {@link #SF_ROUND_FACTOR} (eg, grouped into 10s). Cells are grouped by rounded
 * confidence for fine-grained storage of the images.
 * 
 * @author jbjohns
 */
public class ConstraintColumn {
	
	/**
	 * This defines how many groups there are, evenly spaced from 0...1.
	 */
	public static final int SF_NUM_GROUPS = 10;
	
	/**
	 * Name of the constraint this column holds cells for
	 */
	private String m_constraintName;
	
	/**
	 * Cells in this column, grouped by rounded confidence value.
	 */
	private Map<Integer, List<ConstraintReportCell>> m_cells;
	
	
	/**
	 * Create an empty column with the given name
	 * 
	 * @param constraintName
	 *            Name of the constraint, may not be null
	 */
	public ConstraintColumn(String constraintName) {
		if (constraintName == null) {
			throw new IllegalArgumentException("Constaint name may not be null");
		}
		m_constraintName = constraintName;
		
		m_cells = new HashMap<Integer, List<ConstraintReportCell>>();
	}
	

	/**
	 * Add a cell to the correct group in the column
	 * 
	 * @param cell
	 *            The cell, may not be null
	 */
	public void addCell(ConstraintReportCell cell) {
		if (cell == null) {
			throw new IllegalArgumentException("Cell to add may not be null");
		}
		
		// this is the amount of confidence one group ranges. If you have ten
		// groups, one group spans 0.1, so the first group is from 0 to 0.1
		double groupSize = 1.0 / SF_NUM_GROUPS;
		// this is the group that the cell belongs to. Dividing the confidence
		// by the group size and casting to an integer gives which group we
		// belong to counting from 0. Multiplying
		Integer cellGroup = new Integer(
		        (int) (cell.getConfidence() / groupSize) * SF_NUM_GROUPS);
		
		// get the group of cells for this rounded confidence
		List<ConstraintReportCell> cellList = m_cells.get(cellGroup);
		// no group already? create it and add to the column
		if (cellList == null) {
			cellList = new ArrayList<ConstraintReportCell>();
			m_cells.put(cellGroup, cellList);
		}
		// add the cell to the group
		cellList.add(cell);
	}
	

	/**
	 * Clear all the groups in the column, then clear the column of all groups.
	 */
	public void clear() {
		for (List<ConstraintReportCell> cellList : m_cells.values()) {
			cellList.clear();
		}
		m_cells.clear();
	}
	

	/**
	 * Get the name of the column
	 * 
	 * @return The name of the constraint this column is for
	 */
	public String getConstraintName() {
		return m_constraintName;
	}
	

	/**
	 * Equals based on constraint name
	 * 
	 * @param obj
	 *            Other obj to test equality against
	 * @return Is this equal to other?
	 */
	public boolean equals(Object obj) {
		boolean equal = false;
		
		if (obj instanceof ConstraintColumn) {
			if (this == obj) {
				equal = true;
			}
			else {
				ConstraintColumn other = (ConstraintColumn) obj;
				equal = this.getConstraintName().equals(
				        other.getConstraintName());
			}
		}
		
		return equal;
	}
	

	/**
	 * Hash on constraint name
	 * 
	 * @return the hash code
	 */
	public int hashCode() {
		return m_constraintName.hashCode();
	}
	

	/**
	 * Write all the cells into the given directory, creating subdirectories for
	 * the groups.
	 * 
	 * @param destinationDir
	 *            Dir to write the files to
	 */
	public void writeColumnToFile(File destinationDir) {
		
		System.out.println("... col: " + m_constraintName);
		
		SortedSet<Integer> groups = new TreeSet<Integer>(m_cells.keySet());
		for (Integer group : groups) {
			String confidenceString = group.toString();
			File confidenceDir = new File(destinationDir, confidenceString);
			
			if (!confidenceDir.exists()) {
				confidenceDir.mkdir();
			}
			if (!confidenceDir.isDirectory()) {
				System.out.println(confidenceDir.getAbsolutePath()
				                   + " is not a directory");
				return;
			}
			
			int id = 0;
			List<ConstraintReportCell> cells = m_cells.get(group);
			for (ConstraintReportCell cell : cells) {
				cell.writeAsImageToFile(confidenceDir, id);
				++id;
			}
		}
	}
	
}
