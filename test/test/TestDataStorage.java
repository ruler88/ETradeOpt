package test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.trade.rowData.DataStorage;

public class TestDataStorage {

	@Test
	public void testUpdateDatesEquityJson() {
		List<String> eqDummy = new ArrayList<String>();
		eqDummy.add("GOOG");
		eqDummy.add("BS");
		DataStorage.updateDatesEquityJson(eqDummy);
		
	}

}
