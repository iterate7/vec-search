package util;

import java.util.TreeSet;

import vec.search.word2vec.WordEntry;

public interface ISearchVec {
	
	
	public TreeSet<WordEntry> distance(String queryWord) ;
	
	public TreeSet<WordEntry> distance(int[] vec) ;
}
