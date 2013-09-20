package org.ladder.tools.gui.widgets;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class MatrixDisplay extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5345333435452515459L;
	
	private List<String> m_rows = new ArrayList<String>();
	private List<String> m_cols = new ArrayList<String>();
	private double[][] m_matrix;
	private String m_title;
	
	private class Row extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -6757322958958975406L;

		public Row() {
			super();
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		}
		
		public Component add(Component c) {
			super.add(Box.createHorizontalGlue());
			return super.add(c);
		}
	}
	
	public MatrixDisplay(double[][] matrix, String title) {
		m_matrix = matrix;
		m_title = title;
		initialize();
	}
	
	public MatrixDisplay(List<String> rows, List<String> cols, double[][] matrix, String title) {
		m_matrix = matrix;
		m_rows = rows;
		m_cols = cols;
		m_title = title;
		initialize();
	}
	
	private void initialize() {	
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setupColHeadings();
		setupRows();
		setBorder(BorderFactory.createTitledBorder(m_title));
	}
	
	private void setupRows() {
		for (int i = 0; i < m_matrix.length; i++) {
			Row r = new Row();
			
			if (m_rows.size() > 0) {
				JLabel label = new JLabel(m_rows.get(i));
				label.setPreferredSize(new Dimension(100, 30));
				r.add(label);
			}
			
			for (int j = 0; j < m_matrix[i].length; j++) {
				JLabel label = new JLabel(trimDigits(m_matrix[i][j]));
				label.setPreferredSize(new Dimension(65, 35));
				r.add(label);
			}
			add(r);
			add(Box.createVerticalGlue());
		}
	}
	
	private void setupColHeadings() {
		if (m_cols.size() > 0) {
			Row r = new Row();
			r.add(Box.createHorizontalStrut(100));
			for (String s : m_cols) {
				r.add(new JLabel(s));
			}
			add(r);
			add(Box.createVerticalGlue());
		}
	}
	
	private String trimDigits(double d) {
		if (d == 0.0) {
			return "0.00";
		} else if (String.valueOf(d).length() < 4) {
			return String.valueOf(d);
		} else {
			return String.valueOf(d).substring(0, 4);
		}
	}


}
