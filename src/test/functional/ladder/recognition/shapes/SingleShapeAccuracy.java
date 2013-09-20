package test.functional.ladder.recognition.shapes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.ladder.core.logging.LadderLogger;
import org.ladder.core.sketch.IShape;
import org.ladder.recognition.constraint.domains.ShapeDefinition;

import test.functional.ladder.recognition.constraint.domains.InterpretationConfidenceComparatorDescending;
import edu.tamu.deepGreen.recognition.DeepGreenRecognizer;
import edu.tamu.deepGreen.recognition.IDeepGreenInterpretation;
import edu.tamu.deepGreen.recognition.IDeepGreenNBest;
import edu.tamu.deepGreen.recognition.SIDC;
import edu.tamu.deepGreen.recognition.exceptions.NoSuchAttributeException;

/**
 * Contains the accuracy information for a single shape data file.
 * 
 * @author tracy
 * 
 */
public class SingleShapeAccuracy {
	
	/**
	 * Logger for this class
	 */
	private static final Logger log = LadderLogger
	        .getLogger(SingleShapeAccuracy.class);
	
	/**
	 * the file being read
	 */
	private File m_file = null;
	
	/**
	 * The directory this file is being read from
	 */
	private File m_directory = null;
	
	/**
	 * Shape definition for the file we're supposed to be recognizing
	 */
	private ShapeDefinition m_shapeDefinition = null;
	
	/**
	 * The SIDC that is the goal of recognition. This is a combination of what
	 * base SIDC is in the shape definition plus additional information about
	 * the specific sketch in the XML file that is codified in the file and
	 * directory names.
	 */
	private String m_targetSIDC = null;
	
	/**
	 * The set of fields, with String names and String values, that we're also
	 * going to consider for the correctness of the shape. For example, if this
	 * shape is supposed to have a textual label (ie an assembly area, AA, with
	 * the name Charlie), then we'll put "Text" and "Charlie" into this map. The
	 * interpretation must contain the text "Charlie" to be considered correct.
	 */
	final private Map<String, String> m_targetFields = new HashMap<String, String>();
	
	private static final String DEC_GFX_NUM_ARMOR = "NUM_ARMOR";
	
	private static final String DEC_GFX_NUM_ARMOR_CAV = "NUM_ARMOR_CAV";
	
	private static final String DEC_GFX_NUM_INF = "NUM_INF";
	
	private static final String DEC_GFX_NUM_MECH_INF = "NUM_MECH_INF";
	
	private final String ARMOR_SIDC = "UCA---";
	
	private final String ARMOR_CAV_SIDC = "UCRVA-";
	
	private final String INFANTRY_SIDC = "UCI---";
	
	private final String MECH_INFANTRY_SIDC = "UCIZ--";
	
	private static final String DEC_GFX_PARENT_SIDC = "UC----";
	
	/**
	 * the list of recognized results for this shape
	 */
	private List<LiteInterpretation> m_interpretationList = null;
	
	/**
	 * the file to write results to
	 */
	private FileWriter m_outputWriter = null;
	
	/**
	 * list of primitives that make up the shape
	 */
	private List<String> m_primitives = null;
	
	private final HashMap<String, String> STATUS_CODES = new HashMap<String, String>();
	
	private final HashMap<String, String> ECHELON_CODES = new HashMap<String, String>();
	
	// How long from start to finish it takes to recognize this sketch
	// TODO private long m_timeToRecognize;
	
	/**
	 * Coded value to tell why the shape was not recognized correct
	 */
	private ReasonForFailure m_reasonForFailure = ReasonForFailure.CORRECT;
	
	public static enum ReasonForFailure {
		CORRECT, LOW_LEVEL, HIGH_LEVEL
	}
	
	
	public SingleShapeAccuracy(File file, File dataDir,
	        ShapeDefinition shapeDef, IDeepGreenNBest nbestList,
	        List<IShape> primitives) {
		m_file = file;
		m_directory = dataDir;
		m_shapeDefinition = shapeDef;
		m_interpretationList = createInterpretationList(nbestList);
		m_primitives = this.getPrimitives(primitives);
		
		STATUS_CODES.put("P", "P"); // present
		STATUS_CODES.put("A", "A"); // anticipated
		STATUS_CODES.put("X", "*"); // wildcard
		
		ECHELON_CODES.put("BDE", "H"); // brigade
		ECHELON_CODES.put("BN", "F"); // battalion
		ECHELON_CODES.put("CO", "E"); // company
		ECHELON_CODES.put("PLT", "D"); // platoon
		ECHELON_CODES.put("X", "*"); // wildcard
	}
	

