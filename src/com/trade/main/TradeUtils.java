package com.trade.main;

import java.util.Queue;

public class TradeUtils {
	
	public static void loadDailyHours(Queue<Integer> hours) {
		//load all hour marks of trading time, for logging + reporting purposes
		for(int i = 10; i<= (5+12); i++) {
			hours.offer(i);
		}
	}
	
	public static String getUnderlier(String eq) {
		return eq.split(":")[0];
	}
	public static boolean isOption(String eq) {
		return eq.contains(":");
	}
}
