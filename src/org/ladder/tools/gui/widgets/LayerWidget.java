package org.ladder.tools.gui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.ladder.tools.gui.Layer;
import org.ladder.tools.gui.LayerManager;
import org.ladder.tools.gui.event.LayerChangeEvent;
import org.ladder.tools.gui.event.LayerChangeEventListener;

public class LayerWidget extends JPanel implements LayerChangeEventListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 216174810601508167L;
	private final Layer layer;
	private final LayerManager manager;
	private JButton visible;
	private JButton active;
	private JLabel name;
	private JPanel m_container;
	
	public LayerWidget(Layer l, LayerManager lm) {
		super();
		
		layer = l;
		manager = lm;
		
		m_container = new JPanel();
		m_container.setLayout(new BoxLayout(m_container, BoxLayout.X_AXIS));
		
		name = new JLabel(l.getName());
		name.addMouseListener(new MouseListener() {

			/* (non-Javadoc)
             * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseClicked(MouseEvent e) {
            	manager.activateLayer(layer.getName());
            }

			/* (non-Javadoc)
             * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseEntered(MouseEvent e) {
            }

			/* (non-Javadoc)
             * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseExited(MouseEvent e) {
            }

			/* (non-Javadoc)
             * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
             */
            @Override
            public void mousePressed(MouseEvent e) {
            }

			/* (non-Javadoc)
             * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseReleased(MouseEvent e) {
            }
			
		});
		
		visible = new JButton("Hide");
		
		active = new JButton("Activate");
		if (manager.getActiveLayer() == layer) {
			setBorder(BorderFactory.createTitledBorder("Active Layer"));
			active.setEnabled(false);
		}
		visible.addActionListener(new ActionListener() {
			
			private boolean b_visible = false;
			@Override
			public void actionPerformed(ActionEvent arg0) {
				manager.setVisible(layer.getName(), b_visible);
				if (b_visible) {
					((JButton)arg0.getSource()).setText("Hide");
				} else {
					((JButton)arg0.getSource()).setText("Show");
				}
				b_visible  = ! b_visible;
				
			}
			
		});
		
		active.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				manager.activateLayer(layer.getName());
			}
			
		});
						
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		name.setPreferredSize(new Dimension(150, 35));
		name.setAlignmentX(0.5f);
		visible.setPreferredSize(new Dimension(75, 35));
		active.setPreferredSize(new Dimension(75, 35));
		
		setPreferredSize(new Dimension(180, 80));
		
		add(Box.createVerticalStrut(3));
		add(name);
		add(Box.createVerticalGlue());
		m_container.add(visible);
		m_container.add(Box.createHorizontalStrut(3));
		m_container.add(active);
		add(m_container);
		add(Box.createVerticalStrut(3));
		
		manager.addLayerChangeEventListener(this);
	}

	@Override
	public void changeLayers(LayerChangeEvent lce) {
		if (layer == lce.getActiveLayer()) {
			active.setEnabled(false);
			setBorder(BorderFactory.createTitledBorder("Active Layer"));
		} else if (layer == lce.getOldLayer()) {
			active.setEnabled(true);
			setBorder(null);
		}
		this.repaint();
	}
}