	/**
	 * States whether or not a particular interpretation is in the list of
	 * possible interpretations
	 * 
	 * @param count
	 *            the number of top interpretations to search through
	 * @return true is if is in the top 'count' interpretations, else false
	 */
	public boolean containsInterpretation(int count) {
		for (int rank = 0; rank < count; rank++) {
			String label = getInterpretationLabel(rank);
			if (label == null) {
				return false;
			}
			if (considerCorrect(getInterpretation(rank))) {
				return true;
			}
		}
		return false;
	}
	

	/**
	 * Creates a less memory intensive interpretation list from a
	 * IRecognitionResult
	 * 
	 * @param result
	 *            the RecognitionResult with multiple interpretations
	 * @return the list of interpretations
	 */
	public List<LiteInterpretation> createInterpretationList(
	        IDeepGreenNBest nbestList) {
		
		List<LiteInterpretation> recognizedList = new ArrayList<LiteInterpretation>();
		if (nbestList == null) {
			return recognizedList;
		}
		
		for (IDeepGreenInterpretation interp : nbestList.getNBestList()) {
			
			LiteInterpretation li = convertInterpretation(interp);
			
			// Any sub interpretations?
			if (interp.getSubInterpretations() != null
			    && !interp.getSubInterpretations().isEmpty()) {
				
				for (IDeepGreenInterpretation subInterp : interp
				        .getSubInterpretations()) {
					LiteInterpretation subLi = convertInterpretation(subInterp);
					li.getSubInterpretations().add(subLi);
				}
			}
			
			recognizedList.add(li);
			
			if (log.isDebugEnabled()) {
				log.debug("Converted interpretation: " + li);
			}
		}
		
		Collections.sort(recognizedList,
		        new InterpretationConfidenceComparatorDescending());
		
		return recognizedList;
	}
	

	/**
	 * Convert an {@link IDeepGreenInterpretation} into a
	 * {@link LiteInterpretation}
	 * 
	 * @param interp
	 *            The {@link IDeepGreenInterpretation} to convert
	 * @return The resulting, converted {@link LiteInterpretation}
	 */
	private LiteInterpretation convertInterpretation(
	        IDeepGreenInterpretation interp) {
		LiteInterpretation li = new LiteInterpretation(interp.getSIDC(), interp
		        .getConfidence());
		
		try {
			li.setCommonName(interp
			        .getAttribute(DeepGreenRecognizer.S_ATTR_LABEL));
		}
		catch (NoSuchAttributeException e1) {
			// who cares?
		}
		
		try {
			String shapeLabel = interp
			        .getAttribute(DeepGreenRecognizer.S_ATTR_TEXT_LABEL);
			if (shapeLabel != null) {
				li.setTextualLabel(shapeLabel);
			}
		}
		catch (NoSuchAttributeException e) {
			// who cares?
		}
		
		try {
			String shapeFField = interp
			        .getAttribute(DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F);
			if (shapeFField != null) {
				li.setFieldF(shapeFField);
			}
		}
		catch (NoSuchAttributeException e) {
			// who cares?
		}
		
		try {
			String shapeAAField = interp
			        .getAttribute(DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_AA);
			if (shapeAAField != null) {
				li.setFieldAA(shapeAAField);
			}
		}
		catch (NoSuchAttributeException e) {
			// who cares?
		}
		
		return li;
	}
	

	/**
	 * returns the name of the best shape interpretation
	 * 
	 * @return name of the best interpretation
	 */
	public String getBestLabel() {
		if (getBestShape() == null) {
			return null;
		}
		return getBestShape().getInterpretation();
	}
	

	/**
	 * Returns the best shape interpretation
	 * 
	 * @return best shape
	 */
	public LiteInterpretation getBestShape() {
		if (m_interpretationList.size() == 0) {
			return null;
		}
		return m_interpretationList.get(0);
	}
	

	/**
	 * Get the file being tested
	 * 
	 * @return the example file
	 */
	public File getFile() {
		return m_file;
	}
	

	/**
	 * Get the name of the file being tested
	 * 
	 * @return the filename
	 */
	public String getFileName() {
		return getFile().getName();
	}
	

