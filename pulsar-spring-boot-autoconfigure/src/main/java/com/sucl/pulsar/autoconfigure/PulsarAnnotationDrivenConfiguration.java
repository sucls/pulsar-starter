package com.sucl.pulsar.autoconfigure;

import com.sucl.pulsar.ConsumerConfigUtils;
import com.sucl.pulsar.annotation.EnablePulsar;
import com.sucl.pulsar.config.ConcurrentPulsarListenerContainerFactory;
import com.sucl.pulsar.core.ConsumerFactory;
import com.sucl.pulsar.core.DefaultPulsarConsumerFactory;
import org.apache.pulsar.client.api.PulsarClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ContainerFactory以及其配置类Configurer的注册
 * 在PulsarAutoConfiguration被import，默认情况下就加载了@EnablePulsar
 * @see PulsarAutoConfiguration
 * @author sucl
 * @date 2023/2/22 19:47
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass(EnablePulsar.class)
public class PulsarAnnotationDrivenConfiguration {

    private PulsarProperties properties;

    private PulsarClient pulsarClient;

    public PulsarAnnotationDrivenConfiguration(PulsarProperties properties, PulsarClient pulsarClient) {
        this.properties = properties;
        this.pulsarClient = pulsarClient;
    }

    @Bean
    @ConditionalOnMissingBean
    ConcurrentPulsarListenerContainerFactoryConfigurer pulsarListenerContainerFactoryConfigurer(){
        ConcurrentPulsarListenerContainerFactoryConfigurer configurer = new ConcurrentPulsarListenerContainerFactoryConfigurer();
        configurer.setProperties(properties);
        return configurer;
    }

    @Bean
    @ConditionalOnMissingBean(name = ConsumerConfigUtils.PULSAR_LISTENER_CONTAINER_FACTORY_BEAN_NAME)
    ConcurrentPulsarListenerContainerFactory pulsarListenerContainerFactory(
            ConcurrentPulsarListenerContainerFactoryConfigurer configurer,
            ObjectProvider<ConsumerFactory<Object>> kafkaConsumerFactory) {
        ConcurrentPulsarListenerContainerFactory factory = new ConcurrentPulsarListenerContainerFactory();
        configurer.configure(factory, kafkaConsumerFactory
                .getIfAvailable(() -> new DefaultPulsarConsumerFactory(this.pulsarClient, this.properties.buildConsumerProperties())));
        return factory;
    }


    @Configuration(proxyBeanMethods = false)
    @EnablePulsar
    @ConditionalOnMissingBean(name = ConsumerConfigUtils.PULSAR_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)
    static class EnableKafkaConfiguration {

    }
}
