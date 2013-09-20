package org.ladder.recognition.bullseye;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class BullseyeSVMTraining {


	private static JFileChooser m_chooser = new JFileChooser();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame jf = new JFrame();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);
		BullseyeRecognizer br = null;
		m_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int r = m_chooser.showOpenDialog(jf);
		if (r == JFileChooser.APPROVE_OPTION) {
			try {
				br = new BullseyeRecognizer(m_chooser.getSelectedFile(),new File(m_chooser.getSelectedFile().getCanonicalPath()+".svm"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(br==null)
			System.exit(0);
		m_chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		r = m_chooser.showOpenDialog(jf);
		if (r == JFileChooser.APPROVE_OPTION) {
			File f = m_chooser.getSelectedFile();
			try {
				br.trainSVM(f.getCanonicalPath());
				br.toFile(new File(f.getCanonicalPath()+"/coa.bcb"),new File(f.getCanonicalPath()+"/coa.svm"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
