package test;
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.trade.rowData.Equity;


public class TestEquity {
	
	private void feedEquity(Equity testEquity) throws Exception {
		testEquity.addInfo(new Date(), 102.0, 101.0, 10l, 20l, 1l, 1l);
		testEquity.addInfo(new Date(), 105.0, 101.0, 15l, 22l, 1l, 1l);
		testEquity.addInfo(new Date(), 115.0, 110.0, 20l, 25l, 4l, 5l);
		testEquity.addInfo(new Date(), 120.0, 119.0, 20l, 22l, 1l, 10l);
		testEquity.addInfo(new Date(), 117.0, 110.0, 15l, 40l, 10l, 10l);
		testEquity.addInfo(new Date(), 115.0, 111.0, 50l, 20l, 12l, 20l);
		testEquity.addInfo(new Date(), 115.0, 110.0, 30l, 22l, 13l, 30l);
		testEquity.addInfo(new Date(), 110.0, 109.0, 15l, 22l, 14l, 35l);
		testEquity.addInfo(new Date(), 115.0, 110.0, 25l, 42l, 15l, 36l);
		testEquity.addInfo(new Date(), 115.0, 112.0, 15l, 22l, 16l, 40l);
	}

	@Test
	public void testAvgSummarizeEquity() throws Exception {
		Equity testEquity = new Equity("Test");
		feedEquity(testEquity);
		
		testEquity.avgSummarizeEquity(4);
		System.out.println(testEquity);
		
		assertEquals(3, testEquity.getTime().size());
		assertEquals(3, testEquity.getAsk().size());
		
		assertEquals((Double) 103.5, (Double) testEquity.getAsk().get(0));
		assertEquals((Double) 113.75, (Double) testEquity.getAsk().get(2));
	}
	
	@Test
	public void testAvgSummarizeEquityLong() throws Exception {
		Equity testEquity = new Equity("TestLong");
		feedEquity(testEquity);
		
		testEquity.avgSummarizeEquity(8);
		System.out.println(testEquity);
		
		assertEquals(2, testEquity.getTime().size());
		assertEquals(2, testEquity.getAsk().size());
	}

}
