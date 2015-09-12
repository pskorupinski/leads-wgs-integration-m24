package leadsm24.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;

import leadsm24.Roles;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.base.Optional;

import eu.leads.api.m24.CommonMethods;
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
import eu.leads.api.m36.VisualizationsProcessing;
import eu.leads.infext.datastore.impl.LeadsDataStore;
import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.common.RestxConfig;
import restx.config.ConfigLoader;
import restx.config.ConfigSupplier;
import restx.factory.Component;
import restx.factory.Factory;
import restx.factory.Name;
import restx.security.PermitAll;

@Component @RestxResource
public class LEADSM24Resource {
	
	private PrintStream err = System.err;
	private VisualizationsProcessing vp = new VisualizationsProcessing();

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
    
    @GET("/FS")
    @PermitAll    
    public String simpleFunctionality(String inputJSON) {
    	System.out.println(inputJSON);
		return inputJSON;
    }
    
    @GET("/VIS1")
    @PermitAll 
    /**
     * 
     * @param inputJSON stringified JSON Object with elements:
     *    - keywords, 
     *    - websites,
     *    - startts,
     *    - endts
     * 
     * @return stringified JSON Array with elements with fields:
     *    - "Product Name", 	eg. "adidas adiPure adios Boost 2",
	 *	  - "Product Price",	eg. 100.99,
	 *	  - "Week",				eg. "Week 1",
	 *	  - "Shop",				eg. "Shop 1",
	 *	  - "Category",			eg. "adidas Boost"
	 *
     */
    public String visualization1(String inputJSON) {
    	System.out.println(inputJSON);
    	return vp.process(1, inputJSON);
    }
    
    @GET("/VIS2")
    @PermitAll 
    /**
     * 
     * @param inputJSON stringified JSON Object with elements:
     *    - cat1name,
     *    - cat1keys,
     *    - cat2name,
     *    - cat2keys,
     *    - websites,
     *    - startday,
     *    - endday
     * 
     * @return CSV string of structure:
     *    - category-sentiment-site-keywords,count
     *    e.g. Nike-positive-site1-Nike Free,13
     * 
     * 
     */
    public String visualization2(String inputJSON) {
    	System.out.println(inputJSON);
    	return vp.process(2, inputJSON);
    }
    
    @GET("/VIS1S")
    @PermitAll 
    public String visualization1Static(String inputJSON) {
    	System.out.println(inputJSON);
    	
    	List<JSONObject> results = new ArrayList<>();
    	
    	JSONObject obj1 = new JSONObject();
    	obj1.append("Product Name", "adidas adiPure adios Boost 2");
    	obj1.append("Product Price", 100.99);
    	obj1.append("Week", "Week 1");
    	obj1.append("Shop", "Shop 1");
    	obj1.append("Category", "adidas Boost");
    	results.add(obj1);
    	
    	JSONObject obj2 = new JSONObject();
    	obj2.append("Product Name", "Nike Free Special One");
    	obj2.append("Product Price", 89.99);
    	obj2.append("Week", "Week 1");
    	obj2.append("Shop", "Shop 2");
    	obj2.append("Category", "Nike Free");
    	results.add(obj2);
    	
    	JSONObject obj3 = new JSONObject();
    	obj3.append("Product Name", "Nike Free Another One");
    	obj3.append("Product Price", 99.99);
    	obj3.append("Week", "Week 1");
    	obj3.append("Shop", "Shop 1");
    	obj3.append("Category", "Nike Free");
    	results.add(obj3);
    	
    	JSONObject obj4 = new JSONObject();
    	obj4.append("Product Name", "Nike Free Another One");
    	obj4.append("Product Price", 99.99);
    	obj4.append("Week", "Week 2");
    	obj4.append("Shop", "Shop 1");
    	obj4.append("Category", "Nike Free");
    	results.add(obj4);    	

    	JSONArray outputJSON = new JSONArray(results);
    	String outputJSONString = outputJSON.toString();
		return outputJSONString;
    }
    
    @GET("/VIS2S")
    @PermitAll 
    public String visualization2Static(String inputJSON) {
    	System.out.println(inputJSON);
    	
    	Map<String,Integer> results = new HashMap<>();
    	
    	String category; String sentiment; String site; String keywords;
    	Integer count;
    	
    	category = "adidas";
    	sentiment = "positive";
    	site = "site1";
    	keywords = "adidas Boost";
    	count = 7;
    	results.put(
    			CommonMethods.formCsvString(category,sentiment,site,keywords),
    			count);
    	
    	category = "adidas";
    	sentiment = "negative";
    	site = "site1";
    	keywords = "adidas Boost";
    	count = 11;
    	results.put(
    			CommonMethods.formCsvString(category,sentiment,site,keywords),
    			count);    	
    	
    	category = "Nike";
    	sentiment = "positive";
    	site = "site1";
    	keywords = "Nike Free";
    	count = 13;
    	results.put(
    			CommonMethods.formCsvString(category,sentiment,site,keywords),
    			count);      	
    	
    	category = "Nike";
    	sentiment = "negative";
    	site = "site2";
    	keywords = "Nike Skyknit";
    	count = 15;
    	results.put(
    			CommonMethods.formCsvString(category,sentiment,site,keywords),
    			count);     	
    	
    	String csvString = CommonMethods.mapToJsonString(results);
    	
    	System.err.println(csvString);
    	
		return csvString;
    }
    
    @GET("/file")
    @PermitAll    
    public String file(String name) {
    	
    	if(name == null || name.length()<1)
    		return "{}";
    	
    	System.out.println(name);
	 
	    String files = System.getProperty("resource.files");
	    
	    if(files == null)
	    	return "{}";
	    
    	String path = files + name;
    	String content;
		try {
			content = new Scanner(new File(path)).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "{}";
		}
	    
		return content;
    }
    
    
    
}












