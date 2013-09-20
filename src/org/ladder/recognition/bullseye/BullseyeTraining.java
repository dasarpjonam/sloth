package org.ladder.recognition.bullseye;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;


public class BullseyeTraining {

	private static JFileChooser m_chooser = new JFileChooser();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JFrame jf = new JFrame();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
		m_chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int r = m_chooser.showOpenDialog(jf);
		if (r == JFileChooser.APPROVE_OPTION) {
			File f = m_chooser.getSelectedFile();
			try {
				BullseyeRecognizer br = new BullseyeRecognizer(f.getCanonicalPath());
				br.toFile(new File(f.getCanonicalPath()+"/coa.bcb"),new File(f.getCanonicalPath()+"/coa.svm"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
