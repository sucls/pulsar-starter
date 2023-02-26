package com.sucl.pulsar.sample.test;

import com.sucl.pulsar.autoconfigure.PulsarAutoConfiguration;
import com.sucl.pulsar.core.ProducerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author sucl
 * @date 2023/2/23 18:58
 * @since 1.0.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ProducerTests.ContextConfig.class})
@Import({PulsarAutoConfiguration.class})
public class ProducerTests {

    @Autowired
    private ProducerFactory producerFactory;

    @Test
    public void sendMessage() throws PulsarClientException {
        Producer producer = producerFactory.createProducer("TOPIC_TEST");
        MessageId messageId = producer.send("this is a test message");
        log.info(">>>>>>> 消息发送完成：{}", messageId);
    }

    @Configuration
    @PropertySource(value = "classpath:application-test.properties")
    static class ContextConfig{
        //
    }
}
