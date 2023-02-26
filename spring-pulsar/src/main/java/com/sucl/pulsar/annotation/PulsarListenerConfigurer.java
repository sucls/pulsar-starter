package com.sucl.pulsar.annotation;

import com.sucl.pulsar.config.PulsarListenerEndpointRegistrar;

/**
 * 通过实现该接口PulsarListenerEndpointRegistrar，容器启动后，在PulsarListenerAnnotationBeanProcessor中
 * 对PulsarListenerEndpointRegistrar进行扩展处理
 *
 * @author sucl
 * @date 2023/2/22 19:29
 * @since 1.0.0
 */
public interface PulsarListenerConfigurer {

    void configurePulsarListeners(PulsarListenerEndpointRegistrar registrar);
}
