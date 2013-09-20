/**
 * ColorComboBoxRenderer.java
 *
 * Revision History: <br>
 * (6/17/08) bpaulson - class created <br>
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
package org.ladder.collection;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Combo box renderer that allows labels to have a specific color
 *
 * @author bpaulson
 */
public class ColorComboBoxRenderer extends JLabel implements ListCellRenderer {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 4878545635018156462L;

	/**
	 * List of colors that correspond with the labels
	 */
	private Color[] m_colorList;

	/**
	 * List of textual labels
	 */
	private String[] m_textList;


	/**
	 * Constructor for the renderer
	 */
	public ColorComboBoxRenderer() {
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
		setPreferredSize(new Dimension(77, 25));
	}


	/**
	 * Sets the list of label colors
	 *
	 * @param colors
	 *            list of label colors
	 */
	public void setColorList(Color[] colors) {
		m_colorList = colors;
	}


	/**
	 * Sets the list of text label
	 *
	 * @param text
	 *            list of text labels
	 */
	public void setTextList(String[] text) {
		m_textList = text;
	}


	/**
	 * Rendering function
	 */
	public Component getListCellRendererComponent(JList list, Object value,
	        int index, boolean isSelected, boolean cellHasFocus) {
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			if (index == -1)
				setForeground(m_colorList[list.getSelectedIndex()]);
			else
				setForeground(m_colorList[index]);
		}
		else {
			setBackground(list.getBackground());
			if (index == -1)
				setForeground(m_colorList[list.getSelectedIndex()]);
			else
				setForeground(m_colorList[index]);
		}
		if (index != -1)
			setText(m_textList[index]);
		else
			setText(m_textList[list.getSelectedIndex()]);
		return this;
	}

}
