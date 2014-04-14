package com.trade.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.etrade.etws.market.AllQuote;
import com.etrade.etws.market.CallOptionChain;
import com.etrade.etws.market.DetailFlag;
import com.etrade.etws.market.ExpirationDate;
import com.etrade.etws.market.OptionChainPair;
import com.etrade.etws.market.OptionChainRequest;
import com.etrade.etws.market.OptionChainResponse;
import com.etrade.etws.market.OptionExpireDateGetRequest;
import com.etrade.etws.market.OptionExpireDateGetResponse;
import com.etrade.etws.market.PutOptionChain;
import com.etrade.etws.market.QuoteData;
import com.etrade.etws.market.QuoteResponse;
import com.etrade.etws.sdk.client.MarketClient;
import com.etrade.etws.sdk.common.ETWSException;

public class GetOption {
	private static HashSet<String> persistList = new HashSet<String>();	//list of equities that we try to persist if they still exist
	private static String logFile = "/home/ubuntu/GetOption.log";
	private static FileWriter fileWritter = null;
	
	public static void addExpiringOptions(MarketClient client, List<String> list) throws IOException, ETWSException {
		if(new File(logFile).exists()) {
			fileWritter = new FileWriter(logFile, true);
		}
		ArrayList<String> expOptions = new ArrayList<String>();
		OptionExpireDateGetRequest req = new OptionExpireDateGetRequest();
		for(String underlier : list) {
			try {
				req.setUnderlier(underlier);
				OptionExpireDateGetResponse response = client.getExpiryDates(req);
				List<ExpirationDate> allExpDates = response.getExpireDates();
				if(allExpDates == null || allExpDates.isEmpty()) {
					continue;	//don't add anything for this underlier equity if system finds fucked up exp dates
				} else {
					double underlierPrice = getMostRecentPrice(client, underlier);
					appendOptionsNearPrice(client, underlier, allExpDates.get(0), expOptions, underlierPrice);
					appendOptionsNearPrice(client, underlier, allExpDates.get(1), expOptions, underlierPrice);
				}
			} catch (Exception e) {
				if(new File(logFile).exists()) {
					fileWritter.write(underlier + " failed");
					if(e!=null) fileWritter.write(e.toString());
				}
			}
		}
		for(String s : expOptions) {
			list.add(s);
		}
		removeDup(list);
		Collections.sort(list);
	}
	
	private static void appendOptionsNearPrice(MarketClient client, String underlier, ExpirationDate edate, ArrayList<String> list, double underlierPrice) throws IOException, ETWSException {
		OptionChainRequest req = new OptionChainRequest();
		req.setExpirationMonth(edate.getMonth());
		req.setExpirationYear(edate.getYear()+"");
		req.setChainType("CALLPUT");
		req.setUnderlier(underlier);
		OptionChainResponse response = client.getOptionChain(req);
		
		BigDecimal lastStrikePrice = new BigDecimal(0);
		boolean findingPrice = true;
		for(OptionChainPair op : response.getOptionPairs()) {
			for(CallOptionChain co : op.getCall()) {
				if(co.getStrikePrice().doubleValue() > underlierPrice && findingPrice) {
					//add price above and below stock price point
					String optionSymbol = underlier + ":" + edate.getYear() + ":" + edate.getMonth() + ":" + edate.getDay() + ":" + "CALL:" + co.getStrikePrice().setScale(2, BigDecimal.ROUND_UNNECESSARY);
					list.add(optionSymbol);
					optionSymbol = underlier + ":" + edate.getYear() + ":" + edate.getMonth() + ":" + edate.getDay() + ":" + "CALL:" + lastStrikePrice;
					list.add(optionSymbol);
					optionSymbol = underlier + ":" + edate.getYear() + ":" + edate.getMonth() + ":" + edate.getDay() + ":" + "PUT:" + co.getStrikePrice().setScale(2, BigDecimal.ROUND_UNNECESSARY);
					list.add(optionSymbol);
					optionSymbol = underlier + ":" + edate.getYear() + ":" + edate.getMonth() + ":" + edate.getDay() + ":" + "PUT:" + lastStrikePrice;
					list.add(optionSymbol);
					findingPrice = false;
				}
				
				lastStrikePrice = co.getStrikePrice().setScale(2, BigDecimal.ROUND_UNNECESSARY);
				
				//add on items from persist list
				String optionSymbol = underlier + ":" + edate.getYear() + ":" + edate.getMonth() + ":" + edate.getDay() + ":" + "CALL:" + lastStrikePrice;
				if(persistList != null && persistList.contains(optionSymbol)) {
					list.add(optionSymbol);
				}
				optionSymbol = underlier + ":" + edate.getYear() + ":" + edate.getMonth() + ":" + edate.getDay() + ":" + "PUT:" + lastStrikePrice;
				if(persistList != null && persistList.contains(optionSymbol)) {
					list.add(optionSymbol);
				}
			}
		}
	}
	
	
	//get last trade price for single stock
	private static double getMostRecentPrice(MarketClient client, String symbol) throws IOException, ETWSException {
		ArrayList<String> list = new ArrayList<String>();
		list.add(symbol);
		QuoteResponse response = client.getQuote(list, true, DetailFlag.ALL);
		for(QuoteData qd : response.getQuoteData()) {
			AllQuote allInfo = qd.getAll();
			return allInfo.getLastTrade();
		}
		return 0.0;
	}
	
	//appends all options to the ArrayList "list"
	public static void addOptionList(MarketClient client, ArrayList<String> list) throws IOException, ETWSException {
		OptionExpireDateGetRequest req = new OptionExpireDateGetRequest();
		
		ArrayList<String> newList = new ArrayList<String>();
		for(String underlier : list) {
			req.setUnderlier(underlier);
			OptionExpireDateGetResponse response = client.getExpiryDates(req);
			for(ExpirationDate ed : response.getExpireDates()) {
				requestOptionChain(client, underlier, ed, newList);
			}
			System.out.println();
		}
		list.addAll(newList);
	}
	
	
	private static void requestOptionChain(MarketClient client, String underlier, ExpirationDate edate, ArrayList<String> list) throws IOException, ETWSException {
		OptionChainRequest req = new OptionChainRequest();
		req.setExpirationMonth(edate.getMonth());
		req.setExpirationYear(edate.getYear()+"");
		req.setChainType("CALLPUT");
		req.setUnderlier(underlier);
		OptionChainResponse response = client.getOptionChain(req);
		
		for(OptionChainPair op : response.getOptionPairs()) {
			for(CallOptionChain co : op.getCall()) {
				String optionSymbol = underlier + ":" + edate.getYear() + ":" + edate.getMonth() + ":" + edate.getDay() + ":" + "CALL:" + co.getStrikePrice().setScale(2, BigDecimal.ROUND_UNNECESSARY);
				list.add(optionSymbol);
			}
			
			for(PutOptionChain co : op.getPut()) {
				String optionSymbol = underlier + ":" + edate.getYear() + ":" + edate.getMonth() + ":" + edate.getDay() + ":" + "PUT:" + co.getStrikePrice().setScale(2, BigDecimal.ROUND_UNNECESSARY);
				list.add(optionSymbol);
			}
		}
	}
	
	public static void setPersistList(List<String> newPersistList) {
		if(newPersistList == null) { return; }	//error check
		for(String s : newPersistList) {
			persistList.add(s);
		}
	}
	
	public static void removeDup(List<String> list) {
		HashSet<String> hs = new HashSet<String>(list);
		list.clear();
		list.addAll(hs);
	}
	
	
}
