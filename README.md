Flume NG Fluentd Sink
===

[![Build Status](https://travis-ci.org/cosmo0920/flume-ng-fluentd-sink.svg?branch=master)](https://travis-ci.org/cosmo0920/flume-ng-fluentd-sink)

Apache Flume Sink plugin for Fluentd.

## Configuration

### For Apache Flume

Set configuration properties like this:

```
a1.sinks = k1

a1.sinks.k1.type = com.github.cosmo0920.fluentd.flume.plugins.FluentdSink
al.sinks.k1.hostname = localhost
a1.sinks.k1.port = 24224
a1.sinks.k1.tag = flume.fluentd.sink
a1.sinks.k1.format = text
```

`hostname`, `port`, and `tag` are optional.

You can pass `text` or `json` into `format`.

Please refer to [example single flume node example](config/example-fluentd-sink.conf) for in more detail usage.

### For Fluentd

Set configuration properties like this:

```conf
<source>
  type forward
  bind 0.0.0.0
  port 24224
</source>
```

`bind` and `port` are optional.

## How to use

Create all-in-one jar file.

```bash
$ ./gradlew shadowJar
```

Put created all-in-one jar file into your $FLUME_HOME/lib.

## LICENSE

* [Apache-2.0](LICENSE.txt)
