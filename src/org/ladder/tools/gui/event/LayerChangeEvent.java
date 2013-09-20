package org.ladder.tools.gui.event;

import org.ladder.tools.gui.Layer;

public class LayerChangeEvent {
	
	private Layer active;
	
	private Layer oldLayer;
	
	public LayerChangeEvent(Layer newLayer, Layer oldLayer) {
		active = newLayer;
		this.oldLayer = oldLayer;
	}
	
	public Layer getActiveLayer() {
		return active;
	}
	
	public Layer getOldLayer() {
		return oldLayer;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3620986930879562767L;

}
