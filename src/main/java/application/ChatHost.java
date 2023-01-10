package application;

import org.jspace.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ChatHost {
    private SpaceRepository repository = new SpaceRepository();
    private Space chat = new SequentialSpace();
    private Space messages = new SequentialSpace();
    private Receiver reciever = new Receiver(chat, messages, 1);
    private Thread thread;
    private String name;

    public ChatHost(String uri, String name) {
        thread = new Thread(reciever);
        thread.start();
        try {
            this.name = name;
            if (uri == null) {
                uri = "tcp://127.0.0.1:9001/?keep";
            }
            URI myUri = new URI(uri);
            String gateUri = "tcp://" + myUri.getHost() + ":" + myUri.getPort() + "?keep";
            repository.addGate(gateUri);
            repository.add("chat", chat);
            chat.put("token");
            chat.put("reader_lock");
            chat.put("turn", 1);
            chat.put("players", 1);
            chat.put("readers", 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String message) {
        try {
            chat.get(new ActualField("token"));
            chat.put("message", name + ": " + message);
            chat.put("turn", 1);
            chat.put("token");
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