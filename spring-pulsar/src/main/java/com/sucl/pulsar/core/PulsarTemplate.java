package com.sucl.pulsar.core;

/**
 * @author sucl
 * @date 2023/2/16 22:20
 * @since 1.0.0
 */
public class PulsarTemplate<T> implements PulsarOperations{

    private ProducerFactory producerFactory;

    public PulsarTemplate(ProducerFactory producerFactory) {
        this.producerFactory = producerFactory;
    }

    public void send(T message){
        //
    }
}
