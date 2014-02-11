package com.trade.rowData;

import java.util.ArrayList;
import java.util.List;

public class RowMovingAverage {
		//generate moving average based on list and n (number of points for averge)
		public static List<Number> SimpleMovingAvg(List var, int n) {
			if(var==null || var.size() <=0) {
				return null;
			}
			
			if(var.get(0) instanceof Double) {
				return SMAHelper(var, n, new Double(0.0));
			} else if(var.get(0) instanceof Long) {
				return SMAHelper(var, n, new Long(0));
			} else {
				return null;
			}
			
		}
		private static List<Number> SMAHelper(List<Double> var, int n, Double movingSum) {
			ArrayList<Number> SMA = new ArrayList<Number>();
			int headIndex = 0;
			
			for(int i=0; i<var.size();) {
				movingSum += var.get(i);
				i++;
				if(i <= n) {
					SMA.add(movingSum / i);
				} else {
					movingSum -= var.get(headIndex);
					headIndex++;
					SMA.add(movingSum / n);
				}
			}
			return SMA;
		}
		private static List<Number> SMAHelper(List<Long> var, int n, Long movingSum) {
			ArrayList<Number> SMA = new ArrayList<Number>();
			int headIndex = 0;
			
			for(int i=0; i<var.size();) {
				movingSum += var.get(i);
				i++;
				if(i <= n) {
					SMA.add(movingSum / i);
				} else {
					movingSum -= var.get(headIndex);
					headIndex++;
					SMA.add(movingSum / n);
				}
			}
			return SMA;
		}
		
		
		//generate weighted moving average based on list and n (number of points for averge)
		public static List<Number> WeightedMovingAvg(List var, int n) {
			if(var==null || var.size() <=0) {
				return null;
			}
			if(var.get(0) instanceof Double) {
				return WMAHelper(var, n, new Double(0.0));
			} else if(var.get(0) instanceof Long) {
				return WMAHelper(var, n, new Long(0));
			} else {
				return null;
			}
		}
		private static List<Number> WMAHelper(List<Double> var, int n, Double weightedSum) {
			ArrayList<Number> WMA = new ArrayList<Number>();
			int headIndex = 0;
			Double totalSum = 0.0;
			int denom = 0;
			
			for(int i=0; i<var.size(); i++) {
				weightedSum += var.get(i) * n;
				weightedSum -= totalSum;
				totalSum += var.get(i);
				
				if(i < n) {
					denom += (n-i);
					WMA.add(weightedSum / denom);
				} else {
					totalSum -= var.get(headIndex);
					headIndex++;
					WMA.add(weightedSum / denom);
					
				}
			}
			return WMA;
		}
		
		private static List<Number> WMAHelper(List<Long> var, int n, Long weightedSum) {
			ArrayList<Number> WMA = new ArrayList<Number>();
			int headIndex = 0;
			Long totalSum = 0l;
			int denom = 0;
			
			for(int i=0; i<var.size(); i++) {
				weightedSum += var.get(i) * n;
				weightedSum -= totalSum;
				totalSum += var.get(i);
				
				if(i < n) {
					denom += (n-i);
					WMA.add(weightedSum / denom);
				} else {
					totalSum -= var.get(headIndex);
					headIndex++;
					WMA.add(weightedSum / denom);
					
				}
			}
			return WMA;
		}
		
}
