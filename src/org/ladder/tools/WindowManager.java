/**
 * WindowManager.java
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
package org.ladder.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.ladder.tools.gui.widgets.SRLToolPalette;


public class WindowManager {
	
	private Map<String, SRLToolPalette> toolPalettes = new HashMap<String, SRLToolPalette>();

	private JMenu palettesMenu;
		
	public WindowManager() {
		
	}
	
	public void addToolPalettes(List<SRLToolPalette> palettes) {
		for (SRLToolPalette srltp : palettes) {
			toolPalettes.put(srltp.getName(), srltp);
		}
		buildMenu();
	}
	
	public void addToolPalette(SRLToolPalette palette) {
		toolPalettes.put(palette.getName(), palette);
		buildMenu();
	}
	
	public Map<String, SRLToolPalette> getToolPalettes() {
		return toolPalettes;
	}
	
	private void buildMenu() {
		palettesMenu = new JMenu("Tool Palettes");
		
		for (JFrame f : toolPalettes.values()) {
			JMenuItem menuItem = new JMenuItem(f.getName());
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					JMenuItem item = (JMenuItem) arg0.getSource();
					String name = item.getText();
					JFrame frame = toolPalettes.get(name);
					frame.setVisible(true);
				}
			});
			palettesMenu.add(menuItem);
		}
			
	}
	
	public JMenu getToolPaletteMenu() {
		return palettesMenu;
	}
}
