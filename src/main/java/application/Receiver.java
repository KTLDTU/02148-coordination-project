package application;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.Space;

import java.util.List;
import java.util.Queue;

class Receiver implements Runnable{
    private Space chat;
    private Space messages;

    public Receiver(Space chat, Space messages){
        this.chat = chat;
        this.messages = messages;
    }

    @Override
    public void run() {
        while(true) {
            try {
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
                    chat.getAll(new ActualField("message"), new FormalField(String.class));
                    chat.put("token");
                }
                chat.put("reader_lock");

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
