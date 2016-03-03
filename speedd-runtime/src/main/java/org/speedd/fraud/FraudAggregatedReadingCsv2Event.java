package org.speedd.fraud;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.speedd.EventParser;
import org.speedd.ParsingError;
import org.speedd.data.Event;
import org.speedd.data.EventFactory;

public class FraudAggregatedReadingCsv2Event implements EventParser, Constants {
	private static final int ATTR_TIMESTAMP_INDEX            = 0;
	private static final int ATTR_TRANSACTION_ID_INDEX       = 1;
	private static final int ATTR_IS_CNP_INDEX               = 2;
	private static final int ATTR_AMOUNT_EUR_INDEX           = 3;
	private static final int ATTR_CARD_PAN_INDEX             = 4;
	private static final int ATTR_CARD_EXP_DATE_INDEX        = 5;
	private static final int ATTR_CARD_COUNTRY_INDEX         = 6;
	private static final int ATTR_CARD_FAMILY_INDEX          = 7;
	private static final int ATTR_CARD_TYPE_INDEX            = 8;
    private static final int ATTR_CARD_TECH_INDEX            = 9;
    private static final int ATTR_ACQUIRER_COUNTRY_INDEX     = 10;
    private static final int ATTR_MERCHANT_MCC_INDEX         = 11;
    private static final int ATTR_TERMINAL_BRAND_INDEX       = 12;
    private static final int ATTR_TERMINAL_ID_INDEX          = 13;
    private static final int ATTR_TERMINAL_TYPE_INDEX        = 14;
    private static final int ATTR_TERMINAL_EMV_INDEX         = 15;
    private static final int ATTR_TRANSACTION_RESPONSE_INDEX = 16;
    private static final int ATTR_CARD_AUTH_INDEX            = 17;
    private static final int ATTR_TERMINAL_AUTH_INDEX        = 18;
    private static final int ATTR_CLIENT_AUTH_INDEX          = 19;
    private static final int ATTR_CARD_BRAND_INDEX           = 20;
    private static final int ATTR_CVV_VALIDATION_INDEX       = 21;
    private static final int ATTR_TMP_CARD_PAN_INDEX         = 22;
    private static final int ATTR_TMP_CARD_EXP_DATE_INDEX    = 23;
    private static final int ATTR_TRANSACTION_TYPE_INDEX     = 24;
    private static final int ATTR_AUTH_TYPE_INDEX            = 25;
    private static final int ATTR_IS_FRAUD_INDEX             = 26;

	private static final int NUM_FIELDS = 27;

	private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMM");


	private EventFactory eventFactory;

	public FraudAggregatedReadingCsv2Event(EventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}

    private static Logger logger = LoggerFactory.getLogger(FraudAggregatedReadingCsv2Event.class);

