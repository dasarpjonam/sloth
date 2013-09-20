package test.srbook;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IPoint;
import org.ladder.engine.command.AddStrokeCommand;
import org.ladder.engine.command.CommandExecutionException;

public class CreationDrawPanelUI extends DrawPanelUI {

	/**
	 * Auto-gen serial ID
	 */
	private static final long serialVersionUID = -1674163432446330200L;

	/**
	 * Logger for the draw panel
	 */
	private static Logger log = LadderLogger
			.getLogger(CreationDrawPanelUI.class);

	/**
	 * Rubine recognizer
	 */
	private static boolean s_rubine = false;

	private static CreationFrame m_cf;

	/**
	 * Default constructor
	 */
	CreationDrawPanelUI(CreationFrame cf) {
		super();
		m_cf = cf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent arg0) {

		log.debug("Mouse released, m_isDrawing == " + isDrawing());

		if (isDrawing()) {

			IPoint p = getCurrentLadderStroke().getPoints().get(
					getCurrentLadderStroke().getNumPoints() - 1);

			log.debug("Draw last point to screen");
			drawPointToPoint((int) p.getX(), (int) p.getY(), arg0.getX(), arg0
					.getY());
			

			log.debug("Add last point to m_currentPoints");
			addPointToCurrent(arg0.getX(), arg0.getY());

			log.debug("add m_currentPoints to sketch");

			try {
				// Run recognizers?
				if (s_rubine) {
					m_cf.outputRubineFeatures(getCurrentLadderStroke());
				}

				// Add the stroke to the engine
				getEngine().execute(
						new AddStrokeCommand(getCurrentLadderStroke()));
			} catch (CommandExecutionException cee) {
				log.error(cee);
				JOptionPane.showMessageDialog(this, cee,
						"Command Execution Error", JOptionPane.ERROR_MESSAGE);
			}

			log.debug("m_isDrawing == " + isDrawing());

			setIsDrawing(false);
		}
	}


	/**
	 * Toggle the state of the Rubine Recognizer.
	 */
	public void toggleRubine() {
		s_rubine = !s_rubine;
	}

}
