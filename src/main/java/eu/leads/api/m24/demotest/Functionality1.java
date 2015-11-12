package eu.leads.api.m24.demotest;

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
import eu.leads.processor.web.QueryResults;
import eu.leads.datastore.DataStoreSingleton;
import eu.leads.datastore.datastruct.StringPair;
import eu.leads.datastore.datastruct.StringPair;
import eu.leads.datastore.impl.LeadsDataStore;
import eu.leads.datastore.impl.LeadsQueryInterface;

public class Functionality1 implements FunctionalityAbst {
	
	private static String ECOM_PROD_NAME = "ecom_prod_name";
	private static String ECOM_PROD_PRICE_MAX = "ecom_prod_price_high";
	private static String ECOM_PROD_PRICE_MIN = "ecom_prod_price_low";
	private static String ECOM_PROD_CURR = "ecom_prod_currency";
	
	public  SortedSet<FunctionalityAbstResultRow> execute(FunctionalityAbstParams paramsX) {
		Functionality1Params params = (Functionality1Params) paramsX;
		SortedSet<FunctionalityAbstResultRow> resultsSet = new TreeSet<>();
		
		List<StringPair> urlsTsList = new ArrayList<>();
		List<StringPair> urlsTsList2= new ArrayList<>();
		
		/*
		 * 1. Get all pages of the shop
		 * 2. Filter by day
		 */
		String minTime = params.periodStart;
		String maxTime = params.periodEnd;
		String query1 = "SELECT uri AS uri, ts AS ts FROM page_core\n"
				+ " WHERE ts>="+minTime+" AND ts<="+maxTime+" AND fqdnurl IN (";
		int i;
		for(i=0; i<params.shopName.size()-1;i++)
			query1 += "'" + Useful.fqdnToNutchUrl(params.shopName.get(i)) + "',";
		query1 += "'" + Useful.fqdnToNutchUrl(params.shopName.get(i)) + "');";
		
		QueryResults rs = LeadsQueryInterface.execute(query1);
		if(rs != null) {
			List<String> rows = rs.getResult();
			for(String row : rows) {
				JSONObject jsonRow = new JSONObject(row);
				String url = jsonRow.getString("uri");
				String ts  = new Long(jsonRow.getLong("ts")).toString();
				urlsTsList.add(new StringPair(url, ts));
			}
		}
		
		/*
		 * 3. Filter those with resource_type = ecom_prod_name
		 * 4. Filter by keywords
		 */
		List<String> keywords = params.keywords;
		String query2 = "SELECT uri AS uri, ts AS ts, partid FROM keywords\n"
				+ "WHERE partid='"+ECOM_PROD_NAME+":000'\n"
				+ "AND keywords IN (";
		for(i=0; i<keywords.size()-1;i++)
			query2 += "'" + keywords.get(i) + "',";
		query2 += "'" + keywords.get(i) + "')\n";	
		query2 += "AND ( ";
		
		for(i=0; i<urlsTsList.size(); i++) {
			String url = urlsTsList.get(i).str1;
			String ts  = urlsTsList.get(i).str2;
			if(i<urlsTsList.size()-1) query2 += "( uri like '"+url+"' AND ts="+ts+") OR ";
			else query2 += "( uri='"+url+"' AND ts='"+ts+"));";
			
		}
		
		QueryResults rs2 = LeadsQueryInterface.execute(query2);
		if(rs2 != null) {
			List<String> rows = rs2.getResult();
			for(String row : rows) {
				JSONObject jsonRow = new JSONObject(row);
				String url = jsonRow.getString("uri");
				String ts  = new Long(jsonRow.getLong("ts")).toString();
				urlsTsList2.add(new StringPair(url, ts));
			}
		}
		
		/*
		 * 5. Get all resources for every page
		 */
		String query3 = "SELECT uri AS uri, ts AS ts, resourceparttype AS resourceparttype, resourcepartvalue AS resourcepartvalue FROM resourcepart\n"
				+ "WHERE ";
		
		for(i=0; i<urlsTsList2.size(); i++) {
			String url = urlsTsList.get(i).str1;
			String ts  = urlsTsList.get(i).str2;
			if(i<urlsTsList.size()-1) query2 += "( uri like '"+url+"' AND ts="+ts+") OR ";
			else query2 += "( uri='"+url+"' AND ts='"+ts+"));";
			
		}
		
		QueryResults rs3 = LeadsQueryInterface.execute(query3);
		if(rs3 != null) {
			List<String> rows = rs3.getResult();
			for(String row : rows) {
				JSONObject jsonRow = new JSONObject(row);
				Functionality1ResultRowAgreed resultRow = new Functionality1ResultRowAgreed();
				String url  = jsonRow.getString("uri");
				String ts   = jsonRow.getString("ts");
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
				
				if(resultRow.prod_name != null
						&& resultRow.prod_price_cur != null
						&& resultRow.prod_price_max != null
						&& resultRow.prod_price_min != null) {
					resultRow.day = TimeConvertionUtils.timestampToDay(new Long(ts)).toString();
					resultRow.shop_name = Useful.nutchUrlToFullyQualifiedDomainName(url);
					resultsSet.add(resultRow);
				}
			}
		}
		
		return resultsSet;
	}
	
	////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
		LeadsQueryInterface.initialize("http://5.147.254.199", 8080);
		Functionality1 func1 = new Functionality1();
		Functionality1Params params = new Functionality1Params();
		params.shopName = new ArrayList<String>() {{ add("www.holabirdsports.com"); add("www.wiggle.com"); }};
		params.periodStart = new Long(0L).toString();
		params.periodEnd = new Long(new Date().getTime()).toString();
		params.keywords = new ArrayList<String>() {{ add("adidas"); add("nike"); }};
//		params.keywords = new ArrayList<String>() {{ add("adidas"); }};
		Set<FunctionalityAbstResultRow> rows = func1.execute(params);
		int i=0;
		for(FunctionalityAbstResultRow row : rows) {
			System.out.println(i + ": " + row);
			i++;
		}
	}
}












