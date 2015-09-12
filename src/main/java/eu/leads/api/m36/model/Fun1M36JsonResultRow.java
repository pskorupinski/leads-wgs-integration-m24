package eu.leads.api.m36.model;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import eu.leads.api.m24.FunctionalityAbstResultRow;

public class Fun1M36JsonResultRow extends FunctionalityAbstResultRow {
	
	private Map<String,Object> row = new HashMap<String, Object>();

	public JSONObject getJsonRow() {
		return new JSONObject(row);
	}
	
	public Object getParameter(String key) {
		return row.get(key);
	}
	
	public boolean isParameter(String key) {
		return row.containsKey(key);
	}
	
	public void setParameter(String key, Object value) {
		row.put(key, value);
	}

	public Fun1M36JsonResultRow(Map<String,Object> row) {
		this.row = row;
	}
	
	public Fun1M36JsonResultRow() {
		
	}
	
}
