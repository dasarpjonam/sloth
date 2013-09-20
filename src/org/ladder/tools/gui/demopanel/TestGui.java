package org.ladder.tools.gui.demopanel;

import java.awt.*;
import java.io.File;

import javax.swing.*;

public class TestGui extends Gui
{
	/*** primary methods ***/
	protected void addContent(JFrame frame)
	{
		Constraints c;
		
		// ---------------------------
		// initialize your panels here
		// ---------------------------
		JPanel greenPanel = getGreenPanel();	// get the green panel
		JPanel greyPanel = getGreyPanel();		// get the grey panel

		
		
		// -------------------------
        // position your panels here
		// -------------------------
		
		// place the green panel at grid position (0, 0)
        c = new Constraints.Builder(0, 0).build();
        frame.add(greenPanel, c);
        
        // place the green panel at grid position (0, 1)
        c = new Constraints.Builder(0, 1).build();
        frame.add(greyPanel, c);
	}
	
	private JPanel getGreenPanel()
	{
		Constraints c;
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);
		
		c = new Constraints.Builder().build();
		panel.add(new JLabel(new ImageIcon(GREEN_IMAGE_FILE_LOC)), c);
		
		return panel;
	}
	
	private JPanel getGreyPanel()
	{
		Constraints c;
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.WHITE);
		
		c = new Constraints.Builder().build();
		panel.add(new JLabel(new ImageIcon(GREY_IMAGE_FILE_LOC)), c);
		
		return panel;
	}
	
	/*** auxiliary methods ***/
	
	
	
	/*** main method ***/
	public static void main(String[] args)
	{
		Gui gui = new TestGui();
		gui.run();
	}
	
	
	
	/*** fields ***/
	public static final String GREEN_IMAGE_FILE_LOC = IMAGES_DIR_LOC + "green.png";
	public static final String GREY_IMAGE_FILE_LOC = IMAGES_DIR_LOC + "grey.png";
}