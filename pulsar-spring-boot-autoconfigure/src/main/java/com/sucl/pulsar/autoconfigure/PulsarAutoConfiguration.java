package com.sucl.pulsar.autoconfigure;

import com.sucl.pulsar.core.*;
import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.impl.ClientBuilderImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Pulsar关键配置自动加载:
 * Consumer工厂
 * Producer工厂
 * PulsarTemplate
 * ...
 *
 * 由spring.factories触发
 *
 * @author sucl
 * @date 2023/2/4 14:30
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties({PulsarProperties.class})
@Import({PulsarAnnotationDrivenConfiguration.class})
public class PulsarAutoConfiguration {

    private final PulsarProperties properties;

    public PulsarAutoConfiguration(PulsarProperties properties) {
        this.properties = properties;
    }

    @Bean(destroyMethod = "close")
    public PulsarClient pulsarClient() throws PulsarClientException {
        ClientBuilder clientBuilder = new ClientBuilderImpl(properties);
        return clientBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean(ConsumerFactory.class)
    public ConsumerFactory pulsarConsumerFactory() throws PulsarClientException {
        return new DefaultPulsarConsumerFactory(pulsarClient(), properties.getConsumer().buildProperties());
    }

    @Bean
    @ConditionalOnMissingBean(ProducerFactory.class)
    public ProducerFactory pulsarProducerFactory() throws PulsarClientException {
        return new DefaultPulsarProducerFactory(pulsarClient(), properties.getProducer().buildProperties());
    }

    @Bean
    @ConditionalOnMissingBean(PulsarTemplate.class)
    public PulsarTemplate pulsarTemplate(ProducerFactory producerFactory){
        return new PulsarTemplate(producerFactory);
    }

}
