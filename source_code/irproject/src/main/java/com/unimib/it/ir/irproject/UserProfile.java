package com.unimib.it.ir.irproject;

import java.util.HashMap;

import org.la4j.Vector;
import org.la4j.vector.dense.BasicVector;

public class UserProfile {
	
	private static final int TOPK = 100;

	private String screenName;
	private HashMap<String, Integer> termFrequencyMap = new HashMap<>();

	public UserProfile(String screenName) {
		this.screenName = screenName;
	}
	
	private Vector getUserProfileVector() {
		Vector vector = new BasicVector(TOPK);
		Integer[] termFreq = termFrequencyMap.values().toArray(new Integer[0]);
		for (int i = 0; i < termFreq.length; ++i)
			vector.set(i, termFreq[i]);
		return vector;
	}
	
	private Vector getDocumentVectorFromUserProfile(HashMap<String, Integer> documentTermFrequency) {
		Vector vector = new BasicVector(TOPK);
		int i = 0;
		for (String term : termFrequencyMap.keySet()) {
			if (documentTermFrequency.containsKey(term))
				vector.set(i, documentTermFrequency.get(term));
			++i;
		}
		return vector;
	}
	
	public double computeCosineSimilarity(double initialScore, HashMap<String, Integer> documentTermFrequency) {
		Vector userVector = getUserProfileVector();
		Vector documentVector = getDocumentVectorFromUserProfile(documentTermFrequency);
		double numerator = userVector.innerProduct(documentVector);
		double denominator = userVector.euclideanNorm() * documentVector.euclideanNorm();
		if (denominator < 1e-10)
			return 0;
		else
			return numerator / denominator;
	}

	public String getScreenName() {
		return screenName;
	}

	public HashMap<String, Integer> getTermFrequencyMap() {
		return termFrequencyMap;
	}
	
	public void outputTermFrequency() {
		System.out.println();
		int i = 0;
		for (String term : termFrequencyMap.keySet()) {
			i += 1;
			if (i > TOPK)
				break;
			System.out.println("screen_name: " + screenName + ", Term: " + term + ", frequency: " + termFrequencyMap.get(term));}
	}

	public void addTermFrequency(String term, int frequency) {
		if (!termFrequencyMap.containsKey(term))
			termFrequencyMap.put(term, 0);

		int totalFrequency = termFrequencyMap.get(term);
		totalFrequency += frequency;
		termFrequencyMap.put(term, totalFrequency);
	}
	
	public void deleteLessFrequentTerms() {
		HashMap<String, Integer> tmp = new HashMap<>();
		int i = 0;
		for (String term : termFrequencyMap.keySet()) {
			tmp.put(term, termFrequencyMap.get(term));
			i += 1;
			if (i == TOPK)
				break;
		}
		termFrequencyMap = tmp;
	}
	
	public void sortHashMap() {
		termFrequencyMap = (HashMap<String, Integer>) Utils.sortByValue(termFrequencyMap);
	}

}
