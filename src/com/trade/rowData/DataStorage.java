package com.trade.rowData;

import java.io.File;
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
import com.trade.insights.Notifier;

public class DataStorage {
	public static final String outputDir = "/mnt/tradingData/";
	public static final String dailyMarket = "dailyMarket";
	public static final String persistEquityList = outputDir + "persistEquity";
	public static final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
	public static final DateFormat timeStampedDate = new SimpleDateFormat("yyyyMMdd-HHmmss");
	public static final DateFormat timeFileDir = new SimpleDateFormat("yyyy/MM/dd/");
	
	
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
    	
		String outputName = outputDir+timeFileDir.format(today)+
				dailyMarket+timeStampedDate.format(today);
    	
		File dir = new File(outputName);
		dir.getParentFile().mkdirs();
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
			try{
				//emergency file dump so data is not lost
				synchronized(allEquity) {
					String emergencyFileDump = "/mnt/tradingData/" + dailyMarket+timeStampedDate.format(today);
					FileOutputStream fileOut = new FileOutputStream(emergencyFileDump);
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
			        out.writeObject(allEquity);
			        fileOut.close();
			        out.close();
			        System.err.println("EMERGENCY serialization complete, file written to: " + outputName);
			        updateDatesEquityJson( new ArrayList<String>(allEquity.keySet()) );
			        Notifier.sendEmail("helloworld0424@gmail.com", "Etrade emergency file dump activated", e.getMessage());
				}
			} catch (Exception reallyFailed) {
				System.err.println("serialization failed: " + outputName);
				reallyFailed.printStackTrace();
				Notifier.sendEmail("helloworld0424@gmail.com", "Etrade file write failing", e.getMessage());
		        Notifier.sendSMS("Etrade file write failing");
			}
	    }
	}
	
	public static List<String> deserializePersistEquity() {
		List<String> result = null;
		
		try {
			FileInputStream fileIn = new FileInputStream(persistEquityList);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			result = (List<String>) in.readObject();
			in.close();
			fileIn.close();
			System.err.println("Deserialization complete: " + persistEquityList);
		} catch (Exception e) {
			System.err.println("Deserialization failed: " + e.getMessage());
		}
		return result;
	}
	
	//serialize a list containing equity need to persist
	public static void serializePersistEquity(List<String> eqList) {
		File file = new File(persistEquityList);
		try {
			FileOutputStream fileOut = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(eqList);
			fileOut.close();
			out.close();
			System.err.println("Persist Equity serialization complete");
		} catch (Exception e) {
			System.err.println("Persist Equity serialization failed: " + e.getMessage());
		}
		
	}
	
	@SuppressWarnings("unchecked")
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
