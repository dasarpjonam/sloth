package org.ladder.recognition.bullseye;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

public class BullseyePanel extends JPanel {

	Bullseye m_b;
	public BullseyePanel(Bullseye b){
		m_b=b;
		setBackground(Color.WHITE);
	}
	
	@Override
	public Dimension getPreferredSize(){
		return new Dimension(800,800);
	}
	
	public void paintComponent(Graphics g){
		g.translate(200, 200);
		m_b.paint(g);
	}
}
