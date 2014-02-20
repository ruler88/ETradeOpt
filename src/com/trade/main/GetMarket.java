package com.trade.main;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import com.etrade.etws.market.AllQuote;
import com.etrade.etws.market.DetailFlag;
import com.etrade.etws.market.QuoteData;
import com.etrade.etws.market.QuoteResponse;
import com.etrade.etws.sdk.client.ClientRequest;
import com.etrade.etws.sdk.client.Environment;
import com.etrade.etws.sdk.client.MarketClient;
import com.etrade.etws.sdk.common.ETWSException;
import com.trade.accountVerify.AccountVerification;
import com.trade.rowData.DataStorage;
import com.trade.rowData.Equity;


public class GetMarket {
	Hashtable<String, Equity> allEquity = new Hashtable<String, Equity>();
	private static boolean testMode = false;
	private static int testCount = 0;
	
	public static final String TZ = "America/New_York";
	public static String logFile = "/home/ubuntu/dailyLog.log";
	Queue<Integer> dailyHour = new LinkedList<Integer>();
	FileWriter fileWritter = null;
	private static Calendar calendar = new GregorianCalendar();
	private static ConcurrentHashMap<String, StockThread> currentThreads = new ConcurrentHashMap<String, StockThread>();
	
	public static void main(String[] args) throws IOException, ETWSException, InterruptedException, URISyntaxException {
		if(args.length>0) {
			testMode = true;
			testCount = Integer.valueOf(args[0]);
			System.err.println("Test mode activated");
		}

		calendar.setTimeZone(TimeZone.getTimeZone(TZ));
		
		if(testMode) {
			//testMode does not run account verification
			String oauth_access_token = "fnryOsxeg/W3maKgfe7kCLDfUIH+qAYIWP6u3tQVv4s=";
			String oauth_access_token_secret = "x0qGM8Ck9Lm/m3HoPuRaz80N4NUBRylCwX56Sfab89w=";
			
			logFile = "/Users/kchao/dailyLog.log";
			GetMarket gm = new GetMarket(oauth_access_token, oauth_access_token_secret);
		} else {
			try{
				AccountVerification acct = new AccountVerification();
				GetMarket gm = new GetMarket(acct.getAccessToken(), acct.getAccessTokenSecret());
			} catch (Exception e) {
				e.printStackTrace();
				System.err.print(e.getMessage());
				System.exit(1);	//exception error code
			}
			System.exit(0);  //normal exit
		}
	}
	
	public GetMarket(String oauth_access_token, String oauth_access_token_secret) throws IOException, ETWSException, InterruptedException {
		String oauth_consumer_key = "628c14e46b6744606fdb18fff4b91d33"; // Your consumer key
		String oauth_consumer_secret = "ede49fab7ad76aa98623278367ef22de"; // Your consumer secret
		
		fileWritter = new FileWriter(logFile,true);
		
		ClientRequest request = new ClientRequest();
		request.setEnv(Environment.LIVE);
		request.setConsumerKey(oauth_consumer_key);
		request.setConsumerSecret(oauth_consumer_secret);
		request.setToken(oauth_access_token);
		request.setTokenSecret(oauth_access_token_secret);
		
		TradeUtils.loadDailyHours(dailyHour);		//loading hours for hourly updates	
		
		ArrayList<String> list = new ArrayList<String>();
		//list.add("ZNGA");
		//list.add("FLWS");
		list.add("GOOG");
		list.add("GE");
		list.add("AAPL");
		list.add("GS");
		list.add("F");
		list.add("RIO");
		list.add("AMZN");
		
		List<String> persistList = DataStorage.deserializePersistEquity();	//get the old equity list
		GetOption.setPersistList(persistList);
		GetOption.addExpiringOptions(new MarketClient(request), list);
		
		System.err.println(Arrays.deepToString(list.toArray()));
		System.err.println("Equity list size: " + list.size());
		//shut down STDOUT to conserve instance RAM
		System.err.println("\nTrading system starting!!!");
		System.err.println("STDOUT is shutting down");
		PrintStream originalStream = System.out;
		PrintStream dummyStream = new PrintStream(new OutputStream(){
		    public void write(int b) {
		        //NO-OUTPUT
		    }
		});
		System.setOut(dummyStream);
		
		threadManager(list, request);
		
		//persist today's equity list for tomorrow
		DataStorage.serializePersistEquity(list);
		
		//turn STDOUT back on
		System.setOut(originalStream);
		System.out.println("\n" + "Trading day over!!\n" + "STDOUT is back on\n");
		
		fileWritter.close();
	}	
	
