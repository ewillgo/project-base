package cc.sportsdb.common.data.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public final class MqUtil implements ApplicationContextAware {

    private static RabbitAdmin rabbitAdmin;
    private static RabbitTemplate rabbitTemplate;
    private static final Logger logger = LoggerFactory.getLogger(MqUtil.class);

    private MqUtil() {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        rabbitAdmin = applicationContext.getBean(MqConstant.AMQP_ADMIN, RabbitAdmin.class);
        rabbitTemplate = applicationContext.getBean(MqConstant.AMQP_TEMPLATE, RabbitTemplate.class);
    }

    public static Exchange declareDirectExchange(String exchangeName, boolean durable, boolean autoDelete) {
        return declareExchange(exchangeName, ExchangeTypes.DIRECT, durable, autoDelete, null);
    }

    public static Exchange declareTopicExchange(String exchangeName, boolean durable, boolean autoDelete) {
        return declareExchange(exchangeName, ExchangeTypes.TOPIC, durable, autoDelete, null);
    }

    public static Exchange declareFanoutExchange(String exchangeName, boolean durable, boolean autoDelete) {
        return declareExchange(exchangeName, ExchangeTypes.FANOUT, durable, autoDelete, null);
    }

    public static Exchange declareHeadersExchange(String exchangeName, boolean durable, boolean autoDelete, Map<String, Object> arguments) {
        return declareExchange(exchangeName, ExchangeTypes.HEADERS, durable, autoDelete, arguments);
    }

    private static Exchange declareExchange(String exchangeName, String exchangeType, boolean durable, boolean autoDelete, Map<String, Object> arguments) {
        Exchange exchange = new CustomExchange(exchangeName, exchangeType, durable, autoDelete, arguments);
        try {
            rabbitAdmin.declareExchange(exchange);
        } catch (Exception e) {
            logger.error("Declare exchange fail.", e);
        }
        return exchange;
    }

    public static Queue declareDurableQueue(String queueName) {
        return declareQueue(queueName, true, null);
    }

    public static Queue declareDurableQueue(String queueName, Map<String, Object> arguments) {
        return declareQueue(queueName, true, arguments);
    }

    public static Queue declareNonDurableQueue(String queueName) {
        return declareQueue(queueName, false, null);
    }

    public static Queue declareNonDurableQueue(String queueName, Map<String, Object> arguments) {
        return declareQueue(queueName, false, arguments);
    }

    private static Queue declareQueue(String queueName, boolean durable, Map<String, Object> arguments) {
        QueueBuilder queueBuilder = null;

        if (durable) {
            queueBuilder = QueueBuilder.durable(queueName);
        } else {
            queueBuilder = QueueBuilder.nonDurable(queueName);
        }

        if (arguments != null) {
            queueBuilder.withArguments(arguments);
        }

        Queue queue = queueBuilder.build();
        boolean status = StringUtils.isEmpty(rabbitAdmin.declareQueue(queue));
        if (status) {
            logger.error("Declare queue fail.");
        }

        return queue;
    }
}
