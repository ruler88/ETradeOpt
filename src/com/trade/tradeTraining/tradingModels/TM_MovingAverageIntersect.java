package com.trade.tradeTraining.tradingModels;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.trade.rowData.Equity;

public class TM_MovingAverageIntersect extends TradingModelAbstract {
	private final int MVperiod = 30;
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
	public void emulateDailyTrade(Hashtable<String, Equity> equityList) throws Throwable {
		//get data once per minute to get the MA per 30 mins
		for(String eqKey : equityList.keySet()) {
			Equity eq = equityList.get(eqKey);
			if( !eq.isCleanEquity() ) {
				System.err.print("UNCLEAN EQUITY: \n" + eq.toString());
				throw new IllegalArgumentException("Unclean equity exception");
			}
			
			for(int i=0; i<eq.getTime().size(); i++) {
				long curTime = eq.getTime().get(i).getTime() / 1000;
				double ask = eq.getAsk().get(i);
				double bid = eq.getBid().get(i);
				if(!MAcache.containsKey(eqKey)) {
					MAcache.put( eqKey, new MovingAverageVars(ask, 1, curTime) );
					continue;
				}
				
				MovingAverageVars MAV = MAcache.get(eqKey);
				//System.out.println("CurTime: " + curTime + " lastTIme: " + MAV.lastTime);
				if(curTime <= MAV.lastTime) {
					continue;
				}
				
				System.out.println(MAV);
				
				MAV.lastTime = curTime;
				MAV.movingSum += ask;
				if(MAV.n < MVperiod) {
					MAV.n += 1;
					MAV.updateMA(ask);
				} else {
					MAV.movingSum -= MAV.priceQueue.poll();
					double lastMA = MAV.MA;
					double thisMA = MAV.updateMA(ask);
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
		private double movingSum;
		private int n;
		private long lastTime; //dateTime / 1000
		private double MA;
		private final Queue<Double> priceQueue = new LinkedList<Double>();
		
		public MovingAverageVars (double movingSum, int n, long lastTime) {
			this.movingSum = movingSum;
			this.n = n;
			this.lastTime = lastTime;
			this.MA = movingSum / n;
		}
		
		public double updateMA(double price) {
			priceQueue.add(price);
			//code here
			MA = movingSum / n;
			return MA;
		}
		
		@Override
		public String toString() {
			String s = "movingSum: " + movingSum + "\n";
			s += "n: " + n + "\n";
			s += "lastTime: " + lastTime + "\n";
			s += "MA: " + MA + "\n";
					
			return s;
		}
	}

}
