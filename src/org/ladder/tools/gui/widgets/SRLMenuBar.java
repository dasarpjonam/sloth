/**
 * SRLMenuBar.java
 * 
 * Revision History:<br>
 * Sep 4, 2008 intrect - File created
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
package org.ladder.tools.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class SRLMenuBar extends JMenuBar {
	
	private JMenu file;
	private JMenu edit;
	private JMenu analysis;
	
	private JMenuItem save;
	private JMenuItem open;
	private JMenuItem close;
	private JMenuItem undo;
	private JMenuItem redo;
	private JMenuItem batchlabel;
	private JMenuItem batchimages;
	
	private List<String> menus =  new ArrayList<String>();
	
	private boolean m_default;
	
	/**
	 * Creates a new SRLMenuBar
	 */
	public SRLMenuBar(boolean defaultSetup) {
		super();
		
		m_default = defaultSetup;
		if (defaultSetup)
			initializeMenu();
	}
	
	/**
	 * Start up with a MenuBar that has File, Edit menus that can be hooked into.
	 */
	private void initializeMenu() {
		file = new JMenu("File");
		edit = new JMenu("Edit");
		analysis = new JMenu("Analysis");
		
		save = new JMenuItem("Save");
		open = new JMenuItem("Open");
		close = new JMenuItem("Close");
		
		file.add(save);
		file.add(open);
		file.add(close);
		
		undo =  new JMenuItem("Undo");
		redo =  new JMenuItem("Redo");
		
		edit.add(undo);
		edit.add(redo);
		
		batchlabel = new JMenuItem("Batch Label");
		batchimages = new JMenuItem("Batch Images");
		
		analysis.add(batchlabel);
		analysis.add(batchimages);
		
		add(file);
		add(edit);
		add(analysis);
		
	}
	
	/**
	 * Appends the specified menu to the end of the menu bar.
	 * @param menu - the component to add
	 * @return the menu component
	 */
	@Override
	public JMenu add(JMenu menu) {
		if (menus.contains(menu.getText()))
			return menu;
		menus.add(menu.getText());
		return super.add(menu);
	}
	
	/**
	 * If the default setup is enabled, we return a file menu, else we return null.
	 * @return
	 */
	public JMenu getFileMenu() {
		if (m_default)
			return file;
		return null;
	}
	
	/**
	 * If the default setup is enabled, we return a edit menu, else we return null.
	 * @return
	 */
	public JMenu getEditMenu() {
		if (m_default)
			return edit;
		return null;
	}
	
	public JMenuItem getSaveItem() {
		if (m_default)
			return save;
		return null;
	}
	
	public JMenuItem getOpenItem() {
		if (m_default)
			return open;
		return null;
	}
	
	public JMenuItem getCloseItem() {
		if (m_default)
			return close;
		return null;
	}
	
	public JMenuItem getUndoItem() {
		if (m_default)
			return undo;
		return null;
	}
	
	public JMenuItem getRedoItem() {
		if (m_default)
			return redo;
		return null;
	}

}
