package com.unimib.it.ir.irproject;

import java.util.ArrayList;

public class Tweet {
	
	private String screenName;
	private String createdAt;
	private String fullText;
	private ArrayList<String> citations;
	private ArrayList<String> hashtags;
	private ArrayList<String> emojis;
	
	public Tweet(String screenName, String createdAt, String fullText, ArrayList<String> citations, 
			ArrayList<String> hashtags, ArrayList<String> emojis) {
		this.screenName = screenName;
		this.createdAt = createdAt;
		this.fullText = fullText;
		this.citations = citations;
		this.hashtags = hashtags;
		this.emojis = emojis;
	}

	public String getScreenName() {
		return screenName;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getFullText() {
		return fullText;
	}

	public ArrayList<String> getCitations() {
		return citations;
	}

	public ArrayList<String> getHashtags() {
		return hashtags;
	}
	
	public ArrayList<String> getEmojis() {
		return emojis;
	}
	
}
