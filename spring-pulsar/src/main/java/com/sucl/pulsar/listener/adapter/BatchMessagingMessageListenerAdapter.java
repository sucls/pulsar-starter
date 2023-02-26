package com.sucl.pulsar.listener.adapter;

import com.sucl.pulsar.listener.BatchMessageListener;
import com.sucl.pulsar.support.Acknowledgment;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Messages;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sucl
 * @date 2023/2/23 15:56
 * @since 1.0.0
 */
public class BatchMessagingMessageListenerAdapter<T> extends MessagingMessageListenerAdapter implements BatchMessageListener<T> {

    private Message message;

    public BatchMessagingMessageListenerAdapter(Object bean, Method method) {
        super(bean, method);
    }

    @Override
    public void onMessage(org.apache.pulsar.client.api.Messages<T> data, Acknowledgment acknowledgment, Consumer<?> consumer) {
        message = toMessagingMessage(data, acknowledgment, consumer);
        invokeHandler(data, acknowledgment, message, consumer);
    }

    private Message toMessagingMessage(Messages<T> data, Acknowledgment acknowledgment, Consumer<?> consumer) {
        List<Object> values = new ArrayList<>();
        data.forEach(message->{
            values.add(message.getValue());
        });
        return MessageBuilder.withPayload(values).build();
    }
}
