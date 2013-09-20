/**
 * SIDC.java
 * 
 * Revision History:<br>
 * Mar 25, 2009 awolin - File created
 * Code reviewed
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
package edu.tamu.deepGreen.recognition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * SIDC class that contains static methods to check SIDCs against the 2525B
 * encoding scheme.
 * 
 * @author awolin
 */
public final class SIDC {
	
	/**
	 * SIDC regular expression checker for Phase 1.<br>
	 * <UL>
	 * <LI>'n' = '-' = {@code null}
	 * <LI>'w' = '*' = {@code wild}
	 * </UL>
	 */
	private static final Pattern S_SIDC_PATTERN_YEAR_1 = Pattern
	        .compile("[SG]" // (1) Coding scheme (ONLY YEAR 1's)
	                 + "[wFH]" // (2) Affiliation (Friendly, Hostile)
	                 + "[TGMFS]" // (3) Category (Graphics)
	                 + "[wPA]" // (4) Status (Present, Anticipated)
	                 + "[n[A-Z]]{6}" // (5-10) Function ID
	                 + "[nw[A-G]]" // (11) Symbol modifier
	                 + "[nw[A-K]]" // (12) Symbol modifier
	                 + "[nw[A-Z]]{2}" // (13-14) Country code (unsupported)
	                 + "[nwAECGNSX]"); // (15) Order of battle
	
	/**
	 * Regex pattern that we use to check our made-up decision graphics SIDCs
	 * 
	 * private static final Pattern S_SIDC_PATTERN_DECISION_GRAPHICS = Pattern
	 * .compile("[D]" // (1) Coding scheme (ONLY YEAR 1's) + "[wFH]" // (2)
	 * Affiliation (Friendly, Hostile) + "[G]" // (3) Category (Graphics) +
	 * "[wPA]" // (4) Status (Present, Anticipated) + "[n[A-Z0-9]]{6}" // (5-10)
	 * Function ID + "[nw[A-G]]" // (11) Symbol modifier + "[nw[A-K]]" // (12)
	 * Symbol modifier + "[nw[A-Z]]{2}" // (13-14) Country code (unsupported) +
	 * "[nwAECGNSX]"); // (15) Order of battle
	 */
	
	private static final Pattern S_SIDC_PATTERN_DECISION_GRAPHICS = Pattern
	        .compile("[S]" // (1) Coding scheme
	                 + "[F]" // (2) Affiliation (Friendly only)
	                 + "[n]" // (3) Category (nothing)
	                 + "[wPA]" // (4) Status (Present, Anticipated)
	                 + "[U]" // (5) Mandatory function ID 1
	                 + "[C]" // (6) Mandatory function ID 2
	                 + "(RVAn|" // Armored Cavalry
	                 + "Annn|" // Armor
	                 + "Innn|" // Infantry
	                 + "IZnn)" // Mechanized Infantry
	                 + "[n]" // (11) Symbol modifier
	                 + "[nw[A-K]]" // (12) Symbol modifier
	                 + "[n]{2}" // (13-14) Country code (unsupported)
	                 + "[n]"); // (15) Order of battle
	
	
	/**
	 * Converts the {@code text} argument in into a regular expression format we
	 * can match. Capitalizes all input letters, and then replaces all '-' with
	 * 'n' and all '*' with 'w'.
	 * 
	 * @param text
	 *            text to format.
	 * @return the regular expression-readable text.
	 */
	private static String convertToRegex(String text) {
		
		// Convert all of the characters to upper-case
		String upperSIDC = text.toUpperCase();
		
		// Replace all the '-'s with 'n's (nulls)
		String replacedDashesSIDC = "";
		
		for (int i = 0; i < upperSIDC.length(); i++) {
			if (Character.getType(upperSIDC.charAt(i)) == Character.DASH_PUNCTUATION) {
				replacedDashesSIDC += 'n';
			}
			else {
				replacedDashesSIDC += upperSIDC.charAt(i);
			}
		}
		
		// Replace all the '*'s with 'w's (wilds)
		String regexedSIDC = replacedDashesSIDC.replace('*', 'w');
		
		return regexedSIDC;
	}
	

	/**
	 * Get the regular expression pattern used for checking SIDCs.
	 * 
	 * @return the regular expression pattern to match against SIDCs.
	 */
	public static Pattern getPattern() {
		return S_SIDC_PATTERN_YEAR_1;
	}
	

