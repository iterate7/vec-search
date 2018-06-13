package vecsearch.elasticsearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
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
	public static CosineHash cosineHash = new CosineHash(dimension, numOfRandProjection); 
	
	public static void main(String[] args) throws IOException {

//		  createIndex(client, "vec");
//		  createMapping(client, "vec", "vec");
		  write(args[0]);
	}
	
	
	public static boolean createIndex(Client client, String indexName) {
		
		Map<String,Object> settings = new HashMap<String,Object>();
	 	settings.put("index.refresh_interval", "-1");
        settings.put("index.number_of_shards", "5");
        settings.put("index.number_of_replicas","3");
        settings.put("index.translog.flush_threshold_ops","30000");
        settings.put("index.merge.policy.merge_factor","30000");
		settings.put("client.transport.ping_timeout", "100s");
		settings.put("client.transport.sniff", "true");
		
		CreateIndexResponse  indexresponse = client.admin().indices()
				.prepareCreate(indexName).setSettings(settings).execute().actionGet();
		if(indexresponse.getAcknowledged()) {
			return true;
		}
		else 
			return false;
	}
	
	public static void createMapping(Client client, String index, String type) {
		try {
			InputStream is = InputStream.class.getResourceAsStream("/vecsearch/elasticsearch/vec.json");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = "";
			StringBuffer str = new StringBuffer();
			while ((line = br.readLine()) != null) {
				str.append(line);
			}
			System.out.println(str);
			PutMappingRequest mapping = Requests.putMappingRequest(index)
					.type(type).source(str.toString());
			client.admin().indices().putMapping(mapping).actionGet();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("create mapping succed");
	}
	
	public static void deleteIndex(Client client_log, String indexName){
		IndicesExistsRequest inExistsRequest = new IndicesExistsRequest(indexName);
		IndicesExistsResponse inExistsResponse = client_log.admin().indices()
		                    .exists(inExistsRequest).actionGet();
		if(inExistsResponse.isExists()){
			DeleteIndexResponse dResponse = client_log.admin().indices().prepareDelete(indexName)
                .execute().actionGet();
			System.out.println("delete index " + indexName + " " + dResponse.getAcknowledged());
		}
	}
	
	public static void write(String path) throws IOException
	{
		Word2VEC vec = new Word2VEC();
		vec.loadGoogleModel(path);
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
		//vecMerge.put("vec2", Arrays.toString(vec));
		//		for(int i = 0; i< vec.length; i++)
		//		{
		//			vecMerge.put("f_"+i, (float)vec[i]); //range?!
		//		}
		BitSet hashVector = cosineHash.hashArray(vec);
		//List<String> bitHash = new ArrayList<String>();
		String hashTrue = "";
		String hashFalse = "";
		List<String> tA = new ArrayList<String>();
		List<String> fA = new ArrayList<String>();
		for(int i = 0; i< hashVector.length(); i++)
		{
			if(hashVector.get(i))
			{
				//hashTrue+=("t"+i)+" ";
				tA.add(("t"+i));
			}
			else
			{
				//hashFalse+=("f"+i)+" ";
				fA.add("f"+i);
			}
		}
		vecMerge.put("ccoshash", tA);
		vecMerge.put("coshash_false", fA);
		tA = null;
		fA = null;
		return vecMerge;
	}
	
	
	 
	

}
