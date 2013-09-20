/**
 * LayerManager.java
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
package org.ladder.tools.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.ladder.tools.gui.event.LayerChangeEvent;
import org.ladder.tools.gui.event.LayerChangeEventListener;
import org.ladder.tools.gui.widgets.SRLToolPalette;

public class LayerManager {
	
	private Layer active;
	private List<LayerChangeEventListener> listeners;
	private Map<String, Layer> layers = new HashMap<String, Layer>();
	
	private JMenu layerMenu;
	
	
	/**
	 * Create a LayerManager
	 */
	public LayerManager() {
		listeners = new ArrayList<LayerChangeEventListener>();
	}

	/**
	 * Get a list of the layers
	 */
	public Collection<Layer> getLayers() {
		return layers.values();
	}

	/**
	 * 
	 * @param l
	 */
	public void addLayer(Layer l) {
		layers.put(l.getName(), l);
		
		if (layers.size() == 1) {
			active = l;
		}
		
		//buildMenus();
	}
	
	private void buildMenus() {
		layerMenu = new JMenu("Layer");
		
		for (Layer l : layers.values()) {
			JMenuItem menuItem = new JMenuItem(l.getName());
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					JMenuItem item = (JMenuItem) arg0.getSource();
					String name = item.getText();
					activateLayer(name);
				}
			});
			layerMenu.add(menuItem);
		}
	}
	
	/**
	 * 
	 * @param index
	 */
	public void activateLayer(String name) {
		Layer oldLayer = active;
		active = layers.get(name);
		fireLayerChangeEvent(new LayerChangeEvent(active, oldLayer));
		//for (SRLToolPalette srltp : active.getToolPalettes()) {
		//	srltp.setVisible(true);
		//}
	}
	
	/**
	 * 
	 * @param lce
	 */
	public void fireLayerChangeEvent(LayerChangeEvent lce) {
		for (LayerChangeEventListener listener : listeners)
			listener.changeLayers(lce);
	}
	
	public void addLayerChangeEventListener(LayerChangeEventListener lcel) {
		listeners.add(lcel);
	}
	
	public Layer getActiveLayer() {
		return active;
	}
	
	public JMenu getLayerMenu() {
		buildMenus();
		return layerMenu;
	}
	
	public void addLayersToContentPane(JLayeredPane layeredPane) {
		int i = 0;
		for (Layer l : layers.values()) {
			layeredPane.add(l, i);
			i++;
		}
	}
	
	public void setVisible(String name, boolean visibility) {
		Layer l = layers.get(name);
		l.setVisible(visibility);
	}
	
}
