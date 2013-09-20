package test.functional.ladder.recognition.rubine;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.rubine.RubineClassifier;
import org.ladder.recognition.rubine.RubineClassifier.MultiStrokeMethod;
import org.ladder.recognition.rubine.RubineStroke.FeatureSet;

/**
 * Train rubine data on a hierarchical, directory-labeled set of data. If the
 * user presses cancel on the file chooser dialog, a default directory in
 * LadderDomains will be used.
 * 
 * @author Aaron
 */
public class RubineTrainer extends JFrame {
	
	/**
	 * The feature set to use with the rubine classifier
	 */
	private FeatureSet m_rubineFeatures = FeatureSet.Long;
	
	/**
	 * Method of handling multiple strokes
	 */
	private MultiStrokeMethod m_strokeMethod = MultiStrokeMethod.MERGE;
	
	/**
	 * Auto-generated serial ID
	 */
	private static final long serialVersionUID = 6258823800126774180L;
	
	/**
	 * Test directory
	 */
	private static final File S_TRAINDIR = new File("..//LadderData");
	
	
	/**
	 * Default constructor
	 */
	public RubineTrainer() {
		super();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	

	/**
	 * Set the features to calculate for the rubine classifier
	 * 
	 * @param features
	 *            The feature set
	 */
	public void setFeatureSet(FeatureSet features) {
		m_rubineFeatures = features;
	}
	

	/**
	 * Set the method for handling multiple strokes
	 * 
	 * @param strokeMethod
	 *            the stroke method
	 */
	public void setStrokeMethod(MultiStrokeMethod strokeMethod) {
		m_strokeMethod = strokeMethod;
	}
	

	/**
	 * Opens a file chooser dialog in the selected choose mode.
	 * 
	 */
	public void openFileChooser() {
		
		JFileChooser chooser = new JFileChooser(S_TRAINDIR);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new XMLFileFilter());
		
		File choice = null;
		
		chooser.setDialogTitle("Select a sketch directory or file to open");
		chooser.showOpenDialog(this);
		choice = chooser.getSelectedFile();
		
		if (choice == null || !choice.isDirectory()) {
			return;
		}
		
		System.out.println("Training the data...");
		RubineClassifier rubineClassifier = new RubineClassifier(
		        m_rubineFeatures);
		rubineClassifier.setMultiStrokeMethod(m_strokeMethod);
		
		rubineClassifier.trainOnDirectoryOrganizedData(choice);
		rubineClassifier.saveWeights("weights");
		
		this.dispose();
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RubineTrainer trainer = new RubineTrainer();
		trainer.setFeatureSet(FeatureSet.Long);
		trainer.setStrokeMethod(MultiStrokeMethod.MERGE);
		
		trainer.openFileChooser();
		
		System.out.println("Goodbye!");
		System.exit(-1);
	}
}
