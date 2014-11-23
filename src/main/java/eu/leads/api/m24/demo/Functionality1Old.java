package eu.leads.api.m24.demo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONObject;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import eu.leads.api.com.TimeConvertionUtils;
import eu.leads.api.m24.FunctionalityAbst;
import eu.leads.api.m24.FunctionalityAbstParams;
import eu.leads.api.m24.FunctionalityAbstResultRow;
import eu.leads.api.m24.model.Functionality1AParams;
import eu.leads.api.m24.model.Functionality1Params;
import eu.leads.api.m24.model.Functionality1ResultRowAgreed;
import eu.leads.infext.Useful;
import eu.leads.infext.datastore.DataStoreSingleton;
import eu.leads.infext.datastore.datastruct.UrlTimestamp;
import eu.leads.infext.datastore.impl.LeadsDataStore;
import eu.leads.processor.web.QueryResults;

public class Functionality1Old implements FunctionalityAbst {
	
	private static String ECOM_PROD_NAME = "ecom_prod_name";
	private static String ECOM_PROD_PRICE_MAX = "ecom_prod_price_high";
	private static String ECOM_PROD_PRICE_MIN = "ecom_prod_price_low";
	private static String ECOM_PROD_CURR = "ecom_prod_currency";
	
	public  SortedSet<FunctionalityAbstResultRow> execute(FunctionalityAbstParams paramsX) {
		Functionality1Params params = (Functionality1Params) paramsX;
		SortedSet<FunctionalityAbstResultRow> resultsSet = new TreeSet<>();
		
		List<UrlTimestamp> urlsTsList = new ArrayList<>();
		
		String minTime = params.periodStart;
		String maxTime = params.periodEnd;
		
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
		
		String query1 = "SELECT C.uri, C.ts\n" +
			"FROM page_core C\n" +
			"JOIN keywords K ON C.uri = K.uri\n" + 
			"WHERE C.ts = K.ts\n" +
			"AND K.partid like 'ecom_prod_name:000'\n";
		int shopsNo = params.shopName != null ? params.shopName.size() : 0;
		if(shopsNo > 0) query1 += "AND C.fqdnurl IN (";
		for(int i=0; i<shopsNo; i++) {
			String shopName = params.shopName.get(i);
			String shopUri = Useful.fqdnToNutchUrl(shopName);
			query1 += "'";
			query1 += shopUri;
			if(i<shopsNo-1) query1 += "',";
			else query1 += "')\n";
		}
		query1 += "AND C.ts>="+ minTime +" AND C.ts <="+ maxTime +"\n";
		int keysNo = params.keywords != null ? params.keywords.size() : 0;
		if(keysNo > 0) query1 += "AND K.keywords IN (";
		for(int i=0; i<keysNo; i++) {
			String keywords = params.keywords.get(i);
			query1 += "'";
			query1 += keywords;
			if(i<keysNo-1) query1 += "',";
			else query1 += "')";
		}
		query1 += ";";

//		System.out.println(query1);
		
		QueryResults rs = LeadsDataStore.send_query_and_wait(query1);
		if(rs != null) {
			List<String> rows = rs.getResult();
			for(String row : rows) {
				JSONObject jsonRow = new JSONObject(row);
				String url = jsonRow.getString("default.c.uri");
				String ts  = new Long(jsonRow.getLong("default.c.ts")).toString();
				urlsTsList.add(new UrlTimestamp(url, ts));
			}
		}
		
		/*
		 * 5. Get all resources for every page
		 * 
		 *  SELECT resourceparttype, resourcepartvalue FROM leads.resourcepart
		 *  WHERE uri = uri AND ts = ts;
		 */
		for(UrlTimestamp urlTs : urlsTsList) {
			String url = urlTs.url;
			String ts  = urlTs.timestamp;
			
			Functionality1ResultRowAgreed resultRow = new Functionality1ResultRowAgreed();

			String query2 = "SELECT resourceparttype AS resourceparttype, resourcepartvalue AS resourcepartvalue FROM resourcepart\n"
					+ "WHERE uri='"+url+"'\n"
					+ "AND ts="+ts+";";

//			System.out.println(query2);

			QueryResults rs2 = LeadsDataStore.send_query_and_wait(query2);
			if(rs2 != null) {
				List<String> rows2 = rs2.getResult();
				for(String row : rows2) {
					JSONObject jsonRow = new JSONObject(row);
					String type = jsonRow.getString("resourceparttype");
					String value= jsonRow.getString("resourcepartvalue");
					if(type.equals(ECOM_PROD_NAME))
						resultRow.prod_name = value;
					else if(type.equals(ECOM_PROD_CURR))
						resultRow.prod_price_cur = value;
					else if(type.equals(ECOM_PROD_PRICE_MAX))
						resultRow.prod_price_max = value;
					else if(type.equals(ECOM_PROD_PRICE_MIN))
						resultRow.prod_price_min = value;
				}
			}
			
			if(resultRow.prod_name != null
					&& resultRow.prod_price_cur != null
					&& resultRow.prod_price_max != null
					&& resultRow.prod_price_min != null) {
				resultRow.day = TimeConvertionUtils.timestampToDay(new Long(ts)).toString();
				resultRow.shop_name = Useful.nutchUrlToFullyQualifiedDomainName(url);
				resultsSet.add(resultRow);
			}
		}
		
		return resultsSet;
	}
	
	////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
		LeadsDataStore.initialize("http://clu25.softnet.tuc.gr", 8080);
		Functionality1Old func1 = new Functionality1Old();
		Functionality1Params params = new Functionality1Params();
		//params.shopName = new ArrayList<String>() {{ add("www.holabirdsports.com"); add("www.wiggle.com"); }};
		params.periodStart = new Long(0L).toString();
		params.periodEnd = new Long(new Date().getTime()).toString();
		//params.keywords = new ArrayList<String>() {{ add("adidas Boost"); add("nike free"); }};
		//params.keywords = new ArrayList<String>() {{ add("adidas"); }};
		Set<FunctionalityAbstResultRow> rows = func1.execute(params);
		int i=0;
		for(FunctionalityAbstResultRow row : rows) {
			System.out.println(i + ": " + row);
			i++;
		}
	}
}













