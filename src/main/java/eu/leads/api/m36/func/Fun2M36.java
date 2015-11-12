package eu.leads.api.m36.func;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONObject;

import eu.leads.api.m24.FunctionalityAbst;
import eu.leads.api.m24.FunctionalityAbstParams;
import eu.leads.api.m24.FunctionalityAbstResultRow;
import eu.leads.api.m36.model.Fun2M36ResultRow;
import eu.leads.api.m36.model.FunM36JsonParams;
import eu.leads.datastore.impl.LeadsQueryInterface;
import eu.leads.processor.web.QueryResults;
import eu.leads.utils.LEADSUtils;

public class Fun2M36 implements FunctionalityAbst {
	
	private static String ARTICLE = "article_content";
	
	public static class SentimentWebsiteKeywords {
		
		private static String SENTIMENT_POS = "positive";
		private static String SENTIMENT_NEU = "neutral";
		private static String SENTIMENT_NEG = "negative";
		
		private String sentiment;
		private String website;
		private String keywords;
		
		public SentimentWebsiteKeywords(Double sentiment, 
				String website, 
				String keywords) {
			if(sentiment >= 0.2) this.sentiment = SENTIMENT_POS;
			else if(sentiment > -0.2) this.sentiment = SENTIMENT_NEU;
			else this.sentiment = SENTIMENT_NEG;
			this.website = website;
			this.keywords = keywords;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof SentimentWebsiteKeywords) {
				SentimentWebsiteKeywords other = (SentimentWebsiteKeywords) obj;
				if(this.sentiment.equals(other.sentiment) &&
						this.website.equals(other.website) &&
						this.keywords.equals(other.keywords))
					return true;
			}
			return false;
		}

		public String getSentiment() {
			return sentiment;
		}

		public String getWebsite() {
			return website;
		}

		public String getKeywords() {
			return keywords;
		}
		
	}
	
	public static class CategoryRecognizer {
		
		private String cat1Name;
		private String cat2Name;
		private List<String> cat1Keys;
		private List<String> cat2Keys;

		public CategoryRecognizer(String cat1Name, List<String> cat1Keys, 
				String cat2Name, List<String> cat2Keys) {
			this.cat1Name = cat1Name;
			this.cat2Name = cat2Name;
			this.cat1Keys = cat1Keys;
			this.cat2Keys = cat2Keys;			
		}
		
		public String getCategory(String key) {
			if(cat1Keys.contains(key))
				return cat1Name;
			else if(cat2Keys.contains(key))
				return cat2Name;
			
			return null;
		}
		
	}

	@Override
	public SortedSet<FunctionalityAbstResultRow> execute(
			FunctionalityAbstParams params) {
		
		FunM36JsonParams jsonParams = (FunM36JsonParams) params;
		
		SortedSet<FunctionalityAbstResultRow> resultRows = new TreeSet<>();
		
		String cat1Name 		= jsonParams.getParameter("cat1name", java.lang.String.class);
		List<String> cat1Keys 	= jsonParams.getParameter("cat1keys", java.util.List.class);
		String cat2Name 		= jsonParams.getParameter("cat2name", java.lang.String.class);
		List<String> cat2Keys 	= jsonParams.getParameter("cat2keys", java.util.List.class);
		List<String> websites 	= jsonParams.getParameter("websites", java.util.List.class);
		Long startts  			= jsonParams.getParameter("startts" , java.lang.Long.class);
		Long endts   			= jsonParams.getParameter("endts"   , java.lang.Long.class);
		
		if(cat1Name == null || cat1Keys == null || cat2Name == null || cat2Keys == null || 
				websites == null || startts == null || endts == null) {
			return null;
		}		
		
		List<String> fqdnUrls	= new ArrayList<>();
		for(String website : websites)
			fqdnUrls.add(LEADSUtils.fqdnToNutchUrl(website));
		
		List<String> keywords	= new ArrayList<>();
		keywords.addAll(cat1Keys);
		keywords.addAll(cat2Keys);
		
		CategoryRecognizer categoryRecognizer = new CategoryRecognizer(cat1Name, cat1Keys, cat2Name, cat2Keys);
		
		/*
		 * 
		 * 
		 * 
		 */
		
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
		
		String query1 = "SELECT C.fqdnurl AS website, K.keywords AS keywords, "
				+ "K.sentiment AS sentiment\n"
				+ "FROM keywords K\n"
				+ "JOIN page_core C ON C.uri = K.uri\n"
				+ "WHERE C.ts = K.ts\n"
				+ "AND K.partid like '"+ARTICLE+":000'\n"
				+ "AND K.ts >= "+ startts +" AND K.ts <= "+ endts +"\n"
				+ "AND C.fqdnurl IN (";
		int sitesNo = fqdnUrls.size();
		for(int i=0; i<sitesNo; i++) {
			String site = fqdnUrls.get(i);
			query1 += "'";
			query1 += site;
			if(i<sitesNo-1) query1 += "',";
			else query1 += "')\n";
		}
		query1 += "AND K.keywords IN (";
		int keysNo = keywords.size();
		for(int i=0; i<keysNo; i++) {
			String keyword = keywords.get(i);
			query1 += "'";
			query1 += keyword;
			if(i<keysNo-1) query1 += "',";
			else query1 += "')";
		}
		query1 += ";";
		
//		System.out.println(query1);
		
		List<SentimentWebsiteKeywords> sentimentWebsiteKeywordsList = new ArrayList<Fun2M36.SentimentWebsiteKeywords>();
		Map<SentimentWebsiteKeywords,Integer> sentimentWebsiteKeywordsMap  = new HashMap<Fun2M36.SentimentWebsiteKeywords,Integer>();
		
		
		QueryResults rs = LeadsQueryInterface.execute(query1);
		if(rs != null) {
			List<String> rows = rs.getResult();
			for(String row : rows) {
				JSONObject jsonRow = new JSONObject(row);
				String website   = LEADSUtils.nutchUrlToFullyQualifiedDomainName(jsonRow.getString("website"));
				String keyword   = jsonRow.getString("keywords");
				String sentimentStr = jsonRow.getString("sentiment");
				Double sentiment = Double.parseDouble(sentimentStr);
				SentimentWebsiteKeywords sentimentWebsiteKeywords = new SentimentWebsiteKeywords(sentiment, website, keyword);
				sentimentWebsiteKeywordsList.add(sentimentWebsiteKeywords);
				sentimentWebsiteKeywordsMap.put(sentimentWebsiteKeywords,null);
			}
		}
		
		for(SentimentWebsiteKeywords sentimentWebsiteKeywords : sentimentWebsiteKeywordsMap.keySet()) {
			String sentiment = sentimentWebsiteKeywords.getSentiment();
			String website   = sentimentWebsiteKeywords.getWebsite();
			String keyword   = sentimentWebsiteKeywords.getKeywords();
			String category  = categoryRecognizer.getCategory(keyword);
			Integer count    = Collections.frequency(sentimentWebsiteKeywordsList, sentimentWebsiteKeywords);
			
			List<String> values = new ArrayList<>();
			values.add(category);
			values.add(sentiment);
			values.add(website);
			values.add(keyword);
			Fun2M36ResultRow fun2m36ResultRow = new Fun2M36ResultRow(values, count);
			resultRows.add(fun2m36ResultRow); // TODO FIX IT!!!!
		}
		
		/*
		 * 
		 * 
		 * 
		 */		
		
		return resultRows;
	}

}





