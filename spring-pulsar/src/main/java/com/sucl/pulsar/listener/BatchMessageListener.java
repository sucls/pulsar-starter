package com.sucl.pulsar.listener;

import com.sucl.pulsar.support.Acknowledgment;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Messages;

/**
 * @author sucl
 * @date 2023/2/25 18:33
 * @since 1.0.0
 */
public interface BatchMessageListener<T> extends GenericMessageListener<Messages<T>> {

    @Override
    default void onMessage(Messages<T> data) {
        throw new UnsupportedOperationException("Container should never call this");
    }

    @Override
    void onMessage(Messages<T> data, Acknowledgment acknowledgment, Consumer<?> consumer);
}
