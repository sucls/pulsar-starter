package com.sucl.pulsar.config;

import com.sucl.pulsar.listener.MessageListenerContainer;
import com.sucl.pulsar.listener.adapter.BatchMessagingMessageListenerAdapter;
import com.sucl.pulsar.listener.adapter.HandlerAdapter;
import com.sucl.pulsar.listener.adapter.MessagingMessageListenerAdapter;
import com.sucl.pulsar.listener.adapter.RecordMessagingMessageListenerAdapter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import java.lang.reflect.Method;

/**
 * 监听端点，即由注解PulsarHandler标记的方法
 *
 * @author sucl
 * @date 2023/2/22 17:16
 * @since 1.0.0
 */
@Slf4j
@Getter
@Setter
public class MethodPulsarListenerEndpoint extends AbstractPulsarListenerEndpoint{

    private Method method;

    private Object bean;

    private MessageHandlerMethodFactory messageHandlerMethodFactory;

    @Override
    public void setupListenerContainer(MessageListenerContainer container) {
        MessagingMessageListenerAdapter adapter = createListenerAdapter();
        Object messageListener = adapter;
        container.setupMessageListener(messageListener);
    }

    private MessagingMessageListenerAdapter createListenerAdapter() {
        MessagingMessageListenerAdapter messageListener = createMessagingMessageListenerAdapter();
        messageListener.setHandlerMethod(configureListenerAdapter(messageListener));
        return messageListener;
    }

    private MessagingMessageListenerAdapter createMessagingMessageListenerAdapter(){
        MessagingMessageListenerAdapter adapter;
        if(isBatch()){
            adapter = new BatchMessagingMessageListenerAdapter(this.bean, this.method);
        }else{
            adapter = new RecordMessagingMessageListenerAdapter(this.bean, this.method);
        }
        return adapter;
    }

    private boolean isBatch() {
        return getBatchListener()!=null && getBatchListener().booleanValue();
    }

    private HandlerAdapter configureListenerAdapter(MessagingMessageListenerAdapter messageListener){
        InvocableHandlerMethod invocableHandlerMethod = messageHandlerMethodFactory.createInvocableHandlerMethod(getBean(), getMethod());
        return new HandlerAdapter(invocableHandlerMethod);
    }
}
