package com.trade.main;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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
import com.trade.insights.Notifier;
import com.trade.rowData.DataStorage;
import com.trade.rowData.Equity;


public class GetMarket {
	Hashtable<String, Equity> allEquity = new Hashtable<String, Equity>();
	public static boolean testMode = false;
	private static int testCount = 0;
	
	public static final String TZ = "America/New_York";
	public static String logFile = "/home/ubuntu/dailyLog.log";
	public static String errFile = "/home/ubuntu/errLog.log";
	Queue<Integer> dailyHour = new LinkedList<Integer>();
	FileWriter logWritter= null;
	FileWriter errWriter = null;
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
			String oauth_access_token = "jEqq84pcixZNQwKYuac9lhXrZ2UzyLbos9Duq+Q8qzw=";
			String oauth_access_token_secret = "4sv6IO5gorh0d1uy5OUJHdsiQ/RHrNQdbCf69p/jKus=";
			
			logFile = "/Users/kchao/dailyLog.log";
			errFile = logFile;
			GetMarket gm = new GetMarket(oauth_access_token, oauth_access_token_secret);
		} else {
			try{
				AccountVerification acct = new AccountVerification();
				GetMarket gm = new GetMarket(acct.getAccessToken(), acct.getAccessTokenSecret());
			} catch (Exception e) {
				e.printStackTrace();
				System.err.print(e.getMessage());
				Notifier.sendSMS("Etrade failed, check email");
				Notifier.sendEmail("helloworld0424@gmail.com", "Etrade failed!", e.getMessage());
				System.exit(1);	//exception error code
			}
			System.exit(0);  //normal exit
		}
	}
	
	public GetMarket(String oauth_access_token, String oauth_access_token_secret) throws IOException, ETWSException, InterruptedException {
		String oauth_consumer_key = "628c14e46b6744606fdb18fff4b91d33"; // Your consumer key
		String oauth_consumer_secret = "ede49fab7ad76aa98623278367ef22de"; // Your consumer secret
		
		logWritter = new FileWriter(logFile,true);
		errWriter = new FileWriter(errFile,true);
		
		ClientRequest request = new ClientRequest();
		request.setEnv(Environment.LIVE);
		request.setConsumerKey(oauth_consumer_key);
		request.setConsumerSecret(oauth_consumer_secret);
		request.setToken(oauth_access_token);
		request.setTokenSecret(oauth_access_token_secret);
		
		TradeUtils.loadDailyHours(dailyHour);		//loading hours for hourly updates	
		
		ArrayList<String> list = new ArrayList<String>();
		list.add("TWTR");
		list.add("VZ");
		list.add("GOOG");
		list.add("GE");
		list.add("AAPL");
		list.add("GS");
		list.add("F");
		list.add("RIO");
		list.add("AMZN");
		list.add("TSLA");
		
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
		
		logWritter.close();
	}	
	
	public void threadManager(ArrayList<String> list, ClientRequest request) throws InterruptedException {
		ArrayList<ArrayList<String>> allThreadsList = TradeUtils.getEquityThreadList(list);
		for(int i=0; i<allThreadsList.size(); i++) {
			try {
				//logging all eqs for the day
				logWritter.write( Arrays.deepToString(allThreadsList.get(i).toArray()) + "\n");
			} catch (Exception e) { System.err.println(logFile + ", file not found"); }
			
		}
		
		while( (calendar.get(Calendar.HOUR_OF_DAY) > 8 && calendar.get(Calendar.HOUR_OF_DAY) < 16) 
				|| testCount > 0) {
			calendar = Calendar.getInstance(TimeZone.getTimeZone(TZ));
			timeCheck(calendar.get(Calendar.HOUR_OF_DAY));	//log current time on the hour
			
			for(int i=0; i<allThreadsList.size(); i++) {
				Thread.sleep(250);
				if(i%4 == 0) {
					//rest every second to keep rate limit
					Thread.sleep(250);
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
					Notifier.sendEmail("helloworld0424@gmail.com", "Etrade illegal thread", e.getMessage());
				}
			}
			if(testCount > 0) {
				testCount--;
			}
		}
		
		//wait on any running threads
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
					System.err.println();
					System.err.println(e.getMessage());
					System.err.println(e.getErrorMessage());
					System.err.println("Error code: " + e.getErrorCode());
					e.printStackTrace();
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				Notifier.sendEmail("helloworld0424@gmail.com", "Etrade thread went bad", e.getMessage());
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
			Hashtable<String, Equity> allEquityCopy = allEquity;
			allEquity = new Hashtable<String, Equity>();
			dailyHour.poll();
			synchronized(allEquityCopy) {
				try {
					logWritter.write("\nTrading system is operational! Currently EST " + hour);
					System.err.println("This is hour: " + hour);
					
					DataStorage.serializePartFile(allEquityCopy);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
