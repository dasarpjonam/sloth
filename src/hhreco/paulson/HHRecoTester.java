package hhreco.paulson;

import hhreco.recognition.HHRecognizer;
import hhreco.recognition.MSTrainingModel;
import hhreco.recognition.MSTrainingParser;
import hhreco.recognition.RecognitionSet;
import hhreco.recognition.TimedStroke;
import hhreco.toolbox.ApproximateStrokeFilter;
import hhreco.toolbox.InterpolateStrokeFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;

public class HHRecoTester {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		long totalTime = 0;
		File train = new File("../HHRecoData/train.sml");
		File test = new File("../HHRecoData/test.sml");
		HHRecognizer reco = new HHRecognizer();
		BufferedReader br = new BufferedReader(new FileReader(train));
		BufferedReader br2 = new BufferedReader(new FileReader(test));
		MSTrainingParser parser = new MSTrainingParser();
		MSTrainingModel trainModel = (MSTrainingModel) parser.parse(br);
		MSTrainingModel testModel = (MSTrainingModel) parser.parse(br2);
		ApproximateStrokeFilter approx = new ApproximateStrokeFilter(1.0);
		InterpolateStrokeFilter interp = new InterpolateStrokeFilter(10.0);
		MSTrainingModel model = new MSTrainingModel();
		for (Iterator iter = trainModel.types(); iter.hasNext();) {
			String type = (String) iter.next();
			for (Iterator iter2 = trainModel.positiveExamples(type); iter2
					.hasNext();) {
				TimedStroke[] strokes = (TimedStroke[]) iter2.next();
				strokes = HHRecognizer
						.preprocess(strokes, approx, interp, null);
				model.addPositiveExample(type, strokes);
			}
		}
		MSTrainingModel tModel = new MSTrainingModel();
		for (Iterator iter = testModel.types(); iter.hasNext();) {
			String type = (String) iter.next();
			for (Iterator iter2 = testModel.positiveExamples(type); iter2
					.hasNext();) {
				TimedStroke[] strokes = (TimedStroke[]) iter2.next();
				strokes = HHRecognizer
						.preprocess(strokes, approx, interp, null);
				tModel.addPositiveExample(type, strokes);
			}
		}
		System.out.println("Training...");
		reco.train(model);
		System.out.println("Done Training...");

		double total = 0;
		double totalCorrect = 0;
		for (Iterator iter = tModel.types(); iter.hasNext();) {
			String type = (String) iter.next();
			double num = 0;
			double numCorrect = 0;
			for (Iterator iter2 = tModel.positiveExamples(type); iter2
					.hasNext();) {
				TimedStroke[] strokes = (TimedStroke[]) iter2.next();
				strokes = HHRecognizer
						.preprocess(strokes, approx, interp, null);
				long c = System.currentTimeMillis();
				RecognitionSet results = reco.sessionCompleted(strokes);
				long c2 = System.currentTimeMillis();
				totalTime += (c2 - c);
				if (type.compareToIgnoreCase(results
						.getHighestValueRecognition().getType().toString()) == 0) {
					numCorrect++;
					totalCorrect++;
				}
				System.out.println("actual: " + type + " predicted: "
						+ results.getHighestValueRecognition().getType());
				num++;
				total++;
			}
			System.out.println(type + ": (" + numCorrect + "/" + num + ") = "
					+ (numCorrect / num));
		}
		System.out.println("total: (" + totalCorrect + "/" + total + ") = "
				+ (totalCorrect / total));
		System.out.println("total time: " + totalTime + " ms = "
				+ ((double) totalTime / 1000) + " sec");
	}

}
