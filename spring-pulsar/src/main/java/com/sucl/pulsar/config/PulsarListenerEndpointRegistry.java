package com.sucl.pulsar.config;

import com.sucl.pulsar.listener.MessageListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;

import java.util.HashMap;
import java.util.Map;

/**
 * endpoint的注册器
 * 创建ListenerContainer
 * 完成Consumer的监听（SmartLifecycle）
 *
 * @author sucl
 * @date 2023/2/22 17:00
 * @since 1.0.0
 */
public class PulsarListenerEndpointRegistry implements DisposableBean, SmartLifecycle, ApplicationContextAware {

    private Map<String,MessageListenerContainer> listenerContainers = new HashMap<>();

    public void registerListenerContainer(PulsarListenerEndpoint endpoint, PulsarListenerContainerFactory containerFactory, boolean startImmediately){
        synchronized (this.listenerContainers){
            MessageListenerContainer listenerContainer = createListenerContainer(containerFactory, endpoint);
            if(!this.listenerContainers.containsKey(endpoint.getId())){
                this.listenerContainers.put(endpoint.getId(), listenerContainer);
            }
            if( startImmediately ){
                startIfNecessary(listenerContainer);
            }
        }
    }

    private MessageListenerContainer createListenerContainer(PulsarListenerContainerFactory containerFactory, PulsarListenerEndpoint endpoint) {
        MessageListenerContainer listenerContainer = containerFactory.createListenerContainer(endpoint);
        if(listenerContainer instanceof InitializingBean){
            try {
                ((InitializingBean) listenerContainer).afterPropertiesSet();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return listenerContainer;
    }

    private void startIfNecessary(MessageListenerContainer listenerContainer) {
        listenerContainer.start();
    }

    public void registerListenerContainer(PulsarListenerEndpoint endpoint, PulsarListenerContainerFactory containerFactory){
        registerListenerContainer(endpoint, containerFactory, false);
    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }

    @Override
    public void start() {
        this.listenerContainers.values().forEach(MessageListenerContainer::start);
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
