package eu.leads.api.m24.func;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
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
import eu.leads.api.m24.model.Functionality1AReturnRow;
import eu.leads.infext.Useful;
import eu.leads.infext.datastore.DataStoreSingleton;
import eu.leads.infext.datastore.datastruct.UrlTimestamp;

public class Functionality1A extends FunctionalityAbst {

	private static String ECOM_PROD_NAME = "ecom_prod_name";
	private static String ECOM_PROD_PRICE_MAX = "ecom_prod_price_high";
	private static String ECOM_PROD_PRICE_MIN = "ecom_prod_price_low";
	private static String ECOM_PROD_CURR = "ecom_prod_currency";
	
	///////////////////////////////////////////////////////////////////
	
	static public class ShopKeywordRow {

		public String shop_name = null;
		public String country = null;
		public Long week = null;
				
		@Override
		public String toString() {
			return "ShopKeywordRow [shop_name=" + shop_name + ", country="
					+ country + ", week=" + week + "]";
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((country == null) ? 0 : country.hashCode());
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
	
	///////////////////////////////////////////////////////////////////
	
	public  SortedSet<FunctionalityAbstResultRow> execute(FunctionalityAbstParams paramsX) {
		Functionality1AParams params = (Functionality1AParams) paramsX;
		SortedSet<FunctionalityAbstResultRow> resultsSet = new TreeSet<>();
		
		Session session = (Session) DataStoreSingleton.getDataStore().getFamilyStorageHandle(null);
		
		/*
		 * 1. Filter pages with resource_type = ecom_prod_name
		 * 2. Filter by keywords
		 */
		List<String> keywords = params.keywords;
		
		for(String keyword : keywords) {
			HashMap<ShopKeywordRow,Long> keywordRows = new HashMap<>();
			
			List<UrlTimestamp> urlsTsList = new ArrayList<>();
			
			String query1 = "SELECT uri, ts FROM leads.keywords\n"
					+ "WHERE partid='"+ECOM_PROD_NAME+":000'\n"
					+ "AND keywords = '"+keyword+"' ALLOW FILTERING;";		
			System.out.println(query1);
			ResultSet rs = session.execute(query1);
			for(Row row : rs)
				urlsTsList.add(new UrlTimestamp(row.getString(0), new Long(row.getLong(1)).toString()));		
			
			/*
			 * 3. Group by siteName
			 */
			HashMap<String,Set<UrlTimestamp>> sitesAndUrls = new HashMap<>();
			for(UrlTimestamp urlTs : urlsTsList) {
				String site = Useful.nutchUrlToFullyQualifiedDomainNameUrl(urlTs.url);
				Set<UrlTimestamp> urlsTsSet = sitesAndUrls.get(site);
				if(urlsTsSet == null)
					urlsTsSet = new HashSet<>();
				urlsTsSet.add(urlTs);
				sitesAndUrls.put(site, urlsTsSet);
			}
			
			/*
			 * 4. Find sites from requested countries
			 */
			HashMap<String,String> sitesCountries = new HashMap<>();
			for(String site : sitesAndUrls.keySet()) {
				String query2 = "SELECT country FROM leads.site\n"
						+ "WHERE uri='"+site+"' ORDER BY ts DESC LIMIT 1;";		
				System.out.println(query2);
				ResultSet rs2 = session.execute(query2);
				if(rs2.iterator().hasNext()) {
					Row row = rs2.iterator().next();
					String countryCode = row.getString(0);
					if(params.country.contains(countryCode))
						sitesCountries.put(site, countryCode);
				}	
			}
			
			/*
			 * 5. 
			 */
			for(String site : sitesCountries.keySet()) {
				HashMap<String,List<Long>> productVersionsMap = new HashMap<>();
				for(UrlTimestamp urlTs : sitesAndUrls.get(site)) {
					String url = urlTs.url;
					String timestamp = urlTs.timestamp;
					String query3 = "SELECT resourcepartvalue FROM leads.resourcepart\n"
							+ "WHERE uri='"+url+"'\n"
							+ "AND ts="+timestamp+"\n"
							+ "AND partid='000'\n"
							+ "AND resourceparttype='"+ECOM_PROD_NAME+"';";		
					System.out.println(query3);
					ResultSet rs3 = session.execute(query3);	
					if(rs3.iterator().hasNext()) {
						Row row = rs3.iterator().next();
						String prodName = row.getString(0);
						List<Long> productVersions = productVersionsMap.get(prodName);
						if(productVersions==null)
							productVersions = new ArrayList<>();
						productVersions.add(Long.parseLong(timestamp));
						productVersionsMap.put(prodName, productVersions);
					}
				}
				
				for(String prodName : productVersionsMap.keySet()) {
					String shop_name = Useful.nutchUrlToFullyQualifiedDomainName(site);
					System.out.println(shop_name);
					for(Long timestamp : productVersionsMap.get(prodName)) {
						ShopKeywordRow resultRow = new ShopKeywordRow();
						resultRow.country = sitesCountries.get(site);
						resultRow.shop_name = shop_name;
						resultRow.week = TimeConvertionUtils.timestampToWeek(timestamp);
						System.out.println("### "+resultRow);
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
			
			for(Entry<ShopKeywordRow, Long> row : keywordRows.entrySet()) {
				Functionality1AReturnRow retRow = new Functionality1AReturnRow();
				retRow.country_code = row.getKey().country;
				retRow.keyword = keyword;
				retRow.shop_name = row.getKey().shop_name;
				retRow.products_no = row.getValue();
				retRow.week = row.getKey().week;
				System.out.println(">>> "+row);
				System.out.println("<<< "+retRow);
				resultsSet.add(retRow);
			}
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
