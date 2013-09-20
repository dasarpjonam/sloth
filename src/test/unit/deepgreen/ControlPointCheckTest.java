package test.unit.deepgreen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.ladder.core.config.LadderConfig;

/*
 * This class tests to see if any shape defs in COA.xml is missing any control points.
 * 
 * Notes:
 * - Output statements found in main method.
 * - No output implies no shape defs are missing control points (i.e., all shape defs have control points).
 * - SpecialCaseSymbols.txt contains list of symbols (i.e., arrows and vision-based symbols) that have control points, 
 *   but are hard-coded elsewhere and not in shape def itself.
 */
public class ControlPointCheckTest
{
	/*** fields ***/
	private static List<String> mySpecialCaseSymbols;

	// constant absolute path names
	public static final String COA_XML_FILE_NAME = LadderConfig.getProperty(LadderConfig.DOMAIN_DESC_LOC_KEY)+"COA.xml";
	public static final String SHAPES_DIR_NAME = LadderConfig.getProperty(LadderConfig.SHAPE_DESC_LOC_KEY);
	public static final String SPECIAL_CASE_SYMBOLS_FILE_NAME = "testFiles/SpecialCaseSymbols.txt";
	
	public static final File COA_XML_FILE = new File(COA_XML_FILE_NAME);
	public static final File SHAPES_DIR = new File(SHAPES_DIR_NAME);
	public static final File SPECIAL_CASE_SYMBOLS_FILE = new File(SPECIAL_CASE_SYMBOLS_FILE_NAME);
	
	public static void main(String[] args)
	{
		List<String> shapeNames = new ArrayList<String>();
		List<String> shapeNamesMissingCPs = new ArrayList<String>();
		
		// initialize list of arrows shapes
		mySpecialCaseSymbols = getSymbolListFromFile(SPECIAL_CASE_SYMBOLS_FILE);
		
		// read COA.xml file and get all symbols that begin w/ a number
		shapeNames = readCoaXmlFile(COA_XML_FILE);
		
		// run through each shape def
		shapeNamesMissingCPs = readShapesDir(shapeNames, SHAPES_DIR);
		
		// remove arrow symbols from shape names missing control points
		removeSpecialCaseSymbols(shapeNamesMissingCPs.listIterator());
		
		/*** OUTPUT STATEMENTS ***/
		for (String shapeName : shapeNamesMissingCPs) {
			
			// output to console the file path of shapes missing control points
//			System.out.println(SHAPES_DIR_NAME + shapeName);
			
			// output to console the shapes missing control points
			System.out.println(shapeName.substring(0, shapeName.length()-4));
		}
		
		if (shapeNamesMissingCPs.isEmpty()) {
			
			System.out.println("*** All shape defs in COA.xml have at least one control point. ***");
		}
	}
	
	private static void removeSpecialCaseSymbols(ListIterator<String> shapeNamesMissingCPsItr)
	{
		String currentShapeName = "";
		boolean isArrow = false;
		while (shapeNamesMissingCPsItr.hasNext()) {
			
			currentShapeName = shapeNamesMissingCPsItr.next();
			
			// check if current shape name is an arrow
			for (String currentArrowSymbol : mySpecialCaseSymbols) {
				
				if (currentShapeName.contains(currentArrowSymbol)) {
					isArrow = true;
				}
			}
			
			if (isArrow) {
				shapeNamesMissingCPsItr.remove();
				isArrow = false;
			}
		}
	}
	
	private static boolean hasControlPoints(File file)
	{
		BufferedReader br = null;
		String line = "";
		
		try {
			
			// initialize the reader
			br = new BufferedReader(new FileReader(file));
			
			// run through each line until no more
			line = br.readLine();
			while (line != null) {
				
				if (line.contains("<aliasList>")) {
										
					if (hasControlPointInAliasSegment(br))
						return true;
					else
						return false;
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
		
		return false;
	}
	
	private static boolean hasControlPointInAliasSegment(BufferedReader br) throws IOException
	{
		String line = "";
		
		line = br.readLine();
		while (!line.contains("</aliasList>")) {
			
			line = br.readLine();
			if (!line.equals(""))
				if (line.contains("PT."))
					return true;
		}
		
		return false;
	}
	
	private static List<String> readShapesDir(List<String> shapeNames, File shapesDir)
	{
		List<String> shapeNamesMissingCPs = new ArrayList<String>();
		String shapeFilePathName = "";
		File shapeFile = null;
		
		// run through shape def of each shape name
		for (String shapeName : shapeNames) {
			
			shapeFilePathName = SHAPES_DIR_NAME + shapeName + ".xml";
			shapeFile = new File(shapeFilePathName);
			
			if (!hasControlPoints(shapeFile))
				shapeNamesMissingCPs.add(shapeFile.getName());
		}
		
		return shapeNamesMissingCPs;
	}
	
	// read COA.xml file and get all symbols that begin w/ a number
	private static List<String> readCoaXmlFile(File file)
	{
		List<String> shapeNames = new ArrayList<String>();
		
		BufferedReader br = null;
		String line = "";
		String token = "";
		int firstQuotePos = -1;
		int secondQuotePos = -1;
		StringTokenizer st = null;
		
		try {
			
			// initialize the reader
			br = new BufferedReader(new FileReader(file));
			
			// read each line until no more left
			line = br.readLine();
			while (line != null) {
				
				// process shape name lines
				if (line.contains("<shape name") && line.contains("_")) {
					
					// initialize tokenizer
					st = new StringTokenizer(line);
					
					// process desired tokens
					st.nextToken();
					token = st.nextToken();
					
					// get positions of quotes
					firstQuotePos = token.indexOf('"');
					secondQuotePos = token.indexOf('"', firstQuotePos+1);
					
					// add shape name
					shapeNames.add(token.substring(firstQuotePos+1, secondQuotePos));
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
		
		return shapeNames;
	}
	
	private static List<String> getSymbolListFromFile(File file)
	{
		List<String> list = new ArrayList<String>();
		
		BufferedReader br = null;
		String line = "";
		
		try {
			
			// initialize the reader
			br = new BufferedReader(new FileReader(file));
			
			// read while there are still lines
			line = br.readLine();
			while (line != null) {
				
				list.add(line);
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

		return list;
	}
	
	
	
}