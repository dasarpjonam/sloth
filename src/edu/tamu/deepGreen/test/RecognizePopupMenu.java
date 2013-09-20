package edu.tamu.deepGreen.test;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class RecognizePopupMenu extends JPopupMenu implements MouseListener
{
	/**
	 * Generated id
	 */
	private static final long serialVersionUID = -8933917989288795980L;

	/**
	 * Constants
	 */
	private final String ICON_PATH = "src/edu/tamu/deepGreen/test/Icons/";

	/**
	 * The icon for the recognize button
	 */
	private Image m_recognizeImage;

	/**
	 * Pressed icon for recognize button
	 */
	private Image m_pressedImage;

	/**
	 * Used to determine which icon to paint
	 */
	private Image m_drawImage;

	/**
	 * Used to determine if button is pressed, since button is round
	 */
	private boolean m_isPressed = false;

	/**
	 * Background image in case pop-up menu becomes opaque
	 */
	private static BufferedImage m_popupBgImage;


	public RecognizePopupMenu()
	{
		m_recognizeImage = null;
		m_pressedImage = null;

		setDefaultImage(ICON_PATH + "recognizeButton.png");
		setPressedImage(ICON_PATH + "recognizeButtonPressed.png");

		initialize();
	}

	private void initialize()
	{

		setPreferredSize(new Dimension(m_recognizeImage.getWidth(null), m_recognizeImage.getHeight(null)));
		setOpaque(false);
		setBackground(new Color(1.0f, 0.0f, 1.0f, 0.0f));
		setBorderPainted(false);
		addMouseListener(this);
		m_drawImage = m_recognizeImage;
	}

	public void setDefaultImage(String path)
	{
		m_recognizeImage = null;
		try 
		{
			File f = new File(path);
			m_recognizeImage = ImageIO.read(f);
		} 
		catch (IOException e) 
		{
		}
	}

	public void setPressedImage(String path)
	{
		m_pressedImage = null;
		try 
		{
			File f = new File(path);
			m_pressedImage = ImageIO.read(f);
		} 
		catch (IOException e) 
		{
		}
	}

	public int getWidth()
	{
		if (m_recognizeImage != null)
			return m_recognizeImage.getWidth(null);
		else
			return 0;
	}

	public boolean isPressed()
	{
		return m_isPressed;
	}

	/**
	 * Add a background capture method to show() to help make the 
	 *    pop-up menu transparent if it extends beyond the frame
	 */
	public void show(Component invoker, int x, int y) 
	{
		//captureBackground(invoker, x, y);
		//repaint();
		super.show(invoker, x, y);
	}

	private void captureBackground(Component c, int x, int y) 
	{
		if (m_recognizeImage != null)
		{
			try {
				final Robot robot = new Robot();
				Point correctLocation = new Point(x, y);
				SwingUtilities.convertPointToScreen(correctLocation, c);
				final Rectangle localRectangle = new Rectangle(correctLocation.x,
															   correctLocation.y,
															   m_recognizeImage.getWidth(null),
															   m_recognizeImage.getHeight(null));
				m_popupBgImage = robot.createScreenCapture(localRectangle);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void paintComponent(Graphics g)
	{
		if (m_popupBgImage != null)
		{
			Graphics2D g2 = (Graphics2D)g;
			g2.drawImage(m_popupBgImage,
					0, 0,
					m_popupBgImage.getWidth(null),
					m_popupBgImage.getHeight(null),
					0, 0,
					m_popupBgImage.getWidth(null),
					m_popupBgImage.getHeight(null),
					null);
		}
		
		if (m_drawImage != null)
		{
			Graphics2D g2 = (Graphics2D)g;
			g2.drawImage(m_drawImage,
					0, 0,
					m_drawImage.getWidth(null),
					m_drawImage.getHeight(null),
					0, 0,
					m_drawImage.getWidth(null),
					m_drawImage.getHeight(null),
					null);
		}
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) 
	{
		if (arg0.getButton() == MouseEvent.BUTTON1)
		{
			if (m_pressedImage != null)
			{
				int centerX = m_pressedImage.getWidth(null) / 2 + getX();
				int centerY = m_pressedImage.getHeight(null) / 2 + getY();
				int distance = (int)java.lang.Math.sqrt((double)(java.lang.Math.pow(centerX - arg0.getX(), 2) + 
						java.lang.Math.pow(centerY - arg0.getY(), 2)));
				if (distance <= m_pressedImage.getWidth(null) / 2) 
				{
					m_drawImage = m_pressedImage;
					m_isPressed = true;
				}
				else
				{
					m_drawImage = m_recognizeImage;
					m_isPressed = false;
				}
				repaint();
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) 
	{
		// Always hide the button on left click
		if (arg0.getButton() == MouseEvent.BUTTON1)
		{
			m_drawImage = m_recognizeImage;
			repaint();
			setVisible(false);
		}
	}
}
