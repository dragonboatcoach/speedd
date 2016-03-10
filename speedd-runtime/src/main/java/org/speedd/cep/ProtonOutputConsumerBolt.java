package org.speedd.cep;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.speedd.data.Event;
import org.speedd.data.EventFactory;
import org.speedd.data.impl.SpeeddEventFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.ibm.hrl.proton.metadata.event.EventHeader;
import com.ibm.hrl.proton.routing.STORMMetadataFacade;

public class ProtonOutputConsumerBolt extends BaseRichBolt implements org.speedd.Fields {
	private static final long serialVersionUID = 1L;
	
	private OutputCollector collector;
	private static final EventFactory eventFactory = SpeeddEventFactory.getInstance();
	Logger logger = LoggerFactory.getLogger(ProtonOutputConsumerBolt.class);

	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
	}

	@Override
	public void execute(Tuple input) {
		logger.debug("Processing tuple " + input.toString());
		
		String eventName = (String)input.getValueByField(EventHeader.NAME_ATTRIBUTE);

		Map<String, Object> inAttrs = (Map<String, Object>)input.getValueByField(STORMMetadataFacade.ATTRIBUTES_FIELD);
		
		long timestamp = 0;
		
		if(inAttrs.containsKey(EventHeader.DETECTION_TIME_ATTRIBUTE)){
			timestamp = (Long)inAttrs.get(EventHeader.DETECTION_TIME_ATTRIBUTE);
		}
		
		Event outEvent = eventFactory.createEvent(eventName, timestamp, inAttrs);

		logger.debug("Emitting out event: " + outEvent.getEventName());
		
		//FIXME use meaningful value for the 'key' field. It'll be used by kafka for partitioning
		collector.emit(new Values("1", outEvent));
		collector.ack(input);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("key", "message"));
	}

}