	/**
	 * Returns an interpretation. A rank of 0 gets the best interpretation
	 * 
	 * @param rank
	 *            the interpretation number
	 * @return the interpretation
	 */
	public LiteInterpretation getInterpretation(int rank) {
		if (!hasInterpretation()) {
			return null;
		}
		if (getInterpretationsCount() <= rank) {
			return null;
		}
		
		return m_interpretationList.get(rank);
	}
	

	/**
	 * Returns the confidence of the rank's interpretation rank = 0 provides the
	 * confidence of the best interpretation
	 * 
	 * @param rank
	 *            the interpretation number, 0 is best
	 * @return the confidence of the rank's interpretation
	 */
	public double getInterpretationConfidence(int rank) {
		LiteInterpretation i = getInterpretation(rank);
		if (i == null) {
			return 0;
		}
		return i.getConfidence();
	}
	

	/**
	 * Returns a string describing name of one of the interpretation
	 * interpretation A rank of 0 gets the best interpretation.
	 * 
	 * @param rank
	 *            the interpretation number
	 * @return the name of the interpretation
	 */
	public String getInterpretationLabel(int rank) {
		LiteInterpretation i = getInterpretation(rank);
		if (i == null) {
			return null;
		}
		return getInterpretation(rank).getInterpretation();
	}
	

	public int getInterpretationsCount() {
		return m_interpretationList.size();
	}
	

	/**
	 * Returns a string reporting all interpretations Interpretation 0 is the
	 * best interpretation.
	 * 
	 * @return a string such as " Interpretation 0: ...\n  Interpretation 1:"
	 */
	public String getInterpretationsString() {
		String s = "";
		int count = 0;
		for (LiteInterpretation li : m_interpretationList) {
			s += "  Interpretation " + count++ + ": " + li.getInterpretation()
			     + ":" + li.getConfidence() + " (" + li.getCommonName() + ")"
			     + "\n";
		}
		return s;
	}
	

	/**
	 * Returns a string representing the top interpretations Interpretation 0 is
	 * the best interpretation.
	 * 
	 * @param count
	 *            the number of top interpretations to get
	 * @return a string such as " Interpretation 0: ...\n  Interpretation 1:"
	 */
	public String getInterpretationsString(int count) {
		String s = "";
		for (int rank = 0; rank < count; rank++) {
			String ilabel = getInterpretationLabel(rank);
			if (ilabel == null) {
				break;
			}
			s += "  Interpretation " + rank + ": "
			     + getInterpretationLabel(rank) + ":"
			     + getInterpretationConfidence(rank) + "\n";
		}
		return s;
	}
	

	/**
	 * Returns a string representing the top interpretations Interpretation 0 is
	 * the best interpretation.
	 * 
	 * @param count
	 *            the number of top interpretations to get
	 * @return a string such as " Interpretation 0: ...\n  Interpretation 1:"
	 */
	public String getBetterInterpretationsString() {
		StringBuffer sb = new StringBuffer();
		
		// is the correct answer in the list?
		boolean correctInList = false;
		
		for (LiteInterpretation li : m_interpretationList) {
			sb.append('\t').append(li.getInterpretation()).append('\t').append(
			        li.getCommonName()).append('\t').append(li.getConfidence())
			        .append('\n');
			
			// if this interpretation is a decision graphic, print out the
			// counts of its subtypes for more debugging information
			if (isDecisionGraphic(li.getCommonName())) {
				Map<String, Integer> subCountMap = countDecisionGraphicSubInterpretations(li);
				for (String subName : subCountMap.keySet()) {
					sb.append("\t\t").append(subName).append(" = ").append(
					        subCountMap.get(subName)).append('\n');
				}
			}
			
			if (considerCorrect(li)) {
				correctInList = true;
				break;
			}
		}
		
		if (correctInList) {
			sb
			        .append("\t---- Correct interpretation in the list, in this location ----");
		}
		else {
			sb
			        .append("\t---- Correct interpretation is **NOT** in the list ----");
		}
		
		return sb.toString();
	}
	

	/**
	 * Gets the set output writer
	 * 
	 * @return the output writer
	 */
	public FileWriter getOutputWriter() {
		return m_outputWriter;
	}
	

	/**
	 * Computes a vector string of the primitives
	 * 
	 * @param primitives
	 *            the recognition result of the primitives
	 * @return a list of the primitives
	 */
	public List<String> getPrimitives(List<IShape> primitives) {
		m_primitives = new ArrayList<String>();
		int numLines = 0;
		for (IShape primitive : primitives) {
			if (primitive.getLabel().startsWith("Polyline")) {
				String lbl = primitive.getLabel().substring(
				        primitive.getLabel().indexOf('(') + 1);
				lbl = lbl.substring(0, lbl.indexOf(')'));
				int num = Integer.parseInt(lbl);
				numLines += num;
			}
			else if (primitive.getLabel().startsWith("Line")) {
				numLines++;
			}
			else
				m_primitives.add(primitive.getLabel());
		}
		if (numLines > 0) {
			m_primitives.add("Lines (" + numLines + ")");
		}
		return m_primitives;
	}
	

