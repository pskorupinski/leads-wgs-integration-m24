package eu.leads.api.m24;

import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

public class CommonMethods {

	public static String formCsvString(String... params) {
		
		if(params.length < 1)
			return null;
		
		String ret = "";
		
		for(String param : params) {
			ret += param + "-";
		}
		
		ret = ret.substring(0, ret.length()-1);
		
		return ret;
	}
	
	public static String mapToJsonString(Map<?,?> map) {
		String ret = "";
		
		JSONObject jsonObj = new JSONObject();
		
		for(Entry<?, ?> entry : map.entrySet()) {
			jsonObj.put(entry.getKey().toString(), entry.getValue());
		}
		
		ret = jsonObj.toString();
		
		return ret;
	}
	
}
