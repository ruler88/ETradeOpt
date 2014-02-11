package com.trade.main;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import com.etrade.etws.sdk.client.ClientRequest;
import com.etrade.etws.sdk.client.Environment;
import com.etrade.etws.sdk.client.MarketClient;
import com.etrade.etws.sdk.common.ETWSException;

public class GetOption {
	public static void main(String[] args) throws ETWSException, IOException {
		String oauth_access_token = "dildzDcce7zaLX4YXwRz797yUvvLzzn6EwBMI9O774I=";
		String oauth_access_token_secret = "UDe15GYw/L180BH+k0T4y7OpDrqtDAp5foCHuYz7kOw=";
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("GOOG");
		list.add("GE");
		
		String oauth_consumer_key = "628c14e46b6744606fdb18fff4b91d33"; // Your consumer key
		String oauth_consumer_secret = "ede49fab7ad76aa98623278367ef22de"; // Your consumer secret
		
		ClientRequest request = new ClientRequest();
		request.setEnv(Environment.LIVE);
		request.setConsumerKey(oauth_consumer_key);
		request.setConsumerSecret(oauth_consumer_secret);
		request.setToken(oauth_access_token);
		request.setTokenSecret(oauth_access_token_secret);
		
		MarketClient client = new MarketClient(request);
		
		//addOptionList(client, list);
		addExpiringOptions(client, list);
		for(String s : list) {
			System.out.println(s);
		}
	}

	
	public static void addExpiringOptions(MarketClient client, ArrayList<String> list) throws IOException, ETWSException {
		ArrayList<String> expOptions = new ArrayList<String>();
		OptionExpireDateGetRequest req = new OptionExpireDateGetRequest();
		for(String underlier : list) {
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
		}
		for(String s : expOptions) {
			list.add(s);
		}
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
		for(OptionChainPair op : response.getOptionPairs()) {
			for(CallOptionChain co : op.getCall()) {
				if(co.getStrikePrice().doubleValue() > underlierPrice) {
					//add price above and below stock price point
					String optionSymbol = underlier + ":" + edate.getYear() + ":" + edate.getMonth() + ":" + edate.getDay() + ":" + "CALL:" + co.getStrikePrice().setScale(2, BigDecimal.ROUND_UNNECESSARY);
					list.add(optionSymbol);
					optionSymbol = underlier + ":" + edate.getYear() + ":" + edate.getMonth() + ":" + edate.getDay() + ":" + "CALL:" + lastStrikePrice;
					list.add(optionSymbol);
					optionSymbol = underlier + ":" + edate.getYear() + ":" + edate.getMonth() + ":" + edate.getDay() + ":" + "PUT:" + co.getStrikePrice().setScale(2, BigDecimal.ROUND_UNNECESSARY);
					list.add(optionSymbol);
					optionSymbol = underlier + ":" + edate.getYear() + ":" + edate.getMonth() + ":" + edate.getDay() + ":" + "PUT:" + lastStrikePrice;
					list.add(optionSymbol);
					return;
				}
				lastStrikePrice = co.getStrikePrice().setScale(2, BigDecimal.ROUND_UNNECESSARY);
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
	
	
}
