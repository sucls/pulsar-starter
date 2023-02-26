package com.sucl.pulsar.config;

import com.sucl.pulsar.core.ConsumerFactory;
import com.sucl.pulsar.listener.ConcurrentMessageListenerContainer;
import com.sucl.pulsar.listener.ContainerProperties;
import com.sucl.pulsar.listener.MessageListenerContainer;
import com.sucl.pulsar.support.JavaUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 并发监听端点容器工厂
 * 实现了容器的创建与配置
 * @author sucl
 * @date 2023/2/22 19:55
 * @since 1.0.0
 */
@Getter
@Setter
public class ConcurrentPulsarListenerContainerFactory<C extends MessageListenerContainer> implements PulsarListenerContainerFactory<C>{

    private final ContainerProperties containerProperties = new ContainerProperties();

    private ConsumerFactory consumerFactory;

    /**
     *
     */
    private Boolean batchListener;

    @Override
    public C createListenerContainer(PulsarListenerEndpoint endpoint) {
        C instance = createContainerInstance(endpoint);
        if(endpoint instanceof AbstractPulsarListenerEndpoint){
            initializeContainer(instance, (AbstractPulsarListenerEndpoint)endpoint);
        }
        endpoint.setupListenerContainer(instance);
        customizeContainer(instance);
        return instance;
    }

    private C createContainerInstance(PulsarListenerEndpoint endpoint) {
        String[] topics = endpoint.getTopics();
        if (topics != null && topics.length > 0) {
            ContainerProperties properties = new ContainerProperties(topics);
            ConcurrentMessageListenerContainer container = new ConcurrentMessageListenerContainer(getConsumerFactory(), properties);
            return (C) container; //
        }
        return null;
    }

    private void initializeContainer(C instance, AbstractPulsarListenerEndpoint endpoint) {
        JavaUtils.INSTANCE
                .acceptIfNotNull(this.batchListener, endpoint::setBatchListener);
    }

    private void customizeContainer(C instance) {
        //
    }

    @Override
    public void setConsumerFactory(ConsumerFactory consumerFactory) {
        this.consumerFactory = consumerFactory;
    }
}
