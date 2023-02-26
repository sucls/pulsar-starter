package com.sucl.pulsar.sample;

import com.sucl.pulsar.annotation.EnablePulsar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sucl
 * @date 2023/2/7 19:20
 * @since 1.0.0
 */
@EnablePulsar
@RestController
@SpringBootApplication
public class PulsarApplication extends SpringBootServletInitializer {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(PulsarApplication.class, args);
    }

    /**
     * for servlet container
     * eg: tomcat + war
     * @param builder a builder for the application context
     * @return
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(PulsarApplication.class);
    }
}