	public Event fromBytes(byte[] bytes) throws ParsingError {
		String name = TRANSACTION;

		try {
			//register the detected time to serve as the timestamp of the received event.
			//it can differ from the occurrence time that is in the event
			long detectionTime = System.currentTimeMillis();
			
			String[] tuple = new String(bytes).split(",");
			
			int tupleLength = tuple.length;

            if (tupleLength != NUM_FIELDS) {
                throw new IllegalStateException("Expected " + NUM_FIELDS + " fields in event, but got " + tupleLength + "...");
            }
			
			//extend to expected length padding the rest with nulls
			if(tupleLength < NUM_FIELDS){
				String[] padded = Arrays.copyOf(tuple, NUM_FIELDS);
				
				Arrays.fill(padded, tupleLength, NUM_FIELDS - 1, "0");
				
				tuple = padded;
			}

            long timestamp = Long.valueOf(tuple[ATTR_TIMESTAMP_INDEX]);

			HashMap<String, Object> attrMap = new HashMap<String, Object>();

            attrMap.put(ATTR_TIMESTAMP,            timestamp);
            attrMap.put(ATTR_DETECTION_TIME, 	   Long.valueOf(detectionTime));
            attrMap.put(ATTR_TRANSACTION_ID,       tuple[ATTR_TRANSACTION_ID_INDEX]);
            attrMap.put(ATTR_IS_CNP,               getBooleanValue(tuple[ATTR_IS_CNP_INDEX], ATTR_IS_CNP));
            attrMap.put(ATTR_AMOUNT_EUR,           getDoubleValue(tuple[ATTR_AMOUNT_EUR_INDEX], ATTR_AMOUNT_EUR));
            attrMap.put(ATTR_CARD_PAN,             tuple[ATTR_CARD_PAN_INDEX]);
            attrMap.put(ATTR_CARD_EXP_DATE,        dateTimeFormat.parse(tuple[ATTR_CARD_EXP_DATE_INDEX]));
            attrMap.put(ATTR_CARD_COUNTRY,         getIntegerValue(tuple[ATTR_CARD_COUNTRY_INDEX], ATTR_CARD_COUNTRY));
            attrMap.put(ATTR_CARD_FAMILY,          getIntegerValue(tuple[ATTR_CARD_FAMILY_INDEX], ATTR_CARD_FAMILY));
            attrMap.put(ATTR_CARD_TYPE,            getIntegerValue(tuple[ATTR_CARD_TYPE_INDEX], ATTR_CARD_TYPE));
            attrMap.put(ATTR_CARD_TECH,            getIntegerValue(tuple[ATTR_CARD_TECH_INDEX], ATTR_CARD_TECH));
            attrMap.put(ATTR_ACQUIRER_COUNTRY,     getIntegerValue(tuple[ATTR_ACQUIRER_COUNTRY_INDEX], ATTR_ACQUIRER_COUNTRY));
            attrMap.put(ATTR_MERCHANT_MCC,         getIntegerValue(tuple[ATTR_MERCHANT_MCC_INDEX], ATTR_MERCHANT_MCC));
            attrMap.put(ATTR_TERMINAL_BRAND,       getLongValue(tuple[ATTR_TERMINAL_BRAND_INDEX], ATTR_TERMINAL_BRAND));
            attrMap.put(ATTR_TERMINAL_ID,          getLongValue(tuple[ATTR_TERMINAL_ID_INDEX], ATTR_TERMINAL_ID));
            attrMap.put(ATTR_TERMINAL_TYPE,        getIntegerValue(tuple[ATTR_TERMINAL_TYPE_INDEX], ATTR_TERMINAL_TYPE));
            attrMap.put(ATTR_TERMINAL_EMV,         getIntegerValue(tuple[ATTR_TERMINAL_EMV_INDEX], ATTR_TERMINAL_EMV));
            attrMap.put(ATTR_TRANSACTION_RESPONSE, getIntegerValue(tuple[ATTR_TRANSACTION_RESPONSE_INDEX], ATTR_TRANSACTION_RESPONSE));
            attrMap.put(ATTR_CARD_AUTH,            getIntegerValue(tuple[ATTR_CARD_AUTH_INDEX], ATTR_CARD_AUTH));
            attrMap.put(ATTR_TERMINAL_AUTH,        getIntegerValue(tuple[ATTR_TERMINAL_AUTH_INDEX], ATTR_TERMINAL_AUTH));
            attrMap.put(ATTR_CLIENT_AUTH,          getIntegerValue(tuple[ATTR_CLIENT_AUTH_INDEX], ATTR_CLIENT_AUTH));
            attrMap.put(ATTR_CARD_BRAND,           getIntegerValue(tuple[ATTR_CARD_BRAND_INDEX], ATTR_CARD_BRAND));
            attrMap.put(ATTR_CVV_VALIDATION,       getIntegerValue(tuple[ATTR_CVV_VALIDATION_INDEX], ATTR_CVV_VALIDATION));
            attrMap.put(ATTR_TMP_CARD_PAN,         tuple[ATTR_TMP_CARD_PAN_INDEX]);
            
            //FIXME should the ATTR_TMP_CARD_EXP_DATE be of Date type similar to CARD_EXP_DATE?
            attrMap.put(ATTR_TMP_CARD_EXP_DATE,    tuple[ATTR_TMP_CARD_EXP_DATE_INDEX]);
            attrMap.put(ATTR_TRANSACTION_TYPE,     getIntegerValue(tuple[ATTR_TRANSACTION_TYPE_INDEX], ATTR_TRANSACTION_TYPE));
            attrMap.put(ATTR_AUTH_TYPE,            getIntegerValue(tuple[ATTR_AUTH_TYPE_INDEX], ATTR_AUTH_TYPE));
            attrMap.put(ATTR_IS_FRAUD,             getBooleanValue(tuple[ATTR_IS_FRAUD_INDEX], ATTR_IS_FRAUD));

			return eventFactory.createEvent(name, timestamp, attrMap);
		} catch (Exception e) {
			throw new ParsingError(
					"Error parsing transaction CSV.", e);
		}
	}

    // SAFE PARSING METHODS --------------------------------------------------------------------------------------------
	private Double getDoubleValue(String strVal, String field){
		try {
			return Double.parseDouble(strVal);
		}
		catch (NumberFormatException e){
            logger.warn("Could not parse field '" + field + "' as a double (value was " + strVal + "). Returning null...");
			return null;
		}
	}

    private Integer getIntegerValue(String strVal, String field){
        try {
            return Integer.parseInt(strVal);
        }
        catch (NumberFormatException e){
            logger.warn("Could not parse field '" + field + "' as an integer (value was " + strVal + "). Returning null...");
            return null;
        }
    }

    private Long getLongValue(String strVal, String field){
        try {
            return Long.parseLong(strVal);
        }
        catch (NumberFormatException e){
            logger.warn("Could not parse field '" + field + "' as a long (value was " + strVal + "). Returning null...");
            return null;
        }
    }

