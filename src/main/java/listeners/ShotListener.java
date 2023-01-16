package listeners;

import application.Game;
import application.GameApplication;
import application.Shot;
import broadcasters.KillBroadcaster;
import javafx.application.Platform;
import org.jspace.ActualField;
import org.jspace.FormalField;

public class ShotListener implements Runnable {
    private Game game;

    public ShotListener(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object[] obj = game.gameSpace.get(new ActualField("new shot"), new FormalField(Integer.class), new ActualField(game.MY_PLAYER_ID), new FormalField(Integer.class), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Double.class));
                int playerID = (int) obj[1];
                int shotID = (int) obj[3];
                double shotX = (double) obj[4];
                double shotY = (double) obj[5];
                double shotRot = (double) obj[6];

                Platform.runLater(() -> {
                    Shot shot = game.shotController.shoot(shotX, shotY, shotRot, playerID, shotID);
                    game.shots.put(shotID, shot);

                    // if a player shoots directly into a wall, they die immediately
                    if (GameApplication.isHost && game.grid.isWallCollision(shot)) {
                        new Thread(new KillBroadcaster(game, playerID, shotID)).start();
                    }
                });
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
