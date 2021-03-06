package vecsearch.hash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import util.Util;
import vecsearch.bruteforce.Word2VEC;
import vecsearch.bruteforce.WordEntry;

public class CityBlockHash implements IHash{
	
	private int dimension = 100;
	private double w = 0;
	private double[] randomPartition;
	/**
	 * 为何直接随机就可以了？
	 */
	private static final long serialVersionUID = 778951747630668248L;
	
	public CityBlockHash(int dimension, int w)
	{
		this.w = w;
		this.dimension = dimension;
		randomPartition = generateRandomProjection(dimension);
		 
	}
 
	public long hash(double[] vector) {

		int hash[] = new int[randomPartition.length];
		for(int d=0; d<randomPartition.length; d++) {
			hash[d] =  (int) (vector[d]-randomPartition[d] / Double.valueOf(w));
		}
		return Arrays.hashCode(hash);
	}
	
	 
	
	

	private double[] generateRandomProjection(int dimension) {
		Random rand = new Random();
		double randProject[] = new double[dimension];
		for (int d = 0; d < dimension; d++) {
			// mean 0
			// standard deviation 1.0
			double val = rand.nextGaussian();
			randProject[d] = val;
		}
		return randProject;
	}
	
	public static void test() throws IOException
	{
	
		Word2VEC vec = new Word2VEC();
		vec.loadGoogleModel("E:\\data\\word2vec-corpus\\baike\\vectors.bin.ansj10w.skip");
		String str = "美女";
		for (int i = 0; i < 1; i++) {
			System.out.println(vec.distance(str));
		}
		HashMap<String,float[]> word2vec = vec.getWordMap();
		//CosineHash: 特别小的hashNums肯定不行；但是大到一定程度也没必要，因为根本切割不到足够信息了。64位之后就没意义了
		 CityBlockHash hashTool = new CityBlockHash(vec.getSize(),2);
		 HashMap<String,Long> word2CosineHash = new HashMap<String, Long>();
		 for(String key: word2vec.keySet())
		 {
			 double[] temp = new double[vec.getSize()];
			 for(int k = 0; k < temp.length; k++)
			 {
				 temp[k] = word2vec.get(key)[k];
			 }
			 word2CosineHash.put(key, hashTool.hash(temp));
		 }
		 long meinv = word2CosineHash.get(str);
		 TreeSet<WordEntry> ret = new TreeSet<WordEntry>();
		 for(String key: word2CosineHash.keySet())
		 {
			 ret.add(new WordEntry(key,-Long.bitCount(word2CosineHash.get(key)^meinv)));
			 if(ret.size()>50)
				 ret.pollLast();
		 }
		 for(WordEntry we: ret)
		 {
			 System.out.println(we);
		 }
	}
	
	public static void main(String args[]) throws IOException
	{
		 test();
	}
}