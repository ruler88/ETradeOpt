package com.trade.tradeTraining.tradingModels;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import com.trade.rowData.Equity;

public class TM_MovingAverageIntersect extends TradingModelAbstract {
	Hashtable<String, MovingAverageVars> MAcache = new Hashtable<String, MovingAverageVars>();
	

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
		//get data once per minute to get the MA per 30 mins
		
		for(String eqKey : equityList.keySet()) {
			Equity eq = equityList.get(eqKey);
			if( !eq.isCleanEquity() ) {
				System.err.print("UNCLEAN EQUITY: \n" + eq.toString());
				return;
			}
			
			for(int i=0; i<eq.getTime().size(); i++) {
				
				/*
				 * for(int i=0; i<var.size();) {
						movingSum += var.get(i);
						i++;
						if(i <= n) {
							SMA.add(movingSum / i);
						} else {
							movingSum -= var.get(headIndex);
							headIndex++;
							SMA.add(movingSum / n);
						}
					}
				 */
			}
		}
		
	}
	
	private class MovingAverageVars {
		public double movingSum;
		public int n;
		public float lastTime; //dateTime % 1000
		public MovingAverageVars (double movingSum, int n, float lastTime) {
			this.movingSum = movingSum;
			this.n = n;
			this.lastTime = lastTime;
		}
	}

}
