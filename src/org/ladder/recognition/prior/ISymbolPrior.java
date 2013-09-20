package org.ladder.recognition.prior;

/**
 * Defines a prior probability for a particular symbol. A symbol with a prior
 * probability of 0.0 will not be returned as a possible interpretation, whereas
 * a prior probability of 1.0 will always return that symbol. 
 * 
 * @author awolin
 */
public interface ISymbolPrior {
	
	/**
	 * Returns the prior probability for this symbol, which is a double value
	 * between 0.0 and 1.0.
	 * 
	 * @return prior probability of the symbol.
	 */
	public double getPrior();
	

	/**
	 * Return the SIDC for the symbol.
	 * 
	 * @return SIDC of the symbol.
	 */
	public String getSIDC();
}
