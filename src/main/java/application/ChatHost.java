package application;

import org.jspace.*;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ChatHost {
    private SpaceRepository repository = new SpaceRepository();
    private Space chat = new SequentialSpace();
    String uri;
    public ChatHost() throws IOException {
        try {
            repository.add("chat", chat);
            uri = "tcp://127.0.0.1:9001/chat?keep";
            URI myUri = new URI(uri);
            String gateUri = "tcp://" + myUri.getHost() + ":" + myUri.getPort() +  "?keep" ;
            repository.addGate(gateUri);
            chat.put("token");
        } catch (Exception ignored) {}
    }
    public ChatHost(String uri) throws IOException {
        try {
            chat = new RemoteSpace(uri);
            chat.put("token");
        } catch (Exception ignored) {}
    }
    public void sendMessage(String message) {
        try {
            chat.put("message", message);
        } catch (Exception ignored) {
        }
    }
    public List<String> recieveMessages() {
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