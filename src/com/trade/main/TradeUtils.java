package com.trade.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class TradeUtils {
	
	public static void loadDailyHours(Queue<Integer> hours) {
		//load all hour marks of trading time, for logging + reporting purposes
		for(int i = 10; i<= (5+12); i++) {
			hours.offer(i);
		}
	}
	
	public static String getUnderlier(String eq) {
		if(eq == null) return "";
		return eq.split(":")[0];
	}
	public static boolean isOption(String eq) {
		return eq.contains(":");
	}
	
	public static ArrayList<ArrayList<String>> getEquityThreadList(List<String> list) {
		ArrayList<ArrayList<String>> allThreadsList = new ArrayList<ArrayList<String>>();
		
		ArrayList<String> newList = new ArrayList<String>();
		int urlReqLen = "https://etws.etrade.com/market/rest/quote/".length() + 5;
		int reqCount = 0;
		for(int i=0; i<list.size(); i++) {
			String prevUnderlier = getUnderlier( i>0 ? list.get(i-1) : list.get(i) );
			String currentUnderlier = getUnderlier( list.get(i) );
			urlReqLen += (list.get(i).length()+1);
			reqCount += 1;
			
			if( !currentUnderlier.equals(prevUnderlier) || 
					urlReqLen > 2000 || reqCount > 25 ) {	//url length limit is 2k and request count max is 25
				allThreadsList.add(newList);
				urlReqLen = "https://etws.etrade.com/market/rest/quote/".length() + 5;
				reqCount = 0;
				
				newList = new ArrayList<String>();
				newList.add( list.get(i) );
				urlReqLen += (list.get(i).length()+1);
				reqCount += 1;
			} else {
				newList.add(list.get(i));
			}
		}
		if(reqCount > 0) {
			allThreadsList.add(newList);
		}
		
		return allThreadsList;
	}
}
