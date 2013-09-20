/**
 * SIDC.java
 * 
 * Revision History:<br>
 * Oct 1, 2008 joshua - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package edu.tamu.deepGreen.sidc;

/**
 * This class holds a Symbol IDentication Code (SIDC) for MIL STD 2525 B symbols
 * and graphics.
 * <p>
 * This class is fairly "stupid" at the moment and does not enforce SIDC
 * business rules. It's assumed the user knows how to put a string together
 * correctly.
 * 
 * @author joshua
 */
public class SIDC {
	
	/**
	 * The length of a valid SIDC
	 */
	public static final int S_SIDC_LENGTH = 15;
	
	/**
	 * What position in the SIDC does the coding scheme occupy?
	 */
	protected static final int S_SCHEME_POSITION = 0;
	
	protected static final int S_SCHEME_LENGTH = 1;
	
	protected static final int S_AFFILIATION_POSITION = 1;
	
	protected static final int S_AFFILIATION_LENGTH = 1;
	
	protected static final int S_CATEGORY_LENGTH = 1;
	
	protected static final int S_BATTLE_DIMENSION_POSITION = 2;
	
	protected static final int S_BATTLE_DIMENSION_LENGTH = 1;
	
	protected static final int S_STATUS_POSITION = 3;
	
	protected static final int S_STATUS_LENGTH = 1;
	
	protected static final int S_FUNCTION_ID_POSITION = 4;
	
	protected static final int S_FUNCTION_ID_LENGTH = 6;
	
	/**
	 * The 15 character string that we're assembling. We use a StringBuilder
	 * internally since it's easier to change than a String.
	 */
	protected StringBuilder m_sidcString;
	
	
	/**
	 * Default constructor initializes the SIDC String to all spaces.
	 */
	public SIDC() {
		resetSIDCString();
	}
	

	/**
	 * Resets the SIDC string to all spaces.
	 */
	protected void resetSIDCString() {
		// initialize SIDC to all spaces. We do it like this so we're not as
		// fragile as having to hard code a String and count how many times
		// we hit the space key.
		char[] spaces = new char[S_SIDC_LENGTH];
		for (int i = 0; i < spaces.length; i++) {
			spaces[i] = ' ';
		}
		m_sidcString = new StringBuilder();
		m_sidcString.append(spaces);
	}
	

	/**
	 * Get the SIDC String that's been assembled
	 * 
	 * @return The SIDC String
	 */
	public String getSIDCString() {
		return m_sidcString.toString();
	}
	

	/**
	 * Set the SIDC Coding Scheme. This call erases ALL PRIOR information stored
	 * in the SIDC, since the valid structure of the SIDC is dependent on the
	 * coding scheme. Thus, the coding scheme should be the first information
	 * set in the SIDC.
	 * 
	 * @param scheme
	 *            The coding scheme
	 */
	public void setCodingScheme(CodingScheme scheme) {
		resetSIDCString();
		replaceSIDCRegion(0, 1, scheme.toString());
	}
	

	public String getCodingScheme() {
		return m_sidcString.substring(S_SCHEME_POSITION, S_SCHEME_POSITION
		                                                 + S_SCHEME_LENGTH);
	}
	

	public boolean isWarfightingScheme() {
		return CodingScheme.Warfighting.toString().equals(getCodingScheme());
	}
	

	public boolean isTacticalGraphicsScheme() {
		return CodingScheme.TacticalGraphics.toString().equals(
		        getCodingScheme());
	}
	

	public boolean isMETOCScheme() {
		return CodingScheme.METOC.toString().equals(getCodingScheme());
	}
	

	public boolean isIntelligenceScheme() {
		return CodingScheme.Intelligence.toString().equals(getCodingScheme());
	}
	

	public boolean isMOOTWScheme() {
		return CodingScheme.MOOTW.toString().equals(getCodingScheme());
	}
	

	/**
	 * Set the affiliation in the SIDC. This call is not valid for Coding
	 * Schemes of type {@link CodingScheme#METOC}, and will be ignored if used
	 * when {@link #isMETOCScheme()} returns true
	 * 
	 * @param affiliation
	 *            The affiliation
	 */
	public void setAffiliation(Affiliation affiliation) {
		if (!isMETOCScheme()) {
			replaceSIDCRegion(S_AFFILIATION_POSITION, S_AFFILIATION_POSITION
			                                          + S_AFFILIATION_LENGTH,
			        affiliation.toString());
		}
	}
	

	public void setCategory(Category category) {
		// category is valid only for TacticalGraphics, METOC, and MOOTW
		if (isTacticalGraphicsScheme() || isMETOCScheme() || isMOOTWScheme()) {
			// for TacticalGraphics and MOOTW
			int categoryPosition = 2;
			if (isMETOCScheme()) {
				categoryPosition = 1;
			}
			
			replaceSIDCRegion(categoryPosition, S_CATEGORY_LENGTH, category
			        .toString());
		}
	}
	

	public void setBattleDimension(BattleDimension batDim) {
		// only valid for Warfighting and Intelligence
		if (isWarfightingScheme() || isIntelligenceScheme()) {
			replaceSIDCRegion(S_BATTLE_DIMENSION_POSITION,
			        S_BATTLE_DIMENSION_POSITION + S_BATTLE_DIMENSION_LENGTH,
			        batDim.toString());
		}
	}
	

	public void setStatus(Status status) {
		// not valid for METOC
		if (!isMETOCScheme()) {
			replaceSIDCRegion(S_STATUS_POSITION, S_STATUS_POSITION
			                                     + S_STATUS_LENGTH, status
			        .toString());
		}
	}
	

	public void setFunctionID(String functionID) {
		// all schemes use a function id
		replaceSIDCRegion(S_FUNCTION_ID_POSITION, S_FUNCTION_ID_POSITION
		                                          + S_FUNCTION_ID_LENGTH,
		        functionID);
	}
	
	
	public void setSymbolModifier(SymbolModifiers symMod) {
		// used on Warfighting, Tactical Graphics, and MOOTW
		if (isWarfightingScheme() || isTacticalGraphicsScheme() || isMOOTWScheme()) {
			// TODO
		}
	}
	

	/**
	 * Replace the portion of the SIDC string, starting at beg (inclusive) and
	 * going through end (exclusive, so end-1), and replacing it with the given
	 * replacement string. If the replacement is not the right size, this
	 * function call has no effect.
	 * <p>
	 * If beg < 0, end > length, end <= beg, or (end-beg) !=
	 * replacement.length() this call has no effect
	 * 
	 * @param beg
	 *            The beginning index (inclusive)
	 * @param end
	 *            The end index (exclusive, end-1)
	 * @param replacement
	 *            Replace the portion of the SIDC string between beg and end-1
	 *            with the replacement string, which is just the right size.
	 */
	protected void replaceSIDCRegion(int beg, int end, String replacement) {
		if (beg < 0 || end >= beg || end > S_SIDC_LENGTH || replacement == null
		    || (end - beg) != replacement.length()) {
			return;
		}
		
		m_sidcString.replace(beg, end, replacement);
	}
}
