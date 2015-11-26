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

import com.google.common.base.Preconditions;

public class FluentdSink extends AbstractSink implements Configurable {
	private static final Logger logger = LoggerFactory.getLogger(FluentdSink.class);

	private static final int DEFAULT_PORT = 24424;
	private static final String DEFAULT_HOST = "localhost";
	private static final String DEFAULT_TAG = "flume.fluentd.sink";

	private String hostname;
	private Integer port;
	private String tag;

	private CounterGroup counterGroup;

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

	@Override
	public void start() {
		logger.info("Fluentd sink starting");

		super.start();

		logger.debug("Fluentd sink {} started", this.getName());
	}

	@Override
	public void stop() {
		logger.info("Fluentd sink {} stopping", this.getName());

		super.stop();

		logger.debug("Fluentd sink {} stopped. Metrics:{}", this.getName(), counterGroup);
	}

	@Override
	public Status process() throws EventDeliveryException {
		Status status = Status.READY;

		return status;
	}
}
