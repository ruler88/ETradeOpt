package com.trade.JsonManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.trade.main.DataAnalysis;
import com.trade.rowData.DataStorage;
import com.trade.rowData.Equity;

public class AppendJSON {
	public static String datesName = "dates";
	public static String jsonFileName = "/mnt/tradingData/datesEquity.json";
	
	public static void main(String[] args) {
		//this main is only for first-time run
		List<String> allFiles = getMarketFilesHack("20140210", "20140210");
		
		for(String serializedFile : allFiles) {
			try {
				Hashtable<String, Equity> oneDayEquity = DataStorage.deserializeFile(serializedFile);
				List<String> equityList = new ArrayList<String>(oneDayEquity.keySet());
				Collections.sort(equityList);
				appendDate(serializedFile, equityList);
				
			} catch (Exception e) {
				System.out.println("* Deserialization failed, file not found: " + serializedFile);
			}
		}
	}
	
	public static void appendDate(String fileName, List<String> eq) {
		try {
			String date = getDateFromFilename(fileName);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(jsonFileName));
			
			//update available dates
			JSONArray dates = (JSONArray) jsonObject.get(datesName);
			dates.add(date);
			
			//put in eqs
			jsonObject.put(date, eq);
			
			FileWriter file = new FileWriter(jsonFileName);
			file.write(jsonObject.toJSONString());
			file.flush();
			file.close();
			
		} catch (Exception e) {
			//Catch any exceptions here. don't want to break main process if this fails!
			e.printStackTrace();
		}
	}
	
	public static String getDateFromFilename(String fileName) {
		if( fileName.contains("-") ) {
			return fileName.substring(fileName.length() - 15, fileName.length() - 7); //given hacked date
		} else {
			return fileName.substring(fileName.length() - 8);
		}
		
	}
	
	private static List<String> getMarketFilesHack(String startTime, String endTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		List<String> fileNames = new ArrayList<String>();
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
				List<String> tmpFileList = DataAnalysis.getDayFile(fileDir);
				if(tmpFileList != null && tmpFileList.size() > 0
						&& tmpFileList.get(0) != null) {
					fileNames.add(tmpFileList.get(0));
				}
			}
			startDate.add(Calendar.DATE, 1);
		}
		
		return fileNames;
	}
}