	/**
	 * Get the regular expression patter used for checking decision graphics
	 * SIDCs.
	 * 
	 * @return the regular expression pattern to match against Decision Graphics
	 *         SIDCs
	 */
	public static Pattern getDecisionGraphicsPattern() {
		return S_SIDC_PATTERN_DECISION_GRAPHICS;
	}
	

	/**
	 * Returns whether a given SIDC is anticipated.
	 * 
	 * @param sidc
	 *            SIDC to check.
	 * @return {@code true} if the given {@code sidc} is anticipated.
	 * 
	 * @throws NullPointerException
	 *             if the passed {@code sidc} argument is {@code null}.
	 * @throws PatternSyntaxException
	 *             if the {@code sidc} argument does not match our SIDC regular
	 *             expression pattern.
	 */
	public static boolean isAnticipated(String sidc)
	        throws PatternSyntaxException, NullPointerException {
		
		if (sidc == null) {
			throw new NullPointerException("SIDC input null.");
		}
		
		// Check that the SIDC is in a proper 2525B format. If not, this should
		// pass along the exception.
		if (properSIDC(sidc)) {
			
			char[] sidcChars = sidc.toCharArray();
			if (sidcChars[3] == 'A') {
				return true;
			}
			else {
				return false;
			}
		}
		
		// We should never reach here
		return false;
	}
	

	/**
	 * Checks whether the input {@code sidc} argument is an SIDC in the proper
	 * format in the 2525B encoding scheme, or whether the SIDC is a valid
	 * decision graphics SIDC.
	 * <p>
	 * Throws a PatternSyntaxException if the input {@code sidc} argument is
	 * invalid.
	 * 
	 * @param sidc
	 *            SIDC to check.
	 * @return {@code true} if the {@code sidc} conforms to the 2525B standards;
	 *         otherwise, the method throws an exception.
	 * 
	 * @throws NullPointerException
	 *             if the passed {@code sidc} argument is {@code null}.
	 * @throws PatternSyntaxException
	 *             if the {@code sidc} argument does not match our SIDC regular
	 *             expression pattern.
	 */
	public static boolean properSIDC(String sidc)
	        throws PatternSyntaxException, NullPointerException {
		
		if (sidc == null) {
			throw new NullPointerException("SIDC to validate is null.");
		}
		
		// Convert the conventional SIDC used by DARPA into a readable regular
		// expression format
		String regexedSIDC = convertToRegex(sidc);
		
		// Check the SIDC against the proper pattern
		Matcher sidcChecker = SIDC.getPattern().matcher(regexedSIDC);
		
		if (sidcChecker.matches()) {
			return true;
		}
		
		// else check decision graphics
		Matcher decGfxChecker = SIDC.getDecisionGraphicsPattern().matcher(
		        regexedSIDC);
		
		if (decGfxChecker.matches()) {
			return true;
		}
		
		throw new PatternSyntaxException("SIDC " + sidc
		                                 + " is in an improper format.",
		        SIDC.getPattern().toString() + "\n -- or -- \n"
		                + SIDC.getDecisionGraphicsPattern(), -1);
	}
	

	/**
	 * Checks whether the input {@code sidc} argument is an SIDC in the proper
	 * decision graphic encoding scheme.
	 * <p>
	 * Throws a PatternSyntaxException if the input {@code sidc} argument is
	 * invalid.
	 * 
	 * @param sidc
	 *            SIDC to check.
	 * @return {@code true} if the {@code sidc} conforms to the decision graphic
	 *         standards; otherwise, the method throws an exception.
	 * 
	 * @throws NullPointerException
	 *             if the passed {@code sidc} argument is {@code null}.
	 * @throws PatternSyntaxException
	 *             if the {@code sidc} argument does not match our SIDC regular
	 *             expression pattern.
	 */
	public static boolean properDecisionGraphicSIDC(String sidc)
	        throws PatternSyntaxException, NullPointerException {
		
		if (sidc == null) {
			throw new NullPointerException("SIDC to validate is null.");
		}
		
		// Convert the conventional SIDC used by DARPA into a readable regular
		// expression format
		String regexedSIDC = convertToRegex(sidc);
		
		// else check decision graphics
		Matcher decGfxChecker = SIDC.getDecisionGraphicsPattern().matcher(
		        regexedSIDC);
		
		if (decGfxChecker.matches()) {
			return true;
		}
		
		throw new PatternSyntaxException("SIDC " + sidc
		                                 + " is in an improper format.", SIDC
		        .getDecisionGraphicsPattern().toString(), -1);
	}
	

