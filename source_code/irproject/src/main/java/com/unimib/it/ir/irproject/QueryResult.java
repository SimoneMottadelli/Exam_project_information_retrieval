package com.unimib.it.ir.irproject;

import org.apache.lucene.document.Document;

public class QueryResult implements Comparable<QueryResult> {

	private Document document;
	private int docId;

	private double queryScore, userModelScore, finalScore;

	public QueryResult(Document document, int docId, double queryScore, double userModelScore) {
		this.document = document;
		this.docId = docId;
		this.queryScore = queryScore;
		this.userModelScore = userModelScore;
		this.finalScore = queryScore;
	}


	public QueryResult(Document document, int docId, double queryScore) {
		this(document, docId, queryScore, 0);
	}

	public int getDocId() {
		return docId;
	}

	public double getUserModelScore() {
		return userModelScore;
	}

	public void setUserModelScore(double userModelScore) {
		this.userModelScore = userModelScore;
	}

	public double getFinalScore() {
		return finalScore;
	}

	public void setFinalScore(double finalScore) {
		this.finalScore = finalScore;
	}

	public Document getDocument() {
		return document;
	}

	public double getQueryScore() {
		return queryScore;
	}

	@Override
	public int compareTo(QueryResult other) {
		return new Double(other.finalScore).compareTo(finalScore);
	}

}
