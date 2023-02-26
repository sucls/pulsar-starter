package com.sucl.pulsar.listener.adapter;


import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.messaging.support.GenericMessage;

/**
 * 消息处理适配器
 *
 * @author sucl
 * @date 2023/2/23 15:25
 * @since 1.0.0
 */
public class HandlerAdapter {

    private InvocableHandlerMethod handlerMethod;

    public HandlerAdapter(InvocableHandlerMethod handlerMethod) {
        this.handlerMethod = handlerMethod;
    }

    /**
     *
     * @param args
     * @return
     */
    public Object invoke(Object data, Object... args){
        Message<?> message = new GenericMessage(data);
        try {
            return handlerMethod.invoke(message, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
