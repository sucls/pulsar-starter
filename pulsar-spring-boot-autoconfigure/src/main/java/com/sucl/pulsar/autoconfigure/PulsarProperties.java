package com.sucl.pulsar.autoconfigure;

import com.sucl.pulsar.ConsumerConfigUtils;
import com.sucl.pulsar.listener.ContainerProperties.AckMode;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.impl.conf.ClientConfigurationData;
import org.apache.pulsar.client.impl.conf.ConsumerConfigurationData;
import org.apache.pulsar.client.impl.conf.ProducerConfigurationData;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Pulsar配置
 *
 * @author sucl
 * @date 2023/2/4 14:34
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "cycads.pulsar")
public class PulsarProperties extends ClientConfigurationData {

    /**
     * 扩展属性
     */
    private final Map<String, String> properties = new HashMap<>();

    /**
     *
     */
    private Listener listener = new Listener();

    /**
     *
     */
    private Consumer consumer = new Consumer();

    /**
     *
     */
    private Producer producer = new Producer();

    @Getter
    @Setter
    public static class Consumer extends ConsumerConfigurationData {
        /**
         *
         */
        private SchemaType schemaType = SchemaType.STRING;
        /**
         * 是否自动提交
         */
        private Boolean autoCommit = true;

        public Map<String,Object> buildProperties(){
            Properties properties = new Properties();
            PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
            map.from(this::getSchemaType).as(SchemaType::getSchema).to(properties.in(ConsumerConfigUtils.SCHEMA_NAME));
            map.from(this::getSubscriptionName).to(properties.in(ConsumerConfigUtils.SUBSCRIPTION_NAME_NAME));
            map.from(this::getAutoCommit).to(properties.in(ConsumerConfigUtils.AUTO_COMMIT_NAME));
            return properties.with(this.getProperties());
        }

        public String getSubscriptionName(){
            if(super.getSubscriptionName() != null){
                return super.getSubscriptionName();
            }
            return UUID.randomUUID().toString();
        }

    }

    @Getter
    @Setter
    public static class Producer extends ProducerConfigurationData {

        /**
         * Schema类型
         */
        private SchemaType schemaType = SchemaType.STRING;

        public Map<String,Object> buildProperties(){
            Properties properties = new Properties();
            PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
            map.from(this::getSchemaType).as(SchemaType::getSchema).to(properties.in(ConsumerConfigUtils.SCHEMA_NAME));
            return properties.with(this.getProperties());
        }

    }

    @Getter
    @Setter
    public static class Listener{
        /**
         * ACK模式
         */
        private AckMode ackMode = AckMode.RECORD;
        /**
         * 是否批量接收
         */
        private Boolean batch = false;
    }

    public Map<String, Object> buildConsumerProperties() {
        Map<String, Object> properties = buildCommonProperties();
        properties.putAll(this.consumer.buildProperties());
        return properties;
    }

    private Map<String, Object> buildCommonProperties() {
        Map<String, Object> properties = new HashMap<>();
        // common
        return properties;
    }

    @Getter
    public enum SchemaType{
        STRING(Schema.STRING),
        ;

        private Schema<?> schema;

        private String type;
        private Class clazz;

        SchemaType(Schema schema){
            this.schema = schema;
        }

        SchemaType(String type, Class clazz){
            this.type = type;
            this.clazz = clazz;
        }

        public Schema<?> getSchema() {
            if(schema != null){
                return schema;
            }
            return resolveSchema(this);
        }

        private Schema<?> resolveSchema(SchemaType schemaType) {
            switch (schemaType.type){
                case "JSON":
                    return Schema.JSON(clazz);
            }
            return null;
        }
    }

    private static class Properties extends HashMap<String, Object> {

        <V> java.util.function.Consumer<V> in(String key) {
            return (value) -> put(key, value);
        }

        Properties with( Map<String, String> properties) {
            putAll(properties);
            return this;
        }

    }
}
