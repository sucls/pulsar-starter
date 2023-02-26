package com.sucl.pulsar.autoconfigure;

import com.sucl.pulsar.config.ConcurrentPulsarListenerContainerFactory;
import com.sucl.pulsar.core.ConsumerFactory;
import com.sucl.pulsar.listener.ContainerProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.PropertyMapper;

/**
 * ContainerFactory的配置类，在ContainerFactoryBean注册时进行扩展
 *
 * @author sucl
 * @date 2023/2/22 19:58
 * @since 1.0.0
 */
@Getter
@Setter
public class ConcurrentPulsarListenerContainerFactoryConfigurer {

    private PulsarProperties properties;

    /**
     *
     * @param factory
     * @param consumerFactory
     */
    public void configure(ConcurrentPulsarListenerContainerFactory listenerFactory, ConsumerFactory<Object> consumerFactory) {
        listenerFactory.setConsumerFactory(consumerFactory);
        configureListenerFactory(listenerFactory);
        configureContainer(listenerFactory.getContainerProperties());
    }

    private void configureListenerFactory(ConcurrentPulsarListenerContainerFactory listenerFactory) {
        listenerFactory.setBatchListener(properties.getListener().getBatch());
    }

    private void configureContainer(ContainerProperties container) {
        // config
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        PulsarProperties.Listener properties = this.properties.getListener();
        map.from(properties::getAckMode).to(container::setAckMode);
    }
}
