package org.ladder.recognition.VisionEye;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class VisionEyeCodebook {

	List<VisionEye> m_codewords;
	
	public VisionEyeCodebook(List<VisionEye> codewords){
		m_codewords=codewords;
	}
	
	public double[] lookup(List<VisionEye> eyes){
		double[] codewordSimilairities = new double[m_codewords.size()];
		for(VisionEye ve : eyes)
			for(int i=0;i<m_codewords.size();i++){
				double similairity = m_codewords.get(i).similairity(ve);
				if(similairity>.66)
					codewordSimilairities[i]+=similairity;
			}
		return codewordSimilairities;
	}

	public static VisionEyeCodebook loadFromFile(String codebookFileName) {
		List<VisionEye> centers = new ArrayList<VisionEye>();
		try {
			BufferedReader bw = new BufferedReader(new FileReader(new File(codebookFileName)));
			String line = bw.readLine();
			int num=0;
			while(line!=null){
				StringTokenizer strTok = new StringTokenizer(line," ");
				int numberTokens = strTok.countTokens();
				double[] vals = new double[numberTokens];
				for(int i=0;i<numberTokens;i++)
					vals[i] = Double.valueOf(strTok.nextToken());
				centers.add(new VisionEye(vals));
				line=bw.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new VisionEyeCodebook(centers);
	}
	

}
