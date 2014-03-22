package com.trade.tradeTraining.tradingModels;

import java.util.HashSet;
import java.util.Hashtable;

import com.trade.rowData.Equity;

public class TM_MovingAverageIntersect extends TradingModelAbstract {
	

	@Override
	public void equityFilter(Hashtable<String, Equity> equity) {
		HashSet<String> filterSet = new HashSet<String>(filterList);
		
		for(String eqKey : equity.keySet()) {
			if(!filterSet.contains(eqKey)) {
				equity.remove(eqKey);
			}
		}
	}

	@Override
	public void emulateDailyTrade(Hashtable<String, Equity> equityList) {
		// TODO DO THIS SHIT
		
	}

}
