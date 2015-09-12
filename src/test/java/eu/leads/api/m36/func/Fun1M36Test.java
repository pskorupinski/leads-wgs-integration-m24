package eu.leads.api.m36.func;

import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Test;

import eu.leads.api.m24.FunctionalityAbstParams;
import eu.leads.api.m36.model.FunM36JsonParams;

public class Fun1M36Test {

	@Test
	public void test() {
		String inputJSON = "{'keywords':['a','b'],"
				+ "'websites':['c','d'],"
				+ "'startts':1430438400000,"
				+ "'endts':1430438499000}";
		JSONObject json = new JSONObject(inputJSON);
		FunctionalityAbstParams params = new FunM36JsonParams(json);		
		
		Fun1M36 fun1M36 = new Fun1M36();
		fun1M36.execute(params);
	}

}
