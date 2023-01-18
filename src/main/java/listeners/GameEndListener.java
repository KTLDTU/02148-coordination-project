package listeners;

import application.Game;
import application.Shot;
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

            synchronized (game.shotsLock) {
                while (!game.shots.isEmpty()) {
                    Shot shot = (Shot) game.shots.values().toArray()[0];

                    if (shot != null)
                        game.shotController.removeShot(shot);
                    else
                        game.shots.remove(shot.getShotID());
                }
            }

            Integer winnerPlayerID = (game.tractors.isEmpty() ? null : (Integer) game.tractors.keySet().toArray()[0]);
            game.incrementPlayerScore(winnerPlayerID);
            game.newRound();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
