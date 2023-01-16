package listeners;

import application.Game;
import org.jspace.ActualField;

public class GameEndListener implements Runnable {
    private Game game;

    public GameEndListener(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        try {
            game.gameSpace.get(new ActualField("game end"), new ActualField(game.MY_PLAYER_ID));
            Integer winnerPlayerID = (game.tractors.isEmpty() ? null : (Integer) game.tractors.keySet().toArray()[0]);
            game.incrementPlayerScores(winnerPlayerID);
            game.newRound();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
