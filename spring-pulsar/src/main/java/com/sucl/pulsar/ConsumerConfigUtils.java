package com.sucl.pulsar;

/**
 * @author sucl
 * @date 2023/2/21 10:32
 * @since 1.0.0
 */
public class ConsumerConfigUtils {

    public static final String PULSAR_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME = "com.sucl.pulsar.config.defaultPulsarListenerAnnotationBeanProcessor";
    public static final String PULSAR_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME = "com.sucl.pulsar.config.defaultPulsarListenerEndpointRegistry";
    public static final String PULSAR_LISTENER_CONTAINER_FACTORY_BEAN_NAME = "com.sucl.pulsar.config.defaultPulsarListenerContainerFactory";

    public static final String SCHEMA_NAME = "schema";
    public static final String SUBSCRIPTION_NAME_NAME = "subscriptionName";
    public static final String AUTO_COMMIT_NAME = "autoCommit";

}
