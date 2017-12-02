package cc.sportsdb.common.data.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public final class MqUtil implements ApplicationContextAware {

    private static AmqpTemplate amqpTemplate;
    private static final Logger logger = LoggerFactory.getLogger(MqUtil.class);

    private MqUtil() {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        amqpTemplate = applicationContext.getBean(MqConstant.RABBITMQ_TEMPLATE, RabbitTemplate.class);
    }
}
