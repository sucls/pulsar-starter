package com.sucl.pulsar.config;

import lombok.Data;

import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 监听端点抽象类，定义了端点的通用属性
 *
 * @author sucl
 * @date 2023/2/23 21:45
 * @since 1.0.0
 */
@Data
public abstract class AbstractPulsarListenerEndpoint implements PulsarListenerEndpoint {

    private String id;

    private String[] topics;

    private String[] tags;

    private Pattern topicPattern;

    private Boolean batchListener;

    private Properties consumerProperties;

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String[] getTopics() {
        return this.topics;
    }

    @Override
    public String[] getTags() {
        return this.tags;
    }

    @Override
    public Boolean getBatchListener() {
        return this.batchListener;
    }

}
