package com.sucl.pulsar.listener;

import com.sucl.pulsar.core.ConsumerFactory;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 消息监听容器：
 * 1、启动Consumer的监听，实现消息的接收
 *
 * @author sucl
 * @date 2023/2/23 15:04
 * @since 1.0.0
 */
@Slf4j
@Setter
public class ConcurrentMessageListenerContainer extends AbstractMessageListenerContainer{

    private boolean batchListener = false;

    private List<PulsarMessageListenerContainer> containers = new ArrayList<>();

    /**
     * 将一个Endpoint由多少个线程处理
     * 理论上一个分区一个线程处理
     */
    private int concurrency = 1;

    public ConcurrentMessageListenerContainer(ConsumerFactory consumerFactory, ContainerProperties properties) {
        super(consumerFactory, properties);
    }

    @Override
    public void doStart() {
        if(!isRunning()){
            setRunning(true);

            ContainerProperties containerProperties = getContainerProperties();

            for(int i = 0; i< this.concurrency; i++){
                PulsarMessageListenerContainer container = new PulsarMessageListenerContainer(getConsumerFactory(), containerProperties, i);
                container.start();
                containers.add(container);
            }
        }
    }

    private boolean isBatchListener(){
        return batchListener;
    }

}
