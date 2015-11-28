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

import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.annotations.VisibleForTesting;

public class FluentdSink extends AbstractSink implements Configurable {
	private static final Logger logger = LoggerFactory.getLogger(FluentdSink.class);

	private static final int DEFAULT_PORT = 24224;
	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_TAG = "flume.fluentd.sink";

	private String hostname;
	private int port;
	private String tag;
	private String format;

	@VisibleForTesting
	public FluencyPublisher publisher;

	private CounterGroup counterGroup;

	public FluentdSink() {
		counterGroup = new CounterGroup();
	}

	public void configure(Context context) {
		hostname = context.getString("hostname");
		String portStr = context.getString("port");
		tag = context.getString("tag");
		format = context.getString("format");

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
		Preconditions.checkState(format != null, "No format specified");

		publisher = new FluencyPublisher(tag, format);
	}

	@Override
	public void start() {
		logger.info("Fluentd sink starting");

		try {
			publisher.setup(hostname, port);
		} catch (IOException e) {
			logger.error("Unable to create Fluentd logger using hostname:"
						 + hostname + " port:" + port + ". Exception follows.", e);

			publisher.close();

			return; // FIXME: mark this plugin as failed.
		}

		super.start();

		logger.debug("Fluentd sink {} started", this.getName());
	}

	@Override
	public void stop() {
		logger.info("Fluentd sink {} stopping", this.getName());

		publisher.close();

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
				transaction.rollback();
			} else {
				publisher.publish(event);
				counterGroup.incrementAndGet("event.fluentd");
				transaction.commit();
			}

		} catch (ChannelException e) {
			transaction.rollback();
			logger.error(
					"Unable to get event from channel. Exception follows.",
					e);
			status = Status.BACKOFF;
		} catch (IOException e) {
			transaction.rollback();
			logger.error(
					"Unable to communicate with Fluentd. Exception follows.",
					e);
			status = Status.BACKOFF;
			publisher.close();
		} catch (RuntimeException e) {
			transaction.rollback();
			logger.error(
					"Unable to parse event body. Exception follows.",
					e);
			status = Status.BACKOFF;
			publisher.close();
		} finally {
			transaction.close();
		}

		return status;
	}
}
