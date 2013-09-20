package edu.tamu.deepGreen.test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class SaveAskDialog {

	/**
	 * Global variables
	 */
	private boolean m_isKeyPressed;
	private Object m_selectedValue;
	JDialog m_dialog;
	
	public SaveAskDialog()
	{
		m_isKeyPressed = false;
		m_selectedValue = null;
	}

	/**
	 * Show Option Dialog replaces JOptionPane.showConfirmDialog so that
	 *    arrow keys can be used to select buttons in the dialog
	 */
	public int showOptionDialog(Component parentComponent, Object message,
			String title, int optionType, int messageType, Icon icon,
			Object[] options, Object initialValue) 
	{

		// the JOptionPane to modify
		JOptionPane optionPane = new JOptionPane(message, messageType, optionType,
				icon, options, initialValue);
		
		optionPane.setInitialValue(initialValue);
		
		HashSet<AWTKeyStroke> forwardSet = new HashSet<AWTKeyStroke>();
		forwardSet.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_RIGHT, 0, true));
		forwardSet.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_UP, 0, true));
		forwardSet.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0, true));
		
		optionPane.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardSet);
		
		HashSet<AWTKeyStroke> backwardSet = new HashSet<AWTKeyStroke>();
		backwardSet.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK, true));
		backwardSet.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_LEFT, 0, true));
		backwardSet.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_DOWN, 0, true));

		optionPane.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardSet);
		
		m_dialog = optionPane.createDialog(parentComponent, title);
		
		ActionListener yesActionListener = new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				m_isKeyPressed = true;
				m_dialog.dispose();
				m_selectedValue = new Integer(0);
			}
		};
		
		KeyStroke yKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, 0, true);
		optionPane.registerKeyboardAction(yesActionListener, yKeyStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		ActionListener noActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				m_isKeyPressed = true;
				m_dialog.dispose();
				m_selectedValue = new Integer(1);
			}
		};
		
		KeyStroke nKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_N, 0, true);
		optionPane.registerKeyboardAction(noActionListener, nKeyStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		optionPane.selectInitialValue();

		m_dialog.setVisible(true);

		if (!m_isKeyPressed) 
		{
			m_dialog.dispose();
			m_selectedValue = optionPane.getValue();

			m_isKeyPressed = false;
		} 
		else 
		{
			m_isKeyPressed = false;
			return ((Integer) m_selectedValue).intValue();
		}

		if (m_selectedValue == null) 
		{
			return JOptionPane.CLOSED_OPTION;
		}
		
		if (options == null) 
		{
			if (m_selectedValue instanceof Integer) 
			{
				return ((Integer) m_selectedValue).intValue();
			}
			return JOptionPane.CLOSED_OPTION;
		}

		for (int counter = 0, maxCounter = options.length; counter < maxCounter; counter++) 
		{
			if (options[counter].equals(m_selectedValue)) 
			{
				return counter;
			}
		}
		return JOptionPane.CLOSED_OPTION;
	}
	
	public void hide()
	{
		m_dialog.dispose();
	}
	
	public boolean isVisible()
	{
		return m_dialog.isVisible();
	}
}