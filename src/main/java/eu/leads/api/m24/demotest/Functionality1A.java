package eu.leads.api.m24.demotest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONObject;

import com.datastax.driver.core.Session;

import eu.leads.api.com.TimeConvertionUtils;
import eu.leads.api.m24.FunctionalityAbst;
import eu.leads.api.m24.FunctionalityAbstParams;
import eu.leads.api.m24.FunctionalityAbstResultRow;
import eu.leads.api.m24.model.Functionality1AParams;
import eu.leads.api.m24.model.Functionality1AReturnRow;
import eu.leads.infext.Useful;
import eu.leads.infext.datastore.DataStoreSingleton;
import eu.leads.infext.datastore.impl.LeadsDataStore;
import eu.leads.processor.web.QueryResults;

public class Functionality1A implements FunctionalityAbst {

	private static String ECOM_PROD_NAME = "ecom_prod_name";
	private static String ECOM_PROD_PRICE_MAX = "ecom_prod_price_high";
	private static String ECOM_PROD_PRICE_MIN = "ecom_prod_price_low";
	private static String ECOM_PROD_CURR = "ecom_prod_currency";
	
	///////////////////////////////////////////////////////////////////
	
	static public class ShopKeywordRow {

		public String shop_name = null;
		public String country = null;
		public String keyword = null;
		public Long week = null;
				
		@Override
		public String toString() {
			return "ShopKeywordRow [shop_name=" + shop_name + ", country="
					+ country + ", keyword=" + keyword + ", week=" + week + "]";
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((country == null) ? 0 : country.hashCode());
			result = prime * result
					+ ((keyword == null) ? 0 : keyword.hashCode());
			result = prime * result
					+ ((shop_name == null) ? 0 : shop_name.hashCode());
			result = prime * result + ((week == null) ? 0 : week.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ShopKeywordRow other = (ShopKeywordRow) obj;
			if (country == null) {
				if (other.country != null)
					return false;
			} else if (!country.equals(other.country))
				return false;
			if (keyword == null) {
				if (other.keyword != null)
					return false;
			} else if (!keyword.equals(other.keyword))
				return false;
			if (shop_name == null) {
				if (other.shop_name != null)
					return false;
			} else if (!shop_name.equals(other.shop_name))
				return false;
			if (week == null) {
				if (other.week != null)
					return false;
			} else if (!week.equals(other.week))
				return false;
			return true;
		}
		
	}	
	
	public static class UrlTsFqdnKeyword {
		public UrlTsFqdnKeyword(String url2, String ts2, String fqdn2, String keywords2) {
			url = url2;
			ts = ts2;
			fqdn = fqdn2;
			keywords = keywords2;
		}
		public String url = null;
		public String ts = null;
		public String fqdn = null;
		public String keywords = null;
	}
	
	///////////////////////////////////////////////////////////////////
	
	public  SortedSet<FunctionalityAbstResultRow> execute(FunctionalityAbstParams paramsX) {
		Functionality1AParams params = (Functionality1AParams) paramsX;
		SortedSet<FunctionalityAbstResultRow> resultsSet = new TreeSet<>();
		
		/*
		 * 1. Filter pages with resource_type = ecom_prod_name
		 * 2. Filter by keywords
		 */
		HashMap<ShopKeywordRow,Long> keywordRows = new HashMap<>();
		
		List<UrlTsFqdnKeyword> urlsTsFqdnKeywordList = new ArrayList<>();

		String query1 = "SELECT uri AS uri, ts AS ts, keywords AS keywords\n" +
				"FROM keywords\n" + 
				"WHERE partid like 'ecom_prod_name:000'\n"	+
				"AND ts>="+ params.periodStart +" AND ts <="+ params.periodEnd +"\n";
		int keysNo = params.keywords != null ? params.keywords.size() : 0;
		if(keysNo > 0) query1 += "AND keywords IN (";
		for(int i=0; i<keysNo; i++) {
			String keywords = params.keywords.get(i);
			query1 += "'" + keywords;
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
				String url = jsonRow.getString("uri");
				String ts  = new Long(jsonRow.getLong("ts")).toString();
				String keywords = jsonRow.getString("keywords");
				urlsTsFqdnKeywordList.add(new UrlTsFqdnKeyword(url, ts, Useful.nutchUrlToFullyQualifiedDomainNameUrl(url), keywords));
			}
		}		
			
