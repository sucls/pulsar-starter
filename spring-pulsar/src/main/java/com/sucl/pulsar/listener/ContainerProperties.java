package com.sucl.pulsar.listener;

import lombok.Data;
import org.springframework.core.task.AsyncListenableTaskExecutor;

/**
 * 监听容器属性
 * @author sucl
 * @date 2023/2/23 15:03
 * @since 1.0.0
 */
@Data
public class ContainerProperties {

    /**
     *
     */
    private String[] topics;

    /**
     *
     */
    private AckMode ackMode;

    /**
     *
     */
    private Object MessageListener;

    /**
     *
     */
    private AsyncListenableTaskExecutor consumerTaskExecutor;

    public ContainerProperties(){}

    public ContainerProperties(String[] topics) {
        this.topics = topics;
    }

    public enum AckMode {

        /**
         * Commit after each record is processed by the listener.
         */
        RECORD,

        /**
         * Commit whatever has already been processed before the next poll.
         */
        BATCH,

        /**
         * User takes responsibility for acks using an
         * {@link AcknowledgingMessageListener}.
         */
        MANUAL,

        /**
         * User takes responsibility for acks using an
         * {@link AcknowledgingMessageListener}. The consumer
         * immediately processes the commit.
         */
        MANUAL_IMMEDIATE,
    }
}
