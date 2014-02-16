package com.trade.main;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import com.trade.rowData.DataStorage;
import com.trade.rowData.Equity;


public class DataAnalysis {
	public static final String dataHeadDir = DataStorage.outputDir;
	public static final String dailyMarket = DataStorage.dailyMarket;
	public static final DateFormat timeFileDir = DataStorage.timeFileDir;
	
	private final int summarizationFactor = 0;
	public static void main(String[] args) {
//		List<String> allFiles = getMarketFiles("20140124", "20140124");
//		for(String s : allFiles) {
//			System.out.println(s);
//		}
		
		List<String> allFiles = new ArrayList<String>();
		
		allFiles.add("/mnt/tradingData/dailyMarket20140205-210000");
		allFiles.add("/mnt/tradingData/dailyMarket20140205-210003");
		
		DataAnalysis test = new DataAnalysis(allFiles);
	}
	
	public DataAnalysis(List<String> files) {
		Hashtable<String, Equity> allEquity = new Hashtable<String, Equity>();
		for(String name : files) {
			Hashtable<String, Equity> oneDayEquity = DataStorage.deserializeFile(name);
			for(String s : oneDayEquity.keySet()) {
				Equity tmpEquity = oneDayEquity.get(s);
				if( tmpEquity.getAsk().size() == 0 ) {
					System.err.println("Failed equity: " + name + " - " + s);
					continue;	//prevent empty equity from breaking the code
				} else{
					System.out.println("Success equity: " + name + " - " + s);
				}
				
				//tmpEquity.truncateEquity();
				if(allEquity.containsKey(s)) {
					allEquity.get(s).appendEquity(tmpEquity);
				} else {
					allEquity.put(s, tmpEquity);
				}
			}
		}
		
		ArrayList<Equity> viewEqs = new ArrayList<Equity>();
		viewEqs.add(allEquity.get("GOOG"));
		
		printEquities(viewEqs);
	}
	
	public void printEquities(List<Equity> eqs) {
		for(int j=0; j<eqs.size(); j++) {
			//summarize all equities first
			eqs.get(j).summarizeEquity(summarizationFactor);
			//print title
			System.out.print("ticker, askPrice, askSize, bidPrice, bidSize, vol, ");
		}
		System.out.println();
		
		List<Date> time = eqs.get(0).getTime();
		for(int i=0; i<time.size(); i++) {
			for(int j=0; j<eqs.size(); j++) {
				printEquityProp(eqs.get(j), i);
			}
			System.out.println();
		}
	}
	
	private void printEquityProp(Equity eq, int i) {
		System.out.print(
			eq.ticker + ", " +
			eq.getTime().get(i) + ", " +
			eq.getAsk().get(i) + ", " + 
			eq.getAskSize().get(i) + ", " +
			eq.getBid().get(i) + ", " + 
			eq.getBidSize().get(i) + ", " +
			eq.getTotalVolume().get(i) + ", "
		);
	}
	
	
	//get all files within date range
	public static List<String> getMarketFiles(String startTime, String endTime) {
		Calendar startDate = Calendar.getInstance();
		startDate.set(Integer.parseInt(startTime.substring(0, 4)), 
				Integer.parseInt(startTime.substring(4, 6))-1, 	//minus one 'cause calendar is dumb as shit
				Integer.parseInt(startTime.substring(6)));
		
		Calendar endDate = Calendar.getInstance();
		endDate.set(Integer.parseInt(endTime.substring(0, 4)), 
				Integer.parseInt(endTime.substring(4, 6))-1, 
				Integer.parseInt(endTime.substring(6)));
		
		List<String> fileNames = new ArrayList<String>();
		
		while(startDate.before(endDate) || startDate.equals(endDate)) {
			File fileDir = new File( dataHeadDir + 
					timeFileDir.format(startDate.getTime()) );
			
			if(fileDir.isDirectory()) {
				fileNames.addAll(getDayFile(fileDir));
			}
			startDate.add(Calendar.DATE, 1);
		}
		
		return fileNames;
	}
	
	// helper for getMarketFiles
	public static List<String> getDayFile(File fileDir) {
		List<String> fileNames = new ArrayList<String>();
		
		for(File f : fileDir.listFiles()) {
			if(f.isFile()) {
				fileNames.add(f.toString());
			}
		}
		return fileNames;
	}
	
}