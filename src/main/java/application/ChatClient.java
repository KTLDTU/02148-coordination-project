package application;

import org.jspace.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatClient{
    private Space chat = new SequentialSpace();
    private Space messages = new QueueSpace();
    Receiver receiver;
    Thread thread;
    String uri;
    public ChatClient(int player) throws IOException, InterruptedException {
        try {
            uri = "tcp://127.0.0.1:9001/chat?keep";
            chat = new RemoteSpace(uri);
            receiver = new Receiver(chat, messages,player);
            thread = new Thread(receiver);
            thread.start();
            chat.get(new ActualField("players"), new FormalField(Integer.class));
            chat.put("players",player);
        } catch (Exception ignored) {}
    }
    public ChatClient(String uri, int player) throws IOException, InterruptedException {
        try {
            this.uri = uri;
            chat = new RemoteSpace(this.uri);
            receiver = new Receiver(chat, messages,player);
            thread = new Thread(receiver);
            thread.start();
            chat.get(new ActualField("players"), new FormalField(Integer.class));
            chat.put("players",player);
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


