package com.unimib.it.ir.irproject;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import com.unimib.it.ir.irproject.analyzers.CorpusAnalyzer;
import com.unimib.it.ir.irproject.analyzers.SynonymAnalyzer;
import com.unimib.it.ir.irproject.exceptions.QueryEngineException;

public class QueryEngine {

	private static QueryEngine instance;
	private final static int MAXDOC = 10;
	private IndexSearcher searcher;
	private CorpusAnalyzer simpleQueryAnalyzer;
	private SynonymAnalyzer synonymQueryAnalyzer;
	private String lastQuery;

	private QueryEngine() {
		try {
			Directory dir = FSDirectory.open(Paths.get("index_folder"));
			IndexReader reader = DirectoryReader.open(dir);
			searcher = new IndexSearcher(reader);
			simpleQueryAnalyzer = new CorpusAnalyzer();
			synonymQueryAnalyzer = new SynonymAnalyzer();
		} catch (IOException e) {
			throw new QueryEngineException("Initialization failed: index_folder not found");
		}
	}

	public static QueryEngine getInstance() {
		if (instance == null)
			instance = new QueryEngine();
		return instance;
	}

	public void close() {
		try {
			searcher.getIndexReader().close();
			instance = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public QueryResult[] searchBySingleField(String field, String queryText, boolean useSynonyms) {
		Analyzer analyzer;
		if (useSynonyms)
			analyzer = synonymQueryAnalyzer;
		else
			analyzer = simpleQueryAnalyzer;
		QueryResult[] queryResult = null;
		try {
			Query query = new QueryParser(field, analyzer).parse(queryText);
			this.lastQuery = query.toString();
			TopDocs docs = searcher.search(query, MAXDOC);
			queryResult = extractQueryResults(docs);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return queryResult;
	}
	
	public QueryResult[] searchBySingleField(String field, String queryText) {
		return searchBySingleField(field, queryText, false);
	}
	
	public QueryResult[] searchByCustomQuery(String queryText, boolean useSynonyms) {
		return searchBySingleField(null, queryText, useSynonyms);
	}
	
	public QueryResult[] searchByCustomQueryPersonalized(String queryText, 
			boolean useSynonyms, UserProfile userProfile, double weight) {
		return searchBySingleFieldPersonalized(null, queryText, useSynonyms, userProfile, weight);
	}

	public QueryResult[] searchBySingleFieldPersonalized(String field, String queryText, 
			boolean useSynonyms, UserProfile userProfile, double weight) {
		QueryResult[] queryResult = searchBySingleField(field, queryText, useSynonyms);
		if (queryResult != null)
			try {
				reRank(queryResult, userProfile, weight);
			} catch (IOException e) {
				queryResult = null;
			}
		return queryResult;
	}

	private QueryResult[] reRank(QueryResult[] queryResult, UserProfile userProfile, double weight) throws IOException {
		for (int i = 0; i < queryResult.length; ++i) {
			int docId = queryResult[i].getDocId();
			Terms terms = searcher.getIndexReader().getTermVector(docId, "full_text");
			TermsEnum itr = terms.iterator();
			BytesRef term = null;
			PostingsEnum postings = null;
			HashMap<String, Integer> termsInDocMap = new HashMap<>();
			while((term = itr.next()) != null){
				try{
					String termText = term.utf8ToString();
					postings = itr.postings(postings, PostingsEnum.FREQS);
					postings.nextDoc();
					int freq = postings.freq();
					termsInDocMap.put(termText, freq);
				} catch(Exception e){
					System.out.println(e);
				}
			}
			double initialScore = queryResult[i].getQueryScore();
			double userScore = userProfile.computeCosineSimilarity(initialScore, termsInDocMap);
			double finalScore = (1 - weight) * initialScore + weight * userScore;
			queryResult[i].setUserModelScore(userScore);
			queryResult[i].setFinalScore(finalScore);
		}
		Arrays.sort(queryResult);
		return queryResult;
	}

	private QueryResult[] extractQueryResults(TopDocs docs) throws IOException {
		ScoreDoc[] hits = docs.scoreDocs;
		QueryResult[] result = new QueryResult[hits.length];
		if (hits.length != 0) {
			double maxScore = hits[0].score;
			for (int i = 0; i < hits.length; ++i) {
				Document doc = searcher.doc(hits[i].doc);
				double queryScore = hits[i].score / maxScore;
				result[i] = new QueryResult(doc, hits[i].doc, queryScore);
			}
		}
		return result;
	}
	
	public String getLastQuery() {
		return lastQuery;
	}

}
