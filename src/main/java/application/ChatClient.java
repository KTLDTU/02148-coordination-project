package application;

import org.jspace.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatClient{
    private Space chat = new SequentialSpace();
    private Space messages = new QueueSpace();
    Receiver receiver = new Receiver(chat, messages);
    Thread thread;
    String uri;
    public ChatClient() throws IOException {
        try {
            uri = "tcp://127.0.0.1:9001/chat?keep";
            chat = new RemoteSpace(uri);
        } catch (Exception ignored) {}
        receiver = new Receiver(chat, messages);
        thread = new Thread(receiver);
        thread.start();
    }
    public ChatClient(String uri) throws IOException {
        try {
            this.uri = uri;
            chat = new RemoteSpace(this.uri);
        } catch (Exception ignored) {}
    }
    public void sendMessage(String message) {
        try {
            chat.get(new ActualField("token"));
            chat.put("message", message);
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


