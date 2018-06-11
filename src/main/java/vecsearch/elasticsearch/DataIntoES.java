package vecsearch.elasticsearch;

import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Digest;

import util.ClientModule;
import vecsearch.bruteforce.Word2VEC;
import vecsearch.hash.CosineHash;

/**
 * 把数据处理后写入集群。
 * @author iterat7
 *
 */
public class DataIntoES {

	private static int dimension = 300;
	private static int numOfRandProjection = 300;
	private static Client client = ClientModule.client_log;
	/**  **/
	private static CosineHash cosineHash = new CosineHash(dimension, numOfRandProjection); 
	
	public static void main(String[] args) throws IOException {

		write();
	}
	
	public static void write() throws IOException
	{
		Word2VEC vec = new Word2VEC();
		vec.loadGoogleModel("E:\\data\\word2vec-corpus\\baike\\vectors.bin.ansj10w.skip");
		String str = "美女";
		long timeusage = System.currentTimeMillis();
		System.out.println(vec.distance(str));
		for (int i = 0; i < 1; i++) {
			vec.distance(str);
		}
		long timeend = System.currentTimeMillis();
		System.out.println(timeend-timeusage);;
		HashMap<String,float[]> word2vec = vec.getWordMap();
		BulkRequestBuilder bulk = client.prepareBulk();
		int cnt = 0;
		for(String key: word2vec.keySet())
		{
			float [] vecOrig = word2vec.get(key);
			double[] vecConv = new double[vecOrig.length];
			for(int i = 0; i < vecOrig.length; i++){
				vecConv[i] = vecOrig[i];
			}
			Map<String,Object> source = feature(vecConv);
			source.put("ktitle", key);
			source.put("title",key);
			String id = Digest.md5Hex(key);
			bulk.add(client.prepareIndex("vec","vec",id).setSource(source));
			cnt++;
			if(bulk.numberOfActions()>=100)
			{
				bulk.execute();
				System.out.println("processed:"+cnt);
				bulk = client.prepareBulk();
			}
		}
		if(bulk.numberOfActions()>0)
		{
			bulk.execute();
		}
	}
	
	
	/**
	 * 
	 * @param vec
	 * @return
	 */
	public static Map<String,Object> feature(double[] vec)
	{
		Map<String,Object> vecMerge = new HashMap<String,Object>();
		vecMerge.put("vec", Arrays.toString(vec));
		for(int i = 0; i< vec.length; i++)
		{
			vecMerge.put("f_"+i, (float)vec[i]); //range?!
		}
		BitSet hashVector = cosineHash.hashArray(vec);
		for(int i = 0; i< hashVector.length(); i++)
		{
			if(hashVector.get(i))
			vecMerge.put("h_"+i, hashVector.get(i)); //range?!
		}
		
		return vecMerge;
	}
	
	
	 
	

}
