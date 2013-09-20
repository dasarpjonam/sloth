package test.unit.deepgreen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Random;
import java.util.regex.PatternSyntaxException;

import org.junit.Test;
import org.ladder.recognition.prior.ISymbolPrior;
import org.ladder.recognition.prior.SymbolPrior;

import edu.tamu.deepGreen.recognition.SIDC;

/**
 * JUnit tests for our SymbolPrior class.
 * 
 * @author awolin
 */
@SuppressWarnings("unused")
public class SymbolPriorTest {
	
	/**
	 * Delta value used when comparing floating point numbers.
	 */
	private static final double S_DELTA = Math.pow(10, -10);
	
	
	/**
	 * Test the basic SymbolPrior constructor that takes in a SIDC and sets a
	 * default prior.
	 */
	@Test
	public void testSymbolPriorString() {
		
		// Check for a NullPointerException
		try {
			ISymbolPrior sp = new SymbolPrior(null);
			fail("Null SIDC code not caught");
		}
		catch (NullPointerException npe) {
			// Exception properly caught, continue...
		}
		
		// Check 1000 random, bad SIDCs
		for (int i = 0; i < 1000; i++) {
			String badSIDC = generateBadSIDC();
			
			try {
				ISymbolPrior sp = new SymbolPrior(badSIDC);
				fail("PatternSynatxException code not caught for SIDC: "
				     + badSIDC);
			}
			catch (PatternSyntaxException pse) {
				// Exception properly caught, continue...
			}
		}
		
		// Check 1000 random, good SIDCs
		for (int i = 0; i < 1000; i++) {
			String goodSIDC = generateGoodSIDC();
			
			try {
				ISymbolPrior sp = new SymbolPrior(goodSIDC);
				assertEquals(goodSIDC, sp.getSIDC());
				assertEquals(SymbolPrior.S_DEFAULT_PRIOR, sp.getPrior(),
				        S_DELTA);
			}
			catch (PatternSyntaxException pse) {
				fail("PatternSynatxException code caught for good SIDC: "
				     + goodSIDC);
			}
		}
	}
	

	/**
	 * Test the SymbolPrior constructor that takes in a SIDC and prior value.
	 */
	@Test
	public void testSymbolPriorStringDouble() {
		
		// Check for a NullPointerException
		try {
			ISymbolPrior sp = new SymbolPrior(null, Math.random());
			fail("Null SIDC code not caught");
		}
		catch (NullPointerException npe) {
			// Exception properly caught, continue...
		}
		
		// Check for IllegalArgumentExceptions
		try {
			double negPrior = Math.random() - 1.0;
			ISymbolPrior sp = new SymbolPrior(generateGoodSIDC(), negPrior);
			
			fail("Negative prior not caught: " + negPrior);
		}
		catch (IllegalArgumentException iae) {
			// Exception properly caught, continue...
		}
		
		try {
			double largePrior = Math.random() + 1.1;
			ISymbolPrior sp = new SymbolPrior(generateGoodSIDC(), largePrior);
			
			fail("Greater than 1.0 prior not caught: " + largePrior);
		}
		catch (IllegalArgumentException iae) {
			// Exception properly caught, continue...
		}
		
		// Check 1000 random, bad SIDCs and 1000 random, good priors
		for (int i = 0; i < 1000; i++) {
			String badSIDC = generateBadSIDC();
			double goodPrior = Math.random();
			
			try {
				ISymbolPrior sp = new SymbolPrior(badSIDC, goodPrior);
				fail("PatternSynatxException code not caught for SIDC: "
				     + badSIDC);
			}
			catch (PatternSyntaxException pse) {
				// Exception properly caught, continue...
			}
		}
		
		// Check 1000 random, good SIDCs and 1000 random, good priors
		for (int i = 0; i < 1000; i++) {
			String goodSIDC = generateGoodSIDC();
			double goodPrior = Math.random();
			
			try {
				ISymbolPrior sp = new SymbolPrior(goodSIDC, goodPrior);
				assertEquals(goodSIDC, sp.getSIDC());
				assertEquals(goodPrior, sp.getPrior(), S_DELTA);
			}
			catch (PatternSyntaxException pse) {
				fail("PatternSynatxException code caught for good SIDC: "
				     + goodSIDC);
			}
		}
	}
	

	@Test
	public void testGetPrior() {
		String goodSIDC = generateGoodSIDC();
		double goodPrior = Math.random();
		
		ISymbolPrior sp = new SymbolPrior(goodSIDC, goodPrior);
		assertEquals(goodPrior, sp.getPrior(), S_DELTA);
	}
	

	@Test
	public void testGetSIDC() {
		String goodSIDC = generateGoodSIDC();
		double goodPrior = Math.random();
		
		ISymbolPrior sp = new SymbolPrior(goodSIDC, goodPrior);
		assertEquals(goodSIDC, sp.getSIDC());
	}
	

	/**
	 * Generates an random, good SIDC that should be valid for the Phase 1
	 * testing.
	 * 
	 * @return a random, valid SIDC.
	 */
	public static String generateGoodSIDC() {
		
		String[] sidcBuckets = new String[15];
		
		// (1) Coding scheme
		sidcBuckets[0] = "*SG";
		
		// (2) Affiliation
		sidcBuckets[1] = "*FH";
		
		// (3) Category
		sidcBuckets[2] = "*TGMFS";
		
		// (4) Status
		sidcBuckets[3] = "*PA";
		
		// (5-10) Function ID
		sidcBuckets[4] = "-*ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		sidcBuckets[5] = "-*ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		sidcBuckets[6] = "-*ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		sidcBuckets[7] = "-*ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		sidcBuckets[8] = "-*ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		sidcBuckets[9] = "-*ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		
		// (11) Symbol modifier
		sidcBuckets[10] = "-*ABCDEFG";
		
		// (12) Symbol modifier
		sidcBuckets[11] = "-*ABCDEFGHIJK";
		
		// (13-15) not currently supported
		sidcBuckets[12] = "-";
		sidcBuckets[13] = "-";
		sidcBuckets[14] = "-";
		
		// Generate the SIDC
		String generatedSIDC = "";
		Random rand = new Random();
		
		for (int i = 0; i < sidcBuckets.length; i++) {
			int randIndex = rand.nextInt(sidcBuckets[i].length());
			generatedSIDC += sidcBuckets[i].charAt(randIndex);
		}
		
		return generatedSIDC;
	}
	

	/**
	 * Generates an random, bad SIDC that should be invalid for the Phase 1
	 * testing.
	 * 
	 * @return a random, invalid SIDC.
	 */
	public static String generateBadSIDC() {
		
		String sidcBuckets = "-*ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String generatedSIDC = "";
		
		try {
			
			// Generate the SIDC
			Random rand = new Random();
			
			for (int i = 0; i < 15; i++) {
				int randIndex = rand.nextInt(sidcBuckets.length());
				generatedSIDC += sidcBuckets.charAt(randIndex);
			}
			
			if (SIDC.properSIDC(generatedSIDC)) {
				return generateBadSIDC();
			}
		}
		catch (PatternSyntaxException pse) {
			return generatedSIDC;
		}
		
		// Should never get to here
		return generatedSIDC;
	}
}
