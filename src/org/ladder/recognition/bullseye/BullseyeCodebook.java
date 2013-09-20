package org.ladder.recognition.bullseye;

import java.util.ArrayList;
import java.util.List;

import libsvm.svm_node;

/**
 * The codebook for the BullseyeRecognizer, based on Oltman's thesis.  Basically, a list of Bullseyes that are codewords
 * and the look up function
 * @author pcorey
 *
 */
public class BullseyeCodebook {

	/**
	 * The Bullseyes used as code words in the code book
	 */
	private List<Bullseye> codeWords;
	
	/**
	 * Create an empty codebook
	 */
	public BullseyeCodebook(){
		codeWords = new ArrayList<Bullseye>();
	}
	
	/**
	 * Create a codebook from a list of Bulleyes
	 * @param words List of Bullseyes to use as codewords
	 */
	public BullseyeCodebook(List<Bullseye> words){
		codeWords = new ArrayList<Bullseye>(words);
	}

	/**
	 * Determines how closely the input Bullseyes match the codewords
	 * @param input List of Bullseyes to lookup
	 * @return List of svm_nodes to pass to the svm prediction model
	 */
	public svm_node[] lookUpVector(List<Bullseye> input){
		svm_node[] vector = new svm_node[codeWords.size()];
		int index=0;
		for(Bullseye codeWord : codeWords){
			double value=Double.POSITIVE_INFINITY;
			for(Bullseye in: input)
				if(codeWord.compareTo(in)<value)
					value=codeWord.compareTo(in);
			vector[index]=new svm_node();
			vector[index].index=index;
			vector[index].value=value;
			//System.out.print(value+" ");
			index++;
		}
		//System.out.println();
		return vector;
	}

	@Override
	public String toString(){
		String s = codeWords.size()+"\n";
		for(Bullseye b : codeWords){
			for(double d : b.getHistogram()){
				s+=d+" ";
			}
			s=s.trim()+"\n";
		}
		//System.out.println(s);
		return s;
	}
}
