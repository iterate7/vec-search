package vecsearch.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Scanner;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsFilterBuilder;
import org.elasticsearch.search.SearchHit;

import util.ClientModule;
import vecsearch.bruteforce.Word2VEC;

public class ESSearcher {
	
	private static Client client = ClientModule.client_log;
	/**
	 * 通过es的组合查询、分布式特点、以及hash的特点。
	 * @param vec
	 */
	public static void searchNearestNeighbour(double[] vec)
	{
		
		
		
	}
	
	public static void main(String args[]) throws IOException
	{
		Word2VEC vec = new Word2VEC();
		vec.loadGoogleModel("E:\\data\\word2vec-corpus\\baike\\vectors.bin.ansj10w.skip");
		String str = "美女";
		long timeusage = System.currentTimeMillis();
		System.out.println(vec.distance(str));
		for (int i = 0; i < 1; i++) {
			vec.distance(str);
		}
		
		Scanner cin = new Scanner(System.in);
		while(true)
		{
			String q = cin.nextLine();
			float [] vecOrig = vec.wordMap.get(q);
			if(vecOrig==null)continue;
			System.out.println(vec.distance(q));
			double[] vecConv = new double[vecOrig.length];
			for(int i = 0; i < vecOrig.length; i++){
				vecConv[i] = vecOrig[i];
			}
			searchNearestNeighbourBaseRange(vecConv);
		}
	}
	
	/**
	 * 通过es的组合查询、分布式特点、以及hash的特点。
	 * @param vec
	 */
	public static void searchNearestNeighbourBaseRange(double[] vec)
	{
		BitSet hashVector = DataIntoES.cosineHash.hashArray(vec);
		BoolQueryBuilder bq = QueryBuilders.boolQuery();
		String q = "";
		String hashTrue = ""; int cntT = 10;
		String hashFalse = ""; int cntF = 100;
		for(int i = 0; i< hashVector.length(); i++)
		{
		 
				if(hashVector.get(i))
				{
					
					cntT--;
					if(cntT>0)
					{
						hashTrue+=("t"+i)+" ";
					}
				}
				else
				{
					cntF--;
					if(cntF>0)
					{
						hashTrue+=("t"+i)+" ";
					}
					hashFalse+=("f"+i)+" ";
				}
		 
		
		}
		
	
		
		bq.should(QueryBuilders.queryString(hashTrue).field("ccoshash"));
		bq.should(QueryBuilders.queryString(hashFalse).field("coshash_false"));
		
		
		SearchRequestBuilder searchRequestBuilder = client
				.prepareSearch("vec")
				.setQuery(bq)
				.setExplain(false).setFrom(0)
				.setSize(20);
//		List<String> top10 = new ArrayList<String>();
//		int topcnt = 10;
//		for(int i = 0; i< hashVector.length(); i++)
//		{
//			if( topcnt--<0)
//				break;
//			if(hashVector.get(i) )
//			{
//				top10.add("h_"+i);
//			}
//			
//		}
//		BoolFilterBuilder blFilter = FilterBuilders.boolFilter();
//		blFilter.cache(true);
//		//blFilter.must(FilterBuilders.queryFilter(bq));
//		
//		TermsFilterBuilder f2 = FilterBuilders.termsFilter("coshash", top10.toArray(new String[top10.size()]));
//		blFilter.must(f2);
		
		// searchRequestBuilder.setFilter(f2);
		 
		 
		searchRequestBuilder.addFields("title");
		//System.out.println(searchRequestBuilder.toString());
		
		long time = System.currentTimeMillis();
		SearchResponse response = searchRequestBuilder.execute().actionGet();
		System.out.println(response.took());
		for (SearchHit sh : response.getHits().getHits()) {
			System.out.println(sh.getId()+","+sh.field("title").getValue()+","+sh.getScore());
		}
		long end = System.currentTimeMillis();
		System.out.println("timeusage:"+(end-time));
	}

}
