package com.sucl.pulsar.config;

import com.sucl.pulsar.ConsumerConfigUtils;
import com.sucl.pulsar.annotation.PulsarHandler;
import com.sucl.pulsar.annotation.PulsarListener;
import com.sucl.pulsar.annotation.PulsarListenerConfigurer;
import com.sucl.pulsar.annotation.PulsarListeners;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.config.*;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.messaging.converter.GenericMessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 最为核心的类，基于BeanPostProcessor，在容器启动过程中，查找PulsarListener注解的Bean并找到PulsarHandler标记的方法
 * 转换成监听端点，然后通过PulsarListenerEndpointRegistry进行注册，进而和监听容器关联
 *
 * @author sucl
 * @date 2023/2/4 14:30
 * @since 1.0.0
 */
@Slf4j
public class PulsarListenerAnnotationBeanProcessor implements BeanPostProcessor, BeanFactoryAware, SmartInitializingSingleton, InitializingBean, DisposableBean {

    public static final String DEFAULT_PULSAR_LISTENER_CONTAINER_FACTORY_BEAN_NAME = "pulsarListenerContainerFactory";

    private BeanFactory beanFactory;

    private Set<Class> nonAnnotatedClasses = new HashSet<>(); // 缓存

    private BeanExpressionResolver resolver = new StandardBeanExpressionResolver(); // 解析变量，spel properties

    private BeanExpressionContext context;

    private PulsarListenerEndpointRegistrar registrar = new PulsarListenerEndpointRegistrar();

    private PulsarListenerEndpointRegistry endpointRegistry;

    private final PulsarHandlerMethodFactoryAdapter messageHandlerMethodFactory = new PulsarHandlerMethodFactoryAdapter();

    private String defaultContainerFactoryBeanName = DEFAULT_PULSAR_LISTENER_CONTAINER_FACTORY_BEAN_NAME; //

