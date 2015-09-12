package eu.leads.api.m36.func;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONObject;

import eu.leads.api.com.TimeConvertionUtils;
import eu.leads.api.m24.FunctionalityAbst;
import eu.leads.api.m24.FunctionalityAbstParams;
import eu.leads.api.m24.FunctionalityAbstResultRow;
import eu.leads.api.m24.model.Functionality1ResultRowAgreed;
import eu.leads.api.m36.model.Fun1M36JsonResultRow;
import eu.leads.api.m36.model.FunM36JsonParams;
import eu.leads.datastore.impl.LeadsQueryInterface;
import eu.leads.infext.Useful;
import eu.leads.infext.datastore.datastruct.UrlTimestamp;
import eu.leads.processor.web.QueryResults;

public class Fun1M36 implements FunctionalityAbst {
	
	private static String ECOM_PROD_NAME = "ecom_prod_name";
	private static String ECOM_PROD_PRICE_MAX = "ecom_prod_price_high";
	private static String ECOM_PROD_PRICE_MIN = "ecom_prod_price_low";
	private static String ECOM_PROD_CURR = "ecom_prod_currency";

	@Override
	public SortedSet<FunctionalityAbstResultRow> execute(
			FunctionalityAbstParams params) {
		
		FunM36JsonParams jsonParams = (FunM36JsonParams) params;
		
		SortedSet<FunctionalityAbstResultRow> resultRows = new TreeSet<>();
		
		List<String> keywords = jsonParams.getParameter("keywords", java.util.List.class);
		List<String> websites = jsonParams.getParameter("websites", java.util.List.class);
		Long startts  = jsonParams.getParameter("startts" , java.lang.Long.class);
		Long endts    = jsonParams.getParameter("endts"   , java.lang.Long.class);
		
		if(keywords == null || websites == null || startts == null || endts == null) {
			return null;
		}
		
		/*
		 * 
		 * 
		 * 
		 */
		
		List<UrlTimestamp> urlsTsList = new ArrayList<>();
		Map<UrlTimestamp,Set<String>> urlTs2Keywords = new HashMap<>();
		
		/*
		 * 1. Get all pages of the shop
		 * 2. Filter by day
		 * 3. Filter those with resource_type = ecom_prod_name
		 * 4. Filter by keywords
		 * 
		 * 	SELECT C.uri, C.ts 
		 *  FROM page_core C
		 *  JOIN keywords K ON C.uri = K.uri 
		 *  WHERE C.ts = K.ts
		 *  AND K.partid like ‘ecom_prod_name:000’
		 *  AND (C.fqdnurl like shopUri1 OR C.fqdnurl like shopUri2 OR …)
		 *  AND C.ts >= minTime AND C.ts <= maxTime
		 *  AND (K.keywords like keywords1 OR K.keywords like keywords2 OR …);
		 */
		
		String query1 = "SELECT C.uri, C.ts, K.keywords\n" +
			"FROM page_core C\n" +
			"JOIN keywords K ON C.uri = K.uri\n" + 
			"WHERE C.ts = K.ts\n" +
			"AND K.partid like 'ecom_prod_name:000'\n";
		int shopsNo = websites != null ? websites.size() : 0;
		if(shopsNo > 0) query1 += "AND C.fqdnurl IN (";
		for(int i=0; i<shopsNo; i++) {
			String shopName = websites.get(i);
			String shopUri = Useful.fqdnToNutchUrl(shopName);
			query1 += "'";
			query1 += shopUri;
			if(i<shopsNo-1) query1 += "',";
			else query1 += "')\n";
		}
		query1 += "AND C.ts>="+ startts +" AND C.ts <="+ endts +"\n";
		int keysNo = keywords != null ? keywords.size() : 0;
		if(keysNo > 0) query1 += "AND K.keywords IN (";
		for(int i=0; i<keysNo; i++) {
			String keyword = keywords.get(i);
			query1 += "'";
			query1 += keyword;
			if(i<keysNo-1) query1 += "',";
			else query1 += "')";
		}
		query1 += ";";

//		System.out.println(query1);
		
		QueryResults rs =  LeadsQueryInterface.execute(query1);
		if(rs != null) {
			List<String> rows = rs.getResult();
			for(String row : rows) {
				JSONObject jsonRow = new JSONObject(row);
				final String keyword = jsonRow.getString("default.k.keywords");
				String url = jsonRow.getString("default.c.uri");
				String ts  = new Long(jsonRow.getLong("default.c.ts")).toString();
				UrlTimestamp urlTimestamp = new UrlTimestamp(url, ts);
				urlsTsList.add(urlTimestamp);
				//
				Set<String> keywordSet = urlTs2Keywords.get(urlTimestamp);
				if(keywordSet==null)
					keywordSet = new HashSet<>();
				keywordSet.add(keyword);
				urlTs2Keywords.put(urlTimestamp,keywordSet);
			}
		}
		
		/*
		 * 5. Get all resources for every page
		 * 
		 *  SELECT resourceparttype, resourcepartvalue FROM leads.resourcepart
		 *  WHERE uri = uri AND ts = ts;
		 */
		int partitionSize = 10;
		List<List<UrlTimestamp>> urlTsListPartitions = new ArrayList<List<UrlTimestamp>>();
		for (int i = 0; i < urlsTsList.size(); i += partitionSize) {
			urlTsListPartitions.add(urlsTsList.subList(i, i + Math.min(partitionSize, urlsTsList.size() - i)));
		}
		String query2base = "SELECT uri AS uri, ts AS ts, resourceparttype AS resourceparttype, resourcepartvalue AS resourcepartvalue FROM resourcepart\n"
				+ "WHERE (";
		String query2end = "ORDER BY uri, ts;";
		for(List<UrlTimestamp> urlTsPart : urlTsListPartitions) {
			String query2 = query2base;
			
			for(int i=0; i<urlTsPart.size(); i++) {
				String url = urlTsPart.get(i).url;
				String ts  = urlTsPart.get(i).timestamp;
				if(i<urlTsPart.size()-1) query2 += "( uri like '"+url+"' AND ts="+ts+") OR ";
				else query2 += "( uri='"+url+"' AND ts="+ts+"))\n";
			}
			query2 += query2end;
			
//			System.out.println(query2);

			QueryResults rs2 = LeadsQueryInterface.execute(query2);
			if(rs2 != null) {
				List<String> rows2 = rs2.getResult();
				
				String lastUri = "";
				String lastTs = "";
				Fun1M36JsonResultRow resultRow = new Fun1M36JsonResultRow();
				for(String row : rows2) {
					JSONObject jsonRow = new JSONObject(row);
					String url  = jsonRow.getString("uri");
					String ts   = new Long(jsonRow.getLong("ts")).toString();
					Set<String> keywordSet = urlTs2Keywords.get(new UrlTimestamp(url, ts));
					String type = jsonRow.getString("resourceparttype");
					String value= jsonRow.getString("resourcepartvalue");
					System.out.println(url+","+ts+","+type+","+value);
					
					boolean oldSet = false;
					if(lastUri.equals(url) && lastTs.equals(ts))
						oldSet = true;
					
					if(!oldSet) {
						System.out.println(resultRow);
						if(resultRow.isParameter("Product Name")
								&& resultRow.isParameter("Product Price")) {
							resultRow.setParameter("Week",TimeConvertionUtils.timestampToWeek(new Long(ts)).toString());
							resultRow.setParameter("Shop",Useful.nutchUrlToFullyQualifiedDomainName(url));
							
							for(String keyw : keywordSet) {
								resultRow.setParameter("Category", keyw);
								resultRows.add(resultRow);
							}
						}
						resultRow = new Fun1M36JsonResultRow();
					}
					
					if(type.equals(ECOM_PROD_NAME))
						resultRow.setParameter("Product Name",value);
//					else if(type.equals(ECOM_PROD_CURR))
//						resultRow.prod_price_cur = value;
					else if(type.equals(ECOM_PROD_PRICE_MAX))
						resultRow.setParameter("Product Price",value);
//					else if(type.equals(ECOM_PROD_PRICE_MIN))
//						resultRow.prod_price_min = value;
					
					lastUri = url;
					lastTs = ts;
				}
			}
		}
		
		/*
		 * 
		 * 
		 * 
		 */
		
		return resultRows;
	}

}
