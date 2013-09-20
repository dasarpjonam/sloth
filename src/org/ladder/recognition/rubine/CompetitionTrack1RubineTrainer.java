package org.ladder.recognition.rubine;

import java.io.File;

public class CompetitionTrack1RubineTrainer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MultistrokeRubineClassifier mrc = new MultistrokeRubineClassifier();
		File trainingDir=new File("/Users/pcorey/Documents/Track1/");
		mrc.trainOnDirectoryOrganizedData(trainingDir);
		String filename="competition.rub";
		mrc.saveWeights(filename);
	}

}
