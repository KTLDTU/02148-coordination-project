package application;

import datatypes.ArrayListInt;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;
import java.util.Queue;

class Receiver implements Runnable{
    private Space chat;
    private Space messages;
    private List<Integer> players;
    private int player;
    private boolean exit = false;

    public Receiver(Space chat, Space messages, int player, List<Integer> players){
        this.chat = chat;
        this.messages = messages;
        this.player = player;
        this.players = players;
    }

    @Override
    public void run(){
        while(!exit){
            try {
                chat.get(new ActualField("turn"), new ActualField(player));
                players = (ArrayListInt)chat.query(new ActualField("playerIdList"),new FormalField(ArrayListInt.class))[1];
                List<Object[]> messages = chat.queryAll(new ActualField("message"), new FormalField(String.class));
                for (Object[] message : messages) {
                    this.messages.put(message[1]);
                    System.out.println((message[1]) + " was posted");
                }
                System.out.println("Players in receiver thread " + players);
                if(player != players.get(players.size()-1)) chat.put("turn", players.get(players.indexOf(player)+1));
                else {
                    for(Object[] message : messages){
                        chat.get(new ActualField(message[0]), new ActualField(message[1]));
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stop() {
        exit = true;
    }
}
