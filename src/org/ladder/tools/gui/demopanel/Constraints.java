package org.ladder.tools.gui.demopanel;

import java.awt.GridBagConstraints;
import java.awt.Insets;

public class Constraints extends GridBagConstraints
{
	/*** constructor ***/
	private Constraints(Builder builder)
	{
		gridx 		= builder.gridx;
		gridy 		= builder.gridy;
		
		anchor 		= builder.anchor;
		fill		= builder.fill;
		gridheight 	= builder.gridheight;
		gridwidth 	= builder.gridwidth;
		ipadx 		= builder.ipadx;
		ipady 		= builder.ipady;
		insets		= builder.insets;
		weightx		= builder.weightx;
		weighty		= builder.weighty;
	}
	
	public Constraints(int gridx, int gridy)
	{
		this.gridx 		= gridx;
		this.gridy 		= gridy;
	}
	
	/*** inner class ***/
	public static class Builder
	{
		/*** constructor ***/
		// Defaults the cell to grid position (0, 0).
		public Builder()
		{	this(0, 0);	}
		
		// Specifies the cell containing the leading edge of the component's display area, where the first cell in a row has gridx=0.
		// Specifies the cell at the top of the component's display area, where the topmost cell has gridy=0.
		public Builder(int gridx, int gridy)
		{	this.gridx = gridx;	this.gridy = gridy;	}
		
		/*** builder ***/
		public Constraints build()
		{	return new Constraints(this);		}

		/*** setters ***/
		// Used when the component is smaller than its display area. It determines where, within the display area, to place the component.
		public Builder anchor(int value)
		{	anchor = value; return this;		}
		
		// Used when the component's display area is larger than the component's requested size. It determines whether to resize the component, and if so, how. 
		public Builder fill(int value)
		{	fill = value; return this;			}
		
		// Specifies the number of cells in a row for the component's display area. 
		public Builder gridheight(int value)
		{	gridheight = value;	return this;	}
		
		// Specifies the number of cells in a column for the component's display area. 
		public Builder gridwidth(int value)
		{	gridwidth = value;	return this;	}
		
		// Specifies the internal padding of the component, how much space to add to the minimum width of the component. The width of the component is at least its minimum width plus (ipadx * 2) pixels. 
		public Builder ipadx(int value)
		{	ipadx = value;	return this;		}
		
		// Specifies the internal padding, that is, how much space to add to the minimum height of the component. The height of the component is at least its minimum height plus (ipady * 2) pixels. 
		public Builder ipady(int value)
		{	ipady = value;	return this;		}
		
		// Specifies the external padding of the component, the minimum amount of space between the component and the edges of its display area.
		public Builder insets(Insets value)
		{	insets = value;	return this;		}

		// Specifies how to distribute extra horizontal space.
		public Builder weightx(int value)
		{	weightx = value;	return this;	}
		
		// Specifies how to distribute extra vertical space.
		public Builder weighty(int value)
		{	weighty = value;	return this;	}
		
		/*** fields ***/
		// required fields
		private final int gridx;
		private final int gridy;
		
		// optional fields (set to default fields)
		private int anchor			= GridBagConstraints.CENTER;
		private int fill			= GridBagConstraints.NONE;
		private int gridheight		= 1;
		private int gridwidth		= 1;
		private int ipadx			= 0;
		private int ipady			= 0;
		private int weightx			= 0;
		private int weighty			= 0;
		private Insets insets		= new Insets(0, 0, 0, 0);
	}
	
	private static final long serialVersionUID = 1L;
}