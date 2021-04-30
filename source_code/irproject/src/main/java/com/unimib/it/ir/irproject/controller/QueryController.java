package com.unimib.it.ir.irproject.controller;

import java.util.HashMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.unimib.it.ir.irproject.QueryEngine;
import com.unimib.it.ir.irproject.QueryResult;
import com.unimib.it.ir.irproject.UserProfileManager;

@SuppressWarnings("deprecation")
@Controller
public class QueryController {

	@GetMapping("/results")
	@ResponseBody
	public String executeQuery(@RequestParam("field") String field, @RequestParam("query") String query, 
			@RequestParam(value="synonym", required=false) String synonym, 
			@RequestParam(value="weight", defaultValue="0.42") double weight) {	
		query = StringEscapeUtils.unescapeHtml4(query); // to take into account emojis that are encoded in HTML unicode
		boolean useSynonyms = "on".equals(synonym);
		HashMap<String, QueryResult[]> resultsMap;
		if (weight < 0)
			weight = 0;
		if (weight > 1)
			weight = 1;
		if (field == "custom") 
			resultsMap = executeCustomQuery(query, useSynonyms, weight);
		else
			resultsMap = executeSingleFieldQuery(field, query, useSynonyms, weight);

		return createHTMLResponse(resultsMap);
	}

	private HashMap<String, QueryResult[]> executeCustomQuery(String query, boolean useSynonyms,
			double weight) {
		HashMap<String, QueryResult[]> resultsMap = new HashMap<>();
		for (String u : UserProfileManager.getInstance().getUserProfiles().keySet()) {
			QueryResult[] res = QueryEngine.getInstance().searchByCustomQueryPersonalized(query, 
					useSynonyms, UserProfileManager.getInstance().getUserProfile(u), weight);
			resultsMap.put(u, res);
		}
		resultsMap.put("No personalization", QueryEngine.getInstance().searchByCustomQuery(query, useSynonyms));
		return resultsMap;
	}

	private HashMap<String, QueryResult[]> executeSingleFieldQuery(String field, String query, 
			boolean useSynonyms, double weight) {
		HashMap<String, QueryResult[]> resultsMap = new HashMap<>();
		for (String u : UserProfileManager.getInstance().getUserProfiles().keySet()) {
			QueryResult[] res = QueryEngine.getInstance().searchBySingleFieldPersonalized(field, query, useSynonyms, 
					UserProfileManager.getInstance().getUserProfile(u), weight);
			resultsMap.put(u, res);
		}
		resultsMap.put("No personalization", QueryEngine.getInstance().searchBySingleField(field, query, useSynonyms));
		return resultsMap;
	}

	private String createHTMLResponse(HashMap<String, QueryResult[]> resultsMap) {
		String result = "<!DOCTYPE html><html><head><style>table {border: 3px solid black; border-collapse:collapse}"
				+ "table {width: 100%;} th, td {border: 1px solid black;} th{border-bottom: 3px solid black; "
				+ "border-top: 2px solid black;}.header {border-right: 3px solid black;}\r\n"
				+ ".finalColumn {border-right: 3px solid black; border-collapse:collapse} html {overflow-x: auto;}"
				+ "</style></head><body><h2>Query results for " + QueryEngine.getInstance().getLastQuery()
				+ "</h2><div class=\"table-container\"><table style=\"width:100%\"><tr><td class=\"header\" "
				+ "style=\"border-right: 3px solid black; border-collapse:collapse; text-align:center;\" "
				+ "colspan=\"4\"><b>No personalization</b></td>";
		
		for (String u : resultsMap.keySet()) 
			if (!u.equalsIgnoreCase("No personalization"))
				result += "<td class=\"header\" style=\"border-right: 3px solid black; border-collapse:collapse; "
					+ "text-align:center;\" colspan=\"4\"><b>User profile: " + u + "</b></td>";
		result += "</tr><tr>";
			
		for (int i = 0; i < resultsMap.keySet().size(); ++i)
			result += "<th>Score</th><th>Created at</th><th>Author</th><th class=\"finalColumn\">Text</th>";
		result += "</tr>";
		
		String score;
		String date, author, full_text;
		QueryResult[] noPersonalizationRes = resultsMap.remove("No personalization");
		for (int i = 0; i < noPersonalizationRes.length; ++i) {
			result += "<tr>";
			score = String.format("%1$,.2f", noPersonalizationRes[i].getFinalScore());
			date = noPersonalizationRes[i].getDocument().get("created_at");
			author = noPersonalizationRes[i].getDocument().get("screen_name");
			full_text = noPersonalizationRes[i].getDocument().get("full_text");
			result += "<td>" + score + "</td><td>" + date + "</td><td>" + author + 
					"</td><td class=\"finalColumn\">" + full_text + "</td>";
			for (String u : resultsMap.keySet()) {
				score = String.format("%1$,.2f", resultsMap.get(u)[i].getFinalScore());
				date = resultsMap.get(u)[i].getDocument().get("created_at");
				author = resultsMap.get(u)[i].getDocument().get("screen_name");
				full_text = resultsMap.get(u)[i].getDocument().get("full_text");
				result += "<td>" + score + "</td><td>" + date + "</td><td>" + author + 
						"</td><td class=\"finalColumn\">" + full_text + "</td>";
			}
			result += "</tr>";
		}

		result += "</table></div></body></html>";
		
		return result;
	}
}