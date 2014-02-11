package com.trade.JsonManager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.json.simple.JSONObject;

import com.trade.main.DataAnalysis;
import com.trade.rowData.DataStorage;
import com.trade.rowData.Equity;


public class CreateJSON {
	
	public static void main(String[] args) {
		//setting up the json with first two days!
		List<String> allFiles = DataAnalysis.getMarketFiles("20140102", "20140102");
		
		JSONObject json = new JSONObject();
		ArrayList<String> dateList = new ArrayList<String>();
		
		try {
			//try to load file here
			FileWriter file = new FileWriter(AppendJSON.jsonFileName);
			
			for(String serializedFile : allFiles) {
				try {
					Hashtable<String, Equity> oneDayEquity = DataStorage.deserializeFile(serializedFile);
					List equityList = new ArrayList(oneDayEquity.keySet());
					Collections.sort(equityList);
					json.put(AppendJSON.getDateFromFilename(serializedFile), equityList);
					dateList.add(AppendJSON.getDateFromFilename(serializedFile));
				} catch (Exception e) {
					System.out.println("* Deserialization failed, file not found: " + serializedFile);
				}
			}
			json.put(AppendJSON.datesName, dateList);
			file.write(json.toJSONString());
			file.flush();
			file.close();
	 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
