package org.ladder.recognition.VisionEye;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.xml.parsers.ParserConfigurationException;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

import org.ladder.core.sketch.IShape;
import org.ladder.core.sketch.ISketch;
import org.ladder.core.sketch.Sketch;
import org.ladder.io.DOMInput;
import org.ladder.io.UnknownSketchFileTypeException;
import org.xml.sax.SAXException;

public class SVMTraining extends JFrame{
	
	/**
	 * SVM Parameters
	 */
	private final static int SVM_TYPE = 0; // C-SVM
	
	private final static int KERNAL_TYPE = 2; // RBF kernel
	
	private final static double C = 1;
	
	private final static double GAMMA = .05;
	
	private final static double EPS = .1;

	private static JFileChooser m_chooser = new JFileChooser();
	
	public SVMTraining(){//List<VisionEye> clusters){
		super();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		validate();
		pack();
		m_chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int r = m_chooser.showOpenDialog(this);
		List<List<VisionEye>> features = new ArrayList<List<VisionEye>>();
		List<String> classes = new ArrayList<String>();
		if (r == JFileChooser.APPROVE_OPTION) {
			File dir = m_chooser.getSelectedFile();
			for(File shapeDir : dir.listFiles()){
				if(shapeDir.isDirectory()){
					for(File shapeFile : shapeDir.listFiles()){
						if(shapeFile.getName().endsWith(".xml")){
							DOMInput inFile = new DOMInput();
							ISketch m_sketch=new Sketch();
							try {
								m_sketch = inFile.parseDocument(shapeFile);
								System.out.println(shapeFile.getName());
								List<VisionEye> eyes = VisionEye.sample(m_sketch.getStrokes());
								features.add(eyes);
								classes.add(shapeDir.getName());
//								for(IShape shape : m_sketch.getShapes()){
//									List<VisionEye> eyes = VisionEye.sample(shape.getStrokes());
//									features.add(eyes);
//									classes.add(shape.getDescription());
//								}
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

						}
					}
				}
			}
		}
		
		m_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		r = m_chooser.showOpenDialog(this);
		VisionEyeCodebook cb = null;
		if(r == JFileChooser.APPROVE_OPTION){
			cb = VisionEyeCodebook.loadFromFile(m_chooser.getSelectedFile().getAbsolutePath());
		}
		
		List<double[]> codes = new ArrayList<double[]>();
		for(List<VisionEye> singleFeatures : features){
			codes.add(cb.lookup(singleFeatures));
		}
		
		List<String> classNames = new ArrayList<String>();
		for(String s : classes){
			if(!classNames.contains(s))
				classNames.add(s);
		}
		
		System.out.println("Examples: "+codes.size());
		System.out.println("Classes: "+classNames.size());
		
		double[] classNumberArray = new double[codes.size()-1];
		svm_node[][] matchArray = new svm_node[codes.size()-1][];
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("newData.txt")));
			for(String c : classNames){
				bw.write("\t"+c);
			}
			bw.newLine();
			for(int leaveout = 0; leaveout < codes.size(); leaveout++){
				for(int i = 0; i < codes.size(); i++){
					if(i==leaveout)
						continue;
					if(i>leaveout){
						matchArray[i-1] = getSVM_Nodes(codes.get(i));
						classNumberArray[i-1] = classNames.indexOf(classes.get(i));
					}
					else{
						matchArray[i] = getSVM_Nodes(codes.get(i));
						classNumberArray[i] = classNames.indexOf(classes.get(i));
						
					}
				}
		
				svm_problem prob = new svm_problem();
				prob.l = classNumberArray.length;
				prob.x = matchArray;
				prob.y = classNumberArray;
				System.out.println("Training svm");
				svm_model model = svm.svm_train(prob, getParams());
	//			try {
	//				svm.svm_save_model("VisionEye.svm", model);
	//			} catch (IOException e) {
	//				// TODO Auto-generated catch block
	//				e.printStackTrace();
	//			}
				double[] probs = new double[classNames.size()];
				svm.svm_predict_probability(model, getSVM_Nodes(codes.get(leaveout)), probs);
				bw.write(classes.get(leaveout));
				for(int i = 0; i < probs.length; i++)
					bw.write("\t"+probs[i]);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	private svm_node[] getSVM_Nodes(double[] ds) {
		svm_node[] nodes = new svm_node[ds.length];
		for(int i = 0; i<ds.length; i++){
			nodes[i] = new svm_node();
			nodes[i].index=i;
			nodes[i].value=ds[i];
		}
		return nodes;
	}
	
	private svm_parameter getParams() {
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


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SVMTraining svmt = new SVMTraining();

	}

}
