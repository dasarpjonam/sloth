package org.ladder.tools;

import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

/*
 * This class parses various information from a DDAT output file.
 * 
 * Instructions:
 * 1. Run parser.
 * 2. Load source DDAT file.
 * 3. Select attributes of DDAT file to output.
 * 4. Save to a target location (if you try to save to loaded DDAT file, file will be renamed).
 * 
 */
public class DDATParser
{
	/*** main method ***/
	public static void main(String[] args)
	{
		// select the file to load from
		FileFilter textFileFilter = new ExtensionFileFilter("Text File", new String[] { "TXT" });
		JFileChooser chooser = new JFileChooser();
	    chooser.setDialogTitle("Please select a DDAT text file.");
		chooser.setFileFilter(textFileFilter);
		int loadResult = chooser.showOpenDialog(null);
		
		// case: the user clicks the "Open" button
		if (loadResult == JFileChooser.APPROVE_OPTION) {
			
			// get and parse file
			File ddatFile = chooser.getSelectedFile();
			DDATParser parser = new DDATParser();
			List<SymbolData> symbolDataList = parser.run(ddatFile);
			
			// output window to choose data output settings
			outputSelectionWindow();
			
			// set the output properties of the data
			SymbolData.setOutputProperties(SelectionWindow.SELECTIONS);
			
			// select the file to save to
			JFileChooser saver = new JFileChooser();
			saver.setDialogTitle("Save as...");
			saver.setApproveButtonText("Save");
			saver.setFileFilter(textFileFilter);
			int saveResult = saver.showOpenDialog(null);
			
			// case: the user clicks the "Save" button
			if (saveResult == JFileChooser.APPROVE_OPTION) {
				
				// set the save file
				String saveFileName = saver.getSelectedFile().getAbsolutePath();
				
				// append ".txt" file extension of name if user didn't include it
				if (!saveFileName.endsWith(".txt"))
					saveFileName = saveFileName + ".txt";
				
				// initialize the save file
				File saveFile = new File(saveFileName);
				
				// compare save file's location to DDAT file's location, and rename save file if both locations are the same
				if (saveFile.toString().equals(ddatFile.toString())) {
					
					saveFileName = saveFile.getAbsolutePath();
					saveFileName = saveFileName.substring(0, saveFileName.length()-4);
					saveFileName = saveFileName + "_SPREADSHEET.txt";
					saveFile = new File(saveFileName);
				}
				
				else if (!saveFile.exists()) {
					try {
						saveFile.createNewFile();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				// output to file the contents of the symbol data objects (based on the output selection properties)
				writeToFile(saveFile, symbolDataList);
			}
			
			// output to console
			System.out.println(SymbolData.getHeader());
			for (SymbolData symbolData : symbolDataList) {
				
				System.out.println(symbolData);
			}
		}
	}
	
	private static void writeToFile(File file, List<SymbolData> symbolDataList)
	{
		BufferedWriter bw = null;
		
		try {
			// initialize the buffered object
			bw = new BufferedWriter(new FileWriter(file));
			
			// output to console
			bw.write(SymbolData.getHeader());
			bw.newLine();
			for (SymbolData symbolData : symbolDataList) {
				
				bw.write(symbolData.toString());
				bw.newLine();
			}
		}
		catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		finally {
			
			// close the buffered object
			try {
					bw.close();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private static void outputSelectionWindow()
	{
//		javax.swing.SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//            	
//            	// create and set up the window
//                JFrame frame = new JFrame("DDAT Spreadsheet Generator");
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//                // create and set up the content pane
//                JComponent newContentPane = new SelectionWindow();
//                newContentPane.setOpaque(true); //content panes must be opaque
//                frame.setContentPane(newContentPane);
//
//                // display the window
//                frame.pack();
//                frame.setVisible(true);
//                frame.setLocationRelativeTo(null); // centers the window frame
//                
////                (while)
//                frame.dispose();
//            }
//        });
		
		// create and set up the window
        frame = new JFrame("DDAT Spreadsheet Generator");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        // create and set up the content pane
        JComponent newContentPane = new SelectionWindow();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        // display the window
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null); // centers the window frame
        
        // close frame after user clicks button (i.e., when IS_DONE flag is set to true)
        while (!SelectionWindow.IS_DONE) {
        	
//        	System.out.println("*** Running Selection Window. ***");
        }
        frame.dispose();
	}
	
	private static JFrame frame;
	
	/*** constructor ***/
	public DDATParser()
	{
		;
	}
	
	/*** public methods ***/
	public List<SymbolData> run(File file)
	{
		BufferedReader br = null;
		String line = "";
		String token = "";
		StringTokenizer st = null;
		boolean readAccuracyLine = false;
		
		SymbolData symbolData = null;
		List<SymbolData> symbolDataList = new ArrayList<SymbolData>();
		
		int numerator;
		int denominator;
		String ratio;
		double result;
		
		try {
			
			// initialize the buffered object
			br = new BufferedReader(new FileReader(file));
			
			// run through each line in file
			line = br.readLine();
			while (line != null) {
				
				// case: line w/ accuracy data
				if (line.contains("Accuracy:")) {
					
					readAccuracyLine = true;
					
					// tokenize line
					st = new StringTokenizer(line);
					
					// create SymbolData object on token, which is symbol's name
					token = st.nextToken();
					symbolData = new SymbolData(token);

					// set accuracy data
					st.nextToken(); // ignore token "Accuracy"
					numerator = Integer.parseInt(st.nextToken()); // extract numerator token
					st.nextToken(); // ignore token "/"
					denominator = Integer.parseInt(st.nextToken()); // extract denominator token
					ratio = "'" + numerator + " / " + denominator;
					st.nextToken(); // ignore token "="
					result = Double.parseDouble(st.nextToken()); // extract result
					symbolData.setAccuracyData(numerator, denominator, ratio, result);
				}
				
				// case: line after line w/ accuracy data
				else if (readAccuracyLine) {

					readAccuracyLine = false;
					
					if (line.contains("Precision:")) {
						
						// tokenize line
						st = new StringTokenizer(line);
						
						// set precision data
						st.nextToken(); // ignore symbol name token
						st.nextToken(); // ignore token "Accuracy"
						numerator = Integer.parseInt(st.nextToken()); // extract numerator token
						st.nextToken(); // ignore token "/"
						denominator = Integer.parseInt(st.nextToken()); // extract denominator token
						ratio = "'" + numerator + " / " + denominator;
						st.nextToken(); // ignore token "="
						result = Double.parseDouble(st.nextToken()); // extract result
						symbolData.setPrecisionData(numerator, denominator, ratio, result);
					}
					else {
						
						symbolData.setAccuracyToPrecision();
					}
					
					// add SymbolData object to list
					symbolDataList.add(symbolData);
				}
				
				
				line = br.readLine();
			}
		}
		catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		finally {
			
			// close the buffered object
			try {
					br.close();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		
		return symbolDataList;
	}
	
	/*** fields ***/
	public static final int ACCURACY_RESULT = 0;
	public static final int ACCURACY_RATIO = 1;
	public static final int ACCURACY_NUMERATOR = 2;
	public static final int ACCURACY_DENOMINATOR = 3;
	public static final int PRECISION_RESULT = 4;
	public static final int PRECISION_RATIO = 5;
	public static final int PRECISION_NUMERATOR = 6;
	public static final int PRECISION_DENOMINATOR = 7;
}

class SelectionWindow extends JPanel implements ActionListener, ItemListener
{
	/*** constructor ***/
	public SelectionWindow()
	{
		// create the layout
		super(new GridBagLayout());
		
		// create the text panel
		JPanel textPanel = initializeTextPanel();
		
		// create the checkbox panel
		JPanel checkboxPanel = initializeCheckBoxPanel();
		
		// create the button panel
		JPanel buttonPanel = initializeButtonPanel();
		
		// add checkbox panel to main panel
		add(textPanel,     setGridBagConstraints(0, 0, 0, 0, 10));
		add(checkboxPanel, setGridBagConstraints(0, 1, 0, 0, 10));
		add(buttonPanel,   setGridBagConstraints(0, 2, 0, 0, 10));
	}
	
	/*** methods ***/
	private GridBagConstraints setGridBagConstraints(int xPos, int yPos, int xPad, int yPad, int padValue)
	{
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
    	c.gridx = xPos;
    	c.gridy = yPos;
    	c.ipadx = xPad;
    	c.ipady = yPad;
    	c.insets = new Insets(padValue, padValue, padValue, padValue);
		
		return c;
	}
	
	private JPanel initializeTextPanel()
	{
		// create the panel
		JPanel textPanel = new JPanel(new GridLayout());
		
		// create the text
		String text = "Please select the DDAT attribute(s) you wish to generate for a spreadsheet.";
		JLabel textLabel = new JLabel(text);
		
		// set the panel
		textPanel.add(textLabel);
		
		return textPanel;
	}
	
	private JPanel initializeCheckBoxPanel()
	{
		// create the panel
		JPanel checkboxPanel = new JPanel(new GridLayout(4, 2));
		
		// create the checkboxes
		myAccuracyResultCheckBox = new JCheckBox("Accuracy Result");
		myAccuracyResultCheckBox.setSelected(true);
		
		myAccuracyRatioCheckBox = new JCheckBox("Accuracy Ratio");
		myAccuracyRatioCheckBox.setSelected(true);
		
		myAccuracyNumeratorCheckBox = new JCheckBox("Accuracy Number of Correct");
		myAccuracyNumeratorCheckBox.setSelected(false);
		
		myAccuracyDenominatorCheckBox = new JCheckBox("Accuracy Total Number");
		myAccuracyDenominatorCheckBox.setSelected(false);
		
		myPrecisionResultCheckBox = new JCheckBox("Precision Result");
		myPrecisionResultCheckBox.setSelected(true);
		
		myPrecisionRatioCheckBox = new JCheckBox("Precision Ratio");
		myPrecisionRatioCheckBox.setSelected(true);
		
		myPrecisionNumeratorCheckBox = new JCheckBox("Precision Number of Correct");
		myPrecisionNumeratorCheckBox.setSelected(false);
		
		myPrecisionDenominatorCheckBox = new JCheckBox("Precision Total Number");
		myPrecisionDenominatorCheckBox.setSelected(false);
		
		// set the panel
		checkboxPanel.add(myAccuracyResultCheckBox);
		checkboxPanel.add(myPrecisionResultCheckBox);
		checkboxPanel.add(myAccuracyRatioCheckBox);
		checkboxPanel.add(myPrecisionRatioCheckBox);
		checkboxPanel.add(myAccuracyNumeratorCheckBox);
		checkboxPanel.add(myPrecisionNumeratorCheckBox);
		checkboxPanel.add(myAccuracyDenominatorCheckBox);
		checkboxPanel.add(myPrecisionDenominatorCheckBox);
		
		return checkboxPanel;
	}

	private JPanel initializeButtonPanel()
	{
		// create the panel
		JPanel buttonPanel = new JPanel(new GridLayout());
		
		// create the button
		String text = "Generate";
		JButton button = new JButton(text);
		button.setActionCommand("generate");
		button.addActionListener(this);
		
		// set the panel
		buttonPanel.add(button);
		
		return buttonPanel;
	}
	
	/*** fields ***/
	public void actionPerformed(ActionEvent event)
	{
		// check if button has been pressed
		if ("generate".equals(event.getActionCommand())) {
			
			// check the status of each check box
			SELECTIONS = new boolean[] {
				myAccuracyResultCheckBox.isSelected(),
				myAccuracyRatioCheckBox.isSelected(),
				myAccuracyNumeratorCheckBox.isSelected(),
				myAccuracyDenominatorCheckBox.isSelected(),
				myPrecisionResultCheckBox.isSelected(),
				myPrecisionRatioCheckBox.isSelected(),
				myPrecisionNumeratorCheckBox.isSelected(),
				myPrecisionDenominatorCheckBox.isSelected(),
			};
			
			IS_DONE = true;
		}
	}

	public void itemStateChanged(ItemEvent event)
	{
		// TODO Auto-generated method stub
		
	}
	
	/*** fields ***/
	private JCheckBox myAccuracyResultCheckBox;
	private JCheckBox myAccuracyRatioCheckBox;
	private JCheckBox myAccuracyNumeratorCheckBox;
	private JCheckBox myAccuracyDenominatorCheckBox;
	private JCheckBox myPrecisionResultCheckBox;
	private JCheckBox myPrecisionRatioCheckBox;
	private JCheckBox myPrecisionNumeratorCheckBox;
	private JCheckBox myPrecisionDenominatorCheckBox;
	
	public static boolean IS_DONE = false;
	public static boolean[] SELECTIONS;
}

class SymbolData
{
	/*** constructor ***/
	public SymbolData(String name)
	{
		myName = name;
	}
	
	/*** methods ***/
	public void setAccuracyData(int accuracyNumerator, int accuracyDenominator, String accuracyRatio, double accuracyResult)
	{
		myAccuracyNumerator = accuracyNumerator;
		myAccuracyDenominator = accuracyDenominator;
		myAccuracyRatio = accuracyRatio;
		myAccuracyResult = accuracyResult;
	}
	
	public void setPrecisionData(int precisionNumerator, int precisionDenominator, String precisionRatio, double precisionResult)
	{
		myPrecisionNumerator = precisionNumerator;
		myPrecisionDenominator = precisionDenominator;
		myPrecisionRatio = precisionRatio;
		myPrecisionResult = precisionResult;
	}
	
	// sets the accuracy data equal to the precision data (since DDAT doesn't produce precision for shapes w/ 100% accuracy)
	public void setAccuracyToPrecision()
	{
		myPrecisionNumerator = myAccuracyNumerator;
		myPrecisionDenominator = myAccuracyDenominator;
		myPrecisionRatio = myAccuracyRatio;
		myPrecisionResult = myAccuracyResult;
	}
	
	public static void setOutputProperties(boolean[] selections)
	{
		// get the output selections
		mySelections = selections;
		
		// set the header
		myHeader = "Symbol Name;";
		if (mySelections[ACCURACY_RESULT])
			myHeader += "Accuracy Result;";
		if (mySelections[ACCURACY_RATIO])
			myHeader += "Accuracy Ratio;";
		if (mySelections[ACCURACY_NUMERATOR])
			myHeader += "Accuracy Number of Correct;";
		if (mySelections[ACCURACY_DENOMINATOR])
			myHeader += "Accuracy Total Number;";
		if (mySelections[PRECISION_RESULT])
			myHeader += "Precision Result;";
		if (mySelections[PRECISION_RATIO])
			myHeader += "Precision Ratio;";	
		if (mySelections[PRECISION_NUMERATOR])
			myHeader += "Precision Number of Correct;";
		if (mySelections[PRECISION_DENOMINATOR])
			myHeader += "Precision Total Number;";
	}
	
	public String toString()
	{
		String output = "";
		
//		output = myName + "\n";
//		output += "\tAccuracy: " + myAccuracyRatio + " | " + myAccuracyResult + "\n";
//		output += "\tPrecision: " + myPrecisionRatio + " | " + myPrecisionResult + "\n";
			
		output += myName + ";";
		if (mySelections[ACCURACY_RESULT])
			output += myAccuracyResult + ";";
		if (mySelections[ACCURACY_RATIO])
			output += myAccuracyRatio + ";";	
		if (mySelections[ACCURACY_NUMERATOR])
			output += myAccuracyNumerator + ";";
		if (mySelections[ACCURACY_DENOMINATOR])
			output += myAccuracyDenominator + ";";
		if (mySelections[PRECISION_RESULT])
			output += myAccuracyResult + ";";
		if (mySelections[PRECISION_RATIO])
			output += myAccuracyRatio + ";";	
		if (mySelections[PRECISION_NUMERATOR])
			output += myAccuracyNumerator + ";";
		if (mySelections[PRECISION_DENOMINATOR])
			output += myAccuracyDenominator + ";";
		
		return output;
	}
	
	public static String getHeader()
	{
		return myHeader;
	}
	
	/*** field ***/
	// symbol's name
	private String myName;
	
	// symbol's accuracy data
	private int myAccuracyNumerator;
	private int myAccuracyDenominator;
	private String myAccuracyRatio;
	private double myAccuracyResult;
	
	// symbol's precision data
	private int myPrecisionNumerator;
	private int myPrecisionDenominator;
	private String myPrecisionRatio;
	private double myPrecisionResult;
	
	// selection choices
	private static boolean[] mySelections = { false, false, false, false, false, false, false, false };
	private static String myHeader = "";
	
	public static final int ACCURACY_RESULT = 0;
	public static final int ACCURACY_RATIO = 1;
	public static final int ACCURACY_NUMERATOR = 2;
	public static final int ACCURACY_DENOMINATOR = 3;
	public static final int PRECISION_RESULT = 4;
	public static final int PRECISION_RATIO = 5;
	public static final int PRECISION_NUMERATOR = 6;
	public static final int PRECISION_DENOMINATOR = 7;
}

class ExtensionFileFilter extends FileFilter
{
	public ExtensionFileFilter(String description, String extension)
	{
		this(description, new String[] { extension });
	}
	
	public ExtensionFileFilter(String description, String[] extensions)
	{
	    if (description == null) {
			myDescription = extensions[0];
	    }
		else {
			myDescription = description;
	    }
	    myExtensions = (String[]) extensions.clone();
	    toLower(myExtensions);
	  }

	  private void toLower(String array[]) {
	    for (int i = 0, n = array.length; i < n; i++) {
			array[i] = array[i].toLowerCase();
	    }
	}
	
	public String getDescription()
	{
		return myDescription;
	}
	
	public boolean accept(File file)
	{
		if (file.isDirectory()) {
			return true;
	    }
		else {
			String path = file.getAbsolutePath().toLowerCase();
			for (int i = 0, n = myExtensions.length; i < n; i++) {
				String extension = myExtensions[i];
				if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
					return true;
				}
			}
	    }
	    return false;
	}
	  
	/*** fields ***/
	private String myDescription;
	private String[] myExtensions;
}