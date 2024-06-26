package com.yanweiyi.micodebackend.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author yanweiyi
 */
@Component
@Slf4j
public class MessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     */
    public void sendMessage(String exchange, String routingKey, long message) {
        log.info("send message: {}", message);
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

}