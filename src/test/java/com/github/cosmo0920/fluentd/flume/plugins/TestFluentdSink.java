package com.github.cosmo0920.fluentd.flume.plugins;

import java.nio.charset.StandardCharsets;

import org.apache.flume.Sink.Status;
import org.apache.flume.Channel;
import org.apache.flume.ChannelException;
import org.apache.flume.Event;
import org.apache.flume.Transaction;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.event.EventBuilder;

import org.komamitsu.fluency.Fluency;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class TestFluentdSink {
	private static final Logger logger = LoggerFactory.getLogger(FluentdSink.class);

	@Test
	public void testShouldCommitASuccessfulTransaction() throws EventDeliveryException {
		Channel mockChannel = mock(Channel.class);
		Transaction mockTransaction = mock(Transaction.class);
		Event mockEvent = mock(Event.class);
		FluencyPublisher mockFluencyPublisher = mock(FluencyPublisher.class);

		when(mockChannel.getTransaction()).thenReturn(mockTransaction);
		when(mockChannel.take()).thenReturn(mockEvent);
		when(mockEvent.getBody()).thenReturn("test body".getBytes(StandardCharsets.UTF_8));

		FluentdSink sink = new FluentdSink();
		sink.setChannel(mockChannel);
		sink.publisher = mockFluencyPublisher;
		assertNotNull(sink.publisher);

		Status status = sink.process();
		assertEquals(status, Status.READY);

		verify(mockTransaction, times(1)).begin();
		verify(mockTransaction, times(1)).commit();
		verify(mockTransaction, times(1)).close();
	}

	@Test
	public void testShouldRollbackAnUnsuccessfulTransaction() throws EventDeliveryException, IOException {
		Channel mockChannel = mock(Channel.class);
		Transaction mockTransaction = mock(Transaction.class);
		Event mockEvent = mock(Event.class);
		FluencyPublisher mockFluencyPublisher = mock(FluencyPublisher.class);

		when(mockChannel.getTransaction()).thenReturn(mockTransaction);
		when(mockChannel.take()).thenReturn(mockEvent);
		when(mockEvent.getBody()).thenReturn("test body".getBytes(StandardCharsets.UTF_8));

		FluentdSink sink = new FluentdSink();
		sink.setChannel(mockChannel);
		sink.publisher = mockFluencyPublisher;
		assertNotNull(sink.publisher);
		doThrow(new IOException()).when(mockFluencyPublisher).publish(mockEvent);

		Status status = sink.process();
		assertEquals(status, Status.BACKOFF);

		verify(mockTransaction, times(1)).begin();
		verify(mockTransaction, times(1)).rollback();
		verify(mockTransaction, times(1)).close();
	}

	@Test
	public void testShouldRollbackWhenThereAreNoEventsInChannel() throws EventDeliveryException, IOException {
		Channel mockChannel = mock(Channel.class);
		Transaction mockTransaction = mock(Transaction.class);
		FluencyPublisher mockFluencyPublisher = mock(FluencyPublisher.class);

		when(mockChannel.getTransaction()).thenReturn(mockTransaction);
		when(mockChannel.take()).thenReturn(null);

		FluentdSink sink = new FluentdSink();
		sink.setChannel(mockChannel);
		sink.publisher = mockFluencyPublisher;
		assertNotNull(sink.publisher);

		Status status = sink.process();
		assertEquals(status, Status.BACKOFF);

		verify(mockTransaction, times(1)).begin();
		verify(mockTransaction, times(1)).rollback();
		verify(mockTransaction, times(1)).close();
	}

	@Test
	public void testShouldRollbackWhenUnableToGetEventsFromChannel() throws EventDeliveryException, IOException {
		Channel mockChannel = mock(Channel.class);
		Transaction mockTransaction = mock(Transaction.class);
		FluencyPublisher mockFluencyPublisher = mock(FluencyPublisher.class);

		when(mockChannel.getTransaction()).thenReturn(mockTransaction);
		doThrow(new ChannelException("Mock Exception")).when(mockChannel).take();

		FluentdSink sink = new FluentdSink();
		sink.setChannel(mockChannel);
		sink.publisher = mockFluencyPublisher;
		assertNotNull(sink.publisher);

		Status status = sink.process();
		assertEquals(status, Status.BACKOFF);

		verify(mockTransaction, times(1)).begin();
		verify(mockTransaction, times(1)).rollback();
		verify(mockTransaction, times(1)).close();
	}
}
