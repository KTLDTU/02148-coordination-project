package application;

import org.jspace.*;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ChatClient{
    private Space chat = new SequentialSpace();
    String uri;
    public ChatClient() throws IOException {
        try {
            uri = "tcp://127.0.0.1:9001/chat?keep";
            chat = new RemoteSpace(uri);
            chat.put("token");
        } catch (Exception ignored) {}
    }
    public ChatClient(String uri) throws IOException {
        try {
            this.uri = uri;
            chat = new RemoteSpace(this.uri);
            chat.put("token");
        } catch (Exception ignored) {}
    }
    public void sendMessage(String message) {
        try {
            chat.put("message", message);
        } catch (Exception ignored) {
        }
    }
    public List<String> receiveMessages() {
        List<String> strings = new ArrayList<>();
        try {
            chat.get(new ActualField("token"));
            List<Object[]> messages = chat.getAll(new ActualField("message"), new FormalField(String.class));
            for(Object[] message : messages){
                strings.add((String) message[1]);
            }
            chat.put("token");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return strings;
    }
}
