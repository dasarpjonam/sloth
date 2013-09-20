package edu.tamu.deepGreen.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.core.sketch.Stroke;

public class Symbol extends JPanel implements Serializable,
											  MouseListener
{
	/**
	 * Generated ID
	 */
	private static final long serialVersionUID = 2096457799927449292L;

	/**
	 * Constants
	 */
	static final int SYMBOL = 0;
	static final int STROKES = 1;
	private final String SYMBOL_PATH = "src/edu/tamu/deepGreen/test/Symbols/";
	private final String NAME_PATH = "src/edu/tamu/deepGreen/test/Names/";
	private final String PLATOON_SYMBOL = "***";
	private final String COMPANY_SYMBOL = "1";
	private final String BATTALION_SYMBOL = "11";
	private final String BRIGADE_SYMBOL = "X";
	private final double DIAMETER_RATIO = 0.3;
	private final double SPACE_RATIO = 0.05;
	private final int ECHELON_INDEX = 11;
	private final String COMPANY = "CO";
	private final String BATTALION = "BN";
	private final String BRIGADE = "BDE";
	private final String PLATOON = "PLT";
	private final char COMPANY_CHAR = 'E';
	private final char BATTALION_CHAR = 'F';
	private final char BRIGADE_CHAR = 'H';
	private final char PLATOON_CHAR = 'D';
	private final String COMPANY_DRAW_SYMBOL = "|";
	private final String BATTALION_DRAW_SYMBOL = "|  |";

	/**
	 * Path name
	 */
	private String m_path;

	/**
	 * Symbol name
	 */
	private String m_name = "";

	/**
	 * Pretty symbol name
	 */
	private String m_readableName = "";

	/**
	 * Modifiers as a readable string
	 */
	private String m_modifiers = "";

	/**
	 * Symbol number
	 */
	private String m_number = "";

	/**
	 * Symbol SIDC
	 */
	private String m_sidc = "";

	/**
	 * Shape which contains all information needed
	 */
	private IShape m_shape;

	/**
	 * Label returned by the recognizer
	 */
	private String m_recognitionLabel = "";

	/**
	 * Position
	 */
	private Point m_center;

	/**
	 * Scale
	 */
	private Double m_scale;

	/**
	 * The image of the symbol
	 */
	private Image m_img;

	/**
	 * Starting x point of the symbol
	 */
	private int m_xPos = 0;

	/**
	 * Starting y point of the symbol
	 */
	private int m_yPos = 0;

	/**
	 * What to render
	 */
	private int m_drawObject = SYMBOL;

	/**
	 * Special case
	 */
	private boolean m_specialCase = false;
	
	/**
	 * Is selected or not
	 */
	private boolean m_isSelected = false;
	
	/**
	 * Is the mouse over this symbol
	 */
	private boolean m_isMouseOver = false;
	
	/**
	 * Determine if the highlight is to be painted
	 */
	private boolean m_paintHighlight = false;

	public Symbol()
	{
		m_path = "";
		m_name = "";
		m_center = new Point();
		m_scale = 1.0;
		m_img = null;
		m_xPos = 0;
		m_yPos = 0;
		initialize();
	}

	public Symbol(String name, Point point, Double scale)
	{
		m_name = name;
		setReadableName();

		m_path = SYMBOL_PATH + m_name + ".png";
		m_center = new Point(point);
		m_scale = scale;
		m_img = null;
		m_xPos = 0;
		m_yPos = 0;
		openImage();
		initialize();
	}

	public Symbol(String name, int x, int y, Double scale)
	{
		m_name = name;
		setReadableName();

		m_path = SYMBOL_PATH + m_name + ".png";
		m_center = new Point(x, y);
		m_scale = scale;
		m_img = null;
		m_xPos = 0;
		m_yPos = 0;
		openImage();
		initialize();
	}

	public Symbol(BufferedImage img, Point point, Double scale)
	{
		m_path = "No path specified...";
		m_name = "";
		m_img = img;
		m_center = new Point(point);
		m_scale = scale;
		m_xPos = 0;
		m_yPos = 0;
		calculateImageSpecs();
		initialize();
	}

	public Symbol(BufferedImage img, int x, int y, Double scale)
	{
		m_path = "No path specified...";
		m_name = "";
		m_img = img;
		m_center = new Point(x, y);
		m_scale = scale;
		m_xPos = 0;
		m_yPos = 0;
		calculateImageSpecs();
		initialize();
	}

	public Symbol(IShape shape)
	{
		m_name = "";
		m_path = "";
		m_img = null;
		m_center = new Point();
		m_scale = 1.0;
		m_drawObject = STROKES;
		m_xPos = 0;
		m_yPos = 0;
		initialize();
	}

	public Symbol(Symbol s)
	{
		m_path = s.getPath();
		m_name = s.getSymbolName();
		setReadableName();

		m_sidc = s.getSIDC();
		m_recognitionLabel = s.getRecognitionLabel();
		setShape(s.getShape());
		m_img = s.getImage();
		m_center = s.getPosition();
		m_scale = s.getScale();
		m_drawObject = s.getDrawType();

		if (m_img != null)
		{
			calculateImageSpecs();
		}
		else if (new File(m_path).exists())
		{
			openImage();
		}
		else if (m_shape != null)
		{
			calculateStrokeSpecs(false);
		}
		initialize();
	}

	private void initialize()
	{
		setLayout(null);
		setOpaque(false);
		addMouseListener(this);
		
		if (m_shape != null)
			setLocation((int)m_shape.getBoundingBox().getX(), 
						(int)m_shape.getBoundingBox().getY());
		
		cleanUp(m_shape);
	}

	private void openImage()
	{
		m_img = null;
		try
		{
			File f = new File(m_path);
			m_img = ImageIO.read(f);
		}
		catch (IOException e)
		{
		}
		calculateImageSpecs();
	}

	private void calculateImageSpecs()
	{
		if (m_img != null)
		{
			Double width = m_img.getWidth(null) * m_scale;
			Double height = m_img.getHeight(null) * m_scale;

			Double xPos = m_center.x - (width / 2.0);
			Double yPos = m_center.y - (height / 2.0);

			m_xPos = xPos.intValue();
			m_yPos = yPos.intValue();

			setSize(width.intValue(), height.intValue());
		}
	}

	private void calculateStrokeSpecs(boolean changeCenter)
	{
		if (m_shape != null)
		{
			setSize((int) m_shape.getBoundingBox().getWidth() + 1, 
					(int) m_shape.getBoundingBox().getHeight() + 1);
			
			m_xPos = (int) m_shape.getBoundingBox().getX();
			m_yPos = (int) m_shape.getBoundingBox().getY();

			if (changeCenter)
				setCenterPoint(new Point((int) m_shape.getBoundingBox()
						.getCenterPoint().getX(), (int) m_shape
						.getBoundingBox().getCenterPoint().getY()));
		}
	}

	public void setCenterPoint(Point p)
	{
		m_center = new Point(p);
	}

	/**
	 * Use the symbol name to get a readable name from a text file
	 */
	private void setReadableName()
	{
		if (m_name.length() > 0)
		{
			File f = new File(NAME_PATH + "COANameMap.txt");
			if (f.exists())
			{
				FileInputStream fIn;
				try
				{
					fIn = new FileInputStream(f.getPath());
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(fIn));

					String line = reader.readLine();

					while (line != null)
					{
						String elements[] = line.split(";");

						// If a text label is drawn, it appends "_Text" onto the
						// symbol name,
						// but the map file does not have the "_Text", so we
						// must remove it
						String nameWithoutText = m_name.split("_Text")[0];

						if (elements[0].equals(nameWithoutText))
						{
							m_number = elements[1];
							m_readableName = elements[6];
							m_modifiers = elements[2]
									+ ", "
									+ (elements[3].equals("X") ? ""
											: elements[3] + ", ")
									+ elements[4]
									+ (elements[5].equals("X") ? "" : ", "
											+ elements[5]);

							break;
						}
						else
						{
							m_number = m_name.substring(0, 3);
							m_readableName = m_name;
						}

						line = reader.readLine();
					}

				}
				catch (FileNotFoundException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

			}
		}
	}
	
	public boolean isMouseOver()
	{
		return m_isMouseOver;
	}
	
	public boolean isSelected()
	{
		return m_isSelected;
	}
	
	public void setSelected(boolean b)
	{
		m_isSelected = b;
	}

	public boolean isSpecialCase()
	{
		return m_specialCase;
	}

	public List<IStroke> getStrokes()
	{
		return m_shape.getRecursiveStrokes();
	}

	public String getPath()
	{
		return m_path;
	}

	public String getSymbolName()
	{
		return m_name;
	}

	public String getReadableName()
	{
		return m_readableName;
	}

	public String getModifiers()
	{
		return m_modifiers;
	}

	public String getNumber()
	{
		return m_number;
	}

	public String getSIDC()
	{
		return m_sidc;
	}

	public Point getPosition()
	{
		return m_center;
	}

	public Point getStrokeCenter()
	{
		return new Point(
				(int) m_shape.getBoundingBox().getCenterPoint().getX(),
				(int) m_shape.getBoundingBox().getCenterPoint().getY());
	}

	public Double getScale()
	{
		return m_scale;
	}

	public Image getImage()
	{
		if (m_drawObject == SYMBOL)
			return m_img;
		else
		{
			if (getWidth() > 0 && getHeight() > 0)
			{
				BufferedImage strokes = new BufferedImage(getWidth(), getHeight(),
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = (Graphics2D) strokes.getGraphics();
				g2.setColor(Color.BLACK);
				drawStrokes(g2);

				return strokes;
			}
			else
				return null;
		}
	}

	/**
	 * Calculate the space, in pixels, a symbol takes up
	 * 
	 * @param scale
	 */
	public int getArea()
	{
		int area = 0;
		int imageArea = 0;
		int strokeArea = 0;

		// find the area of the image (if there is one)
		if (m_img == null)
			openImage();
		else
			calculateStrokeSpecs(false);

		imageArea = getWidth() * getHeight();

		// find the area of the strokes
		calculateStrokeSpecs(false);
		strokeArea = getWidth() * getHeight();

		if (m_drawObject == SYMBOL)
			calculateImageSpecs();

		area = imageArea < strokeArea ? imageArea : strokeArea;

		return area;
	}

	public Rectangle getBoundingBox()
	{
		Rectangle boundingBox = new Rectangle(m_xPos, m_yPos, getWidth(), getHeight());

		return boundingBox;
	}

	public IShape getShape()
	{
		return m_shape;
	}

	public String getRecognitionLabel()
	{
		return m_recognitionLabel;
	}

	public void setShape(IShape shape)
	{
		m_shape = new Shape(shape);
		
		if (m_shape != null)
			setLocation((int)m_shape.getBoundingBox().getX(), 
						(int)m_shape.getBoundingBox().getY());
	}

	public void setScale(double scale)
	{
		m_scale = scale;
	}

	public void setPath(String path)
	{
		m_path = path;
		openImage();
	}

	public void setSymbolName(String name)
	{
		m_name = name;
		setReadableName();
	}

	public void setSIDC(String sidc)
	{
		m_sidc = sidc;
	}

	public void setCenterPosition(Point p)
	{
		m_center = new Point(p);
		if (m_drawObject == SYMBOL)
		{
			if (m_img != null)
			{
				calculateImageSpecs();
			}
			else if (new File(m_path).exists())
			{
				openImage();
			}
			else
			{
				calculateStrokeSpecs(false);
			}
		}
		else
		{
			calculateStrokeSpecs(false);
		}
	}

	public void setCenterPosition(int x, int y)
	{
		setCenterPosition(new Point(x, y));
	}

	public void setImage(BufferedImage img)
	{
		m_img = img;
		calculateImageSpecs();
	}

	/**
	 * Determine if the symbol is to draw the image symbol or the strokes (Note:
	 * if the image is null, the strokes will be drawn no matter what)
	 * 
	 * @param whatToDraw
	 */
	public void setDrawType(int whatToDraw)
	{
		if (!(whatToDraw == SYMBOL || whatToDraw == STROKES))
		{
			throw new IllegalArgumentException("illegal symbol draw type: "
					+ whatToDraw);
		}
		m_drawObject = whatToDraw;

		if (whatToDraw == SYMBOL)
		{
			openImage();
			if (m_img == null)
				calculateStrokeSpecs(false);
		}
		else if (whatToDraw == STROKES)
		{
			calculateStrokeSpecs(false);
		}
	}

	public void setRecognitionLabel(String s)
	{
		m_recognitionLabel = s;
	}

	public int getDrawType()
	{
		return m_drawObject;
	}

	/**
	 * Draw the symbol on the graphics object
	 */
	private void draw(Graphics2D g2)
	{
		if (m_drawObject == SYMBOL)
		{
			if (m_img != null)
			{
				drawImage(g2);
			}
			else
			{
				drawStrokes(g2);
			}
		}

		if (m_drawObject == STROKES)
		{
			drawStrokes(g2);
		}
		
		handleSpecialCases();
	}

	/**
	 * Switch between STROKES and IMAGE draw types, provided the image exists,
	 * else keep as STROKES
	 */
	public void toggleDrawType()
	{
		if (m_drawObject == SYMBOL)
		{
			m_drawObject = STROKES;
			calculateStrokeSpecs(false);
		}
		else if (m_drawObject == STROKES)
		{
			m_drawObject = SYMBOL;
			if (new File(m_path).exists())
			{
				openImage();
			}
			else
			{
				calculateStrokeSpecs(false);
			}
		}
	}

	public String getEchelonModifier()
	{
		if (m_sidc.length() > 0)
		{
			switch (m_sidc.charAt(ECHELON_INDEX))
			{
				case 'E':
					return COMPANY;
				case 'H':
					return BRIGADE;
				case 'F':
					return BATTALION;
				case 'D':
					return PLATOON;
				default:
					return "X";
			}
		}
		else
			return "X";
	}

	private void drawImage(Graphics2D g2)
	{
		g2.drawImage(m_img, 
					 0, 0, 
					 (int) (getWidth() * m_scale), (int) (getHeight() * m_scale), 
					 0, 0,
					 m_img.getWidth(null), m_img.getHeight(null), null);

		// draw the text
		//drawText(g2);
	}

	private void drawStrokes(Graphics2D g2)
	{
		// turn anti aliasing on
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// draw in black
		g2.setColor(Color.BLACK);

		// draw the text if the draw type is SYMBOL
		// and draw the strokes if draw type is STROKES
		if (m_drawObject == SYMBOL)
		{
			drawSomeStrokes(false, g2);
			drawText(g2);
		}
		else if (m_drawObject == STROKES)
			drawSomeStrokes(true, g2);
	}

	/**
	 * Draw all strokes in the given list using the given Graphics2D object.
	 * However, do not draw text strokes if boolean is set to true.
	 * 
	 * @param drawText - set to true if the text strokes are to be drawn,
	 * set to false if the text strokes are not to be drawn
	 * @param g2 - the Graphics2D object by which to draw the strokes
	 */
	private void drawSomeStrokes(boolean drawText, Graphics2D g2)
	{
		// draw any strokes in this shape
		for (IStroke stroke : m_shape.getStrokes())
		{
			drawStroke(stroke, g2);
		}

		// draw strokes in all subshapes
		for (IShape shape : m_shape.getSubShapes())
		{
			// if drawText is false OR
			// if drawText is true and the shape is not text
			if (drawText == true || 
				(drawText == false && 
						(shape.getLabel().equals("Text") == false &&
						 shape.getLabel().equals("Echelon") == false) ))
			{
				for (IStroke stroke : shape.getStrokes())
				{
					drawStroke(stroke, g2);
				}
			}
		}
	}

	/**
	 * Draws a Stroke with the Graphics2D object
	 * 
	 * @param g2 - the Graphics2D by which to draw the strokes
	 * @param stroke - the stroke to be drawn
	 */
	private void drawStroke(IStroke stroke, Graphics2D g2)
	{
		// find offset to draw stroke based on panel origin
		int xOffset = (int)(-1 * m_shape.getBoundingBox().getLeft());
		int yOffset = (int)(-1 * m_shape.getBoundingBox().getTop());

		for (int i = 0; i < stroke.getNumPoints() - 2; i++)
		{
			g2.drawLine((int) stroke.getPoint(i).getX() + xOffset, (int) stroke
					.getPoint(i).getY()
					+ yOffset, (int) stroke.getPoint(i + 1).getX() + xOffset,
					(int) stroke.getPoint(i + 1).getY() + yOffset);
		}
	}

	private void drawText(Graphics2D g2)
	{
		// turn on anti aliasing
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// find offset to draw based on panel origin
		int xOffset = (int)(-1 * m_shape.getBoundingBox().getLeft());
		int yOffset = (int)(-1 * m_shape.getBoundingBox().getTop());

		// Draw the string in each shape of the label list
		for (IShape shape : m_shape.getSubShapes())
		{
			if (shape.getLabel().equals("Text")
					|| shape.getLabel().equals("Echelon"))
			{
				BoundingBox box = shape.getBoundingBox();
				g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, (int) box
						.getHeight()));

				
				// figure out the most likely echelon modifier of the symbol
				// by
				// the values
				String mostLikelyEchelon = getMostLikelyEchelonModifier(shape);

				if (m_sidc.length() > 0)
				{
					if (m_sidc.charAt(ECHELON_INDEX) == COMPANY_CHAR
							&& mostLikelyEchelon.equals(COMPANY_SYMBOL))
					{
						g2.drawString(COMPANY_DRAW_SYMBOL, (int) box.getX()
								+ xOffset, (int) (box.getY()
								+ box.getHeight() + yOffset));
						continue;
					}
					else if (m_sidc.charAt(ECHELON_INDEX) == BRIGADE_CHAR
							&& mostLikelyEchelon.equals(BRIGADE_SYMBOL))
					{
						drawBrigadeModifier(box, g2, xOffset, yOffset);
						continue;
					}
					else if (m_sidc.charAt(ECHELON_INDEX) == BATTALION_CHAR
							&& mostLikelyEchelon.equals(BATTALION_SYMBOL))
					{
						g2.drawString(BATTALION_DRAW_SYMBOL, (int) box
								.getX()
								+ xOffset, (int) (box.getY()
								+ box.getHeight() + yOffset));
						continue;
					}
					else if (m_sidc.charAt(ECHELON_INDEX) == PLATOON_CHAR
							&& mostLikelyEchelon.equals(PLATOON_SYMBOL))
					{
						drawPlatoonModifier(box, g2, xOffset, yOffset);
						continue;
					}
				}				

				// Just draw the label's best text
				g2.drawString(shape.getAttribute("TEXT_BEST"), (int) box.getX()
						+ xOffset,
						(int) (box.getY() + box.getHeight() + yOffset));
			}
		}
	}

	/**
	 * Find the most likely echelon modifier of the shape
	 * 
	 * @param shape
	 */
	private String getMostLikelyEchelonModifier(IShape shape)
	{
		String mostLikelyEchelon = "";

		if (shape.hasAttribute(COMPANY_SYMBOL)
				&& shape.hasAttribute(BRIGADE_SYMBOL)
				&& shape.hasAttribute(BATTALION_SYMBOL)
				&& shape.hasAttribute(PLATOON_SYMBOL))
		{
			double companyValue = Double.parseDouble(shape
					.getAttribute(COMPANY_SYMBOL));
			double brigadeValue = Double.parseDouble(shape
					.getAttribute(BRIGADE_SYMBOL));
			double battalionValue = Double.parseDouble(shape
					.getAttribute(BATTALION_SYMBOL));
			double platoonValue = Double.parseDouble(shape
					.getAttribute(PLATOON_SYMBOL));
			double maxEchelon = Math.max(Math.max(Math.max(companyValue,
					brigadeValue), battalionValue), platoonValue);
			if (maxEchelon == companyValue)
				mostLikelyEchelon = COMPANY_SYMBOL;
			if (maxEchelon == brigadeValue)
				mostLikelyEchelon = BRIGADE_SYMBOL;
			if (maxEchelon == battalionValue)
				mostLikelyEchelon = BATTALION_SYMBOL;
			if (maxEchelon == platoonValue)
				mostLikelyEchelon = PLATOON_SYMBOL;
		}

		return mostLikelyEchelon;
	}

	/**
	 * Draw the Platoon modifier (three circles in a row) based on bounding box
	 * width
	 * 
	 * @param box
	 *            - BoundingBox to determine size and location
	 * @param g2
	 *            - Graphics2D object to draw the symbol
	 */
	private void drawPlatoonModifier(BoundingBox box, Graphics2D g2,
			int xOffset, int yOffset)
	{
		// turn on anti aliasing
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		int diameter = (int) (box.getWidth() * DIAMETER_RATIO);
		int spaceWidth = (int) (box.getWidth() * SPACE_RATIO);

		g2.setColor(Color.BLACK);
		g2.fillOval((int) box.getX() + xOffset, (int) box.getY() + yOffset,
				diameter, diameter);
		g2.fillOval((int) box.getX() + xOffset + diameter + spaceWidth,
				(int) box.getY() + yOffset, diameter, diameter);
		g2.fillOval((int) box.getX() + xOffset + diameter + spaceWidth
				+ diameter + spaceWidth, (int) box.getY() + yOffset, diameter,
				diameter);
	}

	/**
	 * Draw the Platoon modifier (three circles in a row) based on bounding box
	 * width
	 * 
	 * @param box
	 *            - BoundingBox to determine size and location
	 * @param g2
	 *            - Graphics2D object to draw the symbol
	 */
	private void drawBrigadeModifier(BoundingBox box, Graphics2D g2,
			int xOffset, int yOffset)
	{
		// turn on anti aliasing
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		int squareSide = box.getWidth() > box.getHeight() ? (int) box
				.getHeight() : (int) box.getWidth();
		Point topLeft = new java.awt.Point(
				(int) (box.getCenterX() - squareSide / 2), (int) (box
						.getCenterY() - squareSide / 2));
		int thickness = 3;
		int xPoints[] =
		{ topLeft.x + xOffset, topLeft.x + thickness + xOffset,
				topLeft.x + squareSide + xOffset,
				topLeft.x + squareSide - thickness + xOffset };
		int yPoints[] =
		{ topLeft.y + yOffset, topLeft.y - thickness + yOffset,
				topLeft.y + squareSide - 2 * thickness + yOffset,
				topLeft.y + squareSide - thickness + yOffset };
		int xPoints2[] =
		{ topLeft.x + squareSide + xOffset,
				topLeft.x + squareSide - thickness + xOffset,
				topLeft.x + xOffset, topLeft.x + thickness + xOffset };

		g2.setColor(Color.BLACK);
		g2.fillPolygon(xPoints, yPoints, 4);
		g2.fillPolygon(xPoints2, yPoints, 4);
	}

	/**
	 * HANDLE SPECIAL CASES FIRST: some symbols require special treatment
	 */
	public void handleSpecialCases()
	{
		// This method should only run once
		if (!m_specialCase)
		{
			// Modify the bounding box if shape is a star (special case)
			if (Integer.parseInt(m_name.substring(0, 3)) == 216
					&& !m_recognitionLabel.isEmpty())
			{
				m_specialCase = true;
				
				JTextField labelField = new JTextField();
				labelField.setFont(new Font("Arial", Font.PLAIN, 20));
				labelField.setText(m_recognitionLabel);
				labelField.setEditable(false);
				labelField.setOpaque(false);
				labelField.setBorder(null);
				labelField.setSelectedTextColor(Color.BLACK);
				labelField.setSelectionColor(new Color(0, 0, 0, 0));
				
				// eventually fix this to properly center the text
				if (m_recognitionLabel.length() > 1)
					labelField.setBounds(36, 44, 30, 20);
				else
					labelField.setBounds(45, 44, 30, 20);
				
				labelField.setVisible(true);
				add(labelField);
			}
		}
	}

	/**
	 * Clean up some symbols that have extra handwriting
	 */
	private void cleanUp(IShape shape)
	{
		// Get the two vertical lines on either end of the line
		/*
		if (Integer.parseInt(m_name.substring(0, 3)) == 268)
		{
			for (IShape subshape : shape.getSubShapes())
			{
				if (subshape.getLabel().equals("Text")
						&& !subshape.getAttribute("TEXT_BEST").equals("FPF"))
				{
					subshape.setLabel("");
				}
			}
		}
		*/
		
		// relabel all strokes on a phase line as text except the longest one
		if (m_name != null && shape != null && Integer.parseInt(m_name.substring(0, 3)) == 223)
		{
			IShape newShape = new Shape();
			int height = 0;
			
			// find tallest sub shape
			for (IShape subshape : shape.getSubShapes())
			{
				if (subshape.getBoundingBox().height > height)
				{
					height = (int)(subshape.getBoundingBox().height);
				}					
			}
			
			// remove all strokes from the tallest sub shape except the longest stroke
			for (IShape subshape : shape.getSubShapes())
			{
				if ((int)(subshape.getBoundingBox().height) == height)
				{
					int strokeHeight = 0;
					for (IStroke stroke : subshape.getStrokes())
					{
						if (stroke.getBoundingBox().height > strokeHeight)
							strokeHeight = (int)(stroke.getBoundingBox().height);
					}
					
					List<IStroke> strokes = subshape.getStrokes();
					for (IStroke stroke : strokes)
					{
						if ((int)(stroke.getBoundingBox().height) != strokeHeight)
						{
							newShape.addStroke(stroke);
						}
					}
					
					newShape.setLabel("Text");
					
					for (IStroke stroke : newShape.getStrokes())
						strokes.remove(stroke);
					
					subshape.setStrokes(strokes);
				}
			}
		}
	}

	public void enterSaveState()
	{
		m_img = null;
	}

	public void enterOpenState()
	{
		openImage();
	}
	
	public void paintComponent(Graphics g)
	{		
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
							RenderingHints.VALUE_ANTIALIAS_ON);
		if (m_paintHighlight)
		{
			g2.setColor(new Color(200,200,255,150));
			g2.fillRoundRect(0, 0, getWidth(), getHeight(),
							 20, 20);
		}
		
		draw(g2);
	}

	@Override
	public void mouseClicked(MouseEvent arg0)
	{
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0)
	{
		m_isMouseOver = true;
		m_paintHighlight = true;
		repaint();
	}

	@Override
	public void mouseExited(MouseEvent arg0)
	{
		m_isMouseOver = false;
		m_paintHighlight = false;
		repaint();
	}

	@Override
	public void mousePressed(MouseEvent arg0)
	{
		m_paintHighlight = false;
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0)
	{
		
	}
}
