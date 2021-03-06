package com.trade.tradeTraining.tradingModels;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.trade.rowData.Equity;

public abstract class TradingModelAbstract {
	protected List<String> filterList;
	protected float cashValue;			//cash on hand
	protected float equityValue;		//equity value based on closing day value
	protected HashMap<String, Integer> holdings = new HashMap<String, Integer>();
	protected HashMap<String, Double> equityValueCache = new HashMap<String, Double>();
	
	private String csvFileName = "/mnt/tradingSim/";
	private FileWriter csvWriter;
	
	public abstract void equityFilter(Hashtable<String,Equity> equityList);
	
	public abstract void emulateDailyTrade(Hashtable<String,Equity> equityList) throws Throwable;
	
	public TradingModelAbstract(String startDate, String endDate, 
		String modelName, List<String> filterList) throws IOException {
		//This constructor creates the record csv file
		String fileName = modelName + ":" + startDate + "-" + endDate + ".csv";
		csvFileName += fileName;
		File outputFile = new File(csvFileName);
		outputFile.delete();
		outputFile.createNewFile();
		
		csvWriter = new FileWriter(outputFile);
		csvWriter.write("Trading recording model: " + modelName + "\n");
		csvWriter.write("Filter list - \n");
		for(String s : filterList) csvWriter.write(s + "\n");
		csvWriter.write("\n\n");
		csvWriter.write("Transaction,Equity,Price,Quantity,Time,Delta,Capital" + "\n");
	}
	
	public void setFilter(List<String> filterList) {
		this.filterList = filterList;
	}
	
	public void setCapital(float capital) {
		this.cashValue = capital;
	}
	
	public boolean buyEquity(String eq, int quantity, double price, Date time) throws IOException {
		if(quantity == 0) return false;
		if(quantity * price > cashValue) {
			return false;
		}
		
		cashValue -= (quantity * price);
		if(holdings.containsKey(eq)) {
			holdings.put(eq, holdings.get(eq) + quantity);
		} else {
			holdings.put(eq, quantity);
		}
		csvWriter.write("Buy,"+eq+","+price+","+quantity+","+time+","+(quantity * price * -1)+","+cashValue+"\n");
		System.out.println("Buy,"+eq+","+price+","+quantity+","+time+","+(quantity * price)+","+cashValue+"\n");
		return true;
	}
	
	public boolean sellEquity(String eq, int quantity, double price, Date time) throws IOException {
		if(quantity == 0) return false;
		if(!holdings.containsKey(eq) || holdings.get(eq) < quantity) {
			return false;
		}
		
		cashValue += (quantity * price);
		int holdingQuantity = holdings.get(eq) - quantity;
		if(holdingQuantity == 0) {
			holdings.remove(eq);
		} else {
			holdings.put(eq, holdingQuantity);
		}
		csvWriter.write("Sell,"+eq+","+price+","+quantity+","+time+","+(quantity * price)+","+cashValue+"\n");
		System.out.println("Sell,"+eq+","+price+","+quantity+","+time+","+(quantity * price)+","+cashValue+"\n");
		return true;
	}
	
	public boolean clearPosition(String eq, double price, Date time) throws IOException {
		//sell all position of equity X
		if(!holdings.containsKey(eq)) {
			return false;
		}
		
		int quantity = holdings.get(eq);
		return sellEquity(eq, quantity, price, time);
	}
	
	public float getCashValue() {
		return cashValue;
	}
	
	public float getEquityValue() {
		for(String s : holdings.keySet()) {
			Integer quantity = holdings.get(s);
			equityValue += equityValueCache.get(s) * quantity;
		}
		return equityValue;
	}
	
	public void finish() throws IOException {
		getEquityValue();
		System.out.println("Record written to " + csvFileName);
		csvWriter.flush();
		csvWriter.close();
	}
	
}
