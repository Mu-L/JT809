package com.legaoyi.rabbitmq;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.legaoyi.mq.MQMessageHandler;
import com.legaoyi.storer.handler.MessageHandler;
import com.legaoyi.storer.util.ServerRuntimeContext;

@Component("commonUpstreamMessageRabbitmqListener")
@RabbitListener(queues = "${rabbitmq.subordinate.common.queue}")
public class CommonUpstreamMessageRabbitmqListener {

    private static final Logger logger = LoggerFactory.getLogger(CommonUpstreamMessageRabbitmqListener.class);

    private static final String DEFAULT_CHARSET = "UTF-8";

    @Value("${rabbitmq.message.durable}")
    private boolean durable = true;

    @Autowired
    @Qualifier("serverRuntimeContext")
    protected ServerRuntimeContext serverRuntimeContext;

    @Value("${rabbitmq.subordinate.common.queue}")
    private String commonUpstreamMessageQueue;

    @RabbitHandler
    public void onMessage(byte[] bytes) {
        String json = null;
        try {
            json = new String(bytes, DEFAULT_CHARSET);
            if (logger.isInfoEnabled()) {
                logger.info(json);
            }
            commonUpstreamMessageHandler().handle(json);
        } catch (Exception e) {
            logger.error("handle mq Message error,message={}", json, e);
        }
    }

    @RabbitHandler
    public void onMessage(String json) {
        try {
            if (logger.isInfoEnabled()) {
                logger.info(json);
            }
            commonUpstreamMessageHandler().handle(json);
        } catch (Exception e) {
            logger.error("handle mq Message error,message={}", json, e);
        }
    }

    @Bean("commonUpstreamMessageQueue")
    public Queue commonUpstreamMessageQueue() {
        return new Queue(commonUpstreamMessageQueue, durable);
    }

    @Bean("commonUpstreamMessageHandler")
    public MQMessageHandler commonUpstreamMessageHandler() {
        UpstreamMqMessageHandler handler = new UpstreamMqMessageHandler();
        List<MessageHandler> handlers = new ArrayList<MessageHandler>();
        handlers.add(ServerRuntimeContext.getBean("upstreamMessageHandler", MessageHandler.class));
        handler.setHandlers(handlers);
        return handler;
    }

}
