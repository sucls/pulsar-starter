package com.sucl.pulsar.config;

import com.sucl.pulsar.core.ConsumerFactory;
import com.sucl.pulsar.listener.MessageListenerContainer;

/**
 * 监听容器工厂
 * @author sucl
 * @date 2023/2/22 16:59
 * @since 1.0.0
 */
public interface PulsarListenerContainerFactory<C extends MessageListenerContainer> {

    /**
     *
     * @param endpoint
     * @return
     */
    C createListenerContainer(PulsarListenerEndpoint endpoint);

    /**
     *
     * @param consumerFactory
     */
    void setConsumerFactory(ConsumerFactory<Object> consumerFactory);
}
