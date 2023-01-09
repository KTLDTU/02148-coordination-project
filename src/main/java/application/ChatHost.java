package application;

import org.jspace.*;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ChatHost{
    private SpaceRepository repository = new SpaceRepository();
    private Space chat = new SequentialSpace();
    private Space messages = new SequentialSpace();
    Receiver reciever = new Receiver(chat, messages,1);
    Thread thread;
    String uri;
    public ChatHost() throws IOException {
        thread = new Thread(reciever);
        thread.start();
        try {
            uri = "tcp://127.0.0.1:9001/?keep";
            URI myUri = new URI(uri);
            String gateUri = "tcp://" + myUri.getHost() + ":" + myUri.getPort() +  "?keep" ;
            repository.addGate(gateUri);
            repository.add("chat", chat);
            chat.put("token");
            chat.put("reader_lock");
            chat.put("player_lock");
            chat.put("turn",1);
            chat.put("players",1);
            chat.put("readers",0);
        } catch (Exception ignored) {}
    }
    public ChatHost(String uri) throws IOException {
        try {
            URI myUri = new URI(uri);
            String gateUri = "tcp://" + myUri.getHost() + ":" + myUri.getPort() +  "?keep" ;
            repository.addGate(gateUri);
            repository.add("chat", chat);
            chat.put("token");
            chat.put("reader_lock");
            chat.put("player_lock");
            chat.put("turn",1);
            chat.put("players",1);
            chat.put("readers",0);
        } catch (Exception ignored) {}
    }
    public void sendMessage(String message) {
        try {
            chat.get(new ActualField("token"));
            chat.put("message", message);
            chat.put("turn",1);
            chat.put("token");
        } catch (Exception ignored) {
        }
    }
    public List<String> receiveMessages() throws InterruptedException {
        List<String> strings = new ArrayList<>();
        for(Object[] message : messages.getAll(new FormalField(String.class))){
            strings.add((String) message[0]);
        }
        return strings;
    }
}