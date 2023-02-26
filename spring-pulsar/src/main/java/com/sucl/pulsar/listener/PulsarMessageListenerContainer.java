package com.sucl.pulsar.listener;

import com.sucl.pulsar.core.ConsumerFactory;
import com.sucl.pulsar.listener.adapter.BatchMessagingMessageListenerAdapter;
import com.sucl.pulsar.listener.adapter.RecordMessagingMessageListenerAdapter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.*;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 消息消费的核心容器
 *
 * @author sucl
 * @date 2023/2/26 10:24
 * @since 1.0.0
 */
@Slf4j
public class PulsarMessageListenerContainer extends AbstractMessageListenerContainer{

    private ListenableFuture listenerConsumerFuture;

    private int count;

    public PulsarMessageListenerContainer(ConsumerFactory consumerFactory, ContainerProperties containerProperties, int index) {
        super(consumerFactory, containerProperties);
        this.count = index;
    }

    @Override
    public void doStart() {
        if(isRunning()){
            return;
        }

        ContainerProperties properties = getContainerProperties();

        checkAckMode(properties);

        if(properties.getConsumerTaskExecutor() == null){
            SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor("pulsar-0");
            properties.setConsumerTaskExecutor(asyncTaskExecutor);
        }

        GenericMessageListener listener = (GenericMessageListener) properties.getMessageListener();
        ListenerType listenerType = determineListenerType(listener);

        ListenerConsumer listenerConsumer = new ListenerConsumer(listener, listenerType);
        this.listenerConsumerFuture = properties.getConsumerTaskExecutor().submitListenable(listenerConsumer);

        setRunning(true);

        log.info("Consumer启动完成：consumer-{} topic:{}", count, properties.getTopics());
    }

    private ListenerType determineListenerType(GenericMessageListener listener) {
        ListenerType listenerType = null;
        if (listener instanceof BatchMessageListener) {

        }else if (listener instanceof MessageListener) {

        }else {
            throw new IllegalArgumentException("Unsupported listener type: " + listener.getClass().getName());
        }
        return listenerType;
    }

    private void checkAckMode(ContainerProperties containerProperties) {
        if (!this.consumerFactory.isAutoCommit()) {

        }
    }

    public class ListenerConsumer implements Runnable{

        private Consumer consumer;

        private boolean batchListener;

        private GenericMessageListener listener;

        private ListenerType listenerType;

        public ListenerConsumer(GenericMessageListener listener, ListenerType listenerType) {
            this.listener = listener;
            this.listenerType = listenerType;

            this.consumer = consumerFactory.createConsumer(getContainerProperties().getTopics());
            this.batchListener = listener instanceof BatchMessageListener? true: false;
        }

        @Override
        public void run() {
            doRun();
        }

        private void doRun(){
            while (!Thread.currentThread().isInterrupted()){
                boolean success;
                CommonMessage message = null;
                try {
                    if(batchListener){
                        message = new CommonMessage(this.consumer ,processBatchMessageConsumer());
                    }else{
                        message = new CommonMessage(this.consumer ,processMessageConsumer());
                    }
                    success = true;
                } catch (ExecutionException | PulsarClientException e) {
                    success = false;
                    log.error("消费出错：{}",e.getMessage(),e);
                } catch (InterruptedException e) {
                    success = false;
                    log.error("消费出错：{}",e.getMessage(),e);
                    Thread.currentThread().interrupt();
                }

                doAckWithConsumer(message, success);
            }
        }

        private Message processMessageConsumer() throws ExecutionException, InterruptedException, PulsarClientException {
            Message message = this.consumer.receive();
            listener.onMessage(message, null,this.consumer);
            return message;
        }

        private Messages processBatchMessageConsumer() throws ExecutionException, InterruptedException, PulsarClientException {
            Messages messages = this.consumer.batchReceive();
            listener.onMessage(messages, null, this.consumer);
            return messages;
        }

        private void doAckWithConsumer(CommonMessage message, boolean result){
            if(consumerFactory.isAutoCommit()){
                try {
                    if(result && message != null){
                        message.ack();
                    }else{
                        message.nack();
                    }
                } catch (PulsarClientException e) {
                    log.error("消息ack失败：{}",e.getMessage(),e);
                }
            }
        }

    }

    @Data
    class CommonMessage<T>{
        private Consumer consumer;
        private Message<T> message;
        private Messages<T> messages;

        public CommonMessage(Consumer consumer, Message message){
            this.consumer = consumer;
            this.message = message;
        }

        public CommonMessage(Consumer consumer, Messages messages){
            this.consumer = consumer;
            this.messages = messages;
        }

        public void ack() throws PulsarClientException {
            if(message != null){
                consumer.acknowledge(message);
            }else if(messages != null){
                consumer.acknowledge(messages);
            }
        }

        public void nack(){
            if(message != null){
                consumer.negativeAcknowledge(message);
            }else if(messages != null){
                consumer.negativeAcknowledge(messages);
            }
        }
    }

}
