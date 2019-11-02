open module com.sandpolis.core.stream {
	exports com.sandpolis.core.stream.store;
	exports com.sandpolis.core.stream;

	requires com.google.common;
	requires com.google.protobuf;
	requires com.sandpolis.core.instance;
	requires com.sandpolis.core.net;
	requires com.sandpolis.core.proto;
	requires com.sandpolis.core.util;
	requires org.slf4j;
}