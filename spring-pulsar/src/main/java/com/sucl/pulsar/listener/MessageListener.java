package com.sucl.pulsar.listener;


import com.sucl.pulsar.support.Acknowledgment;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;

/**
 * @author sucl
 * @date 2023/2/23 15:59
 * @since 1.0.0
 */
public interface MessageListener<T> extends GenericMessageListener<Message<T>>{

    @Override
    default void onMessage(Message<T> data) {
        throw new UnsupportedOperationException("Container should never call this");
    }

    @Override
    void onMessage(Message<T> data, Acknowledgment acknowledgment, Consumer<?> consumer);
}
