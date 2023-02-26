package com.sucl.pulsar.core;

import org.apache.pulsar.client.api.Consumer;

import java.util.Map;

/**
 * Consumer工厂
 *
 * @author sucl
 * @date 2023/2/16 22:20
 * @since 1.0.0
 */
public interface ConsumerFactory<T> {

    Consumer<T> createConsumer(String... topicNames);

    Consumer<T> createConsumer(Map<String,Object> config, String... topicNames);

    boolean isAutoCommit();
}
