package com.sucl.pulsar.listener;

import com.sucl.pulsar.core.ConsumerFactory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

/**
 * @author sucl
 * @date 2023/2/26 10:30
 * @since 1.0.0
 */
@Getter
@Setter
public abstract class AbstractMessageListenerContainer implements MessageListenerContainer{

    protected ConsumerFactory consumerFactory;

    private ContainerProperties containerProperties;

    private boolean running;

    public AbstractMessageListenerContainer(ConsumerFactory consumerFactory, ContainerProperties containerProperties) {
        this.consumerFactory = consumerFactory;
        this.containerProperties = containerProperties;
    }

    private Object lifecycleMonitor = new Object();

    @Override
    public void start() {
        synchronized (lifecycleMonitor){
            if(!isRunning()){
                Assert.state(this.containerProperties.getMessageListener() instanceof GenericMessageListener,
                        () -> "A " + GenericMessageListener.class.getName() + " implementation must be provided");
                doStart();
            }
        }
    }

    protected abstract void doStart();

    @Override
    public void setupMessageListener(Object messageListener) {
        this.containerProperties.setMessageListener(messageListener);
    }
}
