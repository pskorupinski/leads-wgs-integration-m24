package eu.leads.api.m36.func;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONObject;

import eu.leads.api.m24.FunctionalityAbst;
import eu.leads.api.m24.FunctionalityAbstParams;
import eu.leads.api.m24.FunctionalityAbstResultRow;
import eu.leads.api.m24.demo.Functionality2.KeywordRow;
import eu.leads.api.m36.model.FunM36JsonParams;
import eu.leads.datastore.impl.LeadsQueryInterface;
import eu.leads.processor.web.QueryResults;

public class Fun2M36 implements FunctionalityAbst {
	
	private static String ARTICLE = "article_content";

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

		
		/*
		 * 
		 * 
		 * 
		 */		
		
		return resultRows;
	}

}
