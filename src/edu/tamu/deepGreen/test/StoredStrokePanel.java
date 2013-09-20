package edu.tamu.deepGreen.test;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.*;
import java.io.File;

import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;


public class StoredStrokePanel extends JPanel implements MouseListener,
														 MouseMotionListener,
														 KeyListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6403844369528734933L;
	
	/**
	 * Constants
	 */
	private final int BOUND_PAD = 3;
	private final int CONTEXT_DISPLACE_X = 2;
	private final int CONTEXT_DISPLACE_Y = 2;
	private final String ICON_32_PATH = "src/edu/tamu/deepGreen/test/Icons/32/";
	private final int UP = 0;
	private final int DOWN = 1;
	private final int LEFT = 2;
	private final int RIGHT = 3;
	
	/**
	 * The array list to hold all the symbols
	 */ //delete this
	//private ArrayList<Symbol> m_symbols;
	
	/**
	 * Location of the mouse
	 */
	private java.awt.Point m_mouseLocation;
	
	/**
	 * Location of the mouse while dragging
	 */
	private java.awt.Point m_mouseDragLocation = new Point();
	
	/**
	 * Bounding box to highlight which shape is being hovered over
	 */
	private Rectangle m_hoverBox;
	
	/**
	 * Rectangle that is used to select multiple symbols
	 */
	private Rectangle m_selectionBox;
	
	/**
	 * Edit tool bar
	 */
	private JToolBar m_toolBar;
	
	/**
	 * Determines if the panel is able to receive key input
	 */
	private boolean m_isEnabled = false;
	
	public StoredStrokePanel()
	{
		initialize();
	}
	
	public StoredStrokePanel(ArrayList<Symbol> symbols)
	{
		for (Symbol symbol : symbols)
		{
			add(new Symbol(symbol), findInsertIndex(symbol.getArea()));
			getComponent(findInsertIndex(symbol.getArea())).addMouseListener(this);
			getComponent(findInsertIndex(symbol.getArea())).addMouseMotionListener(this);
		}
		initialize();
	}
	
	public StoredStrokePanel(Symbol[] symbols)
	{		
		for (Symbol symbol : symbols)
		{
			add(new Symbol(symbol), findInsertIndex(symbol.getArea()));
		}
		initialize();
	}
	
	private void initialize()
	{
		m_mouseLocation = new java.awt.Point();
		m_hoverBox = new Rectangle();
		m_selectionBox = new Rectangle();
		
		setEnabled(true);
		
		setDoubleBuffered(true);
		setOpaque(false);
		setFocusable(true);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		setLayout(null);
		
		m_toolBar = new JToolBar();
		constructEditToolBar(m_toolBar);
		hideEditToolBar();
	}
	
	/**
	 * Adds a new symbol to the panel
	 * of an image.
	 * 
	 * @param strokes
	 */
	public void addSymbol(Symbol symbol)
	{
		add(new Symbol(symbol), findInsertIndex(symbol.getArea()));
		getComponent(findInsertIndex(symbol.getArea())).addMouseListener(this);
		getComponent(findInsertIndex(symbol.getArea())).addMouseMotionListener(this);
		repaint();
	}
	
	/**
	 * Adds an image from a given filename to the stored stroke panel.
	 * The IPoint centerPoint object should be the desired center point
	 * of the symbol being added.  The scale determines the size of the
	 * symbol.
	 * @param imgLocation
	 * @param centerPoint
	 * @param scale
	 */
	public void addImage(String imgLocation, java.awt.Point centerPoint, Double scale)
	{
		Symbol s = new Symbol(imgLocation, (int)centerPoint.getX(), (int)centerPoint.getY(), scale);
		add(new Symbol(s), findInsertIndex(s.getArea()));
		repaint();
	}
	
	/**
	 * Adds an image from a given filename to the stored stroke panel.
	 * The centerX and centerY ints should define the desired center point
	 * of the symbol being added.  The scale determines the size of the
	 * symbol.
	 * @param imgLocation
	 * @param centerX
	 * @param centerY
	 * @param scale
	 */
	public void addImage(String imgLocation, int centerX, int centerY, Double scale)
	{
		Symbol s = new Symbol(imgLocation, centerX, centerY, scale);
		add(new Symbol(s), findInsertIndex(s.getArea()));
		repaint();
	}
	
	/**
	 * Adds an image from a given BufferedImage to the stored stroke panel.
	 * The IPoint centerPoint object should be the desired center point
	 * of the symbol being added.  The scale determines the size of the
	 * symbol.
	 * @param img
	 * @param centerPoint
	 * @param scale
	 */
	public void addImage(BufferedImage img, java.awt.Point centerPoint, Double scale)
	{
		Symbol s = new Symbol(img, (int)centerPoint.getX(), (int)centerPoint.getY(), scale);
		add(new Symbol(s), findInsertIndex(s.getArea()));
		repaint();
	}
	
	/**
	 * Adds an image from a given BufferedImage to the stored stroke panel.
	 * The centerX and centerY ints should define the desired center point
	 * of the symbol being added.  The scale determines the size of the
	 * symbol.
	 * @param img
	 * @param centerX
	 * @param centerY
	 * @param scale
	 */
	public void addImage(BufferedImage img, int centerX, int centerY, Double scale)
	{
		Symbol s = new Symbol(img, centerX, centerY, scale);
		add(new Symbol(s), findInsertIndex(s.getArea()));
		repaint();
	}
	
	/**
	 * Find the index to insert the symbol into m_symbols
	 *    based on area of the symbol
	 *    m_symbols must be sorted by symbol area, with smaller
	 *    areas first in the list
	 * @param area
	 * @return
	 */
	private int findInsertIndex(int area)
	{
		int index = 0;
		
		int size = getComponentCount();
		for (int i = 0; i < size; i++)
		{
			Symbol s = (Symbol)getComponent(i);
			if (area > s.getArea())
			{
				index = i+1;
			}
		}
		
		return index;
	}
	
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		
		// draw the select box around the selected symbols
		g2.setColor(Color.BLUE);
		for (Component c : getComponents())
		{
			Symbol symbol = (Symbol)c;
			if (symbol.isSelected())
			{
				g2.drawRect((int)symbol.getX() - BOUND_PAD,
							(int)symbol.getY() - BOUND_PAD,
							(int)symbol.getWidth() + 2 * BOUND_PAD,
							(int)symbol.getHeight() + 2 * BOUND_PAD);
			}
		}
		
		
		// draw the selection box, if there is one
		if (m_selectionBox.getWidth() != 0)
		{
			g2.setColor(Color.BLACK);
			
			g2.drawLine((int)m_selectionBox.getX(),
						(int)m_selectionBox.getY(),
						(int)(m_selectionBox.getX() + m_selectionBox.getWidth()),
						(int)m_selectionBox.getY());
			g2.drawLine((int)(m_selectionBox.getX() + m_selectionBox.getWidth()),
						(int)m_selectionBox.getY(),
						(int)(m_selectionBox.getX() + m_selectionBox.getWidth()),
						(int)(m_selectionBox.getY() + m_selectionBox.getHeight()));
			g2.drawLine((int)(m_selectionBox.getX() + m_selectionBox.getWidth()),
						(int)(m_selectionBox.getY() + m_selectionBox.getHeight()),
						(int)m_selectionBox.getX(),
						(int)(m_selectionBox.getY() + m_selectionBox.getHeight()));
			g2.drawLine((int)m_selectionBox.getX(),
						(int)(m_selectionBox.getY() + m_selectionBox.getHeight()),
						(int)m_selectionBox.getX(),
						(int)m_selectionBox.getY());
		}
	}
	
	/**
	 * Clears out all the Components (Symbols)
	 */
	public void clear()
	{
		removeAll();
	}
	
	/**
	 * Show the edit tool bar
	 */
	public void showEditToolBar()
	{
		m_toolBar.setVisible(true);
	}
	
	/**
	 * Hide the edit tool bar
	 */
	public void hideEditToolBar()
	{
		m_toolBar.setVisible(false);
	}
	
	/**
	 * Return the edit tool bar
	 */
	public JToolBar getEditToolBar()
	{
		return m_toolBar;
	}
	
	/**
	 * Construct an edit tool bar for manipulating symbols
	 */
	public void constructEditToolBar(JToolBar toolBar)
	{
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		
		// make a select all button
		JButton selectAllButton = new JButton();
		ImageIcon selectAllIcon = new ImageIcon(ICON_32_PATH + "selectAll.png");
		selectAllButton.setIcon(selectAllIcon);
		selectAllButton.setMnemonic(KeyEvent.VK_A);
		selectAllButton.setToolTipText("Select all symbols (Ctrl+A)");
		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectAllSymbols();
			}
		});
		selectAllButton.setFocusable(false);
		
		// make a deselect button
		JButton deselectButton = new JButton();
		ImageIcon deselectIcon = new ImageIcon(ICON_32_PATH + "deselect.png");
		deselectButton.setIcon(deselectIcon);
		deselectButton.setMnemonic(KeyEvent.VK_A);
		deselectButton.setToolTipText("Deselect all symbols (Ctrl+Shift+A)");
		deselectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deselectSymbols();
			}
		});
		deselectButton.setFocusable(false);
		
		// make a duplicate button
		JButton duplicateButton = new JButton();
		ImageIcon duplicateIcon = new ImageIcon(ICON_32_PATH + "duplicate.png");
		duplicateButton.setIcon(duplicateIcon);
		duplicateButton.setMnemonic(KeyEvent.VK_A);
		duplicateButton.setToolTipText("Duplicate selected symbols (Ctrl+D)");
		duplicateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				duplicateSelectedSymbols();
			}
		});
		duplicateButton.setFocusable(false);
		
		// make a delete button
		JButton deleteButton = new JButton();
		ImageIcon deleteIcon = new ImageIcon(ICON_32_PATH + "delete.png");
		deleteButton.setIcon(deleteIcon);
		deleteButton.setMnemonic(KeyEvent.VK_A);
		deleteButton.setToolTipText("Delete selected symbols (Del)");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteSelectedSymbols();
			}
		});
		deleteButton.setFocusable(false);
		
		// make a toggle draw types button
		JButton toggleDrawTypesButton = new JButton();
		ImageIcon toggleDrawTypesIcon = new ImageIcon(ICON_32_PATH + "toggleDrawType.png");
		toggleDrawTypesButton.setIcon(toggleDrawTypesIcon);
		toggleDrawTypesButton.setMnemonic(KeyEvent.VK_T);
		toggleDrawTypesButton.setToolTipText("Toggle Selected Symbols Draw Type (Ctrl+T)");
		toggleDrawTypesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				toggleSelectedSymbolsDrawType();
			}
		});
		toggleDrawTypesButton.setFocusable(false);
		
		toolBar.add(selectAllButton);
		toolBar.add(deselectButton);
		toolBar.add(toggleDrawTypesButton);
		toolBar.add(duplicateButton);
		toolBar.add(deleteButton);
		
		//this.add(toolBar);
	}
	
	/**
	 * Construct the context menu for a symbol
	 */
	private void constructSymbolContextMenu(JPopupMenu menu)
	{
		// Duplicate symbol menu item
		JMenuItem duplicateSymbolMenuItem = new JMenuItem("Duplicate");
		duplicateSymbolMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				duplicateSelectedSymbols();
			}
		});
		
		// Delete symbol menu item
		JMenuItem deleteSymbolMenuItem = new JMenuItem("Delete");
		deleteSymbolMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteSelectedSymbols();
			}
		});
		
		// change draw type menu item
		JMenuItem drawTypeMenuItem = new JMenuItem();
	    
		if (getSelectedSymbolCount() == 1)
		{		
			for (Component c : getComponents())
			{
				Symbol symbol = (Symbol)c;
				if (symbol.isSelected())
				{
					switch(symbol.getDrawType())
					{
						case Symbol.SYMBOL:
							drawTypeMenuItem.setText("Change to strokes");
							break;
						case Symbol.STROKES:
							if (new File(symbol.getPath()).exists())
							{
								drawTypeMenuItem.setText("Change to image");
							}
							else
							{
								drawTypeMenuItem.setText("Switch draw type");
								drawTypeMenuItem.setEnabled(false);
							}
							break;
						default:
							drawTypeMenuItem.setText("Switch draw type");
							drawTypeMenuItem.setEnabled(false);
					}
				}
			}
		}
		else
		{
			drawTypeMenuItem.setText("Switch draw type");
		}
		drawTypeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleSelectedSymbolsDrawType();
			}
		});
		
		menu.add(drawTypeMenuItem);
		menu.add(duplicateSymbolMenuItem);
		menu.add(deleteSymbolMenuItem);
	}
	
	/**
	 * Duplicate all selected symbols
	 */
	private void duplicateSelectedSymbols()
	{		
		// copy all symbols that are selected
		for (Component c : getComponents())
		{
			Symbol symbol = (Symbol)c;

			if (symbol.isSelected())
			{
				Symbol newSymbol = new Symbol(symbol);
				newSymbol.setLocation(symbol.getLocation().x - 20,
								   	  symbol.getLocation().y - 20);
				newSymbol.setSelected(true);
				newSymbol.addMouseListener(this);
				newSymbol.addMouseMotionListener(this);
				int newIndex = findInsertIndex(newSymbol.getArea());
				add(newSymbol, newIndex);
			}
		}

		repaint();
	}
	
	/**
	 * Delete any selected symbols
	 */
	private void deleteSelectedSymbols()
	{
		int size = getComponentCount();
		for (int i = size-1; i >= 0; i--)
		{
			Component c = getComponent(i);
			Symbol s = (Symbol)c;
			if (s.isSelected())
				remove(i);
		}
		deselectSymbols();
	}
	
	/**
	 * Deselect all symbols
	 */
	public void deselectSymbols()
	{
		for (Component c : getComponents())
		{
			Symbol s = (Symbol)c;
			if (s.isSelected())
				s.setSelected(false);
		}
		m_hoverBox = new Rectangle();
		repaint();
	}
	
	/**
	 * Select all symbols
	 */
	private void selectAllSymbols()
	{
		for (Component c : getComponents())
		{
			Symbol s = (Symbol)c;
			if (!s.isSelected())
				s.setSelected(true);
		}
		repaint();
	}
	
	/**
	 * Select all symbols that intersect the selection box
	 */
	private void selectSymbolsInSelectionBox()
	{
		// normalize the selection box points
		int lowX = m_selectionBox.x < m_selectionBox.x + m_selectionBox.width ? 
				   m_selectionBox.x : m_selectionBox.x + m_selectionBox.width;
		int lowY = m_selectionBox.y < m_selectionBox.y + m_selectionBox.height ? 
				   m_selectionBox.y : m_selectionBox.y + m_selectionBox.height;
		int highY = m_selectionBox.y > m_selectionBox.y + m_selectionBox.height ? 
				    m_selectionBox.y : m_selectionBox.y + m_selectionBox.height;
		int highX = m_selectionBox.x > m_selectionBox.x + m_selectionBox.width ? 
				    m_selectionBox.x : m_selectionBox.x + m_selectionBox.width;
		
		m_selectionBox = new Rectangle(lowX, lowY, highX - lowX, highY - lowY);
		
		for (Component c : getComponents())
		{
			Symbol symbol = (Symbol)c;
			Rectangle symbolRect = new Rectangle(symbol.getX(), symbol.getY(),
												 symbol.getWidth(), symbol.getHeight());
			
			if (intersectRectangles(symbolRect, m_selectionBox))
			{
				symbol.setSelected(true);
			}
		}
		repaint();
	}
	
	/**
	 * Returns true if the rectangles intersect.
	 * Returns false is the rectangles do not intersect.
	 */
	private boolean intersectRectangles(Rectangle r1, Rectangle r2)
	{
		// find left/right rectangles
		Rectangle leftRect = new Rectangle(r1);
		Rectangle rightRect = new Rectangle(r2);
		if (r2.getX() < r1.getX())
		{
			leftRect = new Rectangle(r2);
			rightRect = new Rectangle(r1);
		}
		
		// check left and right x
		if (rightRect.getX() > leftRect.getX() + leftRect.getWidth())
			return false;
		
		// find top/bottom rectangles
		Rectangle topRect = new Rectangle(r1);
		Rectangle bottomRect = new Rectangle(r2);
		if (r2.getY() < r1.getY())
		{
			topRect = new Rectangle(r2);
			bottomRect = new Rectangle(r1);
		}
		
		// check top and bottom y
		if (bottomRect.getY() > topRect.getY() + topRect.getHeight())
			return false;		
		
		return true;
	}
	
	/**
	 * Returns true if the mouse is over one of the components (Symbol)
	 */
	private boolean isMouseOverSymbol()
	{
		for (Component c : getComponents())
		{
			Symbol s = (Symbol)c;
			if (s.isMouseOver())
				return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the mouse is over a selected component (Symbol)
	 */
	private boolean isMouseOverSelectedSymbol()
	{
		for (Component c : getComponents())
		{
			Symbol s = (Symbol)c;
			if (s.isMouseOver() && s.isSelected())
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the Symbol object that the mouse is over.
	 * @return
	 */
	private Symbol getMouseOverSymbol()
	{
		Symbol s = new Symbol();
		
		for (Component c : getComponents())
		{
			s = (Symbol)c;
			if (s.isMouseOver())
				return s;
		}
		
		return null;
	}
	
	/**
	 * Returns the number of symbols in the panel (identical to getComponentCount())
	 */
	public int getNumSymbols()
	{
		return getComponentCount();
	}
	
	/**
	 * Returns the number of selected symbols
	 */
	private int getSelectedSymbolCount()
	{
		int count = 0;
		
		for (Component c : getComponents())
		{
			Symbol s = (Symbol)c;
			if (s.isSelected())
				count++;
		}
		
		return count;
	}
	
	/**
	 * Returns a list of all symbols contained in the StoredStrokePanel.
	 * @return
	 */
	public ArrayList<Symbol> getSymbolList()
	{
		ArrayList<Symbol> symbols = new ArrayList<Symbol>();
		
		for (Component c : getComponents())
		{
			Symbol s = (Symbol)c;
			symbols.add(new Symbol(s));
		}
		
		return symbols;
	}
	
	/**
	 * Returns an array of symbols
	 */
	public Symbol[] getSymbolArray()
	{
		Symbol[] symbols = new Symbol[getComponentCount()];
		
		int size = getComponentCount();
		for (int i = 0; i < size; i++)
		{
			symbols[i] = new Symbol((Symbol)getComponent(i));
		}
		
		return symbols;
	}
	
	/**
	 * Clear out the images in the symbol list
	 */
	public void enterSaveState()
	{
		for (Component c : getComponents())
		{
			Symbol symbol = (Symbol)c;
			symbol.enterSaveState();
		}
	}
	
	/**
	 * Reload the images in the symbol list
	 */
	public void enterOpenState()
	{
		for (Component c : getComponents())
		{
			Symbol symbol = (Symbol)c;
			symbol.enterOpenState();
		}
		
		repaint();
	}
	
	/**
	 * Enable or disable the panel
	 */
	@Override
	public void setEnabled(boolean b)
	{
		m_isEnabled = b;
	}
	
	/**
	 * Move all selected symbols by one pixel
	 */
	private void nudgeSelectedSymbols(int direction)
	{
		int xOffset = 0;
		int yOffset = 0;
		
		switch (direction)
		{
			case UP: 
				xOffset = 0;
				yOffset = -1;
				break;
				
			case DOWN:
				xOffset = 0;
		 		yOffset = 1;
		 		break;
		 		
			case LEFT: 
				xOffset = -1;
				yOffset = 0;
				break;
				
			case RIGHT: 
				xOffset = 1;
				yOffset = 0;
				break;
				
			default:
				xOffset = 0;
				yOffset = 0;
		}
		
		// move all selected symbols
		for (Component c : getComponents())
		{
			Symbol s = (Symbol)c;
			if (s.isSelected())
				s.setLocation(s.getLocation().x + xOffset, 
							  s.getLocation().y + yOffset);
		}
		
		// erase the bounding box
		m_hoverBox = new Rectangle();
		
		repaint();
	}
	
	/**
	 * Toggle draw type of all selected symbols
	 */
	private void toggleSelectedSymbolsDrawType()
	{
		for (Component c : getComponents())
		{
			Symbol s = (Symbol)c;
			if (s.isSelected())
				s.toggleDrawType();
		}
		m_hoverBox = new Rectangle();
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) 
	{
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) 
	{
		// left click
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			m_mouseLocation = e.getPoint();
			m_mouseDragLocation = new Point(m_mouseLocation);
			
			if (isMouseOverSymbol())
			{
				for (Component c : getComponents())
				{
					Symbol s = (Symbol)c;
					if (s.isMouseOver())
					{
						s.setSelected(true);
						repaint();
						break;
					}
				}
			}
			else if (!isMouseOverSelectedSymbol() &&
					 !e.isControlDown())
			{
				deselectSymbols();
			}
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) 
	{
		// show context menu for a symbol
		if (e.isPopupTrigger() && isMouseOverSymbol())
		{
			// select the symbol first so the context menu will work
			if (getComponentCount() > 0 &&
				getSelectedSymbolCount() == 0)
			{
				deselectSymbols();
				Symbol s = getMouseOverSymbol();
				s.setSelected(true);
				repaint();
			}
			
			JPopupMenu p = new JPopupMenu();
			constructSymbolContextMenu(p);
			
			
			p.addPopupMenuListener(new PopupMenuListener() 
			{
				@Override
				public void popupMenuCanceled(PopupMenuEvent arg0) {
					deselectSymbols();
					m_mouseLocation = java.awt.MouseInfo.getPointerInfo().getLocation();
					m_mouseLocation.x -= StoredStrokePanel.this.getLocationOnScreen().x;
					m_mouseLocation.y -= StoredStrokePanel.this.getLocationOnScreen().y;
				}
				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {}
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {}
				
			});
			
			p.show(this, e.getX() + CONTEXT_DISPLACE_X, e.getY() + CONTEXT_DISPLACE_Y);
		}
		
		// left click
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			// select all symbols that are in the selection box
			if (m_selectionBox.getWidth() != 0)
			{
				selectSymbolsInSelectionBox();
				m_selectionBox = new Rectangle();
				repaint();
			}
			// if there is more than one symbol selected and the mouse has not
			//   been dragged, select the symbol being hovered over
			else if (getSelectedSymbolCount() > 1 &&
					 m_mouseDragLocation.getX() == m_mouseLocation.x &&
					 m_mouseDragLocation.getY() == m_mouseLocation.y)
			{
					deselectSymbols();
					for (Component c : getComponents())
					{
						Symbol s = (Symbol)c;
						if (s.isMouseOver())
							s.setSelected(true);
					}
					repaint();
			}
			else if (!isMouseOverSymbol())
			{
				deselectSymbols();
			}
		}
	}

	/**
	 * When dragging, move all selected symbols,
	 *    or create a selection box if no symbols are selected
	 */
	@Override
	public void mouseDragged(MouseEvent e) 
	{
		m_mouseDragLocation = e.getPoint();
		
		// only if left mouse button is held down
		if (SwingUtilities.isLeftMouseButton(e))
		{
			if (getSelectedSymbolCount() > 0)
			{
				// we must have some components to move
				if (getComponentCount() > 0)
				{
					// calculate the distance traveled by the mouse
					int xOffset = (int)(e.getX() - m_mouseLocation.getX());
					int yOffset = (int)(e.getY() - m_mouseLocation.getY());
					
					// move all selected symbols
					for (Component c : getComponents())
					{
						Symbol symbol = (Symbol)c;
						if (symbol.isSelected())
						{
							symbol.setLocation(symbol.getX() + xOffset, 
											   symbol.getY() + yOffset);
						}
					}
					
					// erase the bounding box
					m_hoverBox = new Rectangle();
					
					repaint();
				}
			}
			else
			{
				// draw the selection box
				m_selectionBox = new Rectangle((int)m_mouseLocation.getX(),
										 	   (int)m_mouseLocation.getY(),
										 	   e.getX() - (int)m_mouseLocation.getX(),
										 	   e.getY() - (int)m_mouseLocation.getY());
				repaint();
			}
		}
	}

	/**
	 * If the mouse is over a symbol, highlight that symbol
	 *    by drawing a box around it.
	 */
	@Override
	public void mouseMoved(MouseEvent e) 
	{
		// check all symbols
		//    break on the first symbol hovered over 
		//    (so all symbols can be reached, list is sorted
		//     with small areas first)
		int size = getComponentCount();
		for (int i = 0; i < size; i++)
		{	
			Symbol symbol = (Symbol)getComponent(i);
			
			// if the mouse is over a symbol
			if (e.getX() > symbol.getX() &&
				e.getX() < symbol.getX() + symbol.getWidth() &&
				e.getY() > symbol.getY() &&
				e.getY() < symbol.getY() + symbol.getHeight())
			{
				// initialize the bounding box
				m_hoverBox = new Rectangle(symbol.getX() - BOUND_PAD,
											  symbol.getY() - BOUND_PAD,
											  symbol.getWidth() + 2 * BOUND_PAD,
											  symbol.getHeight() + 2 * BOUND_PAD);
				
				repaint();
				break;
			}
		}
		
		// Check to see if the mouse is not over a symbol and the highlight
		//   box is drawn somewhere, and if so, erase it
		if (m_hoverBox.getWidth() != 0)
		{
			// if the mouse is not over the hover box, erase the hover box
			if (e.getX() < m_hoverBox.getX() ||
				e.getX() > m_hoverBox.getX() + m_hoverBox.getWidth() ||
				e.getY() < m_hoverBox.getY() ||
				e.getY() > m_hoverBox.getY() + m_hoverBox.getHeight())
			{
				m_hoverBox = new Rectangle();
				
				repaint();
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) 
	{
		if (m_isEnabled)
		{
			if (e.getModifiers() == KeyEvent.CTRL_DOWN_MASK ||
				e.getModifiers() == KeyEvent.CTRL_MASK)
			{
				if (e.getKeyCode() == KeyEvent.VK_D)
				{
					duplicateSelectedSymbols();
				}
				else if (e.getKeyCode() == KeyEvent.VK_A)
				{
					selectAllSymbols();
				}
				else if (e.getKeyCode() == KeyEvent.VK_T)
				{
					toggleSelectedSymbolsDrawType();
				}
			}
			else if (e.getModifiers() == (KeyEvent.CTRL_DOWN_MASK|KeyEvent.SHIFT_DOWN_MASK) ||
					 e.getModifiers() == (KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK))
			{
				if (e.getKeyCode() == KeyEvent.VK_A)
				{
					deselectSymbols();
				}
			}
			else if (e.getModifiers() == 0)
			{
				if (e.getKeyCode() == KeyEvent.VK_DELETE)
				{					
					deleteSelectedSymbols();
				}
				else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					deselectSymbols();
				}
				else if (e.getKeyCode() == KeyEvent.VK_UP)
				{
					nudgeSelectedSymbols(UP);
				}
				else if (e.getKeyCode() == KeyEvent.VK_DOWN)
				{
					nudgeSelectedSymbols(DOWN);
				}
				else if (e.getKeyCode() == KeyEvent.VK_LEFT)
				{
					nudgeSelectedSymbols(LEFT);
				}
				else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
				{
					nudgeSelectedSymbols(RIGHT);
				}
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}
}

