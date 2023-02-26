package com.sucl.pulsar.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用与类上，标记类为客户端监听
 *
 * @author sucl
 * @date 2023/2/4 14:31
 * @since 1.0.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PulsarListener {

    /**
     *
     * @return
     */
    String[] topics() default {};

    /**
     *
     * @return
     */
    String[] tags() default {};

    /**
     *
     * @return
     */
    String containerFactory() default "";
}
