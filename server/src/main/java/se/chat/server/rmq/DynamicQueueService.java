package se.chat.server.rmq;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Service;

@Service
public class DynamicQueueService {

    private final ConnectionFactory connectionFactory;
    private final RabbitAdmin rabbitAdmin;

    public DynamicQueueService(ConnectionFactory connectionFactory, RabbitAdmin rabbitAdmin) {
        this.connectionFactory = connectionFactory;
        this.rabbitAdmin = rabbitAdmin;
    }

    public void createQueueIfNotExists(String queueName, Object messageHandler) {
        rabbitAdmin.declareQueue(new Queue(queueName, true, false, false));
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);

        MessageListenerAdapter adapter = new MessageListenerAdapter(messageHandler);
        container.setMessageListener(adapter);

        container.start();
    }

}
