package eu.leads.api.m24.demotest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONObject;

import eu.leads.api.com.TimeConvertionUtils;
import eu.leads.api.m24.FunctionalityAbst;
import eu.leads.api.m24.FunctionalityAbstParams;
import eu.leads.api.m24.FunctionalityAbstResultRow;
import eu.leads.api.m24.model.Functionality2Params;
import eu.leads.api.m24.model.Functionality2ResultRow;
import eu.leads.datastore.impl.LeadsQueryInterface;
import eu.leads.infext.Useful;
import eu.leads.infext.datastore.impl.LeadsDataStore;
import eu.leads.processor.web.QueryResults;

public class Functionality2 implements FunctionalityAbst {
	
	private static String ARTICLE = "article_content";
	

	static public class KeywordSiteWeekRows {
		public String site = null;
		public String keywords = null;
		public Long   week = null;
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((keywords == null) ? 0 : keywords.hashCode());
			result = prime * result + ((site == null) ? 0 : site.hashCode());
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
			KeywordSiteWeekRows other = (KeywordSiteWeekRows) obj;
			if (keywords == null) {
				if (other.keywords != null)
					return false;
			} else if (!keywords.equals(other.keywords))
				return false;
			if (site == null) {
				if (other.site != null)
					return false;
			} else if (!site.equals(other.site))
				return false;
			if (week == null) {
				if (other.week != null)
					return false;
			} else if (!week.equals(other.week))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "KeywordSiteWeekRows [url=" + site + ", keywords="
					+ keywords + ", week=" + week + "]";
		}
	}
	
	static public class KeywordRow {
		public String url = null;
		public String keywords = null;
		public String sentiment = null;
		public String relevance = null;
		public Long   timestamp = null;
		
		@Override
		public String toString() {
			return "KeywordRow [url=" + url
					+ ", keywords=" + keywords + ", sentiment=" + sentiment
					+ ", relevance=" + relevance + ", timestamp=" + timestamp
					+ "]";
		}
	}
	
	public 	SortedSet<FunctionalityAbstResultRow> execute(FunctionalityAbstParams paramsX) {
		Functionality2Params params = (Functionality2Params) paramsX;
		SortedSet<FunctionalityAbstResultRow> resultsSet = new TreeSet<>();
		Map<KeywordSiteWeekRows,Map<String,List<KeywordRow>>> keywordSiteWeekRowsMap 
			= new HashMap<Functionality2.KeywordSiteWeekRows, Map<String,List<KeywordRow>>>();
		
		Set<KeywordRow> transientResultsSet = new HashSet<>();
		
		/*
		 * 1. Filter pages with resource_type = article_content
		 * 2. Filter by keywords
		 * 3. Filter by language
		 * 
		 * SELECT uri, ts, sentiment, relevance 
		 * FROM leads.keywords K
		 * JOIN leads.page_core C ON C.uri = K.uri
		 * WHERE C.ts = K.ts
		 * AND K.partid like ‘article:000’
		 * AND C.lang like language
		 * AND C.ts>=minTime AND C.ts <= maxTime
		 * AND (K.keywords like keywords1 OR K.keywords like keywords2 OR …);
		 */
		
		String query1 = "SELECT K.uri, K.ts, K.keywords AS keywords, "
				+ "K.sentiment AS sentiment, K.relevance AS relevance\n"
				+ "FROM keywords K\n"
				+ "JOIN page_core C ON C.uri = K.uri\n"
				+ "WHERE C.ts = K.ts\n"
				+ "AND K.partid like '"+ARTICLE+":000'\n"
				+ "AND C.lang like '"+ params.language +"'\n"
				+ "AND K.ts>="+ params.periodStart +" AND K.ts <="+ params.periodEnd +"\n"
				+ "AND keywords IN (";
		int keysNo = params.keywords.size();
		for(int i=0; i<keysNo; i++) {
			String keywords = params.keywords.get(i);
			query1 += "'";
			query1 += keywords;
			if(i<keysNo-1) query1 += "',";
			else query1 += "')";
		}
		query1 += ";";
		
//		System.out.println(query1);
		
		QueryResults rs = LeadsQueryInterface.execute(query1);
		if(rs != null) {
			List<String> rows = rs.getResult();
			for(String row : rows) {
				JSONObject jsonRow = new JSONObject(row);
				KeywordRow tempRow = new KeywordRow();
				tempRow.url = jsonRow.getString("default.k.uri");
				tempRow.timestamp  = new Long(jsonRow.getLong("default.k.ts"));
				tempRow.keywords = jsonRow.getString("keywords");
				tempRow.sentiment = jsonRow.getString("sentiment");
				tempRow.relevance = jsonRow.getString("relevance");
				transientResultsSet.add(tempRow);
			}
		}

		
		for(KeywordRow transientRow : transientResultsSet) {
			String site = Useful.nutchUrlToFullyQualifiedDomainName(transientRow.url);
			
			KeywordSiteWeekRows keywordSiteWeekRows = new KeywordSiteWeekRows();
			keywordSiteWeekRows.keywords = transientRow.keywords;
			keywordSiteWeekRows.site     = site;
			keywordSiteWeekRows.week     = TimeConvertionUtils.timestampToWeek(transientRow.timestamp);
			
			Map<String, List<KeywordRow>> urlMap = keywordSiteWeekRowsMap.get(keywordSiteWeekRows);
			if(urlMap == null) urlMap = new HashMap<>();
			List<KeywordRow> list = urlMap.get(transientRow.url);
			if(list == null) list = new ArrayList<>();
			
			list.add(transientRow);
			urlMap.put(transientRow.url, list);
			keywordSiteWeekRowsMap.put(keywordSiteWeekRows, urlMap);
		}
		
		for(Entry<KeywordSiteWeekRows, Map<String, List<KeywordRow>>> entry : keywordSiteWeekRowsMap.entrySet()) {
			System.out.println(entry);
			KeywordSiteWeekRows keywordSiteWeek = entry.getKey();
			Map<String, List<KeywordRow>> urlsMap = entry.getValue();
			
			Double sentimentSum = new Double(0);
			for(Entry<String, List<KeywordRow>> row : urlsMap.entrySet())
				sentimentSum += new Double(row.getValue().get(0).sentiment);
			
			Functionality2ResultRow functionality2ResultRow = new Functionality2ResultRow();
			
			functionality2ResultRow.keywords = keywordSiteWeek.keywords;
			functionality2ResultRow.site     = keywordSiteWeek.site;
			functionality2ResultRow.week     = keywordSiteWeek.week;
			functionality2ResultRow.mentions_no = (long) urlsMap.size();
			functionality2ResultRow.avg_sentiment = sentimentSum / urlsMap.size();
			
			resultsSet.add(functionality2ResultRow);
		}
		
		return resultsSet;
	}	
	
	////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
		LeadsQueryInterface.initialize("http://5.147.254.199", 8080);
		Functionality2 func2 = new Functionality2();
		Functionality2Params params = new Functionality2Params();
		params.language = "en";
		params.periodStart = new Long(0L).toString();
		params.periodEnd = new Long(new Date().getTime()).toString();
		params.keywords = new ArrayList<String>() {{ add("adidas"); }};
		Set<FunctionalityAbstResultRow> rows = func2.execute(params);
		int i=0;
		for(FunctionalityAbstResultRow row : rows) {
			System.out.println(i + ": " + row);
			i++;
		}
	}	
	
}




















