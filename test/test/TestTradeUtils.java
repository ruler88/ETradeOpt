package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.testng.Assert;

import com.trade.main.TradeUtils;

public class TestTradeUtils {

	@Test
	public void testGetEquityThreadList() {
		List<String> list = getList();
		
		ArrayList<ArrayList<String>> eqThreads = TradeUtils.getEquityThreadList(list);
		int resultSize = 0;
		
		for( ArrayList<String> arr : eqThreads ) {
			System.out.println( Arrays.deepToString(arr.toArray()) );
			resultSize += arr.size();
		}
		
		Assert.assertEquals(resultSize, list.size());
	}
	
	public List<String> getList() {
		String s = "AAPL, AAPL:2014:3:22:CALL:515.00, AAPL:2014:3:22:CALL:520.00, AAPL:2014:3:22:CALL:525.00, AAPL:2014:3:22:CALL:530.00, AAPL:2014:3:22:CALL:535.00, AAPL:2014:3:22:PUT:515.00, AAPL:2014:3:22:PUT:520.00, AAPL:2014:3:22:PUT:525.00, AAPL:2014:3:22:PUT:530.00, AAPL:2014:3:22:PUT:535.00, AAPL:2014:4:19:CALL:515.00, AAPL:2014:4:19:CALL:520.00, AAPL:2014:4:19:CALL:525.00, AAPL:2014:4:19:CALL:530.00, AAPL:2014:4:19:CALL:535.00, AAPL:2014:4:19:PUT:515.00, AAPL:2014:4:19:PUT:520.00, AAPL:2014:4:19:PUT:525.00, AAPL:2014:4:19:PUT:530.00, AAPL:2014:4:19:PUT:535.00, AMZN, AMZN:2014:3:22:CALL:350.00, AMZN:2014:3:22:CALL:355.00, AMZN:2014:3:22:CALL:360.00, AMZN:2014:3:22:CALL:365.00, AMZN:2014:3:22:CALL:370.00, AMZN:2014:3:22:CALL:375.00, AMZN:2014:3:22:CALL:375.00, AMZN:2014:3:22:CALL:375.00, AMZN:2014:3:22:CALL:375.00, AMZN:2014:3:22:CALL:375.00, AMZN:2014:3:22:CALL:375.00, AMZN:2014:3:22:CALL:375.00, AMZN:2014:3:22:CALL:375.00, AMZN:2014:3:22:PUT:350.00, AMZN:2014:3:22:PUT:355.00, AMZN:2014:3:22:PUT:360.00, AMZN:2014:3:22:PUT:365.00, AMZN:2014:3:22:PUT:370.00, AMZN:2014:3:22:PUT:375.00, AMZN:2014:4:19:CALL:350.00, AMZN:2014:4:19:CALL:355.00, AMZN:2014:4:19:CALL:360.00, AMZN:2014:4:19:CALL:365.00, AMZN:2014:4:19:CALL:370.00, AMZN:2014:4:19:CALL:375.00, AMZN:2014:4:19:PUT:350.00, AMZN:2014:4:19:PUT:355.00, AMZN:2014:4:19:PUT:360.00, AMZN:2014:4:19:PUT:365.00, AMZN:2014:4:19:PUT:370.00, AMZN:2014:4:19:PUT:375.00, F, F:2014:3:22:CALL:15.00, F:2014:3:22:CALL:16.00, F:2014:3:22:PUT:15.00, F:2014:3:22:PUT:16.00, F:2014:4:19:CALL:15.00, F:2014:4:19:CALL:16.00, F:2014:4:19:PUT:15.00, F:2014:4:19:PUT:16.00, GE, GE:2014:3:22:CALL:25.00, GE:2014:3:22:CALL:26.00, GE:2014:3:22:CALL:27.00, GE:2014:3:22:PUT:25.00, GE:2014:3:22:PUT:26.00, GE:2014:3:22:PUT:27.00, GE:2014:4:19:CALL:25.00, GE:2014:4:19:CALL:26.00, GE:2014:4:19:CALL:27.00, GE:2014:4:19:PUT:25.00, GE:2014:4:19:PUT:26.00, GE:2014:4:19:PUT:27.00, GOOG, GOOG:2014:3:22:CALL:1200.00, GOOG:2014:3:22:CALL:1205.00, GOOG:2014:3:22:CALL:1215.00, GOOG:2014:3:22:CALL:1220.00, GOOG:2014:3:22:CALL:1225.00, GOOG:2014:3:22:PUT:1200.00, GOOG:2014:3:22:PUT:1205.00, GOOG:2014:3:22:PUT:1215.00, GOOG:2014:3:22:PUT:1220.00, GOOG:2014:3:22:PUT:1225.00, GOOG:2014:4:19:CALL:1200.00, GOOG:2014:4:19:CALL:1205.00, GOOG:2014:4:19:CALL:1215.00, GOOG:2014:4:19:CALL:1220.00, GOOG:2014:4:19:CALL:1225.00, GOOG:2014:4:19:PUT:1200.00, GOOG:2014:4:19:PUT:1205.00, GOOG:2014:4:19:PUT:1215.00, GOOG:2014:4:19:PUT:1220.00, GOOG:2014:4:19:PUT:1225.00, GS, GS:2014:3:22:CALL:160.00, GS:2014:3:22:CALL:165.00, GS:2014:3:22:CALL:170.00, GS:2014:3:22:CALL:175.00, GS:2014:3:22:PUT:160.00, GS:2014:3:22:PUT:165.00, GS:2014:3:22:PUT:170.00, GS:2014:3:22:PUT:175.00, GS:2014:4:19:CALL:160.00, GS:2014:4:19:CALL:165.00, GS:2014:4:19:CALL:170.00, GS:2014:4:19:CALL:175.00, GS:2014:4:19:PUT:160.00, GS:2014:4:19:PUT:165.00, GS:2014:4:19:PUT:170.00, GS:2014:4:19:PUT:175.00, RIO, RIO:2014:3:22:CALL:52.50, RIO:2014:3:22:CALL:55.00, RIO:2014:3:22:CALL:57.50, RIO:2014:3:22:PUT:52.50, RIO:2014:3:22:PUT:55.00, RIO:2014:3:22:PUT:57.50, RIO:2014:4:19:CALL:52.50, RIO:2014:4:19:CALL:55.00, RIO:2014:4:19:CALL:57.50, RIO:2014:4:19:PUT:52.50, RIO:2014:4:19:PUT:55.00, RIO:2014:4:19:PUT:57.50, TSLA, TSLA:2014:3:22:CALL:240.00, TSLA:2014:3:22:CALL:245.00, TSLA:2014:3:22:CALL:250.00, TSLA:2014:3:22:CALL:255.00, TSLA:2014:3:22:CALL:260.00, TSLA:2014:3:22:PUT:240.00, TSLA:2014:3:22:PUT:245.00, TSLA:2014:3:22:PUT:250.00, TSLA:2014:3:22:PUT:255.00, TSLA:2014:3:22:PUT:260.00, TSLA:2014:4:19:CALL:240.00, TSLA:2014:4:19:CALL:245.00, TSLA:2014:4:19:CALL:250.00, TSLA:2014:4:19:CALL:255.00, TSLA:2014:4:19:CALL:260.00, TSLA:2014:4:19:PUT:240.00, TSLA:2014:4:19:PUT:245.00, TSLA:2014:4:19:PUT:250.00, TSLA:2014:4:19:PUT:255.00, TSLA:2014:4:19:PUT:260.00, TWTR, TWTR:2014:3:22:CALL:52.50, TWTR:2014:3:22:CALL:55.00, TWTR:2014:3:22:CALL:57.50, TWTR:2014:3:22:PUT:52.50, TWTR:2014:3:22:PUT:55.00, TWTR:2014:3:22:PUT:57.50, TWTR:2014:4:19:CALL:52.50, TWTR:2014:4:19:CALL:55.00, TWTR:2014:4:19:CALL:57.50, TWTR:2014:4:19:PUT:52.50, TWTR:2014:4:19:PUT:55.00, TWTR:2014:4:19:PUT:57.50, VZ, VZ:2014:3:22:CALL:46.00, VZ:2014:3:22:CALL:47.00, VZ:2014:3:22:CALL:48.00, VZ:2014:3:22:PUT:46.00, VZ:2014:3:22:PUT:47.00, VZ:2014:3:22:PUT:48.00, VZ:2014:4:19:CALL:46.00, VZ:2014:4:19:CALL:47.00, VZ:2014:4:19:CALL:48.00, VZ:2014:4:19:PUT:46.00, VZ:2014:4:19:PUT:47.00, VZ:2014:4:19:PUT:48.00";
		
		String[] sList = s.split(", ");
		for( int i=0; i<sList.length; i++ ) {
			System.out.println(i + ": " + sList[i]);
		}
		return new ArrayList<String>( Arrays.asList(sList) );
		
	}

}
