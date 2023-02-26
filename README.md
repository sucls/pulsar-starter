## 开始一个Pulsar Starter

之前写过关于Apache Pulsar的简单示例，用来了解如何使用Pulsar这个新生代的消息队列中间件，但是如果想要在项目中使用，还会欠缺很多，最明显的就是
集成复杂，如果你用过其他消息中间件，比如Kafka、RabbitMq，只需要简单的引入jar，就可以通过注解+配置快速集成到项目中。

这个归功于SpringBoot项目中对starter模块的支持，之前有写过一个starter模块关键性的使用方法，今天我们就看下怎么实现一个pulsar-starter

### 目标

写一个完整的类似kafka-spring-boot-starter（springboot项目已经集成到spring-boot-starter中），需要考虑到很多kafka的特性，
今天我们主要实现下面几个模板

+ 在项目中够通过引入jar依赖快速集成
+ 提供统一的配置入口
+ 能够快速发送消息
+ 能够基于注解实现消息的消费

## 定义结构

```
└── pulsar-starter
    ├── pulsar-spring-boot-starter
    ├── pulsar-spring-boot-autoconfigure
    ├── spring-pulsar
    ├── spring-pulsar-xx
    ├── spring-pulsar-sample
└── README.md
```
整个模块的结构如上其中**pulsar-starter**作为一个根模块，主要控制子模块依赖的其他jar的版本以及使用到的插件版本。类似于Spring-Bom，这样我们在后续升级
时，就可以解决各个第三方jar的可能存在版本冲突导致的问题。

+ pulsar-spring-boot-starter

该模块作为外部项目集成的直接引用jar，可以认为是pulsar-spring-boot-starter组件的入口，里面不需要写任何代码，只需要引入需要的依赖（也就是下面的子模块）即可
+ pulsar-spring-boot-autoconfigure

该模块主要定义了spring.factories以及AutoConfigure、Properties。也就是自动配置的核心（配置项+Bean配置）
+ spring-pulsar

该模块是核心模块，主要的实现都在这里
+ spring-pulsar-xx

扩展模块，可以对spring-pulsar做更细化的划分
+ spring-pulsar-sample

starter的使用示例项目

### 实现

上面我们说到实现目标，现在看下各个模块应该包含什么内容，以及怎么实现我们的目标

* **入口pulsar-spring-boot-starter**

上面说到starter主要是引入整个模块基础的依赖即可，里面不用写代码。

```xml
<dependencies>
    <dependency>
        <groupId>com.sucl</groupId>
        <artifactId>spring-pulsar</artifactId>
        <version>${project.version}</version>
    </dependency>

    <dependency>
        <groupId>com.sucl</groupId>
        <artifactId>pulsar-spring-boot-autoconfigure</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

* **pulsar-spring-boot-autoconfigure**

1. 添加spring-boot基础的配置
```xml
<dependencies>
     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot</artifactId>
     </dependency>

     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-logging</artifactId>
     </dependency>

     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-configuration-processor</artifactId>
         <optional>true</optional>
     </dependency>
</dependencies>
```

2. 定义自动配置类*PulsarAutoConfiguration*：
   + 引入**Properties**，基于*EnableConfigurationProperties*与*spring-boot-configuration-processor*解析Properties
     生成对应*spring-configuration-metadata.json*文件，这样编写application.yml配置时就可以自动提示配置项的属性和值了。

   + 构建一些必须的Bean，如PulsarClient、ConsumerFactory、ConsumerFactory等

   + Import配置PulsarAnnotationDrivenConfiguration，这个主要是一些额外的配置，用来支持后面的功能

```java
@Configuration
@EnableConfigurationProperties({PulsarProperties.class})
@Import({PulsarAnnotationDrivenConfiguration.class})
public class PulsarAutoConfiguration {

    private final PulsarProperties properties;

    public PulsarAutoConfiguration(PulsarProperties properties) {
        this.properties = properties;
    }

    @Bean(destroyMethod = "close")
    public PulsarClient pulsarClient() throws PulsarClientException {
        ClientBuilder clientBuilder = new ClientBuilderImpl(properties);
        return clientBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean(ConsumerFactory.class)
    public ConsumerFactory pulsarConsumerFactory() throws PulsarClientException {
        return new DefaultPulsarConsumerFactory(pulsarClient(), properties.getConsumer().buildProperties());
    }

    @Bean
    @ConditionalOnMissingBean(ProducerFactory.class)
    public ProducerFactory pulsarProducerFactory() throws PulsarClientException {
        return new DefaultPulsarProducerFactory(pulsarClient(), properties.getProducer().buildProperties());
    }
    
}
```

3. 配置spring.factory

在目录*src/main/resources/META-INF*下创建**spring.factories**，内容如下：
```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
  com.sucl.pulsar.autoconfigure.PulsarAutoConfiguration
```

* **spring-pulsar**

1. 添加pulsar-client相关的依赖
```xml
 <dependencies>
     <dependency>
         <groupId>org.apache.pulsar</groupId>
         <artifactId>pulsar-client</artifactId>
     </dependency>

     <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-autoconfigure</artifactId>
     </dependency>

     <dependency>
         <groupId>org.springframework</groupId>
         <artifactId>spring-messaging</artifactId>
     </dependency>
</dependencies>
```

2. 定义EnablePulsar，之前说到过，@Enable注解主要是配合AutoConfigure来做功能加强，没有了自动配置，我们依然可以使用这些模块的功能。
这里做了一件事，向Spring容器注册了两个Bean
+ _PulsarListenerAnnotationBeanProcessor_ 在Spring Bean生命周期中解析注解自定义注解PulsarListener、PulsarHandler，
+ _PulsarListenerEndpointRegistry_ 用来构建Consumer执行环境以及对TOPIC的监听、触发消费回调等等，可以说是最核心的Bean

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({PulsarListenerConfigurationSelector.class})
public @interface EnablePulsar {

}
```

3. 定义注解，参考RabbitMq，主要针对需要关注的类与方法，分别对应注解@PulsarListener、@PulsarHandler，通过这两个注解配合可以让我们监听到关注的TOPIC，
当有消息产生时，触发对应的方法进行消费。
```java
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
}

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PulsarHandler {

}
```




* **spring-pulsar-sample**

1. 简单写一个SpringBoot项目，并添加pulsar-spring-boot-starter
```xml
    <dependencies>
    <dependency>
        <groupId>com.sucl</groupId>
        <artifactId>pulsar-spring-boot-starter</artifactId>
        <version>${project.version}</version>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```
2. 添加配置
```yaml

```
3.

### 示例

> [github](https://github.com/sucls/pulsar-starter.git)

### 结束语
    

### 参考
