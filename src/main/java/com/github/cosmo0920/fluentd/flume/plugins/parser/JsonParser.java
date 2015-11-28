package com.github.cosmo0920.fluentd.flume.plugins.parser;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

public class JsonParser extends EventParser {
	private final static ObjectMapper mapper = new ObjectMapper(new JsonFactory());
	private final static TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};

	public JsonParser() {
		super();
	}

	@Override
	public Map<String, Object> parse(String entry) throws RuntimeException {
		try {
			return mapper.readValue(entry, typeRef);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
