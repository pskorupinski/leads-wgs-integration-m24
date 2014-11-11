package eu.leads.api.m24.func;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import eu.leads.api.com.TimeConvertionUtils;
import eu.leads.infext.Useful;
import eu.leads.infext.datastore.DataStoreSingleton;
import eu.leads.infext.datastore.datastruct.UrlTimestamp;

public class Functionality1 {
	
	private static String ECOM_PROD_NAME = "ecom_prod_name";
	private static String ECOM_PROD_PRICE_MAX = "ecom_prod_price_high";
	private static String ECOM_PROD_PRICE_MIN = "ecom_prod_price_low";
	private static String ECOM_PROD_CURR = "ecom_prod_currency";
	
	static public class Functionality1Params {
		public List<String> shopName;
		public List<String> keywords;
		public String periodStart;
		public String periodEnd;
	}
	
	static public class Functionality1ResultRowAgreed {
		public String shop_name = null;
		public String prod_name = null;
		public String day = null;
		public String prod_price_min = null;
		public String prod_price_max = null;
		public String prod_price_cur = null;
		
		@Override
		public String toString() {
			return "Functionality1ResultRowAgreed [shop_name=" + shop_name
					+ ", prod_name=" + prod_name + ", day=" + day
					+ ", prod_price_min=" + prod_price_min
					+ ", prod_price_max=" + prod_price_max
					+ ", prod_price_cur=" + prod_price_cur + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((prod_name == null) ? 0 : prod_name.hashCode());
			result = prime
					* result
					+ ((prod_price_cur == null) ? 0 : prod_price_cur.hashCode());
			result = prime
					* result
					+ ((prod_price_max == null) ? 0 : prod_price_max.hashCode());
			result = prime
					* result
					+ ((prod_price_min == null) ? 0 : prod_price_min.hashCode());
			result = prime * result
					+ ((shop_name == null) ? 0 : shop_name.hashCode());
			result = prime * result
					+ ((day == null) ? 0 : day.hashCode());
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
			Functionality1ResultRowAgreed other = (Functionality1ResultRowAgreed) obj;
			if (prod_name == null) {
				if (other.prod_name != null)
					return false;
			} else if (!prod_name.equals(other.prod_name))
				return false;
			if (prod_price_cur == null) {
				if (other.prod_price_cur != null)
					return false;
			} else if (!prod_price_cur.equals(other.prod_price_cur))
				return false;
			if (prod_price_max == null) {
				if (other.prod_price_max != null)
					return false;
			} else if (!prod_price_max.equals(other.prod_price_max))
				return false;
			if (prod_price_min == null) {
				if (other.prod_price_min != null)
					return false;
			} else if (!prod_price_min.equals(other.prod_price_min))
				return false;
			if (shop_name == null) {
				if (other.shop_name != null)
					return false;
			} else if (!shop_name.equals(other.shop_name))
				return false;
			if (day == null) {
				if (other.day != null)
					return false;
			} else if (!day.equals(other.day))
				return false;
			return true;
		}
		
		
	}	
	
	public Set<Functionality1ResultRowAgreed> execute(Functionality1Params params) {
		Set<Functionality1ResultRowAgreed> resultsSet = new HashSet<>();
		
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
		Set<Functionality1ResultRowAgreed> rows = func1.execute(params);
		int i=0;
		for(Functionality1ResultRowAgreed row : rows) {
			System.out.println(i + ": " + row);
			i++;
		}
	}
}












