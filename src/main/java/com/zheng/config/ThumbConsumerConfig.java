package com.zheng.config;

import org.apache.pulsar.client.api.BatchReceivePolicy;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.RedeliveryBackoff;
import org.apache.pulsar.client.impl.MultiplierRedeliveryBackoff;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.pulsar.annotation.PulsarListenerConsumerBuilderCustomizer;

import java.util.concurrent.TimeUnit;

/**
 * 批量处理策略配置  
 */  
@Configuration
public class ThumbConsumerConfig<T> implements PulsarListenerConsumerBuilderCustomizer<T> {

    @Bean
    public DeadLetterPolicy deadLetterPolicy() {
        return DeadLetterPolicy.builder()
                // 最大重试次数
                .maxRedeliverCount(3)
                // 死信主题名称
                .deadLetterTopic("thumb-dlq-topic")
                .build();
    }

    @Bean
    public RedeliveryBackoff negativeAckRedeliveryBackoff(){
        return MultiplierRedeliveryBackoff.builder()
                .minDelayMs(1000)
                .maxDelayMs(60_000)
                .multiplier(2)
                .build();
    }

    @Bean
    public RedeliveryBackoff ackTimeoutRedeliveryBackoff(){
        return MultiplierRedeliveryBackoff.builder()
                .minDelayMs(5000)
                .maxDelayMs(300_000)
                .multiplier(3)
                .build();
    }


    @Override  
    public void customize(ConsumerBuilder<T> consumerBuilder) {
        consumerBuilder.batchReceivePolicy(  
                BatchReceivePolicy.builder()
                        // 每次处理 1000 条  
                        .maxNumMessages(1000)  
                        // 设置超时时间（单位：毫秒）  
                        .timeout(10000, TimeUnit.MILLISECONDS)
                        .build()  
        );  
    }  
}
