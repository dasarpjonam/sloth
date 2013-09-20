package org.ladder.tools.gui.widgets;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.ladder.tools.gui.Layer;
import org.ladder.tools.gui.LayerManager;

public class SRLLayerWidget {
	
	private JPanel m_layersPanel;
		
	private LayerManager m_layerManager;
	
	public SRLLayerWidget(LayerManager lm) {
		m_layerManager = lm;
		m_layersPanel = new JPanel();
		m_layersPanel.setLayout(new BoxLayout(m_layersPanel, BoxLayout.Y_AXIS));
		m_layersPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		
		JLabel label = new JLabel("SRL Layers");
		label.setFont(new Font("Sans Serif", Font.BOLD, 16));
		label.setAlignmentX(0.5f);
		m_layersPanel.add(label);
	}
	
	public JPanel getPanel() {
		return m_layersPanel;
	}
	
	public void addLayer(Layer l) {
		m_layersPanel.add(new LayerWidget(l, m_layerManager));
	}

}
