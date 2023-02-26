package com.sucl.pulsar.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 关键入口
 *
 * @author sucl
 * @date 2023/2/4 14:31
 * @since 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({PulsarListenerConfigurationSelector.class})
public @interface EnablePulsar {

}
