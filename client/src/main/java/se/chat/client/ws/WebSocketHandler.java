package se.chat.client.ws;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class WebSocketHandler extends TextWebSocketHandler {
    private final String chatName;
    private WebSocketSession session;
    private CountDownLatch latch = new CountDownLatch(1);

    public WebSocketHandler(String chatName) {
        this.chatName = chatName;
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("Connected to server");
        this.session = session;
        latch.countDown();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println("Server: " + message.getPayload());
    }

    public void sendMessage(String message) throws IOException, InterruptedException {
        latch.await();
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        } else {
            System.out.println("Error: Not connected to the server.");
        }
    }
}
