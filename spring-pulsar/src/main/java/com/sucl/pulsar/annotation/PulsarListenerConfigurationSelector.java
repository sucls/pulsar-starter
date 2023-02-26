package com.sucl.pulsar.annotation;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 基于@EnablePulssar选择配置
 *
 * @author sucl
 * @date 2023/2/16 22:07
 * @since 1.0.0
 */
public class PulsarListenerConfigurationSelector implements DeferredImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{PulsarBootstrapConfiguration.class.getName()};
    }

}
