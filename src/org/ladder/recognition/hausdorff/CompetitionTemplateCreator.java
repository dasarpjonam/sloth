package org.ladder.recognition.hausdorff;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.ISketch;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.xml.sax.SAXException;

public class CompetitionTemplateCreator {

	public static void main(String[] args){
		
		File trainingShapesDir = new File("/Users/pcorey/Documents/Track1/");
		String outputImageFolder = "/Users/pcorey/Documents/Track1ImageTemplates/";
		for(File shapeDir : trainingShapesDir.listFiles()){
			if(!shapeDir.isDirectory()) continue;
			try {
				System.out.println(shapeDir.getCanonicalPath());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println(shapeDir.listFiles().length);
			File shapeFile = shapeDir.listFiles()[0];
			DOMInput inFile = new DOMInput();
			ISketch m_sketch=null;
			try {
				m_sketch = inFile.parseDocument(shapeFile);
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownSketchFileTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(m_sketch!=null){
				Template t = new Template(shapeDir.getName(),m_sketch.getStrokes());
				try {
					ImageIO.write(t.getImage(), "png", new File(outputImageFolder+shapeDir.getName()+".png"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				BufferedImage bi = ImageIO.read(new File(outputImageFolder+shapeDir.getName()+".png"));
				
				for(int i=0;i<128;i++){
					for(int j=0;j<128;j++)
						System.out.print(bi.getRGB(i, j)==Color.WHITE.getRGB()?' ':'0');
					System.out.println();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
