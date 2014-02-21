package com.trade.tradingMethod;

import java.util.Date;
import java.util.List;

import com.trade.rowData.Equity;

public class OptionStrikePoint {
	
	public static void process(Equity underlier, Equity call, Equity put) {
		double percentMovementPoint = 0.4 * 0.01;		//waiting for 0.4% movement
		long timeDiff = 1000*60*5;		//indicate limit after reading strike zone [in ms] (5 mins)
		double strikePrice = TradeMethodUtils.getStrikePrice(call.ticker);
		double denom = 0.0;
		double numa = 0.0;
		
		List<Date> time = underlier.getTime();
		
		for(int i=0; i<time.size(); i++) {
			double delta = ( underlier.getAsk().get(i) - strikePrice ) / underlier.getAsk().get(i);
			//let's do call first!
			if(Math.abs(delta) <= percentMovementPoint && delta < 0) {
				//in strike zone!
				long zoneTime = time.get(i).getTime();
				denom += 1;
				for(; i<time.size() && time.get(i).getTime() - zoneTime < timeDiff; i++) {
					//something happens here!
					if(underlier.getAsk().get(i) > strikePrice) {
						denom += 1;
						break;
					}
				}
			}
		}
		
		
		if(denom > 0) {
			System.out.println(numa/denom);
		} else {
			System.out.println("no occurence");
		}
		
		
	}

}
