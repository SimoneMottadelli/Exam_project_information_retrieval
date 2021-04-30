package com.unimib.it.ir.irproject.analyzers;
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

public class CorpusAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer tokenizer = new WhitespaceTokenizer();
		TokenStream tokenStream = tokenizer;

		// Remove punctuation at the beginning of words and at finish
		// Remove all the URLs
		Pattern punctuationStart = Pattern.compile("^[!\"$“–”%&'()*+,/:;\\[\\]<=>?‘’^_―—`{|}~.-]*");
		Pattern punctuationEnd = Pattern.compile("[!\\\"$;%“–”:&'()\\[\\]’‘*+?,.―—-]*$");
		Pattern urls = Pattern.compile("http\\S+");
		tokenStream = new PatternReplaceFilter(tokenStream, punctuationStart, "", true);
		tokenStream = new PatternReplaceFilter(tokenStream, punctuationEnd, "", true);
		tokenStream = new PatternReplaceFilter(tokenStream, urls, "", true);
		tokenStream = new LowerCaseFilter(tokenStream);
		tokenStream = new LengthFilter(tokenStream, 1, 280);
		return new TokenStreamComponents(tokenizer, tokenStream);
	}
	
	@Override
	protected Reader initReader(String fieldName, Reader reader) {
		Pattern p = Pattern.compile("([\\uD83C-\\uDBFF\\uDC00-\\uDFFF]|[pa]m/et)");
		return new PatternReplaceCharFilter(p, " $1 ", reader);
	}

}
