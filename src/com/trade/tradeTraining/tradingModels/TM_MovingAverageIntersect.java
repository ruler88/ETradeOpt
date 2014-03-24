package com.trade.tradeTraining.tradingModels;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.trade.rowData.Equity;

public class TM_MovingAverageIntersect extends TradingModelAbstract {
	private final int MVperiod = 50;
	Hashtable<String, MovingAverageVars> MAcache = new Hashtable<String, MovingAverageVars>();
	
	@Override
	public void equityFilter(Hashtable<String, Equity> equity) {
		if(filterList == null) return;	//no filter in place, use all eq
		
		HashSet<String> filterSet = new HashSet<String>(filterList);
		Iterator<Map.Entry<String, Equity>> it = equity.entrySet().iterator();
		
		while(it.hasNext()) {
			Map.Entry<String, Equity> equityEntry = it.next();
			if( !filterSet.contains(equityEntry.getKey()) ) {
				it.remove();
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
				long curTime = eq.getTime().get(i).getTime() % 1000;
				double ask = eq.getAsk().get(i);
				double bid = eq.getBid().get(i);
				if(!MAcache.containsKey(eqKey)) {
					MAcache.put( eqKey, new MovingAverageVars(ask, 1, curTime, i) );
					continue;
				}
				
				MovingAverageVars MAV = MAcache.get(eqKey);
				if(curTime <= MAV.lastTime) {
					continue;
				}
				
				MAV.lastTime = curTime;
				MAV.movingSum += ask;
				if(MAV.n < MVperiod) {
					MAV.n += 1;
					MAV.updateMA();
				} else {
					MAV.movingSum -= eq.getAsk().get(MAV.headIndex);
					MAV.headIndex += 1;
					double lastMA = MAV.MA;
					double thisMA = MAV.updateMA();
					if( lastMA <= ask && thisMA > ask ) {
						int quantity = (int) (cashValue / 10 / ask);
						buyEquity(eqKey, quantity, ask, eq.getTime().get(i));
					}
					if( lastMA >= ask && thisMA < ask ) {
						clearPosition(eqKey, bid, eq.getTime().get(i));
					}
				}
			}
		}
	}
	
	private class MovingAverageVars {
		public double movingSum;
		public int n;
		public long lastTime; //dateTime % 1000
		public int headIndex;
		public double MA;
		public MovingAverageVars (double movingSum, int n, long lastTime, int headIndex) {
			this.movingSum = movingSum;
			this.n = n;
			this.lastTime = lastTime;
			this.headIndex = headIndex;
			this.MA = movingSum / n;
		}
		
		public double updateMA() {
			MA = movingSum / n;
			return MA;
		}
	}

}
