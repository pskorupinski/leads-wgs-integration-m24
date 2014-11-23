package leadsm24.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import leadsm24.Roles;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.leads.api.m24.FunctionalityAbst;
import eu.leads.api.m24.FunctionalityAbstResultRow;
import eu.leads.api.m24.demo.Functionality1;
import eu.leads.api.m24.demo.Functionality1A;
import eu.leads.api.m24.demo.Functionality2;
import eu.leads.api.m24.model.Functionality1AParams;
import eu.leads.api.m24.model.Functionality1AReturnRow;
import eu.leads.api.m24.model.Functionality1Params;
import eu.leads.api.m24.model.Functionality1ResultRowAgreed;
import eu.leads.api.m24.model.Functionality2Params;
import eu.leads.api.m24.model.Functionality2ResultRow;
import eu.leads.infext.datastore.impl.LeadsDataStore;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

@Component @RestxResource
public class LEADSM24Resource {
	
	private PrintStream err = System.err;

	private void init() {
		LeadsDataStore.initialize("http://clu25.softnet.tuc.gr", 8080);
//    	System.setOut(new PrintStream(new OutputStream() {
//			@Override
//			public void write(int b) throws IOException {}
//		}));
//    	System.setErr(new PrintStream(new OutputStream() {
//			@Override
//			public void write(int b) throws IOException {}
//		}));
	}


    @GET("/F1")
    @PermitAll
    public String functionality1(String inputJSON) {
    	init();
    	long start = System.currentTimeMillis();
    	
    	System.out.println(inputJSON);
    	
    	JSONObject jsonObj = new JSONObject(inputJSON);
    	Functionality1Params params = new Functionality1Params();
    	
    	System.out.println(jsonObj);
    	
    	List<String> shopName = new ArrayList<>();
    	for(int i=0; i<jsonObj.getJSONArray("shopName").length(); i++)
    		shopName.add(jsonObj.getJSONArray("shopName").getString(i));
    	params.shopName = shopName;
    	
    	List<String> keywords = new ArrayList<>();
    	for(int i=0; i<jsonObj.getJSONArray("keywords").length(); i++)
    		keywords.add(jsonObj.getJSONArray("keywords").getString(i));
    	params.keywords = keywords;
    	
    	params.periodStart=new Long(jsonObj.getLong("periodStart")).toString(); // 0
    	params.periodEnd  =new Long(jsonObj.getLong("periodEnd")).toString(); // 1414800000000
    	
//    	FunctionalityAbst f1 = new eu.leads.api.m24.func.Functionality1();
    	FunctionalityAbst f1 = new eu.leads.api.m24.demo.Functionality1();
//    	FunctionalityAbst f1 = new eu.leads.api.m24.demotest.Functionality1();
    	Set<FunctionalityAbstResultRow> rows = f1.execute(params);
    	
    	JSONArray result = new JSONArray();
    	for(FunctionalityAbstResultRow rowX : rows) {
    		Functionality1ResultRowAgreed row = (Functionality1ResultRowAgreed) rowX;
    		JSONObject rowJson = new JSONObject();
    		rowJson.put("shop_name", row.shop_name);
    		rowJson.put("prod_name", row.prod_name);
    		rowJson.put("prod_price_cur", row.prod_price_cur);
    		rowJson.put("prod_price_min", row.prod_price_min);
    		rowJson.put("prod_price_max", row.prod_price_max);
    		rowJson.put("day", row.day);
    		result.put(rowJson);
    	}
    	
    	long end = System.currentTimeMillis();
    	err.println("### Functionality 1 time: "+(end-start));
    	
        return result.toString();
    }
    
    @GET("/F1A")
    @PermitAll
    public String functionality1A(String inputJSON) {
    	init();
    	long start = System.currentTimeMillis();
    	
    	JSONObject jsonObj = new JSONObject(inputJSON);
    	Functionality1AParams params = new Functionality1AParams();
    	
    	List<String> country = new ArrayList<>();
    	for(int i=0; i<jsonObj.getJSONArray("country").length(); i++)
    		country.add(jsonObj.getJSONArray("country").getString(i));
    	params.country = country;
    	
    	List<String> keywords = new ArrayList<>();
    	for(int i=0; i<jsonObj.getJSONArray("keywords").length(); i++)
    		keywords.add(jsonObj.getJSONArray("keywords").getString(i));
    	params.keywords = keywords;
    	
    	params.periodStart=new Long(jsonObj.getLong("periodStart")).toString(); // 0
    	params.periodEnd  =new Long(jsonObj.getLong("periodEnd")).toString(); // 1414800000000
    	
//    	FunctionalityAbst f1a = new eu.leads.api.m24.func.Functionality1A();
    	FunctionalityAbst f1a = new eu.leads.api.m24.demo.Functionality1A();
//    	FunctionalityAbst f1a = new eu.leads.api.m24.demotest.Functionality1A();
    	SortedSet<FunctionalityAbstResultRow> rows = f1a.execute(params);
    	
    	JSONArray result = new JSONArray();
    	for(FunctionalityAbstResultRow rowX : rows) {
    		Functionality1AReturnRow row = (Functionality1AReturnRow) rowX;
    		JSONObject rowJson = new JSONObject();
    		rowJson.put("shop_name", row.shop_name);
    		rowJson.put("country", row.country_code);
    		rowJson.put("keywords", row.keyword);
    		rowJson.put("week", row.week);
    		rowJson.put("no_products", row.products_no);
    		result.put(rowJson);
    	}
    	
    	long end = System.currentTimeMillis();
    	err.println("### Functionality 1A time: "+(end-start));
    	
        return result.toString();
    }
    
    @GET("/F2")
    @PermitAll
    public String functionality2(String inputJSON) {
    	init();
    	long start = System.currentTimeMillis();
    	
    	JSONObject jsonObj = new JSONObject(inputJSON);
    	Functionality2Params params = new Functionality2Params();
    	
    	List<String> keywords = new ArrayList<>();
    	for(int i=0; i<jsonObj.getJSONArray("keywords").length(); i++)
    		keywords.add(jsonObj.getJSONArray("keywords").getString(i));
    	params.keywords = keywords;

    	params.language = jsonObj.getString("language");
    	params.periodStart=new Long(jsonObj.getLong("periodStart")).toString(); // 0
    	params.periodEnd  =new Long(jsonObj.getLong("periodEnd")).toString(); // 1414800000000
    	
//    	FunctionalityAbst f2 = new eu.leads.api.m24.func.Functionality2();
    	FunctionalityAbst f2 = new eu.leads.api.m24.demo.Functionality2();
//    	FunctionalityAbst f2 = new eu.leads.api.m24.demotest.Functionality2();
    	SortedSet<FunctionalityAbstResultRow> rows = f2.execute(params);
    	
    	JSONArray result = new JSONArray();
    	for(FunctionalityAbstResultRow rowX : rows) {
    		Functionality2ResultRow row = (Functionality2ResultRow) rowX;
    		JSONObject rowJson = new JSONObject();
    		rowJson.put("site", row.site);
    		rowJson.put("keywords", row.keywords);
    		rowJson.put("week", row.week);
    		rowJson.put("avg_sentiment", row.avg_sentiment);
    		rowJson.put("mentions", row.mentions_no);
    		result.put(rowJson);
    	}
    	
    	long end = System.currentTimeMillis();
    	err.println("### Functionality 2 time: "+(end-start));
    	
        return result.toString();
    }
    
}












