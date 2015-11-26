package com.github.cosmo0920.fluentd.flume.plugins;

import org.apache.flume.Channel;
import org.apache.flume.ChannelException;
import org.apache.flume.Context;
import org.apache.flume.CounterGroup;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.komamitsu.fluency.Fluency;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import com.google.common.base.Preconditions;

public class FluentdSink extends AbstractSink implements Configurable {
	private static final Logger logger = LoggerFactory.getLogger(FluentdSink.class);

	private static final int DEFAULT_PORT = 24424;
	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_TAG = "flume.fluentd.sink";

	private String hostname;
	private int port;
	private String tag;
	private Fluency fluency; // TODO: extract this member to other class for safety.

	private CounterGroup counterGroup;

	public FluentdSink() {
		counterGroup = new CounterGroup();
	}

	public void configure(Context context) {
		hostname = context.getString("hostname");
		String portStr = context.getString("port");
		tag = context.getString("tag");

		if (portStr != null) {
			port = Integer.parseInt(portStr);
		} else {
			port = DEFAULT_PORT;
		}

		if (hostname == null) {
			hostname = DEFAULT_HOST;
		}

		if (tag == null) {
			tag = DEFAULT_TAG;
		}

		Preconditions.checkState(hostname != null, "No hostname specified");
		Preconditions.checkState(tag != null, "No tag specified");
	}

	private Fluency setupFluency(String hostname, int port) throws  IOException {
		return Fluency.defaultFluency(hostname, port, new Fluency.Config());
	}

	private void closeFluency() {
		if (fluency != null) {
			try {
				fluency.close();
			} catch (IOException e) {
				// Do nothing.
			}
		}
	}

	private void sendFluency(Event event) throws IOException {
		String body = new String(event.getBody());
		Map<String, Object> fluencyEvent = new HashMap<String, Object>();
		fluencyEvent.put("message", body);
		fluency.emit(tag, fluencyEvent);
	}

	@Override
	public void start() {
		logger.info("Fluentd sink starting");

		try {
			fluency = setupFluency(hostname, port);
		} catch (IOException e) {
			logger.error("Unable to create Fluentd logger using hostname:"
						 + hostname + " port:" + port + ". Exception follows.", e);

			closeFluency();

			return; // FIXME: mark this plugin as failed.
		}

		super.start();

		logger.debug("Fluentd sink {} started", this.getName());
	}

	@Override
	public void stop() {
		logger.info("Fluentd sink {} stopping", this.getName());

		closeFluency();

		super.stop();

		logger.debug("Fluentd sink {} stopped. Metrics:{}", this.getName(), counterGroup);
	}

	@Override
	public Status process() throws EventDeliveryException {
		Status status = Status.READY;
		Channel channel = getChannel();
		Transaction transaction = channel.getTransaction();

		try {
			transaction.begin();

			Event event = channel.take();

			if (event == null) {
				 counterGroup.incrementAndGet("event.empty");
				 status = Status.BACKOFF;
			} else {
				sendFluency(event);
				counterGroup.incrementAndGet("event.fluentd");
			}

			transaction.commit();

		} catch (ChannelException e) {
			transaction.rollback();
			logger.error(
					"Unable to get event from channel. Exception follows.", e);
			status = Status.BACKOFF;
		} catch (IOException e) {
			transaction.rollback();
			logger.error(
					"Unable to communicate with Fluentd. Exception follows.",
					e);
			status = Status.BACKOFF;
			closeFluency();
		} finally {
			transaction.close();
		}

		return status;
	}
}
