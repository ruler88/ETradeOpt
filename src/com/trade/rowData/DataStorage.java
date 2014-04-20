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
import com.trade.main.GetMarket;

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
			System.err.println("Deserialization complete: " + fileName);
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
		
		if(GetMarket.testMode) {
			printMapEq(allEquity);
			outputName = "/tmp/"+ dailyMarket+timeStampedDate.format(today);
		}
    	
		File dir = new File(outputName);
		dir.getParentFile().mkdirs();
		try {
	    	synchronized(allEquity) {
	    		lockEquities(allEquity);
	    		FileOutputStream fileOut = new FileOutputStream(outputName);
		        ObjectOutputStream out = new ObjectOutputStream(fileOut);
		        out.writeObject(allEquity);
		        fileOut.close();
		        out.close();
		        System.err.println("Part serialization complete, file written to: " + outputName);
	    	}
		} catch (Exception e) {
			try{
				//emergency file dump so data is not lost
				synchronized(allEquity) {
					lockEquities(allEquity);
					String emergencyFileDump = "/mnt/tradingData/" + dailyMarket+timeStampedDate.format(today);
					FileOutputStream fileOut = new FileOutputStream(emergencyFileDump);
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
			        out.writeObject(allEquity);
			        fileOut.close();
			        out.close();
			        System.err.println("EMERGENCY serialization complete, file written to: " + outputName);
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
	
	private static void lockEquities(Hashtable<String, Equity> allEquity) {
		for( String key : allEquity.keySet() ) {
			allEquity.get(key).lockEquity();
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
	
	private static void printMapEq(Hashtable<String, Equity> allEquities) {
		for( String s : allEquities.keySet() ) {
			System.out.println(s + " " + allEquities.get(s).toString());
		}
	}
}
