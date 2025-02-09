package com.yanweiyi.micodebackend;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
@Slf4j
public class MicodeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicodeBackendApplication.class, args);
    }

    @Value("${spring.rabbitmq.host}")
    private String rabbitmqHost;
    @Value("${spring.rabbitmq.username}")
    private String rabbitmqUsername;
    @Value("${spring.rabbitmq.password}")
    private String rabbitmqPassword;
    /**
     * 初始化消息队列
     */
    @PostConstruct
    private void initMQ() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitmqHost);
        factory.setUsername(rabbitmqUsername);
        factory.setPassword(rabbitmqPassword);
        factory.setVirtualHost(rabbitmqUsername);
        try(Connection connection = factory.newConnection();) {
            Channel channel = connection.createChannel();
            String exchangeName = "judge_exchange";
            channel.exchangeDeclare(exchangeName, "direct");
            // 创建队列
            String queueName = "judge_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, exchangeName, "miRoutingKey");
            log.info("message queue started successfully");
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}