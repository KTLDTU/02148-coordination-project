package application;

import org.jspace.ActualField;
import org.jspace.FormalField;

public class Broadcaster implements Runnable {
    private Game game;

    public Broadcaster(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        broadcastPosition();
    }

    public void broadcastPosition() {
        try {
            // remove all previous position tuples
            game.gameSpace.getAll(new ActualField("position"), new ActualField(game.MY_PLAYER_ID), new FormalField(Integer.class), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Double.class));

            for (int playerID : game.playerIDs)
                if (playerID != game.MY_PLAYER_ID) {
                    game.gameSpace.put("position", game.MY_PLAYER_ID, playerID, game.myTractor.getLayoutX(), game.myTractor.getLayoutY(), game.myTractor.getRotate());
//                    System.out.println("Player " + MY_PLAYER_ID + " has sent position (" + myTractor.getLayoutX() + ", " + myTractor.getLayoutY() + ") to " + playerID);
                }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
