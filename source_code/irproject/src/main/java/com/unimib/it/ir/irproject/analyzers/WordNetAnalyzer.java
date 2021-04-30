package com.unimib.it.ir.irproject.analyzers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.pattern.SimplePatternTokenizer;

public class WordNetAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		return new TokenStreamComponents(new SimplePatternTokenizer(".*"));
	}
}
