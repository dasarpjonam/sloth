package org.ladder.tools.gui.event;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;

public class KeyboardShortcutEvent extends AbstractAction {
    /**
	 * Allows keyboard shortcut to 'press' a GUI Button
	 */
	private static final long serialVersionUID = 1L;
	private JButton button;

    public KeyboardShortcutEvent(JButton button) {
    	this.button = button;
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		this.button.doClick();
	}
}