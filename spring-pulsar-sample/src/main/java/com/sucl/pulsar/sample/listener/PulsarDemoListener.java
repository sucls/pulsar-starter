package com.sucl.pulsar.sample.listener;

import com.sucl.pulsar.annotation.PulsarHandler;
import com.sucl.pulsar.annotation.PulsarListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * @author sucl
 * @date 2023/2/21 22:36
 * @since 1.0.0
 */
@Slf4j
@Component
@PulsarListener(topics = "#{'${cycads.listener-topics}'.split(',')}")
public class PulsarDemoListener {

    @PulsarHandler
    public void onConsumer(Message message){
        log.info(">>> 接收到消息：{}", message.getPayload());
    }

}
