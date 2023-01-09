package application;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;
import java.util.Queue;

class Receiver implements Runnable{
    private Space chat;
    private Space messages;
    private int numberOfPlayers;
    private int player;

    public Receiver(Space chat, Space messages,int player){
        this.chat = chat;
        this.messages = messages;
        this.player = player;
    }

    @Override
    public void run(){
        while(true){
            try {
                Object[] players = chat.query(new ActualField("players"), new FormalField(Integer.class));
                numberOfPlayers = (int)players[1];
                chat.get(new ActualField("turn"), new ActualField(player));
                List<Object[]> messages = chat.queryAll(new ActualField("message"), new FormalField(String.class));
                for (Object[] message : messages) {
                    this.messages.put(message[1]);
                }
                if(player != numberOfPlayers) chat.put("turn", player+1);
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

    /*@Override
    public void run() {
        while(true) {
            try {
                //something is wrong here
                chat.get(new ActualField("reader_lock"));
                Object[] readers = chat.get(new ActualField("readers"), new FormalField(Integer.class));
                chat.put("readers",(int)readers[1]+1);
                if((int)readers[1] == 0) chat.get(new ActualField("token"));
                chat.put("reader_lock");
                List<Object[]> messages = chat.queryAll(new ActualField("message"), new FormalField(String.class));
                for (Object[] message : messages) {
                    this.messages.put(message[1]);
                }
                chat.get(new ActualField("reader_lock"));
                readers = chat.get(new ActualField("readers"), new FormalField(Integer.class));
                chat.put("readers",(int)readers[1]-1);
                if((int)readers[1] == 1){
                    for(Object[] message : messages) {
                        chat.get(new ActualField(message[0]), new ActualField(message[1]));
                    }
                    chat.put("token");
                }
                chat.put("reader_lock");

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }*/
}
