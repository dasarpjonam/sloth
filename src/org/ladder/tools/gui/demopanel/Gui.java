package org.ladder.tools.gui.demopanel;

import java.awt.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

public abstract class Gui
{
	/*** primary methods ***/
	public final void run()
	{
		buildFrame();
	}
	
	protected final JFrame buildFrame()
	{
        // build the frame
		JFrame frame = new JFrame();
        frame.getContentPane().setBackground(Color.WHITE);		// set background color to white
        frame.setLayout(new GridBagLayout());					// set layout manager to GridBagLayout
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 	// exit program when frame is closed
        
        addContent(frame);										// add the panels to frame
        
        frame.pack();											// size frame
        frame.setVisible(true);									// show frame
        frame.setLocationRelativeTo(null);						// center frame
        
        return frame;
	}
	
	protected void addContent(JFrame frame)
	{
		;
	}
	
	/*** auxiliary methods ***/
	public final static String getImagesDirLoc()
	{
		String dirLoc = "";
		
		try {
			dirLoc = new File(".").getCanonicalPath();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return dirLoc + "/src/org/ladder/tools/gui/demopanel/images/";
	}
	
	
	
	/*** fields ***/
	public static final String IMAGES_DIR_LOC = getImagesDirLoc();
}