package com.github.cosmo0920.fluentd.flume.plugins;
import org.apache.flume.Event;

import org.komamitsu.fluency.Fluency;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.lang.RuntimeException;
import java.util.Map;

import com.github.cosmo0920.fluentd.flume.plugins.parser.EventParser;
import com.github.cosmo0920.fluentd.flume.plugins.parser.PlainTextParser;
import com.github.cosmo0920.fluentd.flume.plugins.parser.JsonParser;

class FluencyPublisher {
	private Fluency fluency;
	private String tag;
	private String format;
	private EventParser parser;
	private static final String HEADER_TIMESTAMP = "timestamp";

	public FluencyPublisher(String tag, String format) {
		this.tag = tag;
		this.format = format;
		this.parser = setupEventParser();
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

	public boolean containsHeader(Event event, String headerName) {
		return event.getHeaders().containsKey(headerName);
	}

	public long getHeaderTimestamp(Event event) {
		Map<String, String> headerMap = event.getHeaders();
		return Long.parseLong(headerMap.get(HEADER_TIMESTAMP));
	}

	public void publish(Event event) throws IOException, RuntimeException {
		String body = new String(event.getBody(), StandardCharsets.UTF_8);
		if (containsHeader(event, HEADER_TIMESTAMP)) {
			long time = getHeaderTimestamp(event);
			fluency.emit(tag, time, parser.parse(body));
		} else {
			fluency.emit(tag, parser.parse(body));
		}
	}

	private EventParser setupEventParser() {
		switch (format) {
		case "text":
			return new PlainTextParser();
		case "json":
			return new JsonParser();
		default:
			throw new RuntimeException(format + " format is not supported");
		}
	}
}