		/*
		 * 3. Group by siteName
		 * 4. Find sites from requested countries
		 * 
		 *  SELECT country FROM leads.site 
		 *  WHERE uri = fqdnurl ORDER BY ts DESC LIMIT 1;
		 */
		HashMap<String,Set<UrlTsFqdnKeyword>> sitesAndUrls = new HashMap<>();
		for(UrlTsFqdnKeyword obj : urlsTsFqdnKeywordList) {
			String site = obj.fqdn;
			Set<UrlTsFqdnKeyword> urlsTsSet = sitesAndUrls.get(site);
			if(urlsTsSet == null)
				urlsTsSet = new HashSet<>();
			urlsTsSet.add(obj);
			sitesAndUrls.put(site, urlsTsSet);
		}
		
		HashMap<String,String> sitesCountries = new HashMap<>();
		for(String site : sitesAndUrls.keySet()) {
			String query2 = "SELECT country AS country FROM site\n" +
				"WHERE uri = '"+ site +"' ORDER BY ts DESC LIMIT 1";
			
//			System.out.println(query2);
			
			QueryResults rs2 = LeadsDataStore.send_query_and_wait(query2);
			if(rs2 != null && rs2.getResult().size() == 1) {
				String row = rs2.getResult().get(0);
				JSONObject jsonRow = new JSONObject(row);
				String countryCode = jsonRow.getString("country");
				if(params.country.contains(countryCode))
					sitesCountries.put(site, countryCode);
			}	
		}
		
		/*
		 * 5. Check whether resource "ecom_prod_name" exists. If yes, 
		 */
		for(String site : sitesCountries.keySet()) {
			for(String keyword : params.keywords) {
				HashMap<String,List<Long>> productVersionsMap = new HashMap<>();
				for(UrlTsFqdnKeyword urlTs : sitesAndUrls.get(site)) {
					String url = urlTs.url;
					String timestamp = urlTs.ts;
					
					if(urlTs.keywords.equals(keyword)) {
						String query3 = "SELECT resourcepartvalue AS resourcepartvalue FROM resourcepart\n"
								+ "WHERE uri='"+url+"'\n"
								+ "AND ts="+timestamp+"\n"
								+ "AND partid='000'\n"
								+ "AND resourceparttype='"+ECOM_PROD_NAME+"';";		
						
						System.out.println(query3);
						QueryResults rs3 = LeadsDataStore.send_query_and_wait(query3);
						if(rs3 != null && rs3.getResult().size() == 1) {
							String row = rs3.getResult().get(0);
							JSONObject jsonRow = new JSONObject(row);
							String prodName = jsonRow.getString("resourcepartvalue");
							List<Long> productVersions = productVersionsMap.get(prodName);
							if(productVersions==null)
								productVersions = new ArrayList<>();
							productVersions.add(Long.parseLong(timestamp));
							productVersionsMap.put(prodName, productVersions);
						}
					}
				}
				
				for(String prodName : productVersionsMap.keySet()) {
					String shop_name = Useful.nutchUrlToFullyQualifiedDomainName(site);
					System.out.println(shop_name);
					for(Long timestamp : productVersionsMap.get(prodName)) {
						ShopKeywordRow resultRow = new ShopKeywordRow();
						resultRow.country = sitesCountries.get(site);
						resultRow.keyword = keyword;
						resultRow.shop_name = shop_name;
						resultRow.week = TimeConvertionUtils.timestampToWeek(timestamp);
						System.out.println("### "+resultRow);
						
						// Get count for this tuple
						Long count = keywordRows.get(resultRow);
						System.out.println("--- "+count);
						if (count == null)
							keywordRows.put(resultRow, 1L);
						else
							keywordRows.put(resultRow, count + 1L);
						System.out.println("+++ "+keywordRows);
					}
				}
			}
		}
		
		for(Entry<ShopKeywordRow, Long> row : keywordRows.entrySet()) {
			Functionality1AReturnRow retRow = new Functionality1AReturnRow();
			retRow.country_code = row.getKey().country;
			retRow.keyword = row.getKey().keyword;
			retRow.shop_name = row.getKey().shop_name;
			retRow.products_no = row.getValue();
			retRow.week = row.getKey().week;
			System.out.println(">>> "+row);
			System.out.println("<<< "+retRow);
			resultsSet.add(retRow);
		}
		
		return resultsSet;
	}
	
	////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
		Functionality1A func1A = new Functionality1A();
		Functionality1AParams params = new Functionality1AParams();
		params.country = new ArrayList<String>() {{ add("US"); add("UK"); }};
		params.periodStart = new Long(0L).toString();
		params.periodEnd = new Long(new Date().getTime()).toString();
		params.keywords = new ArrayList<String>() {{ add("adidas"); add("nike"); }};
		SortedSet<FunctionalityAbstResultRow> rows = func1A.execute(params);
		int i=0;
		for(FunctionalityAbstResultRow row : rows) {
			System.out.println(i + ": " + row);
			i++;
		}
	}	
	
}
