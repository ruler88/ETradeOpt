package com.trade.rowData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.etrade.etws.market.AllQuote;
import com.trade.main.GetMarket;


public class Equity implements Serializable {
	private static final long serialVersionUID = -7999195545167588419L;
	transient Lock lock = new ReentrantLock();
	public final String ticker;
	List<Date> time = new ArrayList<Date>();
	List<Double> ask = new ArrayList<Double>();
	List<Double> bid = new ArrayList<Double>();
	List<Long> askSize = new ArrayList<Long>();
	List<Long> bidSize = new ArrayList<Long>();
	List<Long> numTrades = new ArrayList<Long>();
	List<Long> totalVolume = new ArrayList<Long>();
	
	public Equity(String ticker) {
		this.ticker = ticker;
	}
	
	public void updateInfo(AllQuote allInfo) {
		lock.lock();
		try {
			time.add(new Date());
			ask.add(allInfo.getAsk());
			bid.add(allInfo.getBid());
			askSize.add(allInfo.getAskSize());
			bidSize.add(allInfo.getBidSize());
			numTrades.add(allInfo.getNumTrades());
			totalVolume.add(allInfo.getTotalVolume());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
		lock.unlock();
	}
	
	public void truncateEquity() {
		//truncate equity values to only contains active market hours
		DateTimeZone dtz = DateTimeZone.forID(GetMarket.TZ);	//new york time
		
		int startIndex=0;
		for( ; startIndex<time.size(); startIndex++) {
			DateTime tmpTime = new DateTime(time.get(startIndex).getTime());
			tmpTime = tmpTime.withZone(dtz);
			if( tmpTime.hourOfDay().get() >= 9 && tmpTime.minuteOfHour().get() >= 30 ) {
				break;
			}
		}
		int endIndex = time.size()-1;
		for( ; endIndex>startIndex; endIndex--) {
			DateTime tmpTime = new DateTime(time.get(endIndex).getTime());
			tmpTime = tmpTime.withZone(dtz);
			if( tmpTime.hourOfDay().get() <= 16 && tmpTime.minuteOfHour().get() <= 0) {
				break;
			}
		}
		
		time = time.subList(startIndex, endIndex);
		ask = ask.subList(startIndex, endIndex);
		bid = bid.subList(startIndex, endIndex);
		askSize = askSize.subList(startIndex, endIndex);
		bidSize = bidSize.subList(startIndex, endIndex);
		numTrades = numTrades.subList(startIndex, endIndex);
		totalVolume = totalVolume.subList(startIndex, endIndex);
	}
	
	public void summarizeEquity(int factor) {
		//retain only elements mod factor zero
		if(factor == 0) return;	//don't fail me bro
		for(int i=time.size()-1; i>0; i--) {
			if(! (i%factor == 0)) {
				time.remove(i);
				ask.remove(i);
				bid.remove(i);
				askSize.remove(i);
				bidSize.remove(i);
				numTrades.remove(i);
				totalVolume.remove(i);
			}
		}
	}
	
	public void avgSummarizeEquity(int factor) {
		//average every "factor" points of data
		if(factor == 0) return;
		int count = 0;
		for(int i=time.size()-1; i>=0; i--) {
			count++;
			if(count == factor || i == 0) {
				EquityUtils.calcAverage(ask, i, count, ask.get(0));
				EquityUtils.calcAverage(bid, i, count, bid.get(0));
				EquityUtils.calcAverage(askSize, i, count, askSize.get(0));
				EquityUtils.calcAverage(bidSize, i, count, bidSize.get(0));
				EquityUtils.calcAverage(numTrades, i, count, numTrades.get(0));
				EquityUtils.calcAverage(totalVolume, i, count, totalVolume.get(0));
				count = 0;
			} else {
				time.remove(i);
				EquityUtils.removeAdd(ask, i, ask.get(0));
				EquityUtils.removeAdd(bid, i, bid.get(0));
				EquityUtils.removeAdd(askSize, i, askSize.get(0));
				EquityUtils.removeAdd(bidSize,  i,  bidSize.get(0));
				EquityUtils.removeAdd(numTrades, i, numTrades.get(0));
				EquityUtils.removeAdd(totalVolume, i, totalVolume.get(0));
			}
		}
	}
	
	public void appendEquity(Equity eq) {
		this.time.addAll(eq.time);
		this.ask.addAll(eq.ask);
		this.bid.addAll(eq.bid);
		this.askSize.addAll(eq.askSize);
		this.bidSize.addAll(eq.bidSize);
		this.numTrades.addAll(eq.numTrades);
		this.totalVolume.addAll(eq.totalVolume);
	}
	
	public boolean isCleanEquity() {
		int timeLength = time.size();
		if(ask == null || bid == null || askSize == null || bidSize == null ||
			numTrades == null || totalVolume == null) {
			System.err.println("Missing variable " + this.toString());
			return false;
		}
		if(ask.size() != timeLength || bid.size() != timeLength || 
			askSize.size() != timeLength || bidSize.size() != timeLength ||
			numTrades.size() != timeLength || totalVolume.size() != timeLength) {
			System.err.println("Mismatch length: ");
			System.err.println("time: " + time.size() + " ask: " + ask.size() + " bid: " + bid.size() +
					" askSize: " + askSize.size() + " bidSize: " + bidSize.size() + 
					" numTrades: " + numTrades.size() + " totalVolume: " + totalVolume.size()
					);
			System.err.println(this.toString());
			return false;
		}
		return true;
	}
	

	public List<Double> getAsk() {
		return ask;
	}
	public List<Double> getBid() {
		return bid;
	}
	public List<Date> getTime() {
		return time;
	}
	public List<Long> getAskSize() {
		return askSize;
	}
	public List<Long> getBidSize() {
		return bidSize;
	}
	public List<Long> getTotalVolume() {
		return totalVolume;
	}
	
	public void addInfo(Date date, Double ask, Double bid,
			Long askSize, Long bidSize, Long numTrades, Long totalVolume) {
		//this is for testing (mostly) add info
		this.time.add(date);
		this.ask.add(ask);
		this.bid.add(bid);
		this.askSize.add(askSize);
		this.bidSize.add(bidSize);
		this.numTrades.add(numTrades);
		this.totalVolume.add(totalVolume);
	}

	public HashMap<String, List> getVariableMap() {
		HashMap<String, List> equityVars = new HashMap<String, List>();
		List<Long> longTime = new ArrayList<Long>();
		for(Date timePoint : time) {
			longTime.add(timePoint.getTime());
		}
		
		equityVars.put("time", longTime);
		equityVars.put("ask", ask);
		equityVars.put("bid", bid);
		equityVars.put("askSize", askSize);
		equityVars.put("bidSize", bidSize);
		equityVars.put("numTrades", numTrades);
		equityVars.put("totalVolume", totalVolume);
		
		return equityVars;
	}
	
	@Override
	public String toString() {
		String s = "";
		s += "Symbol: " + ticker + "\n";
		s += "time: " + time + "\n";
		s += "ask: " + ask + "\n";
		s += "bid: " + bid + "\n";
		return s;
	}
	
	@Override
	public int hashCode() {
		return ticker.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o==null) {
			return false;
		}
		if( ! (o instanceof Equity )) {
			return false;
		}
		
		Equity eq = (Equity) o;
		return this.ticker.equals(eq.ticker);	
	}
}



