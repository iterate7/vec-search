package util;

import java.util.TreeSet;

import vecsearch.bruteforce.WordEntry;

public interface ISearchVec {
	
	
	public TreeSet<WordEntry> distance(String queryWord) ;
	
	public TreeSet<WordEntry> distance(int[] vec) ;
}
