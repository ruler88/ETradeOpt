package com.trade.tradeTraining;

public class TradeMethodUtils {
	
	public static double getStrikePrice(String s) {
		//an option ticker looks like this: "GOOG:2014:1:18:PUT:1120.00"
		//get strike price from ticker
		
		String components[] = s.split(":");
		double val = Double.parseDouble(components[5]);
		
		return val;
	}

}
