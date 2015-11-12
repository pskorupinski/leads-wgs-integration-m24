package eu.leads.api.m36.model;

import java.util.List;

import org.json.JSONObject;

import eu.leads.api.m24.FunctionalityAbstResultRow;

public class Fun2M36ResultRow extends FunctionalityAbstResultRow implements Comparable<Fun2M36ResultRow> {
	
	private String row = null;

	public String getRow() {
		return row;
	}

	public Fun2M36ResultRow(List<String> values, Integer count) {
		row = "";
		for(String value : values)
			row += value + "-";
		row = row.substring(0, row.length()-1);
		row += "," + count.toString();
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return row;
	}

	@Override
	public int compareTo(Fun2M36ResultRow o) {
		return this.row.compareTo(o.row);
	}
	
}
