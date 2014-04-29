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

//TODO: make comments about this model

public class LowPassFilterModel extends TradingModelAbstract {
	private final float ALPHA = 0.8f;		//alpha var for LPF, the lower the value, the more smooth the output
	private HashMap<String, HashMap<String, Long>> filterCache = new HashMap<String, HashMap<String, Long>>(); 	//cache of latest filter output, second layer of map contains BID,ASK,or BIDASKDIFF

	//some model constants
	private static final String BID = "BID";
	private static final String ASK = "ASK";
	private static final String BIDASKDIFF = "BIDASKDIFF";
	
	private String csvFileName = "/mnt/tradingSim/MATracker.csv";
	private FileWriter csvWriter;
	
	public LowPassFilterModel(String startDate, String endDate,
			String modelName, List<String> filterList) throws IOException {
		super(startDate, endDate, modelName, filterList);
		
		File outputFile = new File(csvFileName);
		outputFile.delete();
		outputFile.createNewFile();
		csvWriter = new FileWriter(csvFileName);
		csvWriter.write("Equity,Time,AskSize,AskFilter,BidSize,BidFilter,BidAskDiffSize,BidAskFilter,AskPrice" + "\n");
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
				long curTime = eq.getTime().get(i).getTime() / 1000 / 30;	//check every 30s
				long askSize = eq.getAskSize().get(i);
				long bidSize = eq.getBidSize().get(i);
				
				if(curTime <= lastTime) {
					continue;
				}
				
				if(!filterCache.containsKey(eqKey)) {
					HashMap<String, Long> cacheVars = new HashMap<String, Long>();
					cacheVars.put(ASK, askSize);
					cacheVars.put(BID, bidSize);
					cacheVars.put(BIDASKDIFF, bidSize-askSize);
					
					filterCache.put(eqKey, cacheVars);
				} else {
					HashMap<String, Long> cacheVars = filterCache.get(eqKey);
					long askCache = (long) (cacheVars.get(ASK) * ALPHA + askSize * (1-ALPHA));
					long bidCache = (long) (cacheVars.get(BID) * ALPHA + bidSize * (1-ALPHA));
					long bidAskCache = (long) (cacheVars.get(BIDASKDIFF) * ALPHA + (bidSize-askSize) * (1-ALPHA));
					
					cacheVars.put(ASK, askCache);
					cacheVars.put(BID, bidCache);
					cacheVars.put(BIDASKDIFF, bidAskCache);
					
					//csvWriter.write("Equity,Time,Ask,AskFilter,Bid,BidFilter,BidAskDiff,BidAskFilter" + "\n");
					csvWriter.write(eqKey+","+eq.getTime().get(i)+","+askSize+","+askCache+","+
							bidSize+","+bidCache+","+(bidSize-askSize)+","+bidAskCache+","+ 
							eq.getAsk().get(i)+"\n");
					
					filterCache.put(eqKey, cacheVars);
				}
				
				equityValueCache.put(eqKey, eq.getAsk().get(i));
			}
		}
		csvWriter.flush();
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
