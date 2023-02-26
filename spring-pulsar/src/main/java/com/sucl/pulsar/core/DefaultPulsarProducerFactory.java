package com.sucl.pulsar.core;

import com.sucl.pulsar.ConsumerConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sucl
 * @date 2023/2/21 11:13
 * @since 1.0.0
 */
@Slf4j
public class DefaultPulsarProducerFactory<T> implements ProducerFactory<T>{

    private PulsarClient pulsarClient;

    private Map<String,Object> config;

    public DefaultPulsarProducerFactory(PulsarClient pulsarClient){
        this(pulsarClient, null);
    }

    public DefaultPulsarProducerFactory(PulsarClient pulsarClient, Map<String, Object> config) {
        this.pulsarClient = pulsarClient;
        this.config = config;
    }

    /**
     *
     * @param topic not null
     * @return
     */
    @Override
    public Producer<T> createProducer(String topic) {
        return createProducer(Collections.EMPTY_MAP, topic);
    }

    /**
     *
     * @param config
     * @return
     */
    @Override
    public Producer<T> createProducer(Map<String,Object> config, String topic) {
        Map<String,Object> producerConfig = new HashMap<>();
        if(this.config != null){
            producerConfig.putAll(this.config);
        }
        if(config != null){
            producerConfig.putAll(config);
        }
        ProducerBuilder producerBuilder = pulsarClient
                .newProducer(obtainSchema(producerConfig))
                .topic(topic)
                .loadConf(producerConfig);

        return createProducer(producerBuilder);
    }

    private Schema obtainSchema(Map<String, Object> config){
        Object schema = config.remove(ConsumerConfigUtils.SCHEMA_NAME);
        if(schema instanceof Schema){
            return (Schema) schema;
        }
        // AUTO
        return Schema.AUTO_CONSUME();
    }

    private Producer createProducer(ProducerBuilder producerBuilder){
        try {
            return producerBuilder.create();
        } catch (PulsarClientException e) {
            log.info("create Producer error: {}" , e.getMessage(), e);
        }
        return null;
    }
}
