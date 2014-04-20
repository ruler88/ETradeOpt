package test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.junit.Test;

import com.trade.rowData.DataStorage;
import com.trade.rowData.Equity;

public class TestDataStorage {
	
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
	public void testSerializePartFile() throws Exception {
		Equity testEquity = new Equity("Test");
		feedEquity(testEquity);
		Hashtable<String, Equity> allEquity = new Hashtable<String, Equity>();
		allEquity.put("Test", testEquity);
		
		DataStorage.serializePartFile(allEquity);
	}

	@Test
	public void testSerializePersistEquity() {
		ArrayList<String> dummyEqs = new ArrayList<String>();
		dummyEqs.add("GOOG");
		dummyEqs.add("ABC");
		dummyEqs.add("XYZ");
		
		DataStorage.serializePersistEquity(dummyEqs);
		
		List<String> deserializedPersist = DataStorage.deserializePersistEquity();
		
		assertTrue(deserializedPersist.contains("GOOG"));
		assertTrue(deserializedPersist.contains("ABC"));
		assertTrue(deserializedPersist.contains("XYZ"));
		
		for(String s : deserializedPersist) {
			System.out.println(s);
		}
	}
	
	@Test
	public void testDeserializePersistEquity() {
		List<String> deserializedPersist = DataStorage.deserializePersistEquity();
		
		for(String s : deserializedPersist) {
			System.out.println(s);
		}
	}
}
