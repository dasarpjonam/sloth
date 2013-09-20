package org.ladder.tools.gui.menus;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * This class simply overrides the JMenu class to allow the easy display
 * of menu items alphabetically according to the attached text.
 * @author tracy
 *
 */
public class SortedMenu extends JMenu {

	/**
	 * generated serial id
	 */
	private static final long serialVersionUID = -5107698940349529023L;

	public SortedMenu(String string) {
		super(string);
	}

	/**
	 * When adding in a JMenuItem, it inserts them in a sorted order
	 * according to the text of the JMenuItem.
	 * If assumes that things in the list are already sorted, else
	 * it may insert into the wrong point.
	 * @param item the JMenuItem to be inserted alphabetically
	 * @return the inserted JMenuItem
	 */
	public JMenuItem add (JMenuItem item){
	  boolean inserted = false;
	  //traverses through each item and inserts in the appropriate location
	  for(int pos = 0; pos < super.getItemCount(); pos++){
		 JMenuItem nextitem = super.getItem(pos);
		 if(nextitem.getText().compareToIgnoreCase(item.getText()) > 0){
		   super.insert(item, pos);		   
		   inserted = true;
		   break;
		 }
	  }
	  if (!inserted){
		super.add(item);
	  }
      return item;		
	}	
}
