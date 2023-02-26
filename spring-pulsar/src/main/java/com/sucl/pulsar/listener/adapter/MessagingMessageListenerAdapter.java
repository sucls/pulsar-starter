package com.sucl.pulsar.listener.adapter;

import com.sucl.pulsar.support.Acknowledgment;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Messages;
import org.springframework.messaging.Message;

import java.lang.reflect.Method;

/**
 * Consumer接收消息后续的处理
 * 1、消息内容的包装
 * 2、消息的发送，到具体的监听端点（@PulsarHandle）
 *
 * @author sucl
 * @date 2023/2/23 15:21
 * @since 1.0.0
 */
public class MessagingMessageListenerAdapter {

    private Object bean;
    private HandlerAdapter handlerAdapter;

    public MessagingMessageListenerAdapter(Object bean, Method method) {
        this.bean = bean;
    }

    public void setHandlerMethod(HandlerAdapter handlerAdapter) {
        this.handlerAdapter = handlerAdapter;
    }

    /**
     *
     * @param record
     * @param acknowledgment
     * @param message
     * @param consumer
     */
    protected void invokeHandler(org.apache.pulsar.client.api.Message record, Acknowledgment acknowledgment, Message message, Consumer<?> consumer){
        handlerAdapter.invoke(message, new Object[]{record, acknowledgment,consumer});
    }

    /**
     *
     * @param records
     * @param acknowledgment
     * @param message
     * @param consumer
     */
    protected void invokeHandler(Messages records, Acknowledgment acknowledgment, Message message, Consumer<?> consumer){
        handlerAdapter.invoke(message, new Object[]{records, acknowledgment,consumer});
    }

}
