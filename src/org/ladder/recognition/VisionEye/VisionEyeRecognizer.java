package org.ladder.recognition.VisionEye;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.IStroke;
import org.ladder.core.sketch.Shape;
import org.ladder.recognition.IRecognitionResult;
import org.ladder.recognition.RecognitionResult;
import org.ladder.recognition.recognizer.OverTimeCheckHelper;
import org.ladder.recognition.recognizer.OverTimeException;
import org.ladder.recognition.recognizer.VisionRecognizer;

public class VisionEyeRecognizer extends VisionRecognizer {
	
	/**
	 * Logger for this class.
	 */
	private static Logger log = LadderLogger
	        .getLogger(VisionEyeRecognizer.class);
	
	/**
	 * SVM Parameters
	 */
	private final static int SVM_TYPE = 0; // C-SVM
	
	private final static int KERNAL_TYPE = 2; // RBF kernel
	
	private final static double C = 200;
	
	private final static double GAMMA = .05;
	
	private final static double EPS = .001;
	
	private List<IStroke> m_strokes;
	
	private VisionEyeCodebook m_codebook;
	
	private svm_model m_model;
	
	private List<String> m_classes;
	
	
	private VisionEyeRecognizer() {
		m_strokes = new ArrayList<IStroke>();
		m_classes = new ArrayList<String>();
	}
	

	public VisionEyeRecognizer(String codebookFileName, String classFileName,
	        String svmFileName) {
		this();
		try {
			m_model = svm.svm_load_model(svmFileName);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_classes = loadClasses(classFileName);
		m_codebook = VisionEyeCodebook.loadFromFile(codebookFileName);
	}
	

	public VisionEyeRecognizer(File codebookFile, File classFile, File svmFile) {
		this();
		try {
			m_model = svm.svm_load_model(svmFile.getPath());
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_classes = loadClasses(classFile.getPath());
		m_codebook = VisionEyeCodebook.loadFromFile(codebookFile.getPath());
	}
	

	private List<String> loadClasses(String classFileName) {
		List<String> classes = new ArrayList<String>();
		try {
			BufferedReader bw = new BufferedReader(new FileReader(new File(
			        classFileName)));
			String line = bw.readLine();
			int num = 0;
			while (line != null) {
				classes.add(line);
				line = bw.readLine();
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return classes;
	}
	

	@Override
	public void submitForRecognition(IStroke submission) {
		m_strokes.add(submission);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.recognizer.IRecognizer#recognize()
	 */
	@Override
	public IRecognitionResult recognize() {
		try {
			return recognizeTimed(Long.MAX_VALUE);
		}
		catch (OverTimeException ote) {
			ote.printStackTrace();
			log.error(ote.getMessage(), ote);
		}
		
		return new RecognitionResult();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ladder.recognition.recognizer.ITimedRecognizer#recognizeTimed(long)
	 */
	@Override
	public IRecognitionResult recognizeTimed(long maxTime)
	        throws OverTimeException {
		
		// Store the start time
		long startTime = System.currentTimeMillis();
		
		List<VisionEye> allEyes = VisionEye.sample(m_strokes);
		OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		
		double[] features = m_codebook.lookup(allEyes);
		OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		
		double[] probs = new double[svm.svm_get_nr_class(m_model)];
		OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		
		svm.svm_predict_probability(m_model, getSVM_Nodes(features), probs);
		OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		
		IRecognitionResult result = new RecognitionResult();
		for (int j = 0; j < probs.length; j++) {
			IShape s = new Shape();
			s.setConfidence(new Double((1 - probs[j]) / 2));
			s.setStrokes(m_strokes);
			s.setLabel(m_classes.get(j));
			result.addShapeToNBestList(s);
			
		}
		OverTimeCheckHelper.overTimeCheck(startTime, maxTime, log);
		
		// if(result.getBestShape().getConfidence().doubleValue()>.99)
		return result;
		// return null;
	}
	

	private static svm_node[] getSVM_Nodes(double[] ds) {
		svm_node[] nodes = new svm_node[ds.length];
		for (int i = 0; i < ds.length; i++) {
			nodes[i] = new svm_node();
			nodes[i].index = i;
			nodes[i].value = ds[i];
		}
		return nodes;
	}
	

	private static svm_parameter getParams() {
		// Setting training parameters (to be fiddled with later...)
		svm_parameter param = new svm_parameter();
		
		param.svm_type = SVM_TYPE; // C-SVM
		param.kernel_type = KERNAL_TYPE; // RBF
		param.C = C;
		param.gamma = GAMMA;
		param.eps = EPS;
		param.probability = 1; // Allows for probability output
		
		return param;
	}
	

	public void clear() {
		m_strokes.clear();
	}
}
