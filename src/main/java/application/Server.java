package application;

import org.jspace.ActualField;
import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;

public class Server implements Runnable {
    SpaceRepository repository;
    SequentialSpace game;

    public Server() {
        repository = new SpaceRepository();
        game = new SequentialSpace();
    }

    @Override
    public void run() {
        try {
            repository.add("game", game);
            String uri = "tcp://127.0.0.1:9005/?keep";
            repository.addGate(uri);

            while (true) {
                System.out.println("Server is checking for \"join\" in sequential space");
                game.get(new ActualField("join"));
                System.out.println("Someone joined");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}