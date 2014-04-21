package com.trade.main;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.objectlab.kit.datecalc.common.DefaultHolidayCalendar;
import net.objectlab.kit.datecalc.jdk.CalendarDateCalculator;
import net.objectlab.kit.datecalc.jdk.CalendarForwardHandler;

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
		allFiles = getMarketFiles("20140310", "20140310");
		
		DataAnalysis test = new DataAnalysis(allFiles);
	}
	
	public DataAnalysis(List<String> files) {
		Hashtable<String, Equity> allEquity = new Hashtable<String, Equity>();
		for(String name : files) {
			Hashtable<String, Equity> oneDayEquity = DataStorage.deserializeFile(name);
			for(String s : oneDayEquity.keySet()) {
				if(!s.equals("GS:2014:3:22:CALL:165.00") && !s.equals("GS")) continue;
				Equity tmpEquity = oneDayEquity.get(s);
				System.err.println(tmpEquity.toString());
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
		viewEqs.add(allEquity.get("GS"));
		
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
		System.out.println("Length: " + time.size());
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
		Calendar startDate = new GregorianCalendar(Integer.parseInt(startTime.substring(0, 4)),
				Integer.parseInt(startTime.substring(4, 6))-1, 	//minus one 'cause calendar is dumb as shit
				Integer.parseInt(startTime.substring(6)));
		
		Calendar endDate = new GregorianCalendar(Integer.parseInt(endTime.substring(0, 4)), 
				Integer.parseInt(endTime.substring(4, 6))-1, 
				Integer.parseInt(endTime.substring(6)));
		
		List<String> fileNames = new ArrayList<String>();
		
		
		while(startDate.before(endDate) || startDate.equals(endDate)) {
			File fileDir = new File( dataHeadDir + 
					timeFileDir.format(startDate.getTime()) );
			
			if(fileDir.isDirectory()) {
				fileNames.addAll(getDayFile(fileDir));
			} else {
				CalendarDateCalculator dateCalc = getDateCalc();
				
				if(!dateCalc.isNonWorkingDay(startDate)) {
					System.err.println("ERROR: missing data on working day: " + timeFileDir.format(startDate.getTime()));
				}
			}
			startDate.add(Calendar.DATE, 1);
		}
		
		return fileNames;
	}
	
	public static CalendarDateCalculator getDateCalc() {
		Set<Calendar> holidayDates = new HashSet<Calendar>();
		//remember that 0=Jan, 11=Dec
		holidayDates.add(new GregorianCalendar(2014,0,1));	//new years
		holidayDates.add(new GregorianCalendar(2014,0,20));	//MLK
		holidayDates.add(new GregorianCalendar(2014,1,17));	//Washington bday
		holidayDates.add(new GregorianCalendar(2014,3,18));	//Good Friday
		holidayDates.add(new GregorianCalendar(2014,4,26));	//Memorial Day -- May 26, 2014
		holidayDates.add(new GregorianCalendar(2014,6,4));	//Independence Day -- July 4, 2014
		holidayDates.add(new GregorianCalendar(2014,8,1));	//Labor Day -- September 1, 2014
		holidayDates.add(new GregorianCalendar(2014,10,27));	//Thanksgiving Day -- November 27, 2014
		holidayDates.add(new GregorianCalendar(2014,11,25));	//Christmas Day -- December 25, 2014
		holidayDates.add(new GregorianCalendar(2015,0,1));	//New Year's Day -- January 1, 2015
		holidayDates.add(new GregorianCalendar(2015,0,2));	//Place holder to remind
		
		
		//calendarDateCalc figures out if the date is business day
		CalendarForwardHandler cfh = new CalendarForwardHandler();
		DefaultHolidayCalendar<Calendar> dhc = new DefaultHolidayCalendar<Calendar>(holidayDates);
		CalendarDateCalculator dateCalc = new CalendarDateCalculator("HolidayCalc", Calendar.getInstance(), dhc, cfh);
		
		return dateCalc;
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