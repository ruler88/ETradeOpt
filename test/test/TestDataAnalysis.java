package test;

import java.util.List;

import org.junit.Test;

import com.trade.main.DataAnalysis;

public class TestDataAnalysis {

	@Test
	public void testGetMarketFiles() {
		String startTime = "20140201";
		String endTime = "20140210";
		
		List<String> results = DataAnalysis.getMarketFiles(startTime, endTime);
		
		for(String s : results) {
			System.out.println(s);
		}
	}

}
