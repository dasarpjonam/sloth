package org.ladder.recognition.rubine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ladder.core.sketch.BoundingBox;
import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.IStroke;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.paleo.PaleoConfig;
import org.ladder.recognition.paleo.PaleoSketchRecognizer;
import org.ladder.recognition.recognizer.IRecognizer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Jama.Matrix;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class TracysClassifier implements
        IRecognizer<List<IStroke>, IRecognitionResult> {
	
	private int S_N_STROKES = 6;
	
	private List<IStroke> m_strokes = null;
	
	private Map<String, List<Double>> m_weights = null;
	
	/**
	 * Rejection probability
	 */
	private static final double S_REJECT_PROBABILITY = 0.95;
	
	
	public TracysClassifier() {
		
	}
	

	public NavigableMap<Double, String> classify(List<IStroke> strokes) {
		
		List<Double> features = null;
		
		strokes = strokes.subList(0, Math.min(strokes.size(), S_N_STROKES));
		
		TreeMap<Double, String> classRanking = new TreeMap<Double, String>();
		
		for (String classLabel : m_weights.keySet()) {
			double val = classifyValue(classLabel, features);
			classRanking.put(val, classLabel);
		}
		
		// Check for rejection
		double denominator = 0.0;
		NavigableSet<Double> valuesInOrder = classRanking.descendingKeySet();
		Object[] objArray = valuesInOrder.toArray();
		Double[] valueArray = new Double[objArray.length];
		for (int i = 0; i < valueArray.length; i++) {
			valueArray[i] = (Double) objArray[i];
		}
		
		for (int i = 1; i < valueArray.length; i++) {
			denominator += Math.exp(valueArray[i] - valueArray[0]);
		}
		
		double pReject = 1.0 / denominator;
		
		// Reject if we do not meet the criterion probability
		if (pReject < S_REJECT_PROBABILITY) {
			return null;
		}
		else {
			return classRanking.descendingMap();
		}
	}
	

	/**
	 * Calculates the dot product of the class's weights and the given features
	 * 
	 * @param features
	 *            Features of a stroke
	 * @return Dot product of the weights and features
	 */
	private double classifyValue(String classLabel, List<Double> features) {
		
		List<Double> classWeights = m_weights.get(classLabel);
		double val = classWeights.get(0);
		
		for (int i = 0; i < features.size(); i++) {
			val += (classWeights.get(i + 1) * features.get(i));
		}
		
		return val;
	}
	

	private List<Double> featurefy(List<IStroke> strokes) {
		
		List<Double> features = new ArrayList<Double>();
		
		BoundingBox boundingBox = calcBoundingBox(strokes);
		
		// Position
		for (int i = 0; i < strokes.size(); i++) {
			features.add((boundingBox.getCenterX() - strokes.get(i)
			        .getBoundingBox().getCenterX())
			             / boundingBox.getArea());
			features.add((boundingBox.getCenterY() - strokes.get(i)
			        .getBoundingBox().getCenterY())
			             / boundingBox.getArea());
		}
		
		if (strokes.size() < S_N_STROKES) {
			for (int i = strokes.size(); i < S_N_STROKES; i++) {
				features.add(0.01);
				features.add(0.01);
			}
		}
		
		// Paleo
		PaleoSketchRecognizer paleo = new PaleoSketchRecognizer(PaleoConfig
		        .deepGreenConfig());
		for (int i = 0; i < strokes.size(); i++) {
			paleo.submitForRecognition(strokes.get(i));
			IRecognitionResult recResults = paleo.recognize();
			
			String primitiveRecognized = recResults.getBestShape().getLabel();
			
			features.add(boolToDouble(primitiveRecognized.equals("line")));
			features.add(boolToDouble(primitiveRecognized.equals("arc")));
			features.add(boolToDouble(primitiveRecognized.equals("ellipse")));
			features.add(boolToDouble(primitiveRecognized.equals("rectangle")));
			features.add(boolToDouble(primitiveRecognized.equals("polyline")));
			features.add(boolToDouble(primitiveRecognized.equals("diamond")));
			features.add(boolToDouble(primitiveRecognized.equals("dot")));
			features.add(boolToDouble(primitiveRecognized.equals("wave")));
			features.add(boolToDouble(primitiveRecognized.equals("gull")));
		}
		
		if (strokes.size() < S_N_STROKES) {
			for (int i = strokes.size(); i < S_N_STROKES; i++) {
				features.add(0.01);
				features.add(0.01);
				features.add(0.01);
				features.add(0.01);
				features.add(0.01);
				features.add(0.01);
				features.add(0.01);
				features.add(0.01);
				features.add(0.01);
			}
		}
		
		return features;
	}
	

	private double boolToDouble(Boolean bool) {
		
		if (bool == false) {
			return 0.01;
		}
		
		return 1.0;
	}
	

	private BoundingBox calcBoundingBox(List<IStroke> strokes) {
		
		// Get the bounding box of the feature points (to be used in a few
		// features)
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		
		for (IStroke s : strokes) {
			for (IPoint p : s.getPoints()) {
				if (p.getX() < minX) {
					minX = p.getX();
				}
				if (p.getX() > maxX) {
					maxX = p.getX();
				}
				if (p.getY() < minY) {
					minY = p.getY();
				}
				if (p.getY() > maxY) {
					maxY = p.getY();
				}
			}
		}
		
		BoundingBox boundingBox = new BoundingBox(minX, minY, maxX, maxY);
		
		return boundingBox;
	}
	

	/**
	 * Gets the average feature values for each class in the training directory.
	 * Assumes that the data is labeled implicitly through a hierarchical
	 * directory grouping. ONLY LOOKS ONE DIRECTORY DOWN. Sets classExamples,
	 * classAvgs, and numFeatures.
	 * 
	 * @param trainingDir
	 */
	public void trainOnDirectoryOrganizedData(File trainingDir) {
		
		DOMInput input = new DOMInput();
		
		// Check whether we are actually in a directory
		if (trainingDir.isDirectory()) {
			
			Map<String, List<List<IStroke>>> trainingData = new HashMap<String, List<List<IStroke>>>();
			
			// Get the subdirectories, which are the labeled class names
			File[] classDirs = trainingDir.listFiles();
			
			for (File currSubdir : classDirs) {
				
				if (currSubdir.isDirectory()) {
					
					System.out.println("training shapes in: "
					                   + currSubdir.getName());
					
					// Traverse the subdirectory
					File[] gestureFiles = currSubdir.listFiles();
					
					for (File currFile : gestureFiles) {
						
						if (currFile.getName().endsWith(".xml")) {
							
							// System.out.println("Training - "
							// + currFile.getName());
							
							try {
								ISketch sketch = input.parseDocument(currFile);
								
								// Get only the last N strokes
								List<IStroke> strokes = sketch.getStrokes();
								strokes = strokes.subList(0, Math.min(strokes
								        .size(), S_N_STROKES));
								
								// Label the stroke based on the directories
								String classLabel = currSubdir.getName();
								
								// Store the strokes in a map
								List<List<IStroke>> classData;
								if (trainingData.get(classLabel) == null) {
									classData = new ArrayList<List<IStroke>>();
								}
								else {
									classData = trainingData.get(classLabel);
								}
								
								classData.add(strokes);
								trainingData.put(classLabel, classData);
								
							}
							catch (ParserConfigurationException e) {
								e.printStackTrace();
							}
							catch (SAXException e) {
								e.printStackTrace();
							}
							catch (IOException e) {
								e.printStackTrace();
							}
							catch (UnknownSketchFileTypeException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			
			// Train the classifier
			trainOnData(trainingData);
		}
	}
	

	/**
	 * Train on given RubineStroke data
	 * 
	 * @param trainingData
	 *            Training data stored as a mapping from class labels to a list
	 *            of example RubineStrokes
	 */
	protected void trainOnData(Map<String, List<List<IStroke>>> trainingData) {
		
		// Calculate the feature averages
		System.out.println("Calculating averages");
		Map<String, List<Double>> classFeatureAverages = calcClassAverageFeatures(trainingData);
		
		// Create a covariance matrix for each class
		System.out.println("Creating class cov matrices");
		Map<String, Matrix> classCovMatrices = calcClassCovMatrices(
		        trainingData, classFeatureAverages);
		
		// Create a common covariance matrix
		System.out.println("Creating common cov matrix");
		Matrix commonCovMatrix = calcCommonCovMatrix(trainingData,
		        classCovMatrices);
		
		// Calculate and store the weights
		System.out.println("Calculating weights");
		m_weights = calcWeights(commonCovMatrix, classFeatureAverages);
		
		System.out.println("Done");
	}
	

	/**
	 * Calculate the feature averages for each class
	 * 
	 * @param trainingData
	 *            Training data stored as a mapping from class labels to a list
	 *            of example RubineStrokes
	 * @return A mapping from class labels to the class feature averages
	 */
	private Map<String, List<Double>> calcClassAverageFeatures(
	        Map<String, List<List<IStroke>>> trainingData) {
		
		Map<String, List<Double>> classFeatureAverages = new HashMap<String, List<Double>>();
		
		for (String classLabel : trainingData.keySet()) {
			
			List<List<IStroke>> classData = trainingData.get(classLabel);
			
			// Sum the data
			int numExamples = classData.size();
			List<Double> averageFeatures = new ArrayList<Double>();
			for (int e = 0; e < numExamples; e++) {
				
				List<Double> exampleFeatures = featurefy(classData.get(e));
				
				if (e == 0) {
					averageFeatures = exampleFeatures;
				}
				else {
					for (int i = 0; i < averageFeatures.size(); i++) {
						averageFeatures.set(i, averageFeatures.get(i)
						                       + exampleFeatures.get(i));
					}
				}
			}
			
			// Average the data
			for (int i = 0; i < averageFeatures.size(); i++) {
				averageFeatures.set(i, averageFeatures.get(i) / numExamples);
			}
			
			// Store the feature averages
			classFeatureAverages.put(classLabel, averageFeatures);
		}
		
		return classFeatureAverages;
	}
	

	/**
	 * Calculate the covariance matrix for each class
	 * 
	 * @param trainingData
	 *            Training data stored as a mapping from class labels to a list
	 *            of example RubineStrokes
	 * @param classFeatureAverages
	 *            Class feature averages stored as a mapping from class labels
	 *            to a list of average feature values
	 * @return A mapping from class labels to the class covariance matrices
	 */
	private Map<String, Matrix> calcClassCovMatrices(
	        Map<String, List<List<IStroke>>> trainingData,
	        Map<String, List<Double>> classFeatureAverages) {
		
		// Create a covariance matrix for each class
		Map<String, Matrix> classCovMatrices = new HashMap<String, Matrix>();
		
		// For each class
		for (String classLabel : trainingData.keySet()) {
			
			List<List<IStroke>> exampleData = trainingData.get(classLabel);
			List<Double> featureAverages = classFeatureAverages.get(classLabel);
			
			int numExamples = exampleData.size();
			int numFeatures = featureAverages.size();
			
			double[][] covMatrixValues = new double[numFeatures][numFeatures];
			
			// For each example
			for (int e = 0; e < numExamples; e++) {
				
				List<Double> exampleFeatures = featurefy(exampleData.get(e));
				
				// Add the current value at (i,j)
				// (example[i] - avg[i]) * (example[j] - avg[j])
				for (int i = 0; i < numFeatures; i++) {
					for (int j = 0; j < numFeatures; j++) {
						double val = (exampleFeatures.get(i) - featureAverages
						        .get(i))
						             * (exampleFeatures.get(j) - featureAverages
						                     .get(j));
						covMatrixValues[i][j] += val;
					}
				}
			}
			
			Matrix covMatrix = new Matrix(covMatrixValues);
			classCovMatrices.put(classLabel, covMatrix);
		}
		
		return classCovMatrices;
	}
	

	/**
	 * Calculate the common covariance matrix for all the classes
	 * 
	 * @param trainingData
	 *            Training data stored as a mapping from class labels to a list
	 *            of example RubineStrokes
	 * @param classCovMatrices
	 *            A mapping from class labels to the class covariance matrices
	 * @return A common covariance matrix for all the class data
	 */
	private Matrix calcCommonCovMatrix(
	        Map<String, List<List<IStroke>>> trainingData,
	        Map<String, Matrix> classCovMatrices) {
		
		Matrix commonCovMatrix = null;
		int totalNumExamples = 0;
		int numClasses = 0;
		
		// Sum the class covariance matrices and the number of examples
		for (String classLabel : classCovMatrices.keySet()) {
			
			Matrix classCovMatrix = classCovMatrices.get(classLabel);
			int numExamples = trainingData.get(classLabel).size();
			
			if (commonCovMatrix == null) {
				commonCovMatrix = classCovMatrix;
			}
			else {
				commonCovMatrix.plus(classCovMatrix);
			}
			
			totalNumExamples += numExamples;
			numClasses++;
		}
		
		// Average the common covariance matrix by a normalizing term
		double normalizingTerm = 1.0 / (-numClasses + totalNumExamples);
		commonCovMatrix.timesEquals(normalizingTerm);
		
		return commonCovMatrix;
	}
	

	/**
	 * Calculates the weights for the classifier given the common covariance
	 * matrix and the feature averages
	 * 
	 * @param commonCovMatrix
	 *            Common covariance matrix
	 * @param classFeatureAverages
	 *            Class feature averages stored as a mapping from class labels
	 *            to a list of average feature values
	 * @return Weights for the classes
	 */
	private Map<String, List<Double>> calcWeights(Matrix commonCovMatrix,
	        Map<String, List<Double>> classFeatureAverages) {
		
		//commonCovMatrix.print(200, 3);
		
		Matrix commonCovMatrixInverse = commonCovMatrix.inverse();
		
		Map<String, List<Double>> weights = new HashMap<String, List<Double>>();
		
		for (String classLabel : classFeatureAverages.keySet()) {
			
			List<Double> featureAverages = classFeatureAverages.get(classLabel);
			double[] classWeights = new double[featureAverages.size() + 1];
			
			// Calculate the class weights
			for (int j = 0; j < featureAverages.size(); j++) {
				for (int i = 0; i < featureAverages.size(); i++) {
					double val = commonCovMatrixInverse.get(i, j)
					             * featureAverages.get(i);
					classWeights[j + 1] += val;
				}
			}
			
			// Calculate the initial weight, w_0
			for (int i = 0; i < featureAverages.size(); i++) {
				classWeights[0] += classWeights[i + 1] * featureAverages.get(i);
			}
			
			classWeights[0] /= -2.0;
			
			// Convert the array to a list
			List<Double> weightList = new ArrayList<Double>();
			for (double w : classWeights) {
				weightList.add(w);
			}
			
			weights.put(classLabel, weightList);
		}
		
		return weights;
	}
	

	/*
	 * Saving and loading weights, training data, and user data via my own XML
	 * format (extension .rub)
	 * 
	 * The format used follows:
	 * 
	 * <?xml version="1.0" encoding="UTF-8" ?> <classes> <class> label="name",
	 * weights="[w0, w1, w2, ... , wn]"> </class> ... </classes>
	 * 
	 * Much of the code in this section was taken and modified from an XML Java
	 * tutorial at http://www.totheriver.com/learn/xml/xmltutorial.html#6.2
	 */

	/**
	 * Saves the weights, training data, and user data to an XML file,
	 * weights.xml
	 */
	public void saveWeights(String filename) {
		
		// Get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			// Get an instance of builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			// Create an instance of DOM
			Document dom = db.newDocument();
			createDOMTree(dom);
			
			// Create the physical file
			printToFile(dom, filename);
		}
		catch (ParserConfigurationException pce) {
			System.out
			        .println("Error while trying to instantiate DocumentBuilder "
			                 + pce);
			System.exit(1);
		}
	}
	

	/**
	 * Creates the DOM tree with the main branches being the Class elements,
	 * where each Class element houses the information needed to initialize a
	 * ClassClassifier.
	 * 
	 * @param dom
	 *            Document to create
	 */
	private void createDOMTree(Document dom) {
		
		// Create the root element
		Element rootEle = dom.createElement("classes");
		// rootEle.setAttribute("featureSet", m_featureSet.toString());
		dom.appendChild(rootEle);
		
		for (String classLabel : m_weights.keySet()) {
			Element classEle = createClassElement(classLabel, dom);
			rootEle.appendChild(classEle);
		}
	}
	

	/**
	 * Creates a Class element that holds all of the information needed for a
	 * ClassClassifier. ClassName and Weights are attributes for the element,
	 * since they are required. TrainingData and UserData are child element, and
	 * they are technically optional.
	 * 
	 * @param c
	 *            ClassClassifier that is mapped to this element
	 * @param dom
	 *            Document to create
	 * @return The created element that houses the ClassClassifier information
	 */
	private Element createClassElement(String classLabel, Document dom) {
		
		Element classEle = dom.createElement("class");
		
		// Create attributes for the class name and weights
		classEle.setAttribute("label", classLabel);
		classEle.setAttribute("weights",
		        generateXMLStringFromDoubleArray(m_weights.get(classLabel)));
		
		return classEle;
	}
	

	/**
	 * This method uses Xerces specific classes prints the XML document to file.
	 */
	private void printToFile(Document dom, String filename) {
		try {
			// Print
			OutputFormat format = new OutputFormat(dom);
			format.setIndenting(true);
			
			// To generate output to console use this serializer
			// XMLSerializer serializer = new XMLSerializer(System.out, format);
			
			// To generate a file output use FileOutputStream instead of
			// System.Out
			XMLSerializer serializer = new XMLSerializer(new FileOutputStream(
			        new File(filename + ".rub")), format);
			
			serializer.serialize(dom);
		}
		catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	

	/**
	 * Load classification weights and training data from a file
	 * 
	 * @param file
	 *            The weights.xml file to load
	 */
	public void loadWeights(File file) {
		
		// Get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		// Initialize the weights
		m_weights = new HashMap<String, List<Double>>();
		
		try {
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			// Parse using builder to get DOM representation of the XML file
			Document dom = db.parse(file);
			
			// Read in the XML document and add all of the data to the
			// LinearClassifier
			parseDocument(dom);
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		catch (SAXException se) {
			se.printStackTrace();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	

	/**
	 * Reads the ClassClassifier elements from the document and places them into
	 * the newly created Hashtable<string, ClassClassifier> for this
	 * LinearClassifier.
	 * 
	 * @param dom
	 *            Document to parse
	 */
	private void parseDocument(Document dom) {
		
		// Get the root element
		Element docEle = dom.getDocumentElement();
		
		// Get the feature set
		// String featureSetString = docEle.getAttribute("featureSet");
		// m_featureSet = FeatureSet.valueOf(featureSetString);
		
		// Get a nodelist of class elements
		NodeList nl = docEle.getElementsByTagName("class");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				
				// Get the class label and weights
				String classLabel = el.getAttribute("label");
				List<Double> weights = readDoubleArrayFromXMLString(el
				        .getAttribute("weights"));
				
				// Store the values
				m_weights.put(classLabel, weights);
			}
		}
	}
	

	/**
	 * Generates a string of the form "[d1, d2, d3, ... , dn]" from a given
	 * double array.
	 * 
	 * @param dArray
	 *            Double array to generate a string for
	 * @return The double array's string
	 */
	private String generateXMLStringFromDoubleArray(List<Double> dArray) {
		String s = "[";
		
		for (int d = 0; d < dArray.size(); d++) {
			s += dArray.get(d);
			
			if (d < dArray.size() - 1)
				s += ", ";
			else
				s += "]";
		}
		
		return s;
	}
	

	/**
	 * Reads a string of the form "[d1, d2, d3, ... , dn]" and parses the string
	 * into a double array.
	 * 
	 * @param s
	 *            String to parse
	 * @return Double array
	 */
	private List<Double> readDoubleArrayFromXMLString(String s) {
		ArrayList<Double> dArrayList = new ArrayList<Double>();
		
		int startIndex = 1;
		int endIndex = s.indexOf(", ", startIndex);
		
		while (endIndex != -1) {
			double d = Double.parseDouble(s.substring(startIndex, endIndex));
			
			startIndex = endIndex + 2;
			endIndex = s.indexOf(", ", startIndex);
			
			dArrayList.add(d);
		}
		
		dArrayList.add(Double.parseDouble(s.substring(startIndex,
		        s.length() - 1)));
		
		// Create the double[]
		return dArrayList;
	}
	

	@Override
	public IRecognitionResult recognize() {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public void submitForRecognition(List<IStroke> submission) {
		// TODO Auto-generated method stub
		
	}
}