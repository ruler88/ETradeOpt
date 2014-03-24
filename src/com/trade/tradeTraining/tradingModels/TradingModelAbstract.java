package com.trade.tradeTraining.tradingModels;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import com.trade.rowData.Equity;

public abstract class TradingModelAbstract {
	protected List<String> filterList;
	protected float cashValue;			//cash on hand
	protected HashMap<String, Integer> holdings = new HashMap<String, Integer>();
	
	public abstract void equityFilter(Hashtable<String,Equity> equityList);
	
	public abstract void emulateDailyTrade(Hashtable<String,Equity> equityList);
	
	public void setFilter(List<String> filterList) {
		this.filterList = filterList;
	}
	
	public void setCapital(float capital) {
		this.cashValue = capital;
	}
	
	public boolean buyEquity(String eq, int quantity, double price, Date time) {
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
		return true;
	}
	
	public boolean sellEquity(String eq, int quantity, double price, Date time) {
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
		return true;
	}
	
	public boolean clearPosition(String eq, double price) {
		//sell all position of equity X
		if(!holdings.containsKey(eq)) {
			return false;
		}
		
		int quantity = holdings.get(eq);
		cashValue += (quantity * price);
		holdings.remove(eq);
		return true;
	}
	
}
