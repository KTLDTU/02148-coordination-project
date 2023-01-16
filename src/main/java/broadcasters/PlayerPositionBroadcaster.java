package broadcasters;

import application.Game;
import org.jspace.ActualField;
import org.jspace.FormalField;

public class PlayerPositionBroadcaster implements Runnable {
    private Game game;

    public PlayerPositionBroadcaster(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        try {
            // remove all previous position tuples
            game.gameSpace.getAll(new ActualField("player position"), new ActualField(game.MY_PLAYER_ID), new FormalField(Integer.class), new FormalField(Double.class), new FormalField(Double.class), new FormalField(Double.class));

            for (int playerID : game.playersIdNameMap.keySet())
                if (playerID != game.MY_PLAYER_ID)
                    game.gameSpace.put("player position", game.MY_PLAYER_ID, playerID, game.myTractor.getLayoutX(), game.myTractor.getLayoutY(), game.myTractor.getRotate());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
