package listeners;

import application.Game;
import javafx.application.Platform;
import javafx.scene.shape.Rectangle;
import org.jspace.ActualField;
import org.jspace.FormalField;

public class MovementListener implements Runnable {
    private Game game;

    public MovementListener(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object[] obj = game.gameSpace.get(new ActualField("player position"), new FormalField(Integer.class), new ActualField(game.MY_PLAYER_ID), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Double.class));
                int playerID = (int) obj[1];
                double tractorX = (double) obj[3];
                double tractorY = (double) obj[4];
                double tractorRot = (double) obj[5];

                Rectangle tractor = game.tractors.get(playerID);

                Platform.runLater(() -> {
                    if (tractor != null) {
                        tractor.setLayoutX(tractorX);
                        tractor.setLayoutY(tractorY);
                        tractor.setRotate(tractorRot);
                    }
                });
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
