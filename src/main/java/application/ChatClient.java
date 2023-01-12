package application;

import org.jspace.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatClient {
    private RemoteSpace chat;
    private Space messages;
    private Receiver receiver;
    private Thread thread;
    private String name;

    public ChatClient(String uri, int player, String name) {
        try {
            this.name = name;
            if (uri == null) {
                uri = "tcp://127.0.0.1:9001/room?keep";
            }
            chat = new RemoteSpace(uri);
            messages = new QueueSpace();
            receiver = new Receiver(chat, messages, player);
            thread = new Thread(receiver);
            thread.start();
            chat.get(new ActualField("players"), new FormalField(Integer.class));
            chat.put("players", player);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String message) {
        try {
            chat.put("message", name + ": " + message);
            chat.put("turn", 1);
        } catch (Exception ignored) {
        }
    }

    public List<String> receiveMessages() throws InterruptedException {
        List<String> strings = new ArrayList<>();
        for (Object[] message : messages.getAll(new FormalField(String.class))) {
            strings.add((String) message[0]);
        }
        return strings;
    }

    public String getName() {
        return name;
    }
}


