package com.sucl.pulsar.listener;

/**
 * @author sucl
 * @date 2023/2/22 16:59
 * @since 1.0.0
 */
public interface MessageListenerContainer {
    void start();

    default void stop(){
        //
    }

    default void close(){
        //
    }

    default void pause(){
        //
    }

    void setupMessageListener(Object messageListener);

}
