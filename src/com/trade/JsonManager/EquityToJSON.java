package com.trade.JsonManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.json.simple.JSONObject;

import com.trade.main.DataAnalysis;
import com.trade.rowData.DataStorage;
import com.trade.rowData.Equity;

public class EquityToJSON {
	
	public static final String jsonDir = "/mnt/eqJson/";
	private static final int summarizationFactor = 62;

	public static void main(String[] args) throws IOException {
		System.out.println("TEST");
		generateJsonRange("20140201", "20140221");
	}
	
	
	public static void generateJsonRange(String startTime, String endTime) throws IOException {
		Calendar startDate = Calendar.getInstance();
		startDate.set(Integer.parseInt(startTime.substring(0, 4)), 
				Integer.parseInt(startTime.substring(4, 6))-1, 	//minus one 'cause calendar is dumb as shit
				Integer.parseInt(startTime.substring(6)));
		
		Calendar endDate = Calendar.getInstance();
		endDate.set(Integer.parseInt(endTime.substring(0, 4)), 
				Integer.parseInt(endTime.substring(4, 6))-1, 
				Integer.parseInt(endTime.substring(6)));
		
		while(startDate.before(endDate) || startDate.equals(endDate)) {
			File fileDir = new File( DataAnalysis.dataHeadDir + 
					DataAnalysis.timeFileDir.format(startDate.getTime()) );
			
			if(fileDir.isDirectory()) {
				//if the day exists, will generate
				List<String> filenames = new ArrayList<String>(DataAnalysis.getDayFile(fileDir));
				generateJson(filenames, startDate);
			}
			startDate.add(Calendar.DATE, 1);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void generateJson(List<String> filenames, Calendar date) throws IOException {
		Hashtable<String, Equity> equityMap = new Hashtable<String, Equity>();
		
		for( int i=0; i<filenames.size(); i++ ) {
			Hashtable<String, Equity> equityTable = DataStorage.deserializeFile( filenames.get(i) );
			
			for( String eq : equityTable.keySet() ) {
				Equity tmpEq = equityTable.get(eq);
				if(!tmpEq.cleanEquity(5)) {
					System.err.println("Bad equity found: " + eq + " " + DataStorage.dateFormat.format(date.getTime()));
					continue;
				}
				tmpEq.avgSummarizeEquity(summarizationFactor);
				
				if(equityMap.containsKey(eq)) {
					equityMap.get(eq).appendEquity(tmpEq);
				} else {
					equityMap.put(eq, tmpEq);
				}
			}
		}
		
		
		for( String eq : equityMap.keySet() ) {
			JSONObject json = makeJsonEquity(equityMap.get(eq));
			File outputDir = new File(jsonDir + DataStorage.timeFileDir.format(date.getTime()) + eq);
			outputDir.getParentFile().mkdirs();
			FileWriter file = new FileWriter(outputDir);
			file.write(json.toJSONString());
			file.flush();
			file.close();
			
			System.out.println("File written to: " + outputDir.toString());
		}
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
