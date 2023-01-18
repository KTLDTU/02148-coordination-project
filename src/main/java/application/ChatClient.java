package application;

import datatypes.ArrayListInt;
import org.jspace.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class ChatClient {
    private RemoteSpace chat;
    private Space messages;
    private List<Integer> players;
    private QueueSpace playerQueue = new QueueSpace();
    private Receiver receiver;
    private Thread thread;
    private String name;
    int player;

    public ChatClient(String uri, int player, String name, ArrayListInt players) {
        try {
            this.player = player;
            this.name = name;
            this.players = players;
            if (uri == null) {
                uri = "tcp://127.0.0.1:9001/room?keep";
            }
            chat = new RemoteSpace(uri);
            messages = new QueueSpace();
            receiver = new Receiver(chat, messages, player, players);
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
            players = (ArrayListInt)chat.query(new ActualField("playerIdList"), new FormalField(ArrayListInt.class))[1];
            chat.put("turn", players.get(0));
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

    public void closeClient() throws InterruptedException {
        players = (ArrayListInt)chat.get(new ActualField("playerIdList"), new FormalField(ArrayListInt.class))[1];
        players.remove((Object)player);
        chat.put("playerIdList", players);
        receiver.stop();
    }
}


