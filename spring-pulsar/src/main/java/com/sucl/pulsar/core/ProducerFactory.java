package com.sucl.pulsar.core;

import org.apache.pulsar.client.api.Producer;

import java.util.Map;

/**
 * 生产者工厂
 *
 * @author sucl
 * @date 2023/2/16 22:20
 * @since 1.0.0
 */
public interface ProducerFactory<T> {

    /**
     *
     * @param topic
     * @return
     */
    Producer<T> createProducer(String topic);

    /**
     *
     * @param config
     * @param topic
     * @return
     */
    Producer<T> createProducer(Map<String, Object> config, String topic);
}
