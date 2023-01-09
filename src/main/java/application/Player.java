package application;

import org.jspace.RemoteSpace;

import java.io.IOException;

public class Player implements Runnable {
    RemoteSpace game;

    @Override
    public void run() {
        try {
            String uri = "tcp://127.0.0.1:9005/game?keep";
            game = new RemoteSpace(uri);
            game.put("join");
            System.out.println("Player has put \"join\" in remote space");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
