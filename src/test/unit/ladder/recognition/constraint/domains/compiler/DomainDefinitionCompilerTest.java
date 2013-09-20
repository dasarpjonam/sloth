package test.unit.ladder.recognition.constraint.domains.compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ladder.recognition.constraint.domains.AliasDefinition;
import org.ladder.recognition.constraint.domains.ComponentDefinition;
import org.ladder.recognition.constraint.domains.ComponentSubPart;
import org.ladder.recognition.constraint.domains.ConstraintDefinition;
import org.ladder.recognition.constraint.domains.ConstraintParameter;
import org.ladder.recognition.constraint.domains.DomainDefinition;
import org.ladder.recognition.constraint.domains.ShapeDefinition;
import org.ladder.recognition.constraint.domains.compiler.DomainDefinitionCompiler;

public class DomainDefinitionCompilerTest {
	
	DomainDefinition m_domainDef = new DomainDefinition();
	
	DomainDefinition m_compiledDef;
	
	DomainDefinitionCompiler m_compiler;
	
	
	@Before
	public void setUp() throws Exception {
		m_domainDef.setDescription("Simple COA");
		m_domainDef.setName("COATest");
		ShapeDefinition rectangle = new ShapeDefinition();
		ShapeDefinition cross = new ShapeDefinition();
		ShapeDefinition cavalry = new ShapeDefinition();
		ShapeDefinition infantry = new ShapeDefinition();
		ShapeDefinition armor = new ShapeDefinition();
		
		// make rectangle
		rectangle.setName("Rectangle");
		rectangle.isA("Shape");
		rectangle.setAbstract(false);
		rectangle.addComponentDefinition(new ComponentDefinition("vLeft",
		        "Line"));
		rectangle.addComponentDefinition(new ComponentDefinition("vRight",
		        "Line"));
		rectangle
		        .addComponentDefinition(new ComponentDefinition("hTop", "Line"));
		rectangle.addComponentDefinition(new ComponentDefinition("hBottom",
		        "Line"));
		ConstraintDefinition cs1 = new ConstraintDefinition();
		ConstraintDefinition cs2 = new ConstraintDefinition();
		ConstraintDefinition cs3 = new ConstraintDefinition();
		ConstraintDefinition cs4 = new ConstraintDefinition();
		ConstraintDefinition cs5 = new ConstraintDefinition();
		ConstraintDefinition cs6 = new ConstraintDefinition();
		ConstraintDefinition cs7 = new ConstraintDefinition();
		ConstraintDefinition cs8 = new ConstraintDefinition();
		cs1.setName("Horizontal");
		cs1.addParameter(new ConstraintParameter("hTop"));
		cs2.setName("Horizontal");
		cs2.addParameter(new ConstraintParameter("hBottom"));
		cs3.setName("Vertical");
		cs3.addParameter(new ConstraintParameter("vLeft"));
		cs4.setName("Vertical");
		cs4.addParameter(new ConstraintParameter("vRight"));
		cs5.setName("Coincident");
		cs5.addParameter(new ConstraintParameter("vLeft",
		        ComponentSubPart.TopMostEnd));
		cs5.addParameter(new ConstraintParameter("hTop",
		        ComponentSubPart.LeftMostEnd));
		cs6.setName("Coincident");
		cs6.addParameter(new ConstraintParameter("vLeft",
		        ComponentSubPart.BottomMostEnd));
		cs6.addParameter(new ConstraintParameter("hBottom",
		        ComponentSubPart.LeftMostEnd));
		cs7.setName("Coincident");
		cs7.addParameter(new ConstraintParameter("vRight",
		        ComponentSubPart.TopMostEnd));
		cs7.addParameter(new ConstraintParameter("hTop",
		        ComponentSubPart.RightMostEnd));
		cs8.setName("Coincident");
		cs8.addParameter(new ConstraintParameter("vRight",
		        ComponentSubPart.BottomMostEnd));
		cs8.addParameter(new ConstraintParameter("hBottom",
		        ComponentSubPart.RightMostEnd));
		rectangle.addConstraintDefinition(cs1);
		rectangle.addConstraintDefinition(cs2);
		rectangle.addConstraintDefinition(cs3);
		rectangle.addConstraintDefinition(cs4);
		rectangle.addConstraintDefinition(cs5);
		rectangle.addConstraintDefinition(cs6);
		rectangle.addConstraintDefinition(cs7);
		rectangle.addConstraintDefinition(cs8);
		rectangle.addAliasDefinition(new AliasDefinition("TopLeftCorner",
		        "hTop", ComponentSubPart.LeftMostEnd));
		rectangle.addAliasDefinition(new AliasDefinition("BottomLeftCorner",
		        "hBottom", ComponentSubPart.LeftMostEnd));
		rectangle.addAliasDefinition(new AliasDefinition("TopRightCorner",
		        "hTop", ComponentSubPart.RightMostEnd));
		rectangle.addAliasDefinition(new AliasDefinition("BottomRightCorner",
		        "hBottom", ComponentSubPart.RightMostEnd));
		m_domainDef.addShapeDefinition(rectangle);
		
		// make cross
		cross.setName("Cross");
		cross.isA("Shape");
		cross.setAbstract(false);
		cross.addComponentDefinition(new ComponentDefinition("p1", "Line"));
		cross.addComponentDefinition(new ComponentDefinition("p2", "Line"));
		ConstraintDefinition cs11 = new ConstraintDefinition();
		ConstraintDefinition cs21 = new ConstraintDefinition();
		ConstraintDefinition cs31 = new ConstraintDefinition();
		ConstraintDefinition cs41 = new ConstraintDefinition();
		cs11.setName("PositiveSlope");
		cs11.addParameter(new ConstraintParameter("p1"));
		cs21.setName("NegativeSlope");
		cs21.addParameter(new ConstraintParameter("n1"));
		cs31.setName("SameSize");
		cs31.addParameter(new ConstraintParameter("n1"));
		cs31.addParameter(new ConstraintParameter("p1"));
		cs41.setName("Coincident");
		cs41.addParameter(new ConstraintParameter("n1.Center"));
		cs41.addParameter(new ConstraintParameter("p1.Center"));
		cross.addConstraintDefinition(cs11);
		cross.addConstraintDefinition(cs21);
		cross.addConstraintDefinition(cs31);
		cross.addConstraintDefinition(cs41);
		cross.addAliasDefinition(new AliasDefinition("TopLeft", "n1",
		        ComponentSubPart.End1));
		cross.addAliasDefinition(new AliasDefinition("BottomLeft", "p1",
		        ComponentSubPart.End1));
		cross.addAliasDefinition(new AliasDefinition("TopRight", "p1",
		        ComponentSubPart.End2));
		cross.addAliasDefinition(new AliasDefinition("BottomRight", "n1",
		        ComponentSubPart.End2));
		cross.addAliasDefinition(new AliasDefinition("Center", "p1",
		        ComponentSubPart.Center));
		m_domainDef.addShapeDefinition(cross);
		
		// make cavalry
		cavalry.setName("Cavalry");
		cavalry.isA("Shape");
		cavalry.setAbstract(false);
		cavalry.addComponentDefinition(new ComponentDefinition("rect",
		        "Rectangle"));
		cavalry.addComponentDefinition(new ComponentDefinition("l1", "Line"));
		ConstraintDefinition cs12 = new ConstraintDefinition();
		ConstraintDefinition cs22 = new ConstraintDefinition();
		ConstraintDefinition cs32 = new ConstraintDefinition();
		cs12.setName("Contains");
		cs12.addParameter(new ConstraintParameter("rect"));
		cs12.addParameter(new ConstraintParameter("l1"));
		cs22.setName("Coincident");
		cs22.addParameter(new ConstraintParameter("rect",
		        ComponentSubPart.TopRight));
		cs22.addParameter(new ConstraintParameter("l1",
		        ComponentSubPart.TopMostEnd));
		cs32.setName("Coincident");
		cs32.addParameter(new ConstraintParameter("rect",
		        ComponentSubPart.BottomLeft));
		cs32.addParameter(new ConstraintParameter("l1",
		        ComponentSubPart.BottomMostEnd));
		cavalry.addConstraintDefinition(cs12);
		cavalry.addConstraintDefinition(cs22);
		cavalry.addConstraintDefinition(cs32);
		cavalry.addAliasDefinition(new AliasDefinition("Center", "rect",
		        ComponentSubPart.Center));
		m_domainDef.addShapeDefinition(cavalry);
		
		// make infantry
		infantry.setName("Infantry");
		infantry.isA("Shape");
		infantry.setAbstract(false);
		infantry.addComponentDefinition(new ComponentDefinition("cross1",
		        "Cross"));
		infantry.addComponentDefinition(new ComponentDefinition("rect",
		        "Rectangle"));
		ConstraintDefinition cs13 = new ConstraintDefinition();
		ConstraintDefinition cs23 = new ConstraintDefinition();
		ConstraintDefinition cs33 = new ConstraintDefinition();
		ConstraintDefinition cs43 = new ConstraintDefinition();
		ConstraintDefinition cs53 = new ConstraintDefinition();
		cs13.setName("Contains");
		cs13.addParameter(new ConstraintParameter("rect"));
		cs13.addParameter(new ConstraintParameter("cross1"));
		cs23.setName("Coincident");
		cs23.addParameter(new ConstraintParameter("rect",
		        ComponentSubPart.TopRight));
		cs23.addParameter(new ConstraintParameter("cross1",
		        ComponentSubPart.TopRight));
		cs33.setName("Coincident");
		cs33.addParameter(new ConstraintParameter("rect",
		        ComponentSubPart.TopLeft));
		cs33.addParameter(new ConstraintParameter("cross1",
		        ComponentSubPart.TopLeft));
		cs43.setName("Coincident");
		cs43.addParameter(new ConstraintParameter("rect",
		        ComponentSubPart.BottomRight));
		cs43.addParameter(new ConstraintParameter("cross1",
		        ComponentSubPart.BottomRight));
		cs53.setName("Coincident");
		cs53.addParameter(new ConstraintParameter("rect",
		        ComponentSubPart.BottomLeft));
		cs53.addParameter(new ConstraintParameter("cross1",
		        ComponentSubPart.BottomLeft));
		infantry.addConstraintDefinition(cs13);
		infantry.addConstraintDefinition(cs23);
		infantry.addConstraintDefinition(cs33);
		infantry.addConstraintDefinition(cs43);
		infantry.addConstraintDefinition(cs53);
		infantry.addAliasDefinition(new AliasDefinition("Center", "rect",
		        ComponentSubPart.Center));
		m_domainDef.addShapeDefinition(infantry);
		
		// make armor
		armor.setName("Armor");
		armor.isA("Shape");
		armor.setAbstract(false);
		armor.addComponentDefinition(new ComponentDefinition("ellipse1",
		        "Ellipse"));
		armor.addComponentDefinition(new ComponentDefinition("rect",
		        "Rectangle"));
		ConstraintDefinition cs14 = new ConstraintDefinition();
		ConstraintDefinition cs24 = new ConstraintDefinition();
		cs14.setName("Contains");
		cs14.addParameter(new ConstraintParameter("rect1"));
		cs14.addParameter(new ConstraintParameter("ellipse1"));
		cs24.setName("Coincident");
		cs24.addParameter(new ConstraintParameter("rect1",
		        ComponentSubPart.Center));
		cs24.addParameter(new ConstraintParameter("ellipse1",
		        ComponentSubPart.Center));
		armor.addConstraintDefinition(cs14);
		armor.addConstraintDefinition(cs24);
		armor.addAliasDefinition(new AliasDefinition("Center", "rect",
		        ComponentSubPart.Center));
		m_domainDef.addShapeDefinition(armor);
	}
	

	@Test
	public void testDomainDefinitionCompiler() {
		m_compiler = new DomainDefinitionCompiler(m_domainDef);
		// TODO
	}
	

	@Test
	public void testCompile() {
		m_compiler = new DomainDefinitionCompiler(m_domainDef);
		try {
	        m_compiledDef = m_compiler.compile();
        }
        catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		// TODO
	}
	

	@Test
	public void testGetCompiledDomainDefinition() {
		m_compiler = new DomainDefinitionCompiler(m_domainDef);
		try {
	        m_compiledDef = m_compiler.compile();
        }
        catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		assertEquals(m_compiler.getCompiledDomainDefinition(), m_compiledDef);
	}
	

	@Test
	public void testGetAssumedPrimitives() {
		m_compiler = new DomainDefinitionCompiler(m_domainDef);
		try {
	        m_compiledDef = m_compiler.compile();
        }
        catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		assertTrue(m_compiler.getAssumedPrimitives().size() == 2);
		assertTrue(m_compiler.getAssumedPrimitives().contains("Ellipse"));
		assertTrue(m_compiler.getAssumedPrimitives().contains("Line"));
	}
	
}
