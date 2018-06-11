package vecsearch.hash;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeSet;

import util.Util;
import vecsearch.bruteforce.Word2VEC;
import vecsearch.bruteforce.WordEntry;

public class CosineHash implements IHash{
	private Random rand = new Random(237);
	private int dimension = 100;
	private int hashNums = 32;
	private double hashPlane[][] = new double[hashNums][dimension];
	/**
	 * 为何直接随机就可以了？
	 */
	private static final long serialVersionUID = 778951747630668248L;
	
	public CosineHash(int dimension, int hashNums)
	{
		this.dimension = dimension;
		hashPlane = new double[hashNums][dimension];
		for(int i=0;i<hashNums;i++)
		{
			hashPlane[i] = generateRandomProjection(dimension);
			if(i==0)
				System.out.println(Arrays.toString(hashPlane[i]));
		}
	}
 
	public long hash(double[] vector) {

		
		int planeState[] = new int[dimension];
		for(int i=0;i<hashPlane.length;i++)
		{
			//dot
			double value = Util.dotProduct(vector, hashPlane[i]);
			//combine
			planeState[i] = value > 0 ? 1 : 0;
		}
		
		return combine(planeState);
	}
	
	
public BitSet hashArray(double[] vector) {

		
		int planeState[] = new int[hashPlane.length];
		
		BitSet bs = new BitSet(hashPlane.length);
		
		for(int i=0;i<hashPlane.length;i++)
		{
			//dot
			double value = Util.dotProduct(vector, hashPlane[i]);
			bs.set(i, (value > 0 ? true :false));
			
		}
		
		return bs;
	}
	 
	
	public long combine(int[] planeState) {
		//Treat the hashes as a series of bits.
		//They are either zero or one, the index 
		//represents the value.
		long result = 0;
		//factor holds the power of two.
		long factor = 1;
		for(int i = 0 ; i < planeState.length ; i++){
			result += planeState[i] == 0 ? 0 : factor;
			factor *= 2;
		}
		return result;
	}

	private double[] generateRandomProjection(int dimension) {
		
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
		long timeusage = System.currentTimeMillis();
		System.out.println(vec.distance(str));
		for (int i = 0; i < 100; i++) {
			vec.distance(str);
		}
		long timeend = System.currentTimeMillis();
		System.out.println(timeend-timeusage);;
		HashMap<String,float[]> word2vec = vec.getWordMap();
		//CosineHash: 特别小的hashNums肯定不行；但是大到一定程度也没必要，因为根本切割不到足够信息了。64位之后就没意义了
		 CosineHash hashTool = new CosineHash(vec.getSize(),50);
		 HashMap<String,BitSet> word2CosineHash = new HashMap<String, BitSet>();
		 for(String key: word2vec.keySet())
		 {
			 double[] temp = new double[vec.getSize()];
			 for(int k = 0; k < temp.length; k++)
			 {
				 temp[k] = word2vec.get(key)[k];
			 }
			 word2CosineHash.put(key, hashTool.hashArray(temp));
		 }
		 word2vec = null;
		 
		 BitSet meinv = word2CosineHash.get(str);
		 timeusage = System.currentTimeMillis();
		 for (int i = 0; i < 100; i++) {
		
			 TreeSet<WordEntry> ret = new TreeSet<WordEntry>();
			 for(String key: word2CosineHash.keySet())
			 {
				 float diff = (float)sim(meinv,word2CosineHash.get(key));
				 ret.add(new WordEntry(key,diff));
				 if(ret.size()>9)
					 ret.pollLast();
			 }
		 }
		 timeend = System.currentTimeMillis();
		 System.out.println(timeend-timeusage);;
		 TreeSet<WordEntry> ret = new TreeSet<WordEntry>();
		 for(String key: word2CosineHash.keySet())
		 {
			 float diff = (float)sim(meinv,word2CosineHash.get(key));
			 ret.add(new WordEntry(key,diff));
			 if(ret.size()>9)
				 ret.pollLast();
		 }
		 for(WordEntry we: ret)
		 {
			 System.out.println(we);
		 }
	}
	
	public static double sim(BitSet v1, BitSet v2)
	{
		int cnt = 0;
		for(int i=0;i<v1.length();i++)
		{
			if(v1.get(i)==v2.get(i))
				cnt++;
		}
		return cnt;
	}
	
	public static void main(String args[]) throws IOException
	{
		// test();
		CosineHash ch = new CosineHash(300,100);
	}
}