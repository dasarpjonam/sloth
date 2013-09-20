package test.functional.ladder.recognition.paleo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.config.LadderConfig;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.io.DOMInput;
import org.ladder.io.ShapeDirFilter;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.paleo.Fit;
import org.xml.sax.SAXException;

public class MultiStrokePrimFinder {
	
	/**
	 * Input file reader
	 */
	private static DOMInput m_input = new DOMInput();
	
	private static Map<String, Integer> m_names = new HashMap<String, Integer>();
	
	private static List<String> m_complex = new ArrayList<String>();
	
	
	/**
	 * @param args
	 * @throws UnknownSketchFileTypeException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static void main(String[] args) throws ParserConfigurationException,
	        SAXException, IOException, UnknownSketchFileTypeException {
		File testData = new File(LadderConfig.getProperty("testData"));
		File[] shapeDirs = testData.listFiles(new ShapeDirFilter());
		for (File shapeDir : shapeDirs) {
			if (!shapeDir.getAbsolutePath().endsWith("2")) {
				System.out.println("Shape Dir: " + shapeDir.getName());
				File[] shapeFiles = shapeDir.listFiles(new XMLFileFilter());
				for (File shapeFile : shapeFiles) {
					ISketch m_sketch = m_input.parseDocument(shapeFile);
					for (int i = 0; i < m_sketch.getNumShapes(); i++) {
						IShape sh = m_sketch.getShapes().get(i);
						if (sh.getStrokes().size() > 1) {
							System.out.println(shapeFile.getName());
							String name = sh.getLabel();
							if (!m_names.containsKey(name))
								m_names.put(name, 1);
							else
								m_names.put(name, m_names.get(name)+1);
							break;
						}
					}
					for (IStroke str : m_sketch.getStrokes()) {
						if (str.getLabel().startsWith(Fit.COMPLEX))
							m_complex.add(shapeFile.getAbsolutePath());
					}
				}
			}
		}
		
		// print individual shape numbers
		for (String n : m_names.keySet()) {
			System.out.println(n + " - " + m_names.get(n));
		}
		
		System.out.println();
		System.out.println("Complex: ");
		for (String x : m_complex)
			System.out.println(x);
	}
	
}