	/**
	 * Returns an SIDC where the status flag is set to anticipated, based on the
	 * given boolean, {@code anticipated}. If {@code anticipated} equals {@code
	 * false}, then the SIDC status is set to Present.
	 * 
	 * @param sidc
	 *            SIDC to set the status to anticipated.
	 * @param anticipated
	 * @return the new SIDC with the correct status.
	 * 
	 * @throws NullPointerException
	 *             if the passed {@code sidc} argument is {@code null}.
	 * @throws PatternSyntaxException
	 *             if the {@code sidc} argument does not match our SIDC regular
	 *             expression pattern.
	 */
	public static String setAnticipated(String sidc, boolean anticipated)
	        throws PatternSyntaxException, NullPointerException {
		
		if (sidc == null) {
			throw new NullPointerException("SIDC input null.");
		}
		
		// Check that the SIDC is in a proper 2525B format. If not, this should
		// pass along the exception.
		if (properSIDC(sidc)) {
			char[] sidcChars = sidc.toCharArray();
			
			if (anticipated) {
				sidcChars[3] = 'A';
			}
			else {
				sidcChars[3] = 'P';
			}
			
			return new String(sidcChars);
		}
		
		return null;
	}
	

	/**
	 * Returns an SIDC where the echelon is set to the given {@code modifier}.
	 * The modifier must be only 1 character matching the pattern [A-K],
	 * with null (-) and wild (*) allowed, otherwise a
	 * PatternSyntaxException is thrown.
	 * 
	 * @param sidc
	 *            SIDC to set the echelon for.
	 * @param modifier
	 *            the new SIDC with the given {@code modifier}.
	 * @return The new SIDC
	 * 
	 * @throws NullPointerException
	 *             if the passed {@code sidc} argument is {@code null}.
	 * @throws PatternSyntaxException
	 *             if the {@code sidc} argument does not match our SIDC regular
	 *             expression pattern, or if the modifier is not a 2-character
	 *             string matching the pattern discussed above.
	 */
	public static String setEchelonModifier(String sidc, String modifier)
	        throws PatternSyntaxException, NullPointerException {
		
		if (sidc == null) {
			throw new NullPointerException("SIDC input null.");
		}
		
		// Check that the SIDC is in a proper 2525B format. If not, this should
		// pass along the exception.
		if (properSIDC(sidc)) {
			char[] sidcChars = sidc.toCharArray();
			
			// Convert the modifier to a regex format.
			String formattedModifier = convertToRegex(modifier);
			Pattern modifierRegEx = Pattern.compile("[nwA-K]");
			Matcher modifierMatcher = modifierRegEx.matcher(formattedModifier);
			
			if (modifierMatcher.matches()) {
				sidcChars[11] = modifier.charAt(0);
			}
			else {
				throw new PatternSyntaxException(
				        "Modifier " + modifier + " is in an improper format.",
				        modifierRegEx.toString(), -1);
			}
			
			return new String(sidcChars);
		}
		
		return null;
	}
	

	/**
	 * Checks whether two given SIDCs are equal.
	 * 
	 * @param sidc1
	 *            first SIDC.
	 * @param sidc2
	 *            second SIDC.
	 * @return {@code true} if {@code sidc1} is equivalent to {@code sidc2},
	 *         {@code false} otherwise.
	 * 
	 * @throws NullPointerException
	 *             if either of the passed {@code sidc1} or {@code sidc2}
	 *             arguments are {@code null}.
	 * @throws PatternSyntaxException
	 *             if the given SIDC arguments do not match our SIDC regular
	 *             expression pattern
	 */
	public static boolean sidcsEqual(String sidc1, String sidc2)
	        throws PatternSyntaxException, NullPointerException {
		
		if (properSIDC(sidc1) && properSIDC(sidc2)) {
			
			for (int i = 0; i < sidc1.length(); i++) {
				if (sidc1.charAt(i) == sidc2.charAt(i)
				    || sidc1.charAt(i) == '*' || sidc2.charAt(i) == '*') {
					continue;
				}
				else {
					return false;
				}
			}
			
			return true;
		}
		else {
			return false;
		}
	}
}
