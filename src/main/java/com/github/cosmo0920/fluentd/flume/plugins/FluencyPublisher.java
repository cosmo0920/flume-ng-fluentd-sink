package com.github.cosmo0920.fluentd.flume.plugins;
import org.apache.flume.Event;

import org.komamitsu.fluency.Fluency;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.lang.RuntimeException;
import java.lang.UnsupportedOperationException;

import com.github.cosmo0920.fluentd.flume.plugins.parser.EventParser;
import com.github.cosmo0920.fluentd.flume.plugins.parser.PlainTextParser;
import com.github.cosmo0920.fluentd.flume.plugins.parser.JsonParser;

class FluencyPublisher {
	private Fluency fluency;
	private String tag;
	private String format;
	private EventParser parser;

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

	public void publish(Event event) throws IOException, RuntimeException {
		String body = new String(event.getBody(), StandardCharsets.UTF_8);
		fluency.emit(tag, parser.parse(body));
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
