package com.trade.rowData;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.trade.JsonManager.AppendJSON;

public class DataStorage {
	private static final String outputDir = "/mnt/tradingData/";
	private static final String dailyMarket = "dailyMarket";
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	private static final DateFormat timeStampedDate = new SimpleDateFormat("yyyyMMdd-HHmmss");
	
	
	//this needs work!
	@SuppressWarnings("unchecked")
	public static Hashtable<String, Equity> deserializeFile(String fileName) {
		//deserialize the quarks.ser file
	    try {
	    	Hashtable<String, Equity> allEquity = new Hashtable<String, Equity>();
			FileInputStream fileIn = new FileInputStream(fileName);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			allEquity = (Hashtable<String, Equity>) in.readObject();
			in.close();
			fileIn.close();
			System.out.println("Deserialization complete: " + fileName);
			return allEquity;
	    } catch ( Exception e ) {
	    	System.err.println("deserialization failed: " + fileName);
	    	e.printStackTrace();
	    }
		return null;
	}
	
	public static void serializePartFile(Hashtable<String, Equity> allEquity) {
		Date today = new Date();
    	String outputName = outputDir+dailyMarket+timeStampedDate.format(today);
		try {
	    	synchronized(allEquity) {
	    		FileOutputStream fileOut = new FileOutputStream(outputName);
		        ObjectOutputStream out = new ObjectOutputStream(fileOut);
		        out.writeObject(allEquity);
		        fileOut.close();
		        out.close();
		        System.err.println("Part serialization complete, file written to: " + outputName);
		        updateDatesEquityJson( new ArrayList<String>(allEquity.keySet()) );
	    	}
		} catch (Exception e) {
	    	System.err.println("serialization failed: " + outputName);
	    	e.printStackTrace();
	    }
	}
	
	@SuppressWarnings("unchecked")		//silly generic typing for JSON
	public static void updateDatesEquityJson(List<String> eq) {
		try {
			Date today = new Date();
			String date = dateFormat.format(today);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(AppendJSON.jsonFileName));
			
			
			JSONArray dates = (JSONArray) jsonObject.get(AppendJSON.datesName);
			if( !dates.contains(date) ) {
				//add to json iff the equity data for that date does not yet exist
				dates.add(date);
				jsonObject.put(date, eq);
				
				FileWriter file = new FileWriter(AppendJSON.jsonFileName);
				file.write(jsonObject.toJSONString());
				file.flush();
				file.close();
			}
			
		} catch (Exception e) {
			//Catch any exceptions here. don't want to break main process if this fails!
			e.printStackTrace();
		}
	}
}
