package se.chat.client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import se.chat.client.ws.WebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

@SpringBootApplication
public class ClientApplication implements CommandLineRunner {
	private static final String DEFAULT_SERVER = "127.0.0.1";
	private static final String CONNECT_STRING = "/connect ";
	private static final String SWITCH_STRING = "/switch ";


	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Override
	public void run(String... args) throws URISyntaxException, IOException {
		if (args.length < 1 || args.length > 2) {
			throw new RuntimeException("Invalid arguments. Need: [Server] <ChatName>");
		}

		String server = args.length == 2 ? args[0] : DEFAULT_SERVER;
		String chatName = args.length == 2 ? args[1] : args[0];

		URI url = new URI("ws://%s:8080/chat".formatted(server));

		WebSocketClient client = new StandardWebSocketClient();
		WebSocketHandler handler = new WebSocketHandler(chatName);

		client.execute(handler, null, url);


		try {
			handler.sendMessage(CONNECT_STRING + chatName);

			try (Scanner scanner = new Scanner(System.in)) {
				while (true) {
					String input = scanner.nextLine();

					if (input.startsWith(SWITCH_STRING)) {
						input = CONNECT_STRING + input.substring(SWITCH_STRING.length());
					}

					handler.sendMessage(input);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
