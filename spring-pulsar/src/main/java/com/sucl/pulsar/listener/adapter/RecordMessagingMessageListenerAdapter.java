package com.sucl.pulsar.listener.adapter;

import com.sucl.pulsar.listener.MessageListener;
import com.sucl.pulsar.support.Acknowledgment;
import org.apache.pulsar.client.api.Consumer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.lang.reflect.Method;

/**
 * @author sucl
 * @date 2023/2/23 15:56
 * @since 1.0.0
 */
public class RecordMessagingMessageListenerAdapter<T> extends MessagingMessageListenerAdapter implements MessageListener<T> {

    private Message messagingMessage;

    public RecordMessagingMessageListenerAdapter(Object bean, Method method) {
        super(bean, method);
    }

    @Override
    public void onMessage(org.apache.pulsar.client.api.Message<T> data, Acknowledgment acknowledgment, Consumer<?> consumer) {
        messagingMessage = toMessagingMessage(data, acknowledgment, consumer);
        invokeHandler(data, acknowledgment, messagingMessage, consumer);
    }

    private Message toMessagingMessage(org.apache.pulsar.client.api.Message<T> data, Acknowledgment acknowledgment, Consumer<?> consumer) {
        //
        return MessageBuilder.withPayload(data.getValue()).build();
    }
}
