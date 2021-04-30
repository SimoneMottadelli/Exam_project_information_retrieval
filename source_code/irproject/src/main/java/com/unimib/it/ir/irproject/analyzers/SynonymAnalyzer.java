package com.unimib.it.ir.irproject.analyzers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceCharFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.synonym.WordnetSynonymParser;

public class SynonymAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer tokenizer = new WhitespaceTokenizer();
		TokenStream tokenStream = tokenizer;

		// Remove punctuation at the beginning of words and at finish
		// Remove all the URLs
		Pattern punctuationStart = Pattern.compile("^[!\"$“–”%&'()*+,/:;\\[\\]<=>?‘’^_―—`{|}~.-]*");
		Pattern punctuationEnd = Pattern.compile("[!\\\"$;%“–”:&'()\\[\\]’‘*+?,.―—-]*$");
		Pattern urls = Pattern.compile("http\\S+");
		Pattern whiteSpaceSynonyms = Pattern.compile(".* .*");
		tokenStream = new PatternReplaceFilter(tokenStream, punctuationStart, "", true);
		tokenStream = new PatternReplaceFilter(tokenStream, punctuationEnd, "", true);
		tokenStream = new PatternReplaceFilter(tokenStream, urls, "", true);
		tokenStream = new LowerCaseFilter(tokenStream);

		SynonymMap mySynonymMap = null;
		try {
			mySynonymMap = buildSynonym();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		tokenStream = new SynonymGraphFilter(tokenStream, mySynonymMap, true);
		tokenStream = new PatternReplaceFilter(tokenStream, whiteSpaceSynonyms, "", true);
		tokenStream = new LengthFilter(tokenStream, 1, 280);

		return new TokenStreamComponents(tokenizer, tokenStream);
	}

	@Override
	protected Reader initReader(String fieldName, Reader reader) {
		Pattern p = Pattern.compile("([\\uD83C-\\uDBFF\\uDC00-\\uDFFF]|[pa]m/et)");
		return new PatternReplaceCharFilter(p, " $1 ", reader);
	}

	private SynonymMap buildSynonym() throws IOException, java.text.ParseException {
		Reader rulesReader = new InputStreamReader(new FileInputStream(new File("wn_s.pl"))); 
		WordnetSynonymParser parser = new WordnetSynonymParser(true, true, new WordNetAnalyzer());
		parser.parse(rulesReader);
		return parser.build();
	}
}

