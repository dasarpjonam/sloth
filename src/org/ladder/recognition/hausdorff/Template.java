package org.ladder.recognition.hausdorff;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Point;

/**
 * Template to use when classifying Hausdorff distance
 * 
 * @author awolin
 */
public class Template {
	
	/**
	 * Mandatory template image size
	 */
	public static final Dimension S_IMGSIZE = new Dimension(128, 128);
	
	/**
	 * Name of the template
	 */
	private String m_name;
	
	/**
	 * Image of the template
	 */
	private BufferedImage m_image = null;
	
	/**
	 * Points of the image (e.g., black pixels)
	 */
	private List<IPoint> m_imagePixels = null;
	
	
	/**
	 * Constructor that takes in the template's name and image file location.
	 * 
	 * @param name
	 *            Template name
	 * @param filePath
	 *            Location of the template's image file
	 * @throws IOException
	 *             Thrown if no file is found
	 */
	public Template(String name, String filePath) throws IOException {
		
		// Set the template name
		m_name = name;
		
		try {
			// Get and set the template's image
			if (new File(filePath).exists()) {
				m_image = ImageIO.read(new File(filePath));
			}
			else {
				throw new IOException("Error: no template image file found at "
				                      + filePath);
			}
		}
		catch (javax.imageio.IIOException ioe) {
			System.out.println("?");
		}
		
		// Find the dark pixels
		grabImagePixels();
	}
	

	/**
	 * Constructor that takes in the template's name and image file.
	 * 
	 * @param name
	 *            Template name
	 * @param image
	 *            Template image
	 */
	public Template(String name, BufferedImage image) {
		
		// Set the template name
		m_name = name;
		
		// Set the template image, scaled if necessary
		m_image = image;
		
		// Find the dark pixels
		grabImagePixels();
	}
	
	/**
	 * Create template from strokes
	 * @param name
	 * 			Template name
	 * @param strokes
	 * 			Strokes in template
	 */
	public Template(String name, List<IStroke> strokes){
		
		m_name = name;
		
		List<IPoint> points = Template.scale(strokes);
		m_image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = m_image.createGraphics();
		g2d.setColor(Color.WHITE);
		for(int x=0;x<128;x++)
			for(int y=0;y<128;y++)
				m_image.setRGB(x, y, Color.WHITE.getRGB());
		System.out.println(points.size()+" Points");
		for(IPoint p : points){
			int x = (int) p.getX();
			int y = (int) p.getY();
			if(x>=128)
				x--;
			if(y>=128)
				y--;
			m_image.setRGB(x, y, Color.BLACK.getRGB());
		}
		
		for(int i=0;i<128;i++){
			for(int j=0;j<128;j++)
				System.out.print(m_image.getRGB(i, j)==Color.WHITE.getRGB()?' ':'0');
			System.out.println();
		}
		
		grabImagePixels();
	}

	/**
	 * Gets the name of the template
	 * 
	 * @return Template name
	 */
	public String getName() {
		return m_name;
	}
	

	/**
	 * Gets the image of the template
	 * 
	 * @return Template image
	 */
	public BufferedImage getImage() {
		return m_image;
	}
	

	/**
	 * Get the black pixels in the image
	 * 
	 * @return Returns the black pixels in the image as points
	 */
	public List<IPoint> getImagePixels() {
		return m_imagePixels;
	}
	

	/**
	 * Get all of the black pixels in the image. Assumes that the image is
	 * already at the scaled image size.
	 */
	private void grabImagePixels() {
		
		if (m_image == null) {
			System.err.println("Error: No image to grab pixels from");
			return;
		}
		
		m_imagePixels = new ArrayList<IPoint>();
		
		// Get each pixel
		for (int x = 0; x < m_image.getWidth(); x++) {
			for (int y = 0; y < m_image.getHeight(); y++) {
				
				// Add dark pixels to the image pixels array
				Color pixelColor = new Color(m_image.getRGB(x, y));
				if (pixelColor.getRed() < 0.2 && pixelColor.getGreen() < 0.2
				    && pixelColor.getBlue() < 0.2) {
					
					m_imagePixels.add(new Point(x, y));
				}
			}
		}
	}
	

	/**
	 * Returns a scaled image of the input image icon, scaled to the image's
	 * size.
	 * 
	 * @param image
	 *            Image to scale
	 * 
	 * @return A scaled image
	 */
	/*
	 * public static BufferedImage scale(BufferedImage image) {
	 * 
	 * BufferedImage scaledImage = (BufferedImage) image.getScaledInstance(
	 * (int) S_IMGSIZE.getWidth(), (int) S_IMGSIZE.getHeight(),
	 * Image.SCALE_DEFAULT);
	 * 
	 * return scaledImage; }
	 */

	/**
	 * Returns a scaled list of IPoints of, scaled to the image's size.
	 * 
	 * @param strokes
	 *            Strokes to use for scaling
	 * @return A scaled list of points
	 */
	public static List<IPoint> scale(List<IStroke> strokes) {
		
		// Grab all of the points from the strokes
		List<IPoint> points = new ArrayList<IPoint>();
		for (IStroke st : strokes) {
			points.addAll(st.getPoints());
		}
		
		// Initialize the min and max values for the bounding box
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		
		// Find the bounding box of the strokes
		for (int i = 0; i < points.size(); i++) {
			
			double x = points.get(i).getX();
			double y = points.get(i).getY();
			
			// Check min/max x
			if (x < minX)
				minX = x;
			if (x > maxX)
				maxX = x;
			
			// Check min/max y
			if (y < minY)
				minY = y;
			if (y > maxY)
				maxY = y;
		}
		
		double bbWidth = (int) (maxX - minX);
		double bbHeight = (int) (maxY - minY);
		
		// Scale the points
		List<IPoint> scaledPoints = new ArrayList<IPoint>();
		
		for (int i = 0; i < points.size(); i++) {
			double newX = ((points.get(i).getX() - minX) / bbWidth)
			              * S_IMGSIZE.getWidth();
			double newY = ((points.get(i).getY() - minY) / bbHeight)
			              * S_IMGSIZE.getHeight();
			
			scaledPoints.add(new Point(newX, newY));
		}
		
		/*
		 * int[][] tmpPixels = new int[(int) bbWidth + 1][(int) bbHeight + 1];
		 * 
		 * for (IPoint pt : points) { int x = (int) (pt.getX() - minX); int y =
		 * (int) (pt.getY() - minY); tmpPixels[x][y] = 1; }
		 * 
		 * double normWidth = bbWidth / S_IMGSIZE.getWidth(); double normHeight
		 * = bbHeight / S_IMGSIZE.getHeight();
		 * 
		 * for (int x = 0; x < S_IMGSIZE.getWidth(); x++) { for (int y = 0; y <
		 * S_IMGSIZE.getHeight(); y++) {
		 * 
		 * int origX = (int) (x normWidth); int origY = (int) (int) (y
		 * normHeight); int black = tmpPixels[origX][origY];
		 * 
		 * if (black == 1) { scaledPoints.add(new Point(x, y)); } } }
		 */

		return scaledPoints;
	}
}