	/**
	 * Returns a string listing of the primitives (only their top
	 * interpretation) used to recognize the shape
	 * 
	 * @return a string such as "Rectangle Gull Line(4)"
	 */
	public String getPrimitiveString() {
		String s = "    Primitives";
		for (String primitive : m_primitives) {
			s += " " + primitive;
		}
		s += "\n";
		return s;
		
	}
	

	/**
	 * Added this to simplify parsing the primitive list
	 * 
	 * @return
	 */
	public String getPrimitiveStringLite() {
		String s = "";
		for (String primitive : m_primitives) {
			s += " " + primitive;
		}
		return s;
	}
	

	/**
	 * Gets the list of recognized interpretations for this file
	 * 
	 * @return the list of interpretations
	 */
	public List<LiteInterpretation> getRecognizedList() {
		return m_interpretationList;
	}
	

	/**
	 * Returns a string of the filename, the correct label, and the label of the
	 * interpretation with the highest confidence
	 * 
	 * @return a string of the type:
	 *         "/Correct_type/ recognized as /interpretation/ (filename)\n"
	 */
	public String getResultString() {
		StringBuilder sb = new StringBuilder();
		if (isCorrect()) {
			sb.append("  CORRECT: ").append(m_file.getName());
		}
		else {
			sb.append("  WRONG: ").append(m_file.getName());
			if (getBestShape() != null) {
				sb.append("\n\t\tExpected SIDC: ").append(m_targetSIDC).append(
				        ". Interpretation's SIDC: ").append(
				        getBestShape().getInterpretation()).append('\n');
				sb.append("\t\t(Common name: ").append(
				        m_shapeDefinition.getName()).append(
				        "). Interpretation's common name: ").append(
				        getBestShape().getCommonName());
				
				Map<String, Integer> bestShapeCounts = countDecisionGraphicSubInterpretations(getBestShape());
				
				if (m_targetFields.get(DEC_GFX_NUM_ARMOR) != null
				    && m_targetFields.get(DEC_GFX_NUM_ARMOR_CAV) != null
				    && m_targetFields.get(DEC_GFX_NUM_INF) != null
				    && m_targetFields.get(DEC_GFX_NUM_MECH_INF) != null) {
					
					sb.append("\n\t\tTarget ARMOR: ").append(
					        m_targetFields.get(DEC_GFX_NUM_ARMOR)).append(
					        ", Interpretation's ARMOR: ").append(
					        bestShapeCounts.get(DEC_GFX_NUM_ARMOR))
					        .append('\n');
					sb.append("\t\tTarget ARMOR CAV: ").append(
					        m_targetFields.get(DEC_GFX_NUM_ARMOR_CAV)).append(
					        ", Interpretation's ARMOR CAV: ").append(
					        bestShapeCounts.get(DEC_GFX_NUM_ARMOR_CAV)).append(
					        '\n');
					sb.append("\t\tTarget INFANTRY: ").append(
					        m_targetFields.get(DEC_GFX_NUM_INF)).append(
					        ", Interpretation's ARMOR CAV: ").append(
					        bestShapeCounts.get(DEC_GFX_NUM_INF)).append('\n');
					sb.append("\t\tTarget MECH INFANTRY: ").append(
					        m_targetFields.get(DEC_GFX_NUM_MECH_INF)).append(
					        ", Interpretation's MECH INF: ").append(
					        bestShapeCounts.get(DEC_GFX_NUM_MECH_INF));
				}
				else if (isDecisionGraphic(getBestShape().getCommonName())) {
					sb
					        .append("\n\t\tWe did not expect any decision graphic sub-parts, but the interpretation came back as a decision graphic!");
					sb.append("\n\t\tInterpretation's ARMOR: ").append(
					        bestShapeCounts.get(DEC_GFX_NUM_ARMOR))
					        .append('\n');
					sb.append("\n\t\tInterpretation's ARMOR CAV: ").append(
					        bestShapeCounts.get(DEC_GFX_NUM_ARMOR_CAV)).append(
					        '\n');
					sb.append("\n\t\tInterpretation's ARMOR CAV: ").append(
					        bestShapeCounts.get(DEC_GFX_NUM_INF)).append('\n');
					sb.append("\n\t\tInterpretation's MECH INF: ").append(
					        bestShapeCounts.get(DEC_GFX_NUM_MECH_INF));
				}
				
				String targetText = m_targetFields
				        .get(DeepGreenRecognizer.S_ATTR_LABEL);
				if (targetText != null) {
					sb.append("\n\t\tExpected textual label: ").append(
					        targetText).append(". Interpretation's Text: ")
					        .append(getBestShape().getTextualLabel());
				}
				
				String fieldF = m_targetFields
				        .get(DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F);
				if (fieldF != null) {
					sb.append("\n\t\tExpected modifier F: ").append(fieldF)
					        .append(". Interpretation's modifier F: ").append(
					                getBestShape().getFieldF());
				}
				
				String fieldAA = m_targetFields
				        .get(DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_AA);
				if (fieldAA != null) {
					sb.append("\n\t\tExpected modifier AA: ").append(fieldAA)
					        .append(". Interpretation's modifier AA: ").append(
					                getBestShape().getFieldAA());
				}
			}
			else {
				sb.append("\t\tNull interpretation");
			}
		}
		return sb.toString();
	}
	

