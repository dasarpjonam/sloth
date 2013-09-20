package test.functional.ladder.recognition.rubine;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.ladder.io.XMLFileFilter;
import org.ladder.recognition.rubine.TracysClassifier;

/**
 * Train rubine data on a hierarchical, directory-labeled set of data
 * 
 * @author Aaron
 */
public class TracyRubineTrainer extends JFrame {
	
	/**
     * Auto-generated serial ID
     */
    private static final long serialVersionUID = 6258823800126774180L;


	/**
	 * Default constructor
	 */
	public TracyRubineTrainer() {
		super();
	}
	

	/**
	 * Opens a file chooser dialog in the selected choose mode.
	 * 
	 * @param chooseMode
	 *            Choose between directories, files, or both
	 */
	public void openFileChooser() {
		
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new XMLFileFilter());
		
		File choice = null;
		
		chooser.setDialogTitle("Select a sketch directory or file to open");
		chooser.showOpenDialog(this);
		choice = chooser.getSelectedFile();
		
		if (choice == null || !choice.isDirectory()) {
			choice = chooser.getCurrentDirectory();
		}
		
		System.out.println("Training the data...");
		TracysClassifier tracyClassifier = new TracysClassifier();
		tracyClassifier.trainOnDirectoryOrganizedData(choice);
		tracyClassifier.saveWeights("weights");
		
		System.out.println("Goodbye!");
		System.exit(-1);
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TracyRubineTrainer trainer = new TracyRubineTrainer();
		trainer.openFileChooser();
	}
	
}
