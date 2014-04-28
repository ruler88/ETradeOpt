package com.trade.tradeTraining.tradingModels;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import com.trade.rowData.Equity;

public class LowPassFilterModel extends TradingModelAbstract {

	public LowPassFilterModel(String startDate, String endDate,
			String modelName, List<String> filterList) throws IOException {
		super(startDate, endDate, modelName, filterList);
	}

	@Override
	public void equityFilter(Hashtable<String, Equity> equityList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void emulateDailyTrade(Hashtable<String, Equity> equityList)
			throws Throwable {
		// TODO Auto-generated method stub
		
	}

}