	/**
	 * Checks any interpretations exist
	 * 
	 * @return 1 if there exists an interpretation, else 0
	 */
	public boolean hasInterpretation() {
		if (getBestShape() != null) {
			return true;
		}
		return false;
	}
	

	/**
	 * States whether or not the interpretation is correct. This returns false
	 * if there are no interpretations.
	 * 
	 * @return true if the interpretation is correct, else false
	 */
	public boolean isCorrect() {
		if (!hasInterpretation() || getBestLabel() == null) {
			return false;
		}
		if (getBestLabel() == null)
			return false;
		
		return considerCorrect(getInterpretation(0));
	}
	

	/**
	 * Set the file being tested
	 * 
	 * @param m_file
	 *            the file with the shape example in it
	 */
	public void setFile(File m_file) {
		this.m_file = m_file;
	}
	

	/**
	 * sets the output writer
	 * 
	 * @param writer
	 *            where to write the information
	 */
	public void setOutputWriter(FileWriter writer) {
		m_outputWriter = writer;
	}
	

	/**
	 * Sets the recognized list for this file
	 * 
	 * @param list
	 *            the list of recognized interpretations
	 */
	public void setRecognizedList(List<LiteInterpretation> list) {
		m_interpretationList = list;
	}
	

	/**
	 * Write a string to the output file
	 * 
	 * @param s
	 */
	public boolean write(String s) {
		try {
			m_outputWriter.write(s);
			m_outputWriter.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	

	/**
	 * Writes all interpretations to the output writer
	 * 
	 * @return 1 if writing was successful, else 0
	 */
	public boolean writeInterpretationString() {
		return write(getInterpretationsString());
	}
	

	/**
	 * Writes a certain number of interpretations to the output writer
	 * 
	 * @param count
	 *            the number of top interpretations to write
	 * @return 1 if writing was successful, else 0
	 */
	public boolean writeInterpretationString(int count) {
		return write(getInterpretationsString(count));
	}
	

	/**
	 * Writes a certain number of interpretations to the output writer
	 * 
	 * @param count
	 *            the number of top interpretations to write
	 * @return 1 if writing was successful, else 0
	 */
	public boolean writeBetterInterpretationString() {
		return write(getBetterInterpretationsString());
	}
	

	/**
	 * Writes the list of primitives (only their top interpretations) used to
	 * recognize this shape to the output writer
	 * 
	 * @return 1 if the write was successful, else 0
	 */
	public boolean writePrimitiveString() {
		return write(getPrimitiveString());
	}
	

	/**
	 * Writes a string describing the name of the interpretation and what it
	 * should have been
	 * 
	 * @return 1 if the write is successful, else 0
	 */
	public boolean writeResult() {
		return write(getResultString());
	}
	

	/**
	 * Sets the reason for failure
	 * 
	 * @param i
	 */
	public void setReasonForFailure(ReasonForFailure r) {
		m_reasonForFailure = r;
	}
	

	/**
	 * Writes out the reason for failure.
	 * 
	 * @return
	 */
	public boolean writeReasonForFailure() {
		switch (m_reasonForFailure) {
			case LOW_LEVEL:
				return write("\tREASON: Low-Level (PALEO) Failure \n");
			case HIGH_LEVEL:
				return write("\tREASON: High-Level (CALVIN) Failure \n");
			default:
				return false;
		}
	}
	

	/**
	 * A gettter for the reason for failure
	 * 
	 * @return
	 */
	public ReasonForFailure getReasonForFailure() {
		return m_reasonForFailure;
	}
	

	private boolean considerCorrect(LiteInterpretation li) {
		if (m_targetSIDC == null || m_targetFields == null) {
			generateTargetInformation();
		}
		
		boolean correct = false;
		
		log.debug("li interpretation:\t\t" + li.getInterpretation());
		log.debug("PARENT_SIDC:\t\t" + DEC_GFX_PARENT_SIDC);
		if (li.getInterpretation().indexOf(DEC_GFX_PARENT_SIDC) > 0) {
			correct = considerDecisionGraphicsCorrect(li);
		}
		else {
			correct = considerShapeCorrect(li);
		}
		
		return correct;
	}
	

	private boolean considerShapeCorrect(LiteInterpretation shape) {
		
		boolean sidcCorrect = SIDC.sidcsEqual(shape.getInterpretation(),
		        m_targetSIDC);
		
		boolean textCorrect = true;
		String targetText = m_targetFields
		        .get(DeepGreenRecognizer.S_ATTR_LABEL);
		if (targetText != null) {
			textCorrect = targetText.equalsIgnoreCase(shape.getTextualLabel());
		}
		
		boolean fieldFCorrect = true;
		String fieldF = m_targetFields
		        .get(DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F);
		if (fieldF != null) {
			fieldFCorrect = fieldF.equals(shape.getFieldF());
		}
		
		boolean fieldAACorrect = true;
		String fieldAA = m_targetFields
		        .get(DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_AA);
		if (fieldAA != null) {
			fieldAACorrect = fieldAA.equals(shape.getFieldAA());
		}
		
		boolean correct = sidcCorrect && textCorrect && fieldFCorrect
		                  && fieldAACorrect;
		
		log.debug("Shape correct? " + correct);
		
		return correct;
	}
	

	private boolean considerDecisionGraphicsCorrect(LiteInterpretation decGfx) {
		if (decGfx == null) {
			throw new NullPointerException("Lite interpretation cannot be null");
		}
		boolean correct = false;
		
		boolean sidcCorrect = SIDC.sidcsEqual(decGfx.getInterpretation(),
		        m_targetSIDC);
		
		int targetArmor = 0;
		try {
			targetArmor = Integer.parseInt(m_targetFields
			        .get(DEC_GFX_NUM_ARMOR));
		}
		catch (NumberFormatException nfe) {
			// meh
		}
		int targetArmorCav = 0;
		try {
			targetArmorCav = Integer.parseInt(m_targetFields
			        .get(DEC_GFX_NUM_ARMOR_CAV));
		}
		catch (NumberFormatException nfe) {
			// meh
		}
		int targetInf = 0;
		try {
			targetInf = Integer.parseInt(m_targetFields.get(DEC_GFX_NUM_INF));
		}
		catch (NumberFormatException nfe) {
			// meh
		}
		int targetMechInf = 0;
		try {
			targetMechInf = Integer.parseInt(m_targetFields
			        .get(DEC_GFX_NUM_MECH_INF));
		}
		catch (NumberFormatException nfe) {
			// meh
		}
		
		Map<String, Integer> counts = countDecisionGraphicSubInterpretations(decGfx);
		if (counts == null) {
			correct = false;
			log.debug("NO DEC GRAPHICS SUBSHAPES IN INTERPRETATION");
		}
		else {
			correct = sidcCorrect
			          && (counts.get(DEC_GFX_NUM_ARMOR).intValue() == targetArmor)
			          && (counts.get(DEC_GFX_NUM_ARMOR_CAV).intValue() == targetArmorCav)
			          && (counts.get(DEC_GFX_NUM_INF).intValue() == targetInf)
			          && (counts.get(DEC_GFX_NUM_MECH_INF).intValue() == targetMechInf);
			
			if (log.isDebugEnabled()) {
				log.debug("SIDC: target=" + m_targetSIDC + ", interp's SIDC="
				          + decGfx.getInterpretation());
				log.debug("ARMOR: target=" + targetArmor + ", interp's #="
				          + counts.get(DEC_GFX_NUM_ARMOR));
				log
				        .debug("ARMOR_CAV: target=" + targetArmorCav
				               + ", interp's #="
				               + counts.get(DEC_GFX_NUM_ARMOR_CAV));
				log.debug("INF: target=" + targetInf + ", interp's #="
				          + counts.get(DEC_GFX_NUM_INF));
				log.debug("MECH_INF: target=" + targetMechInf + ", interp's #="
				          + counts.get(DEC_GFX_NUM_MECH_INF));
				log.debug("correct? = " + correct);
			}
		}
		
		return correct;
	}
	

	private Map<String, Integer> countDecisionGraphicSubInterpretations(
	        LiteInterpretation decGfx) {
		int numArmor = 0;
		int numArmorCav = 0;
		int numMechInf = 0;
		int numInf = 0;
		
		// must have correct number of sub-interpretations
		if (decGfx.getSubInterpretations() != null) {
			for (LiteInterpretation subGfx : decGfx.getSubInterpretations()) {
				
				// which one is it? increment the correct counter
				if (subGfx.getInterpretation().indexOf(ARMOR_SIDC) > 0) {
					numArmor++;
				}
				else if (subGfx.getInterpretation().indexOf(ARMOR_CAV_SIDC) > 0) {
					numArmorCav++;
				}
				else if (subGfx.getInterpretation().indexOf(MECH_INFANTRY_SIDC) > 0) {
					numMechInf++;
				}
				else if (subGfx.getInterpretation().indexOf(INFANTRY_SIDC) > 0) {
					numInf++;
				}
			}
		}
		
		Map<String, Integer> counts = new HashMap<String, Integer>();
		
		counts.put(DEC_GFX_NUM_ARMOR, numArmor);
		counts.put(DEC_GFX_NUM_ARMOR_CAV, numArmorCav);
		counts.put(DEC_GFX_NUM_INF, numInf);
		counts.put(DEC_GFX_NUM_MECH_INF, numMechInf);
		
		return counts;
	}
	

	private void generateTargetInformation() {
		// key for the SIDC attribute stored in the def
		String sidc_key = DeepGreenRecognizer.S_ATTR_SIDC;
		// the SIDC from the shape def
		if (m_shapeDefinition == null) {
			throw new NullPointerException("Given shape definition is null");
		}
		String targetSIDCString = m_shapeDefinition.getAttribute(sidc_key);
		log.debug("Starting SIDC from shape def ("
		          + m_shapeDefinition.getName() + ") : " + targetSIDCString);
		if (targetSIDCString == null) {
			throw new IllegalArgumentException(
			        "The shape definition for " + m_shapeDefinition.getName()
			                + " has no SIDC attribute under the key "
			                + sidc_key);
		}
		
		// tokens from the directory name that give us helpful information
		if (m_directory == null) {
			throw new NullPointerException("Data directory was given as a null");
		}
		log.debug("Directory name: " + m_directory.getName());
		
		// put into a string buffer so we can manipulate it
		StringBuilder targetSIDC = new StringBuilder(targetSIDCString);
		
		// decision graphics need to be parsed independently
		if (isDecisionGraphic(m_directory.getName())) {
			parseDecisionGraphicInformation(targetSIDC);
		}
		else {
			parseWarfightingSymbolInformation(targetSIDC);
		}
		
		m_targetSIDC = targetSIDC.toString();
	}
	

	/**
	 * Send in the common name / shape def name / test data dir name.
	 * 
	 * @param name
	 *            the name
	 * @return Is it a decision graphic?
	 */
	private boolean isDecisionGraphic(String name) {
		return name.startsWith("9");
	}
	

	private void parseDecisionGraphicInformation(StringBuilder targetSIDC) {
		StringTokenizer st = new StringTokenizer(m_directory.getName(), "_");
		
		String shapeNum = st.nextToken();
		log.debug("Shape number: " + shapeNum);
		
		String friendHostile = st.nextToken();
		log.debug("Friend/hostile: " + friendHostile);
		
		String echelon = st.nextToken();
		log.debug("Echelon: " + echelon);
		
		String status = st.nextToken();
		log.debug("Status : " + status);
		
		String numArmor = st.nextToken();
		log.debug("Num armor: " + numArmor);
		m_targetFields.put(DEC_GFX_NUM_ARMOR, numArmor);
		
		String numArmorCav = st.nextToken();
		log.debug("Num armor cav: " + numArmorCav);
		m_targetFields.put(DEC_GFX_NUM_ARMOR_CAV, numArmorCav);
		
		String numInfantry = st.nextToken();
		log.debug("Num infantry: " + numInfantry);
		m_targetFields.put(DEC_GFX_NUM_INF, numInfantry);
		
		String numMechInfantry = st.nextToken();
		log.debug("Num mech. infantry: " + numMechInfantry);
		m_targetFields.put(DEC_GFX_NUM_MECH_INF, numMechInfantry);
		
		String commonName = st.nextToken();
		log.debug("Common name: " + commonName);
		
		log.debug("More tokens? " + st.hasMoreTokens());
		
		// status for SIDC
		setSIDCEchelon(echelon, targetSIDC);
		
		setSIDCStatus(status, targetSIDC);
		
	}
	

	private void parseWarfightingSymbolInformation(StringBuilder targetSIDC) {
		StringTokenizer st = new StringTokenizer(m_directory.getName(), "_");
		
		String shapeNum = st.nextToken();
		log.debug("Shape number: " + shapeNum);
		
		String friendHostile = st.nextToken();
		log.debug("Friend/hostile: " + friendHostile);
		
		String echelon = st.nextToken();
		log.debug("Echelon : " + echelon);
		
		String status = st.nextToken();
		log.debug("Status (Present/Anticipated) : " + status);
		
		String reduceReinforce = st.nextToken();
		log.debug("Reduced/reinforced : " + reduceReinforce);
		
		String commonName = st.nextToken();
		log.debug("Common name : " + commonName);
		
		log.debug("More tokens? " + st.hasMoreTokens());
		if (st.hasMoreTokens()) {
			// if we are an HQ, we'll have our AA type specified
			String aa = st.nextToken();
			if (aa.equalsIgnoreCase("AA")) {
				String targetHQType = st.nextToken();
				m_targetFields.put(
				        DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_AA,
				        targetHQType);
			}
		}
		
		// put the proper echelon code into the SIDC
		setSIDCEchelon(echelon, targetSIDC);
		
		// put the proper status code into the SIDC
		setSIDCStatus(status, targetSIDC);
		
		// Reduced/reinforced modifier field F
		final HashMap<String, String> RED_REI_CODES = new HashMap<String, String>();
		RED_REI_CODES.put("RED", "D");
		RED_REI_CODES.put("REI", "R");
		String targetRedRei = RED_REI_CODES.get(reduceReinforce);
		if (targetRedRei != null) {
			m_targetFields.put(DeepGreenRecognizer.S_ATTR_SYMBOL_MODIFIER_F,
			        targetRedRei);
			log.debug("Target Red/Rei: " + targetRedRei);
		}
		
		// Textual label taken out of the file name: _Text_Charlie
		String fileName = m_file.getName();
		
		String targetText = null;
		final String textMarker = "Text_";
		int textIndex = fileName.indexOf(textMarker);
		if (textIndex > 0) {
			try {
				// first get everything from Text_Charlie_1-2.xml
				// and then cut out the Text_, so we're left with
				// Charlie_1-2.xml
				targetText = fileName.substring(textIndex).substring(
				        textMarker.length());
				log.debug("Found a Text_ marker:" + targetText);
				// take from the beginning to the first _, so cut out Charlie
				targetText = targetText.substring(0, targetText.indexOf('_'));
				
				m_targetFields
				        .put(DeepGreenRecognizer.S_ATTR_LABEL, targetText);
			}
			catch (IndexOutOfBoundsException ioobe) {
				log
				        .error("Error parsing Text_ component from sketch file label");
				log.error("File name: " + fileName);
			}
		}
	}
	

	private void setSIDCStatus(String status, StringBuilder targetSIDC) {
		String targetStatus = STATUS_CODES.get(status);
		if (targetStatus == null) {
			log.error("Invalid status code in dir name ("
			          + m_directory.getName() + "), " + status
			          + " not recognized");
			m_targetSIDC = "invalid_status_aborted";
			return;
		}
		log.debug("Target status: " + targetStatus);
		targetSIDC.replace(3, 4, targetStatus);
	}
	

	private void setSIDCEchelon(String echelon, StringBuilder targetSIDC) {
		String targetEchelon = ECHELON_CODES.get(echelon);
		if (targetEchelon == null) {
			log.error("Invalid ehelon code in dir name ("
			          + m_directory.getName() + "), " + echelon
			          + " not recognized");
			m_targetSIDC = "invalid_echelon_aborted";
			return;
		}
		log.debug("Target echelon: " + targetEchelon);
		targetSIDC.replace(11, 12, targetEchelon);
	}
}
