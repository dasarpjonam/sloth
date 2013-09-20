/**
 * 
 */
package hhreco.paulson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;

import org.ladder.core.sketch.IPoint;
import org.ladder.core.sketch.IStroke;
import org.ladder.io.SousaDataParser;

/**
 * @author bpaulson
 * 
 */
public class XMLtoSMLConverter {

	static String outputFileName = "test.sml";

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		File dir;

		// load main directory
		JFileChooser c = new JFileChooser();
		c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int r = c.showOpenDialog(null);
		if (r == JFileChooser.APPROVE_OPTION)
			dir = c.getSelectedFile();
		else
			return;

		File outputFile = new File(dir.getAbsolutePath() + "\\"
				+ outputFileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		bw.write("<?xml version=\"1.0\" standalone=\"no\"?>");
		bw.newLine();
		bw
				.write("<!DOCTYPE MSTrainingModel PUBLIC \"-//UC Berkeley//DTD train 1//EN\"");
		bw.newLine();
		bw.write("\t\"http://www.gigascale.org/diva/dtd/multiStrokeTrain.dtd\">");
		bw.newLine();
		bw.newLine();
		bw.write("<MSTrainingModel>");
		bw.newLine();

		// get subdirectories
		File[] files = dir.listFiles();
		for (int f = 0; f < files.length; f++) {
			if (!files[f].isDirectory())
				continue;

			// get examples of a particular shape
			File[] shapes = files[f].listFiles();

			// fn = the name of the shape (as determined by the folder name)
			String fn = files[f].getName();

			bw.write("<type name=\"" + fn + "\">");
			bw.newLine();

			// loop through each example of the shape
			for (int s = 0; s < shapes.length; s++) {
				if (shapes[s].isDirectory() || !shapes[s].exists())
					continue;

				// read stroke in from file (should be single stroke for Paleo)
				List<IStroke> strokeList = SousaDataParser
						.parseSousaFile(shapes[s]);
				if (strokeList.size() <= 0)
					continue;

				bw.write("<example label=\"+\" numStrokes=\""
						+ strokeList.size() + "\">");
				bw.newLine();

				for (IStroke st : strokeList) {
					bw.write("\t<stroke points=\"");
					for (IPoint p : st.getPoints()) {
						bw.write(p.getX() + " " + p.getY() + " " + p.getTime() + " ");
					}
					bw.write("\"/>");
					bw.newLine();
				}

				bw.write("</example>");
				bw.newLine();
			}
			bw.write("</type>");
			bw.newLine();
		}
		bw.write("</MSTrainingModel>");
		bw.newLine();
		bw.close();
	}
}
