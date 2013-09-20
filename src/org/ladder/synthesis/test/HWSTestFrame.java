//package org.ladder.synthesis.test;
//
//import java.awt.Dimension;
//
//import javax.swing.JFrame;
//
//import org.ladder.core.sketch.ISketch;
//import org.ladder.core.sketch.Sketch;
//
//import org.jfree.chart.ChartFactory; 
//import org.jfree.chart.ChartFrame; 
//import org.jfree.chart.JFreeChart; 
//import org.jfree.data.general.DefaultPieDataset;
//
//public class HWSTestFrame extends JFrame {
//
//	/**
//	 * Draw panel for handwriten characters
//	 */
//	private HWDrawPanel m_drawPanel;
//
//	/**
//	 * Sketch saving the handwriting
//	 */
//	private ISketch m_sketch;
//
//	/**
//	 * Constructor
//	 */
//	public HWSTestFrame() {
//		super();
//
//		m_sketch = new Sketch();
//		m_drawPanel = new HWDrawPanel(m_sketch);
//		
//		add(m_drawPanel);
//
//		setSize(new Dimension(800, 600));
//		setVisible(true);
//	}
//
//	/**
//	 * Main
//	 * 
//	 * @param args
//	 */
//	public static void main(String args[]) {
//		
//		HWSTestFrame testFrame = new HWSTestFrame();
//
//		// create a dataset...
//		DefaultPieDataset data = new DefaultPieDataset();
//		data.setValue("Category 1", 43.2);
//		data.setValue("Category 2", 27.9);
//		data.setValue("Category 3", 79.5);
//		// create a chart...
//		JFreeChart chart = ChartFactory.createPieChart("Sample Pie Chart",
//				data, true, // legend?
//				true, // tooltips?
//				false // URLs?
//				);
//
//		// create and display a frame...
//		ChartFrame frame = new ChartFrame("First", chart);
//		frame.pack();
//		frame.setVisible(true);
//	}
//}
