package se.chat.server.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
    public static String CONNECT_COMMAND = "/connect";

    Map<String, String> userChat = new ConcurrentHashMap<>();
    Map<String, List<String>> chatUsers = new ConcurrentHashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String wsID = session.getId();
        String text = new String(message.asBytes());

        if (text.startsWith(CONNECT_COMMAND)) {
            String chatName = text.substring(CONNECT_COMMAND.length() + 1);
            session.sendMessage(new TextMessage("Connect to " + chatName));
        } else {
            session.sendMessage(new TextMessage("Error: invalid command"));
        }
    }
}
