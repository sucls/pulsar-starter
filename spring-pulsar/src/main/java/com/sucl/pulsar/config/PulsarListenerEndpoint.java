package com.sucl.pulsar.config;

import com.sucl.pulsar.listener.MessageListenerContainer;

import java.util.Collection;

/**
 * 对应PulsarHandler标记的方法
 * 一个方法一个Endpoint
 *
 * @author sucl
 * @date 2023/2/22 16:34
 * @since 1.0.0
 */
public interface PulsarListenerEndpoint {

    /**
     *
     * @return
     */
    String getId();

    /**
     *
     * @return
     */
    String[] getTopics();

    /**
     *
     * @return
     */
    String[] getTags();

    /**
     *
     * @return
     */
    Boolean getBatchListener();

    /**
     *
     * @param container
     */
    void setupListenerContainer(MessageListenerContainer container);

}
