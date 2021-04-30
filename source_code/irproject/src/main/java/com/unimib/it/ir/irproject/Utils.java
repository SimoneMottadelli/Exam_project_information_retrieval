package com.unimib.it.ir.irproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;

@SuppressWarnings("deprecation")
public class Utils {

	public static String replaceSymbols(String string) {

		string = string.replaceAll("&quot;", "\"");
		string = string.replaceAll("andquot;", "\"");

		string = string.replaceAll("&amp;", "&");
		string = string.replaceAll("andamp;", "&");

		string = string.replaceAll("&apos;", "\'");
		string = string.replaceAll("andapos;", "\'");

		string = string.replaceAll("&#146;", "’");
		string = string.replaceAll("and#146;", "’");

		string = string.replaceAll("&#128;", "€");
		string = string.replaceAll("and#128;", "€");

		string = string.replaceAll("&#153;", "™");
		string = string.replaceAll("and#153;", "™");

		string = string.replaceAll("&acirc;", "â");
		string = string.replaceAll("andacirc;", "â");

		string = string.replaceAll("&cent;", "¢");
		string = string.replaceAll("andcent;", "¢");

		string = string.replaceAll("&#132;", "„");
		string = string.replaceAll("and#132;", "„");

		string = string.replaceAll("&oacute;", "ó");
		string = string.replaceAll("andoacute;", "ó");

		string = string.replaceAll("&reg", "®");
		string = string.replaceAll("andreg;", "®");

		string = string.replaceAll("&eacute;", "é");
		string = string.replaceAll("andeacute;", "é");

		string = string.replaceAll("&Acirc;", "Â");
		string = string.replaceAll("andAcirc;", "Â");

		string = string.replaceAll("&Atilde;", "Ã");
		string = string.replaceAll("andAtilde;", "Ã");

		string = string.replaceAll("&copy;", "©");
		string = string.replaceAll("and&copy;", "©");

		string = string.replaceAll("&ouml;", "ö");
		string = string.replaceAll("andouml;", "ö");

		string = string.replaceAll("&szlig;", "ß");
		string = string.replaceAll("andszlig;", "ß");		

		return string;

	}
	
	public static ArrayList<Tweet> readJSON() throws IOException, ParseException, java.text.ParseException {
		JSONParser jsonParser = new JSONParser();
		FileReader fileReader = new FileReader(new File("tweets.json"));
		Object obj = jsonParser.parse(fileReader);
		JSONArray tweetList = (JSONArray) obj;
		ArrayList<Tweet> tweets = new ArrayList<>();
		for (int i = 0; i < tweetList.size(); ++i) {
			JSONObject tweet = (JSONObject) tweetList.get(i);
			String createdAt;
			try {
				createdAt = convertDateFormat((String) tweet.get("created_at"));
			} catch (java.text.ParseException e) {
				throw new java.text.ParseException((String) tweet.get("created_at"), 0);
			}
			String screenName = ((String) ((JSONObject) tweet.get("user")).get("screen_name"));
			String text = Utils.replaceSymbols(StringEscapeUtils.unescapeJava((String) tweet.get("full_text")));
			ArrayList<String> emojiList = (ArrayList<String>) EmojiParser.extractEmojis(text);
			JSONArray citations = (JSONArray) ((JSONObject) tweet.get("entities")).get("user_mentions");
			ArrayList<String> screenNameList = new ArrayList<>();
			for (int j = 0; j < citations.size(); ++j)
				screenNameList.add("@" + (String) ((JSONObject) citations.get(j)).get("screen_name"));
			JSONArray hashtags = (JSONArray) ((JSONObject) tweet.get("entities")).get("hashtags");
			ArrayList<String> hashtagList = new ArrayList<>();
			for (int j = 0; j < hashtags.size(); ++j) 
				hashtagList.add("#" + (String) ((JSONObject) hashtags.get(j)).get("text"));
			tweets.add(new Tweet(screenName, createdAt, text, screenNameList, hashtagList, emojiList));
		}
		return tweets;
	}
	
	private static String convertDateFormat(String date) throws java.text.ParseException {
		DateFormat fromFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy", Locale.US);
		DateFormat toFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		return toFormat.format(fromFormat.parse(date));
	}
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Entry.comparingByValue(Collections.reverseOrder()));

		Map<K, V> result = new LinkedHashMap<>();
		for (Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}
	
	public static ArrayList<String> stopWords;
	
	public static void buildStopWordList() throws FileNotFoundException {
		Scanner fileReader = new Scanner(new File("stopword.txt"));
		stopWords = new ArrayList<>();
		while(fileReader.hasNextLine())
			stopWords.add(fileReader.nextLine());
		fileReader.close();
	}
	
	public static boolean isStopWord(String word) {
		return stopWords.contains(word);
	}
	
	public static boolean isNumberOrTime(String word) {
		return word.matches("\\d*(:\\d*)?(st|[ap]m(/et)|nd|th|rd|cet|gmt)") || word.matches("[-+]?\\d*[\\.\\:\\,]?\\d+");
	}
	
	public static boolean isEmoji(String word) {
		return EmojiManager.isEmoji(word);
	}
	
	public static boolean isAscii(String word) {
		return Charset.forName("US-ASCII").newEncoder().canEncode(word);
	}
	
	//	public static List<String> analyze(String text, Analyzer analyzer) throws IOException{
	//		List<String> result = new ArrayList<String>();
	//		TokenStream tokenStream = analyzer.tokenStream(null, text);
	//		CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
	//		tokenStream.reset();
	//		while(tokenStream.incrementToken()) 
	//			result.add(attr.toString());
	//		return result;
	//	}
	
}






