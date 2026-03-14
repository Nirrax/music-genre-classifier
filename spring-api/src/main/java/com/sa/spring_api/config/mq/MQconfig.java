package com.sa.spring_api.config.mq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQconfig {
    private final String inboundQueueName;
    private final String inboundRoutingKey;
    private final String outboundRoutingKey;
    private final String exchangeName;

    public MQconfig(@Value("${spring.rabbitmq.inbound.queueName}") String inboundQueueName,
                    @Value("${spring.rabbitmq.inbound.routingKey}") String inboundRoutingKey,
                    @Value("${spring.rabbitmq.outbound.routingKey}") String outboundRoutingKey,
                    @Value("${spring.rabbitmq.exchangeName}") String exchangeName) {
        this.inboundQueueName = inboundQueueName;
        this.inboundRoutingKey = inboundRoutingKey;
        this.outboundRoutingKey = outboundRoutingKey;
        this.exchangeName = exchangeName;
    }

    @Bean
    public Queue inboundQueue() {
        return new Queue(inboundQueueName);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue inboundQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(inboundQueue)
                .to(exchange)
                .with(inboundRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
