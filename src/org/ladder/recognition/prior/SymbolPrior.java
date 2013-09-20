package org.ladder.recognition.prior;

import java.util.regex.PatternSyntaxException;

import edu.tamu.deepGreen.recognition.SIDC;

/**
 * Implements the ISymbolPrior interface with standard functionality. 
 * 
 * @see ISymbolPrior
 * @author awolin
 */
public class SymbolPrior implements ISymbolPrior {
	
	/**
	 * Default symbol prior probability of 0.5.
	 */
	public static final double S_DEFAULT_PRIOR = 0.5;
	
	/**
	 * Prior probability of the symbol, between 0.0 and 1.0.
	 */
	private double m_prior;
	
	/**
	 * Name of the symbol in SIDC format.
	 */
	private String m_sidc;
	
	
	/**
	 * Constructs an ISymbolPrior for a particular symbol with the default prior
	 * probability.
	 * 
	 * @see #S_DEFAULT_PRIOR
	 * 
	 * @param sidc
	 *            SIDC for the symbol. The SIDC can contain asterisks (*) for
	 *            parts of the code that are deemed wild.
	 * 
	 * @throws PatternSyntaxException
	 *             if the sidc argument is in an improper format.
	 * @throws NullPointerException
	 *             if the passed sidc argument is null.
	 */
	public SymbolPrior(String sidc) throws PatternSyntaxException,
	        NullPointerException {
		
		setSIDC(sidc);
		setPrior(S_DEFAULT_PRIOR);
	}
	

	/**
	 * Constructs a SymbolPrior with a given symbol name and prior probability.
	 * The symbols set are defined by a regular expression applied to the symbol
	 * interpretation&#39;s SIDC.
	 * <p>
	 * If the prior probabilities are not between 0.0 and 1.0, then an
	 * IllegalArgumentException is thrown. If the SIDC format is improper, a
	 * PatternSyntaxException is thrown.
	 * 
	 * @param sidc
	 *            SIDC for the symbol. The SIDC can contain asterisks (*) for
	 *            parts of the code that are deemed wild.
	 * @param prior
	 *            symbol prior probability.
	 * 
	 * @throws IllegalArgumentException
	 *             if the prior is not between 0.0 and 1.0, inclusive.
	 * @throws PatternSyntaxException
	 *             if the sidc argument is in an improper format.
	 * @throws NullPointerException
	 *             if the passed sidc argument is null.
	 */
	public SymbolPrior(String sidc, double prior)
	        throws IllegalArgumentException, PatternSyntaxException,
	        NullPointerException {
		
		setSIDC(sidc);
		setPrior(prior);
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.prior.ISymbolPrior#getPrior()
	 */
	@Override
	public double getPrior() {
		return m_prior;
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.prior.ISymbolPrior#getSymbol()
	 */
	@Override
	public String getSIDC() {
		return m_sidc;
	}
	

	/**
	 * Sets the prior probability for this symbol.
	 * <p>
	 * If the prior probabilities are not between 0.0 and 1.0, then an
	 * IllegalArgumentException is thrown.
	 * 
	 * @param prior
	 *            symbol prior probability.
	 * 
	 * @throws IllegalArgumentException
	 *             if the prior is not between 0.0 and 1.0, inclusive.
	 */
	private void setPrior(double prior) throws IllegalArgumentException {
		
		if (prior < 0.0 || prior > 1.0) {
			throw new IllegalArgumentException(
			        "Prior probability not between 0.0 and 1.0.");
		}
		else {
			m_prior = prior;
		}
	}
	

	/**
	 * Sets the name of the symbol for this SymbolPrior. The symbol names are
	 * defined by a regular expression applied to a symbol interpretation&#39;s
	 * SIDC.
	 * <p>
	 * If the name&#39;s SIDC is improper, a PatternSyntaxException is thrown.
	 * 
	 * @param sidc
	 *            symbol SIDC.
	 * 
	 * @throws PatternSyntaxException
	 *             if the passed sidc argument is in an improper format.
	 * @throws NullPointerException
	 *             if the passed sidc argument is null.
	 */
	private void setSIDC(String sidc) throws PatternSyntaxException,
	        NullPointerException {
		
		if (sidc == null) {
			throw new NullPointerException(
			        "SIDC to set in the symbol prior is null.");
		}
		
		if (SIDC.properSIDC(sidc)) {
			m_sidc = sidc;
		}
	}
}
