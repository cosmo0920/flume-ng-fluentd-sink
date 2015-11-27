package com.github.cosmo0920.fluentd.flume.plugins;
import org.apache.flume.Event;

import org.komamitsu.fluency.Fluency;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.lang.RuntimeException;
import java.lang.UnsupportedOperationException;

class FluencyPublisher {
	private Fluency fluency;
	private String tag;
	private String format;

	public FluencyPublisher(String tag, String format) {
		this.tag = tag;
		this.format = format;
	}

	public void setup(String hostname, int port) throws IOException {
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

	public void publish(Event event) throws IOException, Exception {
		String body = new String(event.getBody(), StandardCharsets.UTF_8);
		switch(format) {
		case "text":
			Map<String, Object> fluencyEvent = new HashMap<String, Object>();
			fluencyEvent.put("message", body);
			fluency.emit(tag, fluencyEvent);
			break;
		case "json":
			// TODO: constrct parser at instantiated time
			JsonParser parser = new JsonParser();
			fluency.emit(tag, parser.parse(body));
			break;
		default:
			throw new RuntimeException(format + " format is not supported");
		}
	}
}
