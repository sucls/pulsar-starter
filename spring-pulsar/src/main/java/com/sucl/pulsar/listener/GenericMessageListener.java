package com.sucl.pulsar.listener;

import com.sucl.pulsar.support.Acknowledgment;
import org.apache.pulsar.client.api.Consumer;

/**
 * @author sucl
 * @date 2023/2/25 18:33
 * @since 1.0.0
 */
public interface GenericMessageListener<T>{

    void onMessage(T data);

    default void onMessage(T data, Acknowledgment acknowledgment) {
        throw new UnsupportedOperationException("Container should never call this");
    }

    default void onMessage(T data, Consumer<?> consumer) {
        throw new UnsupportedOperationException("Container should never call this");
    }

    default void onMessage(T data, Acknowledgment acknowledgment, Consumer<?> consumer) {
        throw new UnsupportedOperationException("Container should never call this");
    }

}
