package com.github.cosmo0920.fluentd.flume.plugins.parser;

import java.util.Map;
import java.util.HashMap;

public class PlainTextParser extends EventParser {
	public PlainTextParser () {
		super();
	}

	@Override
	public Map<String, Object> parse(String entry) {
		Map<String, Object> event = new HashMap<>();
		event.put("message", entry);
		return event;
	}
}
