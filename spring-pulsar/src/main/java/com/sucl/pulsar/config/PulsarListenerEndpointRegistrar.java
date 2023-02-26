package com.sucl.pulsar.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * PulsarListenerEndpointRegistry注册Endpoint的上下文
 *
 *
 * @author sucl
 * @date 2023/2/22 16:35
 * @since 1.0.0
 */
public class PulsarListenerEndpointRegistrar implements BeanFactoryAware, InitializingBean {

    private final List<PulsarListenerEndpointDescriptor> endpointDescriptors = new ArrayList<>();

    private List<HandlerMethodArgumentResolver> customMethodArgumentResolvers = new ArrayList<>();

    private PulsarListenerEndpointRegistry endpointRegistry;

    private MessageHandlerMethodFactory messageHandlerMethodFactory;

    private PulsarListenerContainerFactory<?> containerFactory;

    private String containerFactoryBeanName;

    private BeanFactory beanFactory;

    private boolean startImmediately;

    public void setEndpointRegistry(PulsarListenerEndpointRegistry endpointRegistry) {
        this.endpointRegistry = endpointRegistry;
    }

    public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory messageHandlerMethodFactory) {
        this.messageHandlerMethodFactory = messageHandlerMethodFactory;
    }

    public void setContainerFactory(PulsarListenerContainerFactory<?> containerFactory) {
        this.containerFactory = containerFactory;
    }

    public void setContainerFactoryBeanName(String containerFactoryBeanName) {
        this.containerFactoryBeanName = containerFactoryBeanName;
    }

    public void setStartImmediately(boolean startImmediately) {
        this.startImmediately = startImmediately;
    }

    public void setCustomMethodArgumentResolvers(HandlerMethodArgumentResolver... methodArgumentResolvers) {
        this.customMethodArgumentResolvers = Arrays.asList(methodArgumentResolvers);
    }

    /**
     * @param endpoint
     * @param factory
     */
    public void registerEndpoint(PulsarListenerEndpoint endpoint, PulsarListenerContainerFactory factory){
        PulsarListenerEndpointDescriptor endpointDescriptor = new PulsarListenerEndpointDescriptor(endpoint, factory);
        synchronized (this.endpointDescriptors){
            if(this.startImmediately){
                endpointRegistry.registerListenerContainer(endpointDescriptor.endpoint, resolveListenerFactory(endpointDescriptor), true);
            }else{
                endpointDescriptors.add(endpointDescriptor);
            }
        }
    }

    private PulsarListenerContainerFactory resolveListenerFactory(PulsarListenerEndpointDescriptor endpointDescriptor) {
        if( endpointDescriptor.containerFactory != null ){
            return endpointDescriptor.containerFactory;
        }else if(this.containerFactory != null){
            return this.containerFactory;
        }else if(this.containerFactoryBeanName != null){
            Assert.state(this.beanFactory!=null, "BeanFactory must be set");
            this.containerFactory = this.beanFactory.getBean(this.containerFactoryBeanName, PulsarListenerContainerFactory.class);
            return this.containerFactory;
        }else{
            throw new IllegalStateException("Could not resolve the " +
                    PulsarListenerContainerFactory.class.getSimpleName() + " to use for [" +
                    endpointDescriptor.endpoint + "] no factory was given and no default is set.");
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet(){
        registerAllEndpoints();
    }

    private void registerAllEndpoints() {
        synchronized (this.endpointDescriptors){
            for (PulsarListenerEndpointDescriptor endpointDescriptor : this.endpointDescriptors) {
                this.endpointRegistry.registerListenerContainer(endpointDescriptor.endpoint, resolveListenerFactory(endpointDescriptor));
                this.startImmediately = true;
            }
        }
    }

    public PulsarListenerEndpointRegistry getEndpointRegistry() {
        return endpointRegistry;
    }

    public MessageHandlerMethodFactory getMessageHandlerMethodFactory() {
        return this.messageHandlerMethodFactory;
    }

    public List<HandlerMethodArgumentResolver> getCustomMethodArgumentResolvers() {
        return Collections.unmodifiableList(this.customMethodArgumentResolvers);
    }

    private static final class PulsarListenerEndpointDescriptor {

        private final PulsarListenerEndpoint endpoint;

        private final PulsarListenerContainerFactory<?> containerFactory;

        private PulsarListenerEndpointDescriptor(PulsarListenerEndpoint endpoint,
                                                PulsarListenerContainerFactory<?> containerFactory) {
            this.endpoint = endpoint;
            this.containerFactory = containerFactory;
        }

    }
}
