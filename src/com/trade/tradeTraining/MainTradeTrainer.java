package com.trade.tradeTraining;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.trade.main.DataAnalysis;
import com.trade.rowData.DataStorage;
import com.trade.rowData.Equity;
import com.trade.tradeTraining.tradingModels.TM_MovingAverageIntersect;
import com.trade.tradeTraining.tradingModels.TradingModelAbstract;

public class MainTradeTrainer {
	//change these variables for trade simulation
	//private static final String startDate = "20140321";
	private static final String startDate = "20140310";
	private static final String endDate =   "20140312";
	private static final float capital = 10000;
	private static final List<String> filterList = new ArrayList<String>();
	
	//CHANGE trading model class to use different models
	private static TradingModelAbstract tradingModel;
	
	public static void main(String[] args) throws Throwable {
		filterList.add("GS");
		tradingModel = new TM_MovingAverageIntersect(startDate, endDate, TM_MovingAverageIntersect.class.getSimpleName(), filterList);
		new MainTradeTrainer();
	}
	
	public MainTradeTrainer() throws Throwable {
		tradingModel.setFilter(filterList);
		tradingModel.setCapital(capital);
		
		List<String> marketFiles = DataAnalysis.getMarketFiles(startDate, endDate);
		
		for(int i=0; i<marketFiles.size(); i++) {
			Hashtable<String, Equity> dailyEq = DataStorage.deserializeFile(marketFiles.get(i));
			tradingModel.equityFilter(dailyEq);
			tradingModel.emulateDailyTrade(dailyEq);
		}
		
		tradingModel.finish();
		System.out.println("Trade simulation COMPLETE,\nfinal capital: " + tradingModel.getCashValue() + tradingModel.getEquityValue());
	}

}
