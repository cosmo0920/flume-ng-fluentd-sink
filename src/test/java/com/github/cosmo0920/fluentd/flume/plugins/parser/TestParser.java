package com.github.cosmo0920.fluentd.flume.plugins.parser;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.Map;
import java.util.HashMap;

import java.io.IOException;

public class TestParser {

	@Test
	public void testJsonParser() {
		String json = "{\"test\":\"message\"}";
		JsonParser parser = new JsonParser();
		Map<String, Object> result = parser.parse(json);
		Map<String, Object> expect = new HashMap<String, Object>();
		expect.put("test", "message");
		assertEquals(expect, result);
	}

	@Test
	public void testPlainTextParser() {
		String eventBody = "test entry";
		PlainTextParser parser = new PlainTextParser();
		Map<String, Object> result = parser.parse(eventBody);
		Map<String, Object> expect = new HashMap<String, Object>();
		expect.put("message", "test entry");
		assertEquals(expect, result);
	}

	@Test
	public void testHierarchy() {
		EventParser textParser = new PlainTextParser();
		EventParser jsonParser = new JsonParser();

		assertTrue(EventParser.class.isInstance(textParser));
		assertTrue(EventParser.class.isInstance(jsonParser));
	}

}
