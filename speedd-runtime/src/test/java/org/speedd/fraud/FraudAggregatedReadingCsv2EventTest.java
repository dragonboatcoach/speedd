package org.speedd.fraud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jline.internal.InputStreamReader;

import org.junit.Test;
import org.speedd.data.Event;
import org.speedd.data.impl.SpeeddEventFactory;

public class FraudAggregatedReadingCsv2EventTest {
	private static final FraudAggregatedReadingCsv2Event parser = new FraudAggregatedReadingCsv2Event(SpeeddEventFactory.getInstance());

	private volatile Event event;
	
	private volatile List<Object> tuple;
	
	@Test
	public void testFromBytes() throws Exception {
		String eventCsv = "1423150200000,TXN_ID,1,250.0,567745453,201702,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,1532322,201801,17,18,0";

		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMM");
		
		Event event = parser.fromBytes(eventCsv.getBytes(Charset.forName("UTF-8")));
		
		assertNotNull(event);

		assertEquals(Constants.TRANSACTION, event.getEventName());

		Map<String, Object> attrs = event.getAttributes();
        assertEquals(1423150200000L, attrs.get("OccurrenceTime"));
        assertEquals("TXN_ID", attrs.get("transaction_id"));
        assertEquals(true, attrs.get("is_cnp"));
        assertEquals(250.0, attrs.get("amount_eur"));
        assertEquals("567745453", attrs.get("card_pan"));
        assertEquals(dateTimeFormat.parse("201702"), attrs.get("card_exp_date"));
        assertEquals(1, attrs.get("card_country"));
        assertEquals(2, attrs.get("card_family"));
        assertEquals(3, attrs.get("card_type"));
        assertEquals(4, attrs.get("card_tech"));
        assertEquals(5, attrs.get("acquirer_country"));
        assertEquals(6, attrs.get("merchant_mcc"));
        assertEquals(7L, attrs.get("terminal_brand"));
        assertEquals(8L, attrs.get("terminal_id"));
        assertEquals(9, attrs.get("terminal_type"));
        assertEquals(10, attrs.get("terminal_emv"));
        assertEquals(11, attrs.get("transaction_response"));
        assertEquals(12, attrs.get("card_auth"));
        assertEquals(13, attrs.get("terminal_auth"));
        assertEquals(14, attrs.get("client_auth"));
        assertEquals(15, attrs.get("card_brand"));
        assertEquals(16, attrs.get("cvv_validation"));
        assertEquals("1532322", attrs.get("tmp_card_pan"));
        
        //FIXME check if the format should have Date type instead of string for tmp_card_exp_date
        assertEquals("201801", attrs.get("tmp_card_exp_date"));
        assertEquals(17, attrs.get("transaction_type"));
        assertEquals(18, attrs.get("auth_type"));
        assertEquals(false, attrs.get("is_fraud"));
	}

	@Test
	public void testToBytes() throws Exception {
		String inEventCsv = "1423150200000,TXN_ID,1,250.0,567745453,201702,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,1532322,201801,17,18,0";
		Event event = parser.fromBytes(inEventCsv.getBytes());
		
		String outEventCsv = new String(parser.toBytes(event));
		assertEquals(inEventCsv, outEventCsv);
	}
	
	@Test
	public void testPerformanceMircoBenchmark() throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("FeedzaiIntegrationData.csv")));
		
		ArrayList<byte[]> csvArray = new ArrayList<byte[]>();
		
		boolean readCompleted = false;
		while(!readCompleted){
			String csv = reader.readLine();
			
			if(csv != null) {
				csvArray.add(csv.getBytes());
			} else {
				readCompleted = true;
			}
		}
		
		//warm up
		event = parser.fromBytes(csvArray.get(0));
		
		long start = System.currentTimeMillis();
		for (byte[] bs : csvArray) {
			event = parser.fromBytes(bs);
		}
		long end = System.currentTimeMillis();
		long elapsed = end - start;
		double avg = (double)elapsed / csvArray.size();
		
		System.out.println(String.format("Parsed %d events. Total time: %d ms, avg %f ms per event", csvArray.size(), elapsed, avg));
		
		
		
		FraudAggregatedReadingScheme scheme = new FraudAggregatedReadingScheme();
		//warm up
		tuple = scheme.deserialize(csvArray.get(0));
		
		start = System.currentTimeMillis();
		for (byte[] bs : csvArray) {
			tuple = scheme.deserialize(bs);
		}
		end = System.currentTimeMillis();
		elapsed = end - start;
		avg = (double)elapsed / csvArray.size();
		
		System.out.println(String.format("Parsed %d tuples. Total time: %d ms, avg %f ms per tuple", csvArray.size(), elapsed, avg));
		
	}
}
