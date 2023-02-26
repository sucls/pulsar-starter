package com.sucl.pulsar.annotation;

import com.sucl.pulsar.ConsumerConfigUtils;
import com.sucl.pulsar.config.PulsarListenerAnnotationBeanProcessor;
import com.sucl.pulsar.config.PulsarListenerEndpointRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;


/**
 * Pulsar启动相关配置
 * 1、PulsarListenerAnnotationBeanProcessor
 *  a. 解析了注解PulsarListener成客户端监听端点
 *  b. 容器启动后开始对监听端点的注册
 * 2、PulsarListenerEndpointRegistry
 *  a. 注册Pulsar客户端端点到容器，并完成容器的启动（开启Consumer的监听）
 * @author sucl
 * @date 2023/2/20 21:38
 * @since 1.0.0
 */
public class PulsarBootstrapConfiguration implements ImportBeanDefinitionRegistrar {

    /**
     *
     * @param importingClassMetadata annotation metadata of the importing class
     * @param registry current bean definition registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if(!registry.containsBeanDefinition(ConsumerConfigUtils.PULSAR_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)){
            registry.registerBeanDefinition(ConsumerConfigUtils.PULSAR_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME, new RootBeanDefinition(PulsarListenerAnnotationBeanProcessor.class));
        }
        if (!registry.containsBeanDefinition(ConsumerConfigUtils.PULSAR_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME)) {
            registry.registerBeanDefinition(ConsumerConfigUtils.PULSAR_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME, new RootBeanDefinition(PulsarListenerEndpointRegistry.class));
        }
    }
}
