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
	private static final String startDate = "20140227";
	private static final String endDate =   "20140303";
	private static final float capital = 10000;
	private static final List<String> filterList = new ArrayList<String>();
	
	//CHANGE trading model class to use different models
	private final TradingModelAbstract tradingModel = new TM_MovingAverageIntersect();
	
	public static void main(String[] args) {
		new MainTradeTrainer();
	}
	
	public MainTradeTrainer() {
		tradingModel.setFilter(filterList);
		tradingModel.setCapital(capital);
		
		List<String> marketFiles = DataAnalysis.getMarketFiles(startDate, endDate);
		
		for(int i=0; i<marketFiles.size(); i++) {
			Hashtable<String, Equity> dailyEq = DataStorage.deserializeFile(marketFiles.get(i));
			tradingModel.equityFilter(dailyEq);
			tradingModel.emulateDailyTrade(dailyEq);
		}
	}
	
	
	
	
	
	

}
