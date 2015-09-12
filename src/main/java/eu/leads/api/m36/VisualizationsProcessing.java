package eu.leads.api.m36;

import java.util.SortedSet;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.leads.api.m24.FunctionalityAbst;
import eu.leads.api.m24.FunctionalityAbstParams;
import eu.leads.api.m24.FunctionalityAbstResultRow;
import eu.leads.api.m24.model.Functionality1AReturnRow;
import eu.leads.api.m36.func.Fun1M36;
import eu.leads.api.m36.func.Fun2M36;
import eu.leads.api.m36.model.Fun1M36JsonResultRow;
import eu.leads.api.m36.model.Fun2M36ResultRow;
import eu.leads.api.m36.model.FunM36JsonParams;
import eu.leads.infext.datastore.impl.LeadsDataStore;

public class VisualizationsProcessing {
	
	public VisualizationsProcessing() {
		LeadsDataStore.initialize("http://clu25.softnet.tuc.gr", 8080);
	}

	public String process(int visNo, String inputJSON) {
		JSONObject json = new JSONObject(inputJSON);
		
		FunctionalityAbst functionality = null;
		FunctionalityAbstParams params  = new FunM36JsonParams(json);
		
		if(visNo==1) 		functionality = new Fun1M36();
		else if(visNo==2) 	functionality = new Fun2M36();
		else throw new IllegalArgumentException("visNo must be between 1 and 2!");
		
		SortedSet<FunctionalityAbstResultRow> rows = functionality.execute(params);
		
		String result = null;
		
		if(visNo==1) {
			JSONArray resultJson = new JSONArray();
			for(FunctionalityAbstResultRow rowX : rows) {
				Fun1M36JsonResultRow row = (Fun1M36JsonResultRow) rowX;
				resultJson.put(row.getJsonRow());
			}
			result = resultJson.toString();
		}
		else if(visNo==2) {
			for(FunctionalityAbstResultRow rowX : rows) {
				Fun2M36ResultRow row = (Fun2M36ResultRow) rowX;
				result += row.getRow() + "\n";
			}
			result = result.substring(0, result.length()-1);
		}
		
		return result;
	}
	
}
