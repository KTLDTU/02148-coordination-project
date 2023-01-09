package application;

import controllers.GameSceneController;
import org.jspace.ActualField;
import org.jspace.SequentialSpace;
import org.jspace.SpaceRepository;

public class Server implements Runnable {
    SpaceRepository repository;
    SequentialSpace game;

    public Server(Grid grid, GameSceneController gameController) {
        repository = new SpaceRepository();
        game = new SequentialSpace();
        gameController.displayGrid(grid);
    }

    @Override
    public void run() {
        try {
            repository.add("game", game);
            String uri = "tcp://" + Game.HOST_IP + ":9005/?keep";
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