	public void threadManager(ArrayList<String> list, ClientRequest request) throws InterruptedException {
		ArrayList<ArrayList<String>> allThreadsList = new ArrayList<ArrayList<String>>();
		ArrayList<String> newList = new ArrayList<String>();
		int urlReqLen = "https://etws.etrade.com/market/rest/quote/".length() + 5;
		int reqCount = 0;
		for(int i=0; i<list.size(); i++) {
			urlReqLen += (list.get(i).length()+1);
			reqCount += 1;
			if(urlReqLen > 2000 || reqCount > 25 ) {	//url length limit is 2k and request count max is 25
				allThreadsList.add(newList);
				i--;
				urlReqLen = "https://etws.etrade.com/market/rest/quote/".length() + 5;
				reqCount = 0;
				
				//ensure all equity items in the same underlier end up in the same thread
				ArrayList<String> tmpNewList = new ArrayList<String>();
				String lastUnderlier = TradeUtils.getUnderlier( newList.get(newList.size()-1) );
				
				while(lastUnderlier.equals( TradeUtils.getUnderlier( newList.get(newList.size()-1)))) {
					tmpNewList.add(0, newList.remove(newList.size()-1));
					urlReqLen += tmpNewList.get(0).length();
					reqCount += 1;
				}
				newList = tmpNewList;
			} else {
				newList.add(list.get(i));
			}
		}
		if(reqCount > 0) {
			allThreadsList.add(newList);
		}
		
		while( (calendar.get(Calendar.HOUR_OF_DAY) > 8 && calendar.get(Calendar.HOUR_OF_DAY) < 16) 
				|| testCount > 0) {
			calendar = Calendar.getInstance(TimeZone.getTimeZone(TZ));
			timeCheck(calendar.get(Calendar.HOUR_OF_DAY));	//log current time on the hour
			
			for(int i=0; i<allThreadsList.size(); i++) {
				
				if(i%4 == 0) {
					//rest every second to keep rate limit
					Thread.sleep(1000);
				}
				try {
					String s = getFirstString(allThreadsList.get(i));
					if(currentThreads.contains(s)) {
						continue;
					}
					StockThread st = new StockThread(allThreadsList.get(i), request);
					currentThreads.put(s, st);
					st.start();
				} catch ( IllegalThreadStateException e ) {
					System.err.println("illegal thread");
					System.err.println(e.getMessage());
					System.err.println(e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
			if(testCount > 0) {
				testCount--;
			}
		}
		
		while(!currentThreads.isEmpty()) {
			Iterator<String> ite = currentThreads.keySet().iterator();
			if(ite.hasNext()) {
				currentThreads.get(ite.next()).join();
			}
		}
	}

	
	public class StockThread extends Thread {
		ClientRequest request;
		ArrayList<String> list;
		
		public StockThread(ArrayList<String> list, ClientRequest request) {
			this.list = list;
			this.request = request;
		}
		
		@Override
		public void run() {
			MarketClient client = new MarketClient(request);
			try {
				try{
					QuoteResponse response = client.getQuote(list, true, DetailFlag.ALL);
					List<QuoteData> data = response.getQuoteData();
					for(int i=0; i<data.size(); i++) {
						QuoteData qd = data.get(i);
						AllQuote allInfo = qd.getAll();
						String symbol = list.get(i);
						
						if(allEquity.containsKey(symbol)) {
							//if symbol already exists in list
							Equity tmpEquity = allEquity.get(symbol);
							tmpEquity.updateInfo(allInfo);
						} else {
							Equity tmpEquity = new Equity(symbol);
							tmpEquity.updateInfo(allInfo);
							allEquity.put(symbol, tmpEquity);
						}
					}
				} catch ( ETWSException e ) {
					System.err.println(e.getMessage());
					System.err.println(e.getErrorMessage());
					System.err.println(e.getErrorCode());
					e.printStackTrace();
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			currentThreads.remove(getFirstString(list));
		}
		
		@Override
		public int hashCode() {
			//hash and equals are based on first element of the list
			return getFirstString(list).hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if( ! (o instanceof StockThread )) {
				return false;
			}
			StockThread st = (StockThread) o;
			if(st.list.isEmpty() && this.list.isEmpty()) {
				return true;
			}
			if(st.list.isEmpty() || this.list.isEmpty()) {
				return false;
			}
			if(st.list.get(0).equals(this.list.get(0))) {
				return true;
			}
			return false;
		}
	}
	
	private String getFirstString(ArrayList<String> arr) {
		if(arr.isEmpty()) {
			return "";
		}
		return arr.get(0);
	}
	
	private void timeCheck(int hour) {
		//outputs time if it is on the mark (for monitoring)
		if(hour >= dailyHour.peek()) {
			dailyHour.poll();
			synchronized(allEquity) {
				try {
					fileWritter.write("\nTrading system is operational! Currently EST " + hour);
					System.err.println("This is hour: " + hour);
					
					//serialize by part
					Hashtable<String, Equity> allEquityCopy = allEquity;
					allEquity = new Hashtable<String, Equity>();
					DataStorage.serializePartFile(allEquityCopy);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
