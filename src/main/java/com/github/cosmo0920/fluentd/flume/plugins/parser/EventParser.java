package com.github.cosmo0920.fluentd.flume.plugins;

import java.util.Map;

public abstract class EventParser {

	public EventParser() {}
	public abstract Map<String, Object> parse(String body) throws Exception;
}
