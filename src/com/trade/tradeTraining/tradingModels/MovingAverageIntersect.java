package com.trade.tradeTraining.tradingModels;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.trade.rowData.Equity;

/*
 * This model trades base on SMA
 * When the price exceeds SMA, buy equity; when price falls short of SMA, sell equity
 * @Change MVperiod for # of minutes in the moving average calculation
 */
public class MovingAverageIntersect extends TradingModelAbstract {
	private final int MVperiod = 40;
	Hashtable<String, MovingAverageVars> MAcache = new Hashtable<String, MovingAverageVars>();
	
	private String csvFileName = "/mnt/tradingSim/MATracker.csv";
	private FileWriter csvWriter;
	
	
	public MovingAverageIntersect(String startDate, String endDate,
			String modelName, List<String> filterList) throws IOException {
		super(startDate, endDate, modelName, filterList);
		
		//tracking output csv
		File outputFile = new File(csvFileName);
		outputFile.delete();
		outputFile.createNewFile();
		csvWriter = new FileWriter(csvFileName);
		csvWriter.write("Equity,Time,Ask,Bid,ThisMA" + "\n");
	}
	
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
				long curTime = eq.getTime().get(i).getTime() / 1000 / 60;	//check every minute
				double ask = eq.getAsk().get(i);
				double bid = eq.getBid().get(i);
				if(!MAcache.containsKey(eqKey)) {
					MAcache.put( eqKey, new MovingAverageVars(ask, 1, curTime) );
					continue;
				}
				MovingAverageVars MAV = MAcache.get(eqKey);
				if(curTime <= MAV.lastTime) {
					continue;
				}
				
				//updating moving average as the simulation goes forward
				MAV.lastTime = curTime;
				MAV.movingSum += ask;
				if(MAV.n < MVperiod) {
					MAV.n += 1;
					MAV.updateMA(ask);
				} else {
					MAV.movingSum -= MAV.priceQueue.poll();
					double lastAsk = MAV.priceQueue.getLast();
					double thisMA = MAV.updateMA(ask);
					csvWriter.write(eqKey+","+eq.getTime().get(i)+","+ask+","+bid+","+thisMA +"\n");
					if( thisMA >= lastAsk && thisMA < ask ) {
						int quantity = (int) (cashValue / 8 / ask);
						buyEquity(eqKey, quantity, ask, eq.getTime().get(i));
					}
					if( thisMA <= lastAsk && thisMA > ask ) {
						clearPosition(eqKey, bid, eq.getTime().get(i));
					}
				}
				equityValueCache.put(eqKey, ask);
			}
		}
		csvWriter.flush();
	}
	
	private class MovingAverageVars {
		private double movingSum;
		private int n;
		private long lastTime; //dateTime / 1000
		private double MA;
		private final LinkedList<Double> priceQueue = new LinkedList<Double>();
		
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
