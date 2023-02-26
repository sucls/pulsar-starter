package com.sucl.pulsar.core;

import com.sucl.pulsar.ConsumerConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sucl
 * @date 2023/2/20 20:15
 * @since 1.0.0
 */
@Slf4j
public class DefaultPulsarConsumerFactory<T> implements ConsumerFactory<T>{

    private PulsarClient pulsarClient;

    private Map<String,Object> config;

    private boolean autoCommit;

    public DefaultPulsarConsumerFactory(PulsarClient pulsarClient) {
        this(pulsarClient, null);
    }

    public DefaultPulsarConsumerFactory(PulsarClient pulsarClient, Map<String,Object> config) {
        this.pulsarClient = pulsarClient;
        this.config = config;

        Object autoCommitValue = config.remove(ConsumerConfigUtils.AUTO_COMMIT_NAME);
        this.autoCommit = autoCommitValue!=null && Boolean.valueOf(autoCommitValue.toString()).booleanValue();
    }

    /**
     *
     * @param configs
     * @return
     */
    @Override
    public Consumer<T> createConsumer(String... topicNames){
        return this.createConsumer(null, topicNames);
    }

    /**
     *
     * @param config
     * @param topicNames
     * @return
     */
    @Override
    public Consumer<T> createConsumer(Map<String, Object> config, String... topicNames) {
        Map<String,Object> consumerConfig = new HashMap<>();
        if(this.config != null){
            consumerConfig.putAll(this.config);
        }
        if(config != null){
            consumerConfig.putAll(config);
        }
        ConsumerBuilder consumerBuilder = pulsarClient
                .newConsumer(obtainSchema(consumerConfig))
                .topics(Arrays.asList(topicNames))
                .subscriptionName((String) consumerConfig.get(ConsumerConfigUtils.SUBSCRIPTION_NAME_NAME))
                .loadConf(consumerConfig);
        return createConsumer(consumerBuilder);
    }

    private Schema obtainSchema(Map<String, Object> config){
        Object schema = config.remove(ConsumerConfigUtils.SCHEMA_NAME);
        if(schema instanceof Schema){
            return (Schema) schema;
        }
        // AUTO
        return Schema.AUTO_CONSUME();
    }

    private Consumer createConsumer(ConsumerBuilder consumerBuilder){
        try {
            return consumerBuilder.subscribe();
        } catch (PulsarClientException e) {
            log.info("create Consumer error: {}" , e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean isAutoCommit() {
        return this.autoCommit;
    }
}
