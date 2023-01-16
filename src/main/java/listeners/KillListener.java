package listeners;

import application.Game;
import application.GameApplication;
import application.GameEndTimer;
import application.Shot;
import javafx.application.Platform;
import org.jspace.ActualField;
import org.jspace.FormalField;

public class KillListener implements Runnable {
    private Game game;

    public KillListener(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object[] obj = game.gameSpace.get(new ActualField("kill"), new ActualField(game.MY_PLAYER_ID), new FormalField(Integer.class), new FormalField(Integer.class));
                int playerID = (int) obj[2];
                int shotID = (int) obj[3];

                Platform.runLater(() -> {
                    Shot shot = game.shots.get(shotID);

                    if (shot != null)
                        game.shotController.removeShot(shot);

                    game.gamePane.getChildren().remove(game.tractors.get(playerID));
                    game.tractors.remove(playerID);

                    if (GameApplication.isHost && game.numPlayersAlive() == 1)
                        new Thread(new GameEndTimer(game)).start();
                });

                if (playerID == game.MY_PLAYER_ID)
                    game.inputListener.disable();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