    /**
     *
     * @param messageHandlerMethodFactory
     */
    public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory messageHandlerMethodFactory) {
        this.messageHandlerMethodFactory.setHandlerMethodFactory(messageHandlerMethodFactory);
    }

    public PulsarHandlerMethodFactoryAdapter getMessageHandlerMethodFactory() {
        return messageHandlerMethodFactory;
    }

    public void setDefaultContainerFactoryBeanName(String defaultContainerFactoryBeanName) {
        this.defaultContainerFactoryBeanName = defaultContainerFactoryBeanName;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        if( beanFactory instanceof ConfigurableListableBeanFactory){
            resolver = ((ConfigurableListableBeanFactory) beanFactory).getBeanExpressionResolver();
            context = new BeanExpressionContext((ConfigurableListableBeanFactory) beanFactory, null);
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if( !nonAnnotatedClasses.contains(bean.getClass()) ){
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            Set<PulsarListener> pulsarListenerSet = findListenerAnnotations(targetClass);

            boolean hasListener = false;
            if( !pulsarListenerSet.isEmpty() ){
                Map<Method, List<PulsarHandler>> methodAnnotations = findMethodAnnotations(targetClass);
                if( !methodAnnotations.isEmpty() ){
                    hasListener = true;
                    for (PulsarListener pulsarListener : pulsarListenerSet) {
                        processClassAnnotationListener(pulsarListener, methodAnnotations, bean, beanName);
                    }
                }
            }
            if(!hasListener){
                log.debug(">>>>> Bean:{}没有注解PulsarListener", bean);
                nonAnnotatedClasses.add(bean.getClass());
            }
        }
        return bean;
    }

    private void processClassAnnotationListener(PulsarListener pulsarListener, Map<Method, List<PulsarHandler>> methodAnnotations, Object bean, String beanName){
        for(Map.Entry<Method,List<PulsarHandler>> entry: methodAnnotations.entrySet()){
            Method method = entry.getKey();
            for(PulsarHandler pulsarHandler : entry.getValue()){
                processPulsarListenerHandler(pulsarListener, method, pulsarHandler, bean, beanName);
            }
        }
    }

    private void processPulsarListenerHandler(PulsarListener pulsarListener, Method method, PulsarHandler pulsarHandler, Object bean, String beanName) {
        log.info(">>>>> 解析Bean：{}有注解PulsarHandler的方法：{}", bean.getClass(), method);
        MethodPulsarListenerEndpoint endpoint = new MethodPulsarListenerEndpoint();
        endpoint.setMethod(method);
        processListener(pulsarListener, endpoint, pulsarHandler, bean, method, beanName);
    }

    /**
     *  补充endpoint
     *  注册endpoint
     * @param endpoint
     * @param pulsarHandler
     * @param bean
     * @param method
     * @param beanName
     */
    private void processListener(PulsarListener pulsarListener,MethodPulsarListenerEndpoint endpoint, PulsarHandler pulsarHandler, Object bean, Method method, String beanName) {
        //todo config endpoint
        endpoint.setId(UUID.randomUUID().toString());
        endpoint.setBean(bean);
        endpoint.setTopics(resolveTopics(pulsarListener));
        endpoint.setTags(resolveTags(pulsarListener));
        endpoint.setMessageHandlerMethodFactory(messageHandlerMethodFactory);

        PulsarListenerContainerFactory factory = null;
        String containerFactoryBeanName = resolve(pulsarListener.containerFactory());
        if (StringUtils.hasText(containerFactoryBeanName)) {
            Assert.state(this.beanFactory != null, "BeanFactory must be set to obtain container factory by bean name");
            try {
                factory = this.beanFactory.getBean(containerFactoryBeanName, PulsarListenerContainerFactory.class);
            }
            catch (NoSuchBeanDefinitionException ex) {
                throw new BeanInitializationException("Could not register Kafka listener endpoint on [" + bean.getClass()
                        + "] for bean " + beanName + ", no " + PulsarListenerContainerFactory.class.getSimpleName()
                        + " with id '" + containerFactoryBeanName + "' was found in the application context", ex);
            }
        }
        registrar.registerEndpoint(endpoint, factory);
    }

    private String[] resolveTopics(PulsarListener pulsarListener) {
        String[] topics = pulsarListener.topics();
        return resolveExpressValue(topics);
    }

    private String[] resolveTags(PulsarListener pulsarListener) {
        String[] topics = pulsarListener.tags();
        return resolveExpressValue(topics);
    }

    private String[] resolveExpressValue(String[] values){
        List<String> result = new ArrayList<>();
        if(values != null && values.length >0){
            for (String val : values) {
                Object value = resolveExpression(val);
                resolveAsString(value, result);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    private Object resolveExpression(String topic) {
        return resolver.evaluate(resolve(topic), context);
    }

    private void resolveAsString(Object resolvedValue, List<String> result) {
        if (resolvedValue instanceof String[]) {
            for (Object object : (String[]) resolvedValue) {
                resolveAsString(object, result);
            }
        }
        else if (resolvedValue instanceof String) {
            result.add((String) resolvedValue);
        }
        else if (resolvedValue instanceof Iterable) {
            for (Object object : (Iterable<Object>) resolvedValue) {
                resolveAsString(object, result);
            }
        }
        else {
            throw new IllegalArgumentException(String.format(
                    "@PulsarListener can't resolve '%s' as a String", resolvedValue));
        }
    }

    private String resolve(String value) {
        if(this.beanFactory != null && this.beanFactory instanceof ConfigurableBeanFactory){
            return ((ConfigurableBeanFactory)this.beanFactory).resolveEmbeddedValue(value);
        }
        return null;
    }

    private Set<PulsarListener> findListenerAnnotations(Class<?> targetClass) {
        Set<PulsarListener> pulsarListenerSet = new HashSet<>();
        PulsarListener pulsarListener = AnnotatedElementUtils.findMergedAnnotation(targetClass, PulsarListener.class);
        if( pulsarListener!=null ){
            pulsarListenerSet.add(pulsarListener);
        }
        PulsarListeners pulsarListeners = AnnotationUtils.findAnnotation(targetClass, PulsarListeners.class);
        if( pulsarListeners!=null ){
            pulsarListenerSet.addAll(Arrays.asList(pulsarListeners.value()));
        }
        return pulsarListenerSet;
    }

    private Map<Method, List<PulsarHandler>> findMethodAnnotations(Class<?> targetClass){
        return MethodIntrospector.selectMethods(targetClass, new MethodIntrospector.MetadataLookup<List<PulsarHandler>>() {
            @Override
            public List<PulsarHandler> inspect(Method method) {
                PulsarHandler pulsarHandler = AnnotationUtils.findAnnotation(method, PulsarHandler.class);
                return pulsarHandler!=null? Arrays.asList(pulsarHandler): null;
            }
        });
    }

    @Override
    public void afterSingletonsInstantiated() {
        // 配置registrar
        this.registrar.setBeanFactory(this.beanFactory);
        if(this.beanFactory instanceof ConfigurableListableBeanFactory){
            Map<String, PulsarListenerConfigurer> listenerConfigurerMap = ((ConfigurableListableBeanFactory) this.beanFactory).getBeansOfType(PulsarListenerConfigurer.class);
            if( !listenerConfigurerMap.isEmpty() ){
                for (PulsarListenerConfigurer configurer : listenerConfigurerMap.values()) {
                    configurer.configurePulsarListeners(this.registrar);
                }
            }
        }
        // 设置endpointRegistry
        if(this.registrar.getEndpointRegistry() == null){
            if(this.endpointRegistry == null){
                this.endpointRegistry = this.beanFactory.getBean(ConsumerConfigUtils.PULSAR_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME, PulsarListenerEndpointRegistry.class);
            }
            this.registrar.setEndpointRegistry(this.endpointRegistry);
        }
        //
        if (this.defaultContainerFactoryBeanName != null) {
            this.registrar.setContainerFactoryBeanName(this.defaultContainerFactoryBeanName);
        }

        // Set the custom handler method factory once resolved by the configurer
        MessageHandlerMethodFactory handlerMethodFactory = this.registrar.getMessageHandlerMethodFactory();
        if (handlerMethodFactory != null) {
            this.messageHandlerMethodFactory.setHandlerMethodFactory(handlerMethodFactory);
        }
        else {
            addFormatters(this.messageHandlerMethodFactory.defaultFormattingConversionService);
        }

        // Actually register all listeners
        this.registrar.afterPropertiesSet();
    }

    private void addFormatters(DefaultFormattingConversionService defaultFormattingConversionService) {
        // todo
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void destroy() throws Exception {

    }

    public class PulsarHandlerMethodFactoryAdapter implements MessageHandlerMethodFactory {

        private final DefaultFormattingConversionService defaultFormattingConversionService =
                new DefaultFormattingConversionService();

        private MessageHandlerMethodFactory handlerMethodFactory;

        public void setHandlerMethodFactory(MessageHandlerMethodFactory handlerMethodFactory) {
            this.handlerMethodFactory = handlerMethodFactory;
        }

        @Override
        public InvocableHandlerMethod createInvocableHandlerMethod(Object bean, Method method) {
            return getHandlerMethodFactory().createInvocableHandlerMethod(bean, method);
        }

        private MessageHandlerMethodFactory getHandlerMethodFactory() {
            if (this.handlerMethodFactory == null) {
                this.handlerMethodFactory = createDefaultMessageHandlerMethodFactory();
            }
            return this.handlerMethodFactory;
        }

        private MessageHandlerMethodFactory createDefaultMessageHandlerMethodFactory() {
            DefaultMessageHandlerMethodFactory defaultFactory = new DefaultMessageHandlerMethodFactory();
            defaultFactory.setBeanFactory(beanFactory);
            this.defaultFormattingConversionService.addConverter(new BytesToStringConverter(Charset.defaultCharset()));
            defaultFactory.setConversionService(this.defaultFormattingConversionService);
            GenericMessageConverter messageConverter = new GenericMessageConverter(this.defaultFormattingConversionService);
            defaultFactory.setMessageConverter(messageConverter);

            List<HandlerMethodArgumentResolver> customArgumentsResolver =
                    new ArrayList<>(registrar.getCustomMethodArgumentResolvers());
            // Has to be at the end - look at PayloadMethodArgumentResolver documentation
//            customArgumentsResolver.add(new PulsarNullAwarePayloadArgumentResolver(messageConverter, validator));
            defaultFactory.setCustomArgumentResolvers(customArgumentsResolver);

            defaultFactory.afterPropertiesSet();

            return defaultFactory;
        }

    }

    private static class BytesToStringConverter implements Converter<byte[], String> {


        private final Charset charset;

        BytesToStringConverter(Charset charset) {
            this.charset = charset;
        }

        @Override
        public String convert(byte[] source) {
            return new String(source, this.charset);
        }

    }

}
