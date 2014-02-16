package com.trade.JsonManager;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.json.simple.JSONObject;

import com.trade.rowData.DataStorage;
import com.trade.rowData.Equity;

public class EquityToJSON {
	
	public static final String jsonDir = "/mnt/eqJson/";
	private static final int summarizationFactor = 64;

	public static void main(String[] args) throws IOException {
		System.out.println("TEST");
		generateJson("/mnt/tradingData/2014/02/03/dailyMarket20140203-210000");
	}
	
	@SuppressWarnings("unchecked")
	public static void generateJson(String filename) throws IOException {
		Hashtable<String, Equity> equityTable = DataStorage.deserializeFile(filename);
		JSONObject json = new JSONObject();
		
		for( String s : equityTable.keySet() ) {
			Equity eq = equityTable.get(s);
			eq.avgSummarizeEquity(summarizationFactor);
			json.put( s, makeJsonEquity(eq) );
		}
		
		//TODO: change output name
		FileWriter file = new FileWriter(jsonDir + DataStorage.dailyMarket);
		file.write(json.toJSONString());
		file.flush();
		file.close();
		
		System.out.println(json);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static JSONObject makeJsonEquity(Equity eq) {
		if(eq == null || eq.getTime() == null ||
			eq.getTime().size() <=0 ) {
			return null;
		}
		
		JSONObject json = new JSONObject();
		HashMap<String, List> equityVars = eq.getVariableMap();
		
		for( String s : equityVars.keySet() ) {
			json.put(s, equityVars.get(s));
		}
		
		return json;
	}
}
