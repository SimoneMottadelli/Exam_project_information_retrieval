package com.unimib.it.ir.irproject;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class UserProfileManager {

	private static UserProfileManager instance = null;
	HashMap<String, UserProfile> userProfiles = new HashMap<>();

	private UserProfileManager() {} 

	public static UserProfileManager getInstance() {
		if (instance == null) {
			instance = new UserProfileManager();
		}
		return instance;
	}

	public HashMap<String, UserProfile> getUserProfiles() {
		return userProfiles;
	}

	public UserProfile getUserProfile(String screen_name) {
		if (!userProfiles.containsKey(screen_name))
			userProfiles.put(screen_name, new UserProfile(screen_name));
		return userProfiles.get(screen_name);
	}

	public void createUserProfiles() throws IOException {

		Directory dir = FSDirectory.open(Paths.get("index_folder"));
		IndexReader reader = DirectoryReader.open(dir);
		for (int i=0; i<reader.numDocs(); ++i) {
			if (reader.getTermVector(i, "full_text") == null)
				continue;
			Terms termVector = reader.getTermVector(i, "full_text");
			TermsEnum itr = termVector.iterator();
			BytesRef term = null;
			PostingsEnum postings = null;
			String screen_name = reader.document(i).get("screen_name");
			UserProfile u = getUserProfile(screen_name);
			while((term = itr.next()) != null){
				try{
					String termText = term.utf8ToString();
					if (Utils.isStopWord(termText) || Utils.isNumberOrTime(termText) || 
							Utils.isEmoji(termText) || !Utils.isAscii(termText))
						continue;
					postings = itr.postings(postings, PostingsEnum.FREQS);
					postings.nextDoc();
					int freq = postings.freq();
					u.addTermFrequency(termText, freq);
				} catch(Exception e){
					System.out.println(e);
				}
			}
		}

		// sort user profiles
		for (String u_str : userProfiles.keySet()) {
			UserProfile u = getUserProfile(u_str);
			u.sortHashMap();
			u.deleteLessFrequentTerms();
			//u.outputTermFrequency();
		}
	}
}
