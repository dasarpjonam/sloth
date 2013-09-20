package test.functional.ladder.recognition.shapes;

import java.util.ArrayList;
import java.util.List;

/**
 * This class was created because if I hold all of the recognition results in
 * memory, I run out of heap space. That seems not so good.
 * 
 * @author tracy
 * 
 */
public class LiteInterpretation {
	
	// the interpretation
	private String m_interpretation = "";
	
	private String m_commonName = "-null-";
	
	// the confidence of that interpretation
	private double m_confidence = 0;
	
	private String m_textualLabel = null;
	
	private String m_fieldF = null;
	
	private String m_fieldAA = null;
	
	private List<LiteInterpretation> m_subInterpretations = new ArrayList<LiteInterpretation>();
	
	
	/**
	 * 
	 * @param interpretation
	 * @param confidence
	 */
	public LiteInterpretation(String interpretation, double confidence) {
		m_interpretation = interpretation;
		m_confidence = confidence;
	}
	

	/**
	 * Gets the interpretation (the name label of the shape)
	 * 
	 * @return the interpretation
	 */
	public String getInterpretation() {
		return m_interpretation;
	}
	

	/**
	 * Sets the intrepretation (the name label of the shape)
	 * 
	 * @param m_interpretation
	 *            the interpretation
	 */
	public void setInterpretation(String m_interpretation) {
		this.m_interpretation = m_interpretation;
	}
	

	/**
	 * Gets the confidence of the interpretation
	 * 
	 * @return the confidences
	 */
	public double getConfidence() {
		return m_confidence;
	}
	

	/**
	 * Sets the confidence of the interpretation
	 * 
	 * @param m_confidence
	 *            the confidence
	 */
	public void setConfidence(double m_confidence) {
		this.m_confidence = m_confidence;
	}
	

	/**
	 * @return the textualLabel
	 */
	public String getTextualLabel() {
		return m_textualLabel;
	}
	

	/**
	 * @param textualLabel
	 *            the textualLabel to set
	 */
	public void setTextualLabel(String textualLabel) {
		m_textualLabel = textualLabel;
	}
	

	/**
	 * @return the fieldF
	 */
	public String getFieldF() {
		return m_fieldF;
	}
	

	/**
	 * @return the fieldAA
	 */
	public String getFieldAA() {
		return m_fieldAA;
	}
	

	/**
	 * @param fieldF
	 *            the fieldF to set
	 */
	public void setFieldF(String fieldF) {
		m_fieldF = fieldF;
	}
	

	/**
	 * 
	 * @param fieldAA
	 *            the fieldAA to set
	 */
	public void setFieldAA(String fieldAA) {
		m_fieldAA = fieldAA;
	}
	

	/**
	 * @return the commonName
	 */
	public String getCommonName() {
		return this.m_commonName;
	}
	

	/**
	 * @param commonName
	 *            the commonName to set
	 */
	public void setCommonName(String commonName) {
		this.m_commonName = commonName;
	}
	

	/**
	 * @return the subInterpretations
	 */
	public List<LiteInterpretation> getSubInterpretations() {
		return this.m_subInterpretations;
	}
	

	/**
	 * @param subInterpretations
	 *            the subInterpretations to set
	 */
	public void setSubInterpretations(
	        List<LiteInterpretation> subInterpretations) {
		this.m_subInterpretations = subInterpretations;
	}
	

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getInterpretation()).append(" (").append(
		        this.getCommonName()).append(")");
		if (this.getSubInterpretations() != null) {
			for (LiteInterpretation subInterp : this.getSubInterpretations()) {
				sb.append('\n').append("\t- ").append(subInterp.toString());
			}
		}
		
		return sb.toString();
	}
}
