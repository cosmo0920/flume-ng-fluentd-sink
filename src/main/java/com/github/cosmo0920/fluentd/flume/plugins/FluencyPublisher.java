package com.github.cosmo0920.fluentd.flume.plugins;
import org.apache.flume.Event;

import org.komamitsu.fluency.Fluency;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

class FluencyPublisher {
	private Fluency fluency;
	private String tag;

	public FluencyPublisher(String tag) {
		this.tag = tag;
	}

	public void setup(String hostname, int port) throws  IOException {
		fluency = Fluency.defaultFluency(hostname, port, new Fluency.Config());
	}

	public void close() {
		if (fluency != null) {
			try {
				fluency.close();
			} catch (IOException e) {
				// Do nothing.
			}
		}

		fluency = null;
	}

	// TODO: JSON encoded body support.
	public void send(Event event) throws IOException {
		String body = new String(event.getBody());
		Map<String, Object> fluencyEvent = new HashMap<String, Object>();
		fluencyEvent.put("message", body);
		fluency.emit(tag, fluencyEvent);
	}
}