    private Boolean getBooleanValue(String strVal, String field){

        if (!(strVal.equals("1") || strVal.equals("0")) ) {
            logger.warn("Boolean field '" + field + "' should be 0 or 1 (value was " + strVal + "). Returning null...");
            return null;
        }

        return strVal.equals("1");
    }

    public byte[] toBytes(Event event){
    	ArrayList<String> values = new ArrayList<String>(NUM_FIELDS);
    	for(int i=0; i<NUM_FIELDS; ++i){
    		values.add("");
    	}
    	values.set(ATTR_TIMESTAMP_INDEX, Long.toString(((Date)event.getAttributes().get(ATTR_TIMESTAMP)).getTime()));
    	values.set(ATTR_TRANSACTION_ID_INDEX, (String)event.getAttributes().get(ATTR_TRANSACTION_ID));
    	values.set(ATTR_IS_CNP_INDEX, (Boolean)(event.getAttributes().get(ATTR_IS_CNP))? "1" : "0");
    	values.set(ATTR_AMOUNT_EUR_INDEX, event.getAttributes().get(ATTR_AMOUNT_EUR).toString());
    	values.set(ATTR_CARD_PAN_INDEX, (String)event.getAttributes().get(ATTR_CARD_PAN));
    	values.set(ATTR_CARD_EXP_DATE_INDEX, dateTimeFormat.format(event.getAttributes().get(ATTR_CARD_EXP_DATE)));
    	values.set(ATTR_CARD_COUNTRY_INDEX, event.getAttributes().get(ATTR_CARD_COUNTRY).toString());
    	values.set(ATTR_CARD_FAMILY_INDEX, event.getAttributes().get(ATTR_CARD_FAMILY).toString());
    	values.set(ATTR_CARD_TYPE_INDEX, event.getAttributes().get(ATTR_CARD_TYPE).toString());
        values.set(ATTR_CARD_TECH_INDEX, event.getAttributes().get(ATTR_CARD_TECH).toString());
        values.set(ATTR_ACQUIRER_COUNTRY_INDEX, event.getAttributes().get(ATTR_ACQUIRER_COUNTRY).toString());
        values.set(ATTR_MERCHANT_MCC_INDEX, event.getAttributes().get(ATTR_MERCHANT_MCC).toString());
        values.set(ATTR_TERMINAL_BRAND_INDEX, event.getAttributes().get(ATTR_TERMINAL_BRAND).toString());
        values.set(ATTR_TERMINAL_ID_INDEX, event.getAttributes().get(ATTR_TERMINAL_ID).toString());
        values.set(ATTR_TERMINAL_TYPE_INDEX, event.getAttributes().get(ATTR_TERMINAL_TYPE).toString());
        values.set(ATTR_TERMINAL_EMV_INDEX, event.getAttributes().get(ATTR_TERMINAL_EMV).toString());
        values.set(ATTR_TRANSACTION_RESPONSE_INDEX, event.getAttributes().get(ATTR_TRANSACTION_RESPONSE).toString());
        values.set(ATTR_CARD_AUTH_INDEX, event.getAttributes().get(ATTR_CARD_AUTH).toString());
        values.set(ATTR_TERMINAL_AUTH_INDEX, event.getAttributes().get(ATTR_TERMINAL_AUTH).toString());
        values.set(ATTR_CLIENT_AUTH_INDEX, event.getAttributes().get(ATTR_CLIENT_AUTH).toString());
        values.set(ATTR_CARD_BRAND_INDEX, event.getAttributes().get(ATTR_CARD_BRAND).toString());
        values.set(ATTR_CVV_VALIDATION_INDEX, event.getAttributes().get(ATTR_CVV_VALIDATION).toString());
        values.set(ATTR_TMP_CARD_PAN_INDEX, event.getAttributes().get(ATTR_TMP_CARD_PAN).toString());
        values.set(ATTR_TMP_CARD_EXP_DATE_INDEX, event.getAttributes().get(ATTR_TMP_CARD_EXP_DATE).toString());
        values.set(ATTR_TRANSACTION_TYPE_INDEX, event.getAttributes().get(ATTR_TRANSACTION_TYPE).toString());
        values.set(ATTR_AUTH_TYPE_INDEX, event.getAttributes().get(ATTR_AUTH_TYPE).toString());
        values.set(ATTR_IS_FRAUD_INDEX, (Boolean)event.getAttributes().get(ATTR_IS_FRAUD)? "1" : "0");

        
        StringBuffer buf = new StringBuffer();
    	for (String value: values) {
			buf.append(value).append(',');
		}
    	buf.deleteCharAt(buf.length()-1);//delete trailing ','
    	
    	return buf.toString().getBytes();
    }
}
