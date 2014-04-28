package com.trade.tradeTraining.tradingModels;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.trade.rowData.Equity;

public class LowPassFilterModel extends TradingModelAbstract {
	private final double ALPHA = 0.2;		//alpha var for LPF, the lower the value, the more smooth the output
	private HashMap<String, Double> filterCache = new HashMap<String, Double>(); 	//cache of latest filter output
	
	private String csvFileName = "/mnt/tradingSim/MATracker.csv";
	private FileWriter csvWriter;
	
	public LowPassFilterModel(String startDate, String endDate,
			String modelName, List<String> filterList) throws IOException {
		super(startDate, endDate, modelName, filterList);
		
		File outputFile = new File(csvFileName);
		outputFile.delete();
		outputFile.createNewFile();
		csvWriter = new FileWriter(csvFileName);
		csvWriter.write("Equity,Time,Ask,Bid,AskFilter" + "\n");
	}

	@Override
	public void emulateDailyTrade(Hashtable<String, Equity> equityList)
			throws Throwable {
		for(String eqKey : equityList.keySet()) {
			Equity eq = equityList.get(eqKey);
			if( !eq.isCleanEquity() ) {
				System.err.print("UNCLEAN EQUITY: \n" + eq.toString());
				throw new IllegalArgumentException("Unclean equity exception");
			}
			
			long lastTime = 0;
			for(int i=0; i<eq.getTime().size(); i++) {
				long curTime = eq.getTime().get(i).getTime() / 1000 / 60;	//check every minute
				double ask = eq.getAsk().get(i);
				double bid = eq.getBid().get(i);
				
				if(curTime <= lastTime) {
					continue;
				}
				
				if(!filterCache.containsKey(eqKey)) {
					filterCache.put(eqKey, ask);
				} else {
					//TODO: do decision here
					double filterOutput = LowPassFilterHelperB(ask, eqKey);	//change for different methods
					csvWriter.write(eqKey+","+eq.getTime().get(i)+","+ask+","+bid+","+filterOutput+"\n");
				}
				
				equityValueCache.put(eqKey, ask);
			}
		}
		csvWriter.flush();
	}
	
	private double LowPassFilterHelperA(double ask, String eqKey) {
		double filterOutput = filterCache.get(eqKey) * ALPHA + ask * (1-ALPHA);
		filterCache.put(eqKey, filterOutput);
		return filterOutput;
	}
	
	private double LowPassFilterHelperB(double ask, String eqKey) {
		double filterOutput = filterCache.get(eqKey) + ALPHA * (ask - filterCache.get(eqKey));
		filterCache.put(eqKey, filterOutput);
		return filterOutput;
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

}
