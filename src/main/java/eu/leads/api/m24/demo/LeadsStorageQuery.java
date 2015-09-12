package eu.leads.api.m24.demo;
import java.util.List;

import org.json.JSONObject;

import eu.leads.datastore.impl.LeadsQueryInterface;
import eu.leads.infext.datastore.datastruct.UrlTimestamp;
import eu.leads.infext.datastore.impl.LeadsDataStore;
import eu.leads.processor.web.QueryResults;


public class LeadsStorageQuery {

	public static void main(String[] args) {

		LeadsDataStore.initialize("http://5.147.254.199", 8080);
		
		String query1 = "SELECT uri AS uri, ts AS ts\n"
				+ "FROM keywords\n"
				+ "WHERE ts>=0 AND ts <=1416501600106\n"
				+ "AND partid = 'ecom_prod_name:000'";
		String query2 = "SELECT C.uri, C.ts\n"
				+ "FROM page_core C\n"
				+ "JOIN keywords K ON C.uri = K.uri\n"
				+ "WHERE C.ts = K.ts\n"
				+ "AND K.partid like 'ecom_prod_name:000'\n"
				+ "AND C.ts>=0 AND C.ts <=1416502779743;";
		String query3 = "SELECT K.uri, K.ts, K.keywords, K.sentiment AS sentiment, K.relevance AS relevance\r\n"
				+ "FROM keywords K\r\n"
				+ "JOIN page_core C ON C.uri = K.uri\r\n"
				+ "WHERE C.ts = K.ts\r\n"
				+ "AND K.partid like 'article_content:000'\r\n"
				+ "AND C.lang like 'en'\r\n"
				+ "AND K.ts>=0 AND K.ts <=1416504380252\r\n"
				+ "AND keywords IN ('adidas');";
		String query4 = "SELECT uri AS uri, ts AS ts, resourceparttype AS resourceparttype, resourcepartvalue AS resourcepartvalue FROM resourcepart\r\n"
				+ "WHERE (( uri='com.wiggle.www:http/nike-sport-water-bottle/' AND ts=1410884436931) "
				+ "OR ( uri like 'com.wiggle.www:http/nike-womens-nike-df-epic-run-boy-short-fa14/' AND ts=1410884376816) "
				+ "OR ( uri like 'com.holabirdsports.www:http/adidas-roland-garros-on-court-tank-womens.html' AND ts=1410868031920) "
				+ "OR ( uri like 'com.wiggle.www:http/nike-womens-filament-tight-fa14/' AND ts=1410884417188) "
				+ "OR ( uri like 'com.wiggle.www:http/nike-9-phenom-2-in-1-short-su14/' AND ts=1410875255364) "
				+ "OR ( uri like 'com.holabirdsports.www:http/running/clothing/men-s/adidas-supernova-split-shorts-spring-2014-mens.html' AND ts=1410880683719) "
				+ "OR ( uri like 'com.holabirdsports.www:http/adidas-barricade-7-mens-novak-djokovic-french-open.html' AND ts=1411420382915) "
				+ "OR ( uri like 'com.holabirdsports.www:http/adidas-andy-murray-barricade-climachill-tee-us-open-mens.html' AND ts=1410868022477) "
				+ "OR ( uri like 'com.holabirdsports.www:http/adidas-roland-garros-on-court-cap-sleeve-top-womens.html' AND ts=1411323386107) "
				+ "OR ( uri='com.holabirdsports.www:http/running/shoes/cross-country/adidas-xcs-5-spike-mens-blackearth-greeninfrared.html' AND ts=1410878847108))\r\n"
				+ "ORDER BY uri, ts;";
		
		QueryResults rs = LeadsQueryInterface.execute(query2);
		if(rs != null) {
			List<String> rows = rs.getResult();
			for(String row : rows) {
				System.out.println(row);
			}
		}
		
	}
}
