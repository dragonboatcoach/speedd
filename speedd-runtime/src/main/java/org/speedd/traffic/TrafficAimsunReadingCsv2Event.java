package org.speedd.traffic;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;

import org.speedd.EventParser;
import org.speedd.ParsingError;
import org.speedd.data.Event;
import org.speedd.data.EventFactory;

public class TrafficAimsunReadingCsv2Event implements EventParser, Constants {
	protected static final String ATTR_LOCATION = "location";
	protected static final String ATTR_DETECTOR_ID = "detector_id";
	protected static final String ATTR_LANE = "lane";
	protected static final String ATTR_VEHICLES = "average_flow";
	protected static final String ATTR_OCCUPANCY = "average_occupancy";	
	protected static final String ATTR_TIMESTAMP = "timestamp";
	protected static final String ATTR_AVG_SPEED = "average_speed";	
	protected static final String ATTR_NORMALIZED_DENSITY = "average_density";	
	protected static final String ATTR_DM_PARTITION = "dm_partition";		
	protected static final String ATTR_QUEUE_LENGTH = "queue_length";
	
	
	private static final int ATTR_TIME_INDEX = 0;
	private static final int ATTR_OCCUPANCY_INDEX = 8;
	private static final int ATTR_DETECTOR_ID_INDEX = 1;
	private static final int ATTR_NUMBER_OF_CARS_INDEX = 3;
	private static final int ATTR_NUMBER_OF_TRUCKS_INDEX = 4;
	private static final int ATTR_DENSITY_INDEX = 5;	
	private static final int ATTR_AVG_SPEED_INDEX = 3;
	
	//total expected number of fields in a csv line. Assuming here that the length histogram is the ending part of csv
	
	
	private EventFactory eventFactory;

	public TrafficAimsunReadingCsv2Event(EventFactory eventFactory) {
		this.eventFactory = eventFactory;
	}

	public Event fromBytes(byte[] bytes) throws ParsingError {
		String name = TRAFFIC_SENSOR_READING_AVERAGE;

		try {
			SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			String[] tuple = new String(bytes, Charset.forName("UTF-8")).split(",");
			
			long timestamp;

			timestamp = dateTimeFormat.parse(tuple[ATTR_TIME_INDEX]).getTime();

			HashMap<String, Object> attrMap = new HashMap<String, Object>();
			
			Integer carsNumber = Integer.valueOf(tuple[ATTR_NUMBER_OF_CARS_INDEX]);
			Integer trucksNumber  = Integer.valueOf(tuple[ATTR_NUMBER_OF_TRUCKS_INDEX]);
			Double average_density = Double.parseDouble(tuple[ATTR_DENSITY_INDEX]);
			
			
			//normalize density value
			Double calculatedDensity = 0.0;
			if (!(carsNumber == 0 && trucksNumber==0)){
				calculatedDensity = average_density/(1000/((4*carsNumber+7.5*trucksNumber)/(carsNumber+trucksNumber)));
			}
			
			if (calculatedDensity < 0.001){
				calculatedDensity = 0.0;
			}
									
			attrMap.put(ATTR_TIMESTAMP, timestamp);
			attrMap.put(ATTR_DETECTOR_ID, tuple[ATTR_DETECTOR_ID_INDEX]);
			attrMap.put(ATTR_OCCUPANCY, getNumericValue(tuple[ATTR_OCCUPANCY_INDEX])/100);	
			attrMap.put(ATTR_VEHICLES, carsNumber+trucksNumber);			
			attrMap.put(ATTR_AVG_SPEED, getNumericValue(tuple[ATTR_AVG_SPEED_INDEX]));
			attrMap.put(ATTR_NORMALIZED_DENSITY, calculatedDensity);
			

			return eventFactory.createEvent(name, timestamp, attrMap);
		} catch (Exception e) {
			throw new ParsingError(
					"Error parsing CSV for aggregated traffic reading", e);
		}
	}

	
	
	private Double getNumericValue(String strVal){
		try {
			return Double.parseDouble(strVal);
		}
		catch (NumberFormatException e){
			//if cannot parse - the value is not available, - return null
			return null;
		}
	}

}
