package com.unimib.it.ir.irproject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.parser.ParseException;

import com.unimib.it.ir.irproject.analyzers.CorpusAnalyzer;
import com.unimib.it.ir.irproject.exceptions.IndexingEngineException;

public class Indexing {

	public static void execute() {
		try {
			System.out.println("Building the index folder...");
			generateIndex();
			System.out.println("Generating user profiles...");
			Utils.buildStopWordList();
			UserProfileManager.getInstance().createUserProfiles();
			System.out.println("Done!");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new IndexingEngineException("Initialization failed: cannot find tweets.json or stopword.txt");
		} catch (IOException e) {
			throw new IndexingEngineException("Initialization failed: either tweets.json or index_folder cannot be found");			 
		} catch (ParseException e) {
			throw new IndexingEngineException("Initialization failed: cannot parse tweets.json");
		} catch (java.text.ParseException e) {
			throw new IndexingEngineException("Initialization failed: cannot convert in a proper format the date " + e.getMessage());
		}
	}

	private static void generateIndex() throws IOException, ParseException, java.text.ParseException {
		ArrayList<Tweet> tweets = Utils.readJSON();
		//		Tweet t1 = new Tweet("NBA", "2020/11/31 00:00", "A document describing a very simple thing", 
		//				new ArrayList<String>(), new ArrayList<String>(Arrays.asList("#Document")), new ArrayList<String>());
		//		Tweet t2 = new Tweet("NBA", "2020/12/03 00:00", "much more complexed papers here", 
		//				new ArrayList<String>(), new ArrayList<String>(Arrays.asList("#Papers")), new ArrayList<String>());
		//		Tweet t3 = new Tweet("NASA", "2020/12/04 23:59", "There is nothing to see here in this document, so return to your #House with your dog", 
		//				new ArrayList<String>(), new ArrayList<String>(Arrays.asList("#House")), new ArrayList<String>());
		//		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
		//		tweets.add(t1); tweets.add(t2); tweets.add(t3);
		Directory dir = FSDirectory.open(Paths.get("index_folder"));
		IndexWriterConfig iwc = new IndexWriterConfig(new CorpusAnalyzer());
		iwc.setOpenMode(OpenMode.CREATE); 			// Create a NEW index in the directory
		IndexWriter writer = new IndexWriter(dir, iwc);
		addDocuments(tweets, writer);
		writer.close();
	}

	private static void addDocuments(ArrayList<Tweet> tweets, IndexWriter writer) throws IOException {
		for (Tweet tweet : tweets) {
			Document lucene_doc = new Document();
			lucene_doc.add(new StringField("created_at", tweet.getCreatedAt(), Field.Store.YES));
			lucene_doc.add(new TextField("screen_name", tweet.getScreenName(), Field.Store.YES));
			lucene_doc.add(new TextField("hashtags", String.join(" ", tweet.getHashtags()), Field.Store.NO));
			lucene_doc.add(new TextField("citations", String.join(" ", tweet.getCitations()), Field.Store.NO));
			FieldType fullTextType = new FieldType(TextField.TYPE_STORED);
			fullTextType.setStoreTermVectors(true);
			lucene_doc.add(new Field("full_text", tweet.getFullText(), fullTextType));
			writer.addDocument(lucene_doc);
		}
	}
	
}
