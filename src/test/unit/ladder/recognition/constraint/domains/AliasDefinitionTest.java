package test.unit.ladder.recognition.constraint.domains;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ladder.recognition.constraint.domains.AliasDefinition;
import org.ladder.recognition.constraint.domains.ComponentSubPart;
import org.ladder.recognition.constraint.domains.ConstraintParameter;

public class AliasDefinitionTest {
	
	private static final String S_ALIAS_NAME = "Name";
	
	private static final String S_ALIAS_COMPONENT = "Component";
	
	private AliasDefinition ad;
	
	
	@Before
	public void setUp() throws Exception {
		ad = null;
	}
	

	@After
	public void tearDown() throws Exception {
	}
	

	@Test
	public void testToString() {
		ad = new AliasDefinition(S_ALIAS_NAME, S_ALIAS_COMPONENT);
		
		assertNotNull(ad);
		assertEquals(ad.toString(), ad.getAliasName()
		                            + ConstraintParameter.SEPARATOR
		                            + ad.getComponent()
		                            + ConstraintParameter.SEPARATOR
		                            + ad.getComponentSubPart());
	}
	

	@Test
	public void testAliasDefinitionStringString() {
		ad = new AliasDefinition(S_ALIAS_NAME, S_ALIAS_COMPONENT);
		
		assertNotNull(ad);
		assertEquals(ad.getAliasName(), S_ALIAS_NAME);
		assertEquals(ad.getComponent(), S_ALIAS_COMPONENT);
	}
	

	@Test
	public void testAliasDefinitionStringStringComponentSubPart() {
		ComponentSubPart csp = ComponentSubPart.None;
		ad = new AliasDefinition(S_ALIAS_NAME, S_ALIAS_COMPONENT, csp);
		
		assertNotNull(ad);
		assertEquals(ad.getAliasName(), S_ALIAS_NAME);
		assertEquals(ad.getComponent(), S_ALIAS_COMPONENT);
		assertEquals(ad.getComponentSubPart(), csp);
	}
	

	@Test
	public void testGetAliasName() {
		ad = new AliasDefinition(S_ALIAS_NAME, S_ALIAS_COMPONENT);
		
		assertNotNull(ad);
		assertEquals(ad.getAliasName(), S_ALIAS_NAME);
	}
	

	@Test
	public void testSetAliasName() {
		ad = new AliasDefinition("", S_ALIAS_COMPONENT);
		ad.setAliasName(S_ALIAS_NAME);
		
		assertNotNull(ad);
		assertEquals(ad.getAliasName(), S_ALIAS_NAME);
	}
	

	@Test
	public void testCompareTo() {
		
		AliasDefinition ad1 = new AliasDefinition("AD1", "COMP1");
		AliasDefinition ad2 = new AliasDefinition("AD2", "COMP2");
		
		assertNotNull(ad1);
		assertNotNull(ad2);
		
		assertEquals(ad1.compareTo(ad1), 0);
		assertEquals(ad1.compareTo(ad2), ad1.toString().compareTo(
		        ad2.toString()));
	}
	
}
