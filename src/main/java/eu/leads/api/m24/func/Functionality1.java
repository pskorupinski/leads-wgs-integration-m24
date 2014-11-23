package eu.leads.api.m24.func;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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

public class Functionality1 implements FunctionalityAbst {
	
	private static String ECOM_PROD_NAME = "ecom_prod_name";
	private static String ECOM_PROD_PRICE_MAX = "ecom_prod_price_high";
	private static String ECOM_PROD_PRICE_MIN = "ecom_prod_price_low";
	private static String ECOM_PROD_CURR = "ecom_prod_currency";
	
	public  SortedSet<FunctionalityAbstResultRow> execute(FunctionalityAbstParams paramsX) {
		Functionality1Params params = (Functionality1Params) paramsX;
		SortedSet<FunctionalityAbstResultRow> resultsSet = new TreeSet<>();
		
		Session session = (Session) DataStoreSingleton.getDataStore().getFamilyStorageHandle(null);
		
		List<UrlTimestamp> urlsTsList = new ArrayList<>();
		List<UrlTimestamp> urlsTsList2= new ArrayList<>();
		
		/*
		 * 1. Get all pages of the shop
		 * 2. Filter by day
		 */
		for(String shopName : params.shopName) {
			String shopUri = Useful.fqdnToNutchUrl(shopName);
			String minTime = params.periodStart;
			String maxTime = params.periodEnd;
			
			String query1 = "SELECT uri, ts FROM leads.page_core WHERE fqdnurl='"+shopUri+"'"
					+ " AND ts>="+minTime+" AND ts<="+maxTime+" ALLOW FILTERING;";
//			System.out.println(query1);
			ResultSet rs = session.execute(query1);
			for(Row row : rs) {
				String url = row.getString(0);
				String ts  = new Long(row.getLong(1)).toString();
				urlsTsList.add(new UrlTimestamp(url, ts));
			}
		}
		
		/*
		 * 3. Filter those with resource_type = ecom_prod_name
		 * 4. Filter by keywords
		 */
		int x = 0, y=0;
		for(UrlTimestamp urlTs : urlsTsList) {
			String url = urlTs.url;
			String ts  = urlTs.timestamp;
			List<String> keywords = params.keywords;
			
			String query2 = "SELECT uri, ts, partid FROM leads.keywords\n"
					+ "WHERE uri='"+url+"'\n"
					//+ "AND ts="+ts+"\n"
					+ "AND partid='"+ECOM_PROD_NAME+":000'\n"
					//+ "AND keywords IN (";
					+ "AND keywords = ";
			int i;
//			for(i=0; i<keywords.size()-1;i++)
//				query2 += "'" + keywords.get(i) + "',";
//			query2 += "'" + keywords.get(i) + "') ALLOW FILTERING;";	
			for(i=0; i<keywords.size();i++) {
//				query2 += "'" + keywords.get(i) + "',";
//				query2 += "'" + keywords.get(i) + "') ALLOW FILTERING;";
				//System.out.println(query2);
				ResultSet rs = session.execute(query2 + "'" + keywords.get(i) + "' ALLOW FILTERING;");
				int z = 0;
				for(Row row : rs) {
					url = row.getString(0);
					ts  = new Long(row.getLong(1)).toString();
					urlsTsList2.add(new UrlTimestamp(url, ts));
					z++;
				}
			}
//			if(z>0) System.out.println("#### "+z+" "+url);
//			y++;
//			if(y%500==0) {
//				System.out.println(y);
//			}
		}
//		System.out.println("###### "+x);
		
		/*
		 * 5. Get all resources for every page
		 */
		for(UrlTimestamp urlTs : urlsTsList2) {
			String url = urlTs.url;
			String ts  = urlTs.timestamp;
			
			Functionality1ResultRowAgreed resultRow = new Functionality1ResultRowAgreed();
			
			String query3 = "SELECT resourceparttype, resourcepartvalue FROM leads.resourcepart\n"
					+ "WHERE uri='"+url+"'\n"
					+ "AND ts="+ts+";";
//			System.out.println(query3);
			ResultSet rs = session.execute(query3);	
			
			for(Row row : rs) {
				String type = row.getString(0);
				String value= row.getString(1);
				if(type.equals(ECOM_PROD_NAME))
					resultRow.prod_name = value;
				else if(type.equals(ECOM_PROD_CURR))
					resultRow.prod_price_cur = value;
				else if(type.equals(ECOM_PROD_PRICE_MAX))
					resultRow.prod_price_max = value;
				else if(type.equals(ECOM_PROD_PRICE_MIN))
					resultRow.prod_price_min = value;
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
		Functionality1 func1 = new Functionality1();
		Functionality1Params params = new Functionality1Params();
		params.shopName = new ArrayList<String>() {{ add("www.holabirdsports.com"); add("www.wiggle.com"); }};
		params.periodStart = new Long(new Date().getTime()-100*1000*60*60*24L).toString();
		params.periodEnd = new Long(new Date().getTime()).toString();
		//params.keywords = new ArrayList<String>() {{ add("adidas Boost"); add("nike free"); }};
		params.keywords = new ArrayList<String>() {{ add("adidas"); }};
		Set<FunctionalityAbstResultRow> rows = func1.execute(params);
		int i=0;
		for(FunctionalityAbstResultRow row : rows) {
			System.out.println(i + ": " + row);
			i++;
		}
	}
}












