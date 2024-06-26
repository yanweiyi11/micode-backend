package com.yanweiyi.micodebackend.rabbitmq;

import com.yanweiyi.micodebackend.judge.service.JudgeService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import javax.annotation.Resource;

/**
 * @author yanweiyi
 */
@Slf4j
@Component
public class MessageConsumer {

    @Resource
    private JudgeService judgeService;

    @SneakyThrows
    @RabbitListener(queues = {"judge_queue"}, ackMode = "MANUAL")
    public void receiveMessage(long message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receive message: {}", message);
        if (message <= 0) {
            log.error("abnormal questionSubmitId={}", message);
            channel.basicNack(deliveryTag, false, false);
        } else {
            try {
                judgeService.doJudge(message);
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                log.error("questionSubmitId={} judge error", message);
                channel.basicNack(deliveryTag, false, false);
            }
        }
    }
}