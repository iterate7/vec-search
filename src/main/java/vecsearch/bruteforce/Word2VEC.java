package vecsearch.bruteforce;

import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;

import util.VecSearch;

public class Word2VEC extends VecSearch  {
	
	public static void main(String[] args) throws IOException {
		test();
	}
	
	
	public static void test() throws IOException
	{
	
		Word2VEC vec = new Word2VEC();
		vec.loadGoogleModel("D:\\marsspace\\vec-search\\src\\main\\resources\\vectors.bin.ansj10w.skip");
		String str = "美女";
		for (int i = 0; i < 1; i++) {
			System.out.println(vec.distance(str));
		}
		 
	}

	private static Word2VEC instance = null;
	
	public Word2VEC()
	{
		wordMap.clear();
		
	}
	
	public Word2VEC getInstance(String path)
	{
		if(instance==null)
		{
			instance = new Word2VEC();
			try {
				loadGoogleModel(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return instance;
			
	}
	public Word2VEC(String path)
	{
		if(instance==null)
		{
			instance = new Word2VEC();
			try {
				loadGoogleModel(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	

	public TreeSet<WordEntry> distance(String queryWord) {

		float[] center = wordMap.get(queryWord);
		if (center == null) {
			return new TreeSet();
		}

		int resultSize = wordMap.size() < topNSize ? wordMap.size() : topNSize;
		TreeSet<WordEntry> result = new TreeSet<WordEntry>();

		float min = Float.MIN_VALUE;
		min = (float) 0.3;
		for (Map.Entry<String, float[]> entry : wordMap.entrySet()) {
			float[] vector = entry.getValue();
			float dist = 0;
			for (int i = 0; i < vector.length; i++) {
				dist += center[i] * vector[i];
			}

			if (dist > min) {
				result.add(new WordEntry(entry.getKey(), dist));
				if (resultSize < result.size()) {
					result.pollLast();
				}
				min = result.last().score;
			}
		}
		result.pollFirst();

		return result;
	}


	public TreeSet<WordEntry> distance(int[] vec) {
		return null;
	}
	
	
	
//	private HashMap<String,Float> k1k2Sim = new HashMap<String, Float>();
//	public float sim(String q1, String q2)
//	{
//			
//		if(k1k2Sim.containsKey(q1+"-"+q2))
//		{
//			return k1k2Sim.get(q1+"-"+q2);
//		}
//		
//		else
//		if(k1k2Sim.containsKey(q2+"-"+q1))
//		{
//			return k1k2Sim.get(q2+"-"+q1);
//		}
//		return 0;
//	}


}
