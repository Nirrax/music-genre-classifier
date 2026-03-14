package com.sa.spring_api.config.mq;

import com.sa.spring_api.classification.dto.ClassificationRequestMessage;
import com.sa.spring_api.classification.dto.ClassificationResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class MQservice {
    private static final Logger log = LoggerFactory.getLogger(MQservice.class);
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final String exchangeName;
    private final String outboundRoutingKey;

    public MQservice(RabbitTemplate rabbitTemplate,
                          ApplicationEventPublisher eventPublisher,
                          @Value("${spring.rabbitmq.exchangeName}") String exchangeName,
                          @Value("${spring.rabbitmq.outbound.routingKey}") String outboundRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.eventPublisher = eventPublisher;
        this.exchangeName = exchangeName;
        this.outboundRoutingKey = outboundRoutingKey;
    }

    public void sendMessage(ClassificationRequestMessage message) {
        rabbitTemplate.convertAndSend(exchangeName, outboundRoutingKey, message);
    }


    @RabbitListener(queues = "${spring.rabbitmq.inbound.queueName}")
    public void receiveMessage(ClassificationResponseMessage message) {
        try {
            eventPublisher.publishEvent(message);
        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
        }

    }

}
