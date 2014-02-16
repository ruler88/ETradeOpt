package com.trade.rowData;

import java.util.List;

public class EquityUtils {
	public static void removeAdd(List<Double> arr, int i, Double d) {
		//this method removes the last element and adds to the previous item
		arr.set(i-1, arr.get(i-1) + arr.get(i));
		arr.remove(i);
	}
	public static void removeAdd(List<Long> arr, int i, Long l) {
		//this method removes the last element and adds to the previous item
		arr.set(i-1, arr.get(i-1) + arr.get(i));
		arr.remove(i);
	}
	
	public static void calcAverage(List<Double> arr, int i, int count, Double d) {
		//averages the average at point i given total of count values
		arr.set(i, arr.get(i) / count);
	}
	public static void calcAverage(List<Long> arr, int i, int count, Long l) {
		//averages the average at point i given total of count values
		arr.set(i, arr.get(i) / count);
	}
}
