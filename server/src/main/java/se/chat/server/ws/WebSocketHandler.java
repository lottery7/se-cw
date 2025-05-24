package se.chat.server.ws;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import se.chat.server.rmq.DynamicQueueService;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
    public static String CONNECT_COMMAND = "/connect";

    private final DynamicQueueService dynamicQueueService;
    private final RabbitTemplate rabbitTemplate;

    private final Lock dataLock = new ReentrantLock();
    private final Map<String, String> userChat = new HashMap<>();
    private final Map<String, Set<String>> chatUsers = new HashMap<>();


    public WebSocketHandler(DynamicQueueService dynamicQueueService, RabbitTemplate rabbitTemplate) {
        this.dynamicQueueService = dynamicQueueService;
        this.rabbitTemplate = rabbitTemplate;
    }

    private void addUserToChat(String chatID, String userID) {
        dataLock.lock();
        try {
            createChatIfNotExists(chatID);
            if (userChat.containsKey(userID)) {
                chatUsers.get(chatID).remove(userID);
            }
            userChat.put(userID, chatID);
            chatUsers.get(chatID).add(userID);
        } finally {
            dataLock.unlock();
        }
    }

    private void createChatIfNotExists(String chatID) {
        dataLock.lock();
        try {
            if (!chatUsers.containsKey(chatID)) {
                chatUsers.put(chatID, new HashSet<>());
                dynamicQueueService.createQueueIfNotExists(chatID, new MessageHandler(chatID));
            }
        } finally {
            dataLock.unlock();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String wsID = session.getId();
        String text = new String(message.asBytes());

        if (text.startsWith(CONNECT_COMMAND)) {
            String chatID = text.substring(CONNECT_COMMAND.length() + 1);
            addUserToChat(chatID, wsID);
            session.sendMessage(new TextMessage("Connected to " + chatID));
        } else {
            dataLock.lock();
            try {
                rabbitTemplate.convertAndSend(userChat.get(wsID), message);
            } finally {
                dataLock.unlock();
            }
        }
    }

    private class MessageHandler {
        private final String queueName;

        public MessageHandler(String queueName) {
            this.queueName = queueName;
        }

        public void handleMessage(String message) {
            System.out.println(queueName + ": " + message);
        }
    }
}
