package com.sucl.pulsar.support;

/**
 * @author sucl
 * @date 2023/2/23 21:53
 * @since 1.0.0
 */
public interface Acknowledgment {

    /**
     *
     */
    void acknowledge();

    /**
     *
     * @param sleep
     */
    default void nack(long sleep) {
        throw new UnsupportedOperationException("nack(sleep) is not supported by this Acknowledgment");
    }

    /**
     *
     * @param index
     * @param sleep
     */
    default void nack(int index, long sleep) {
        throw new UnsupportedOperationException("nack(index, sleep) is not supported by this Acknowledgment");
    }

}
