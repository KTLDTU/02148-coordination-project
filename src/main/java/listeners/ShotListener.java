package listeners;

import application.Game;
import application.GameApplication;
import broadcasters.KillBroadcaster;
import application.Shot;
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

                if (playerID == -1)
                    break;

                Shot shot;

                synchronized (game.shotsLock) {
                    shot = game.shotController.shoot(shotX, shotY, shotRot, playerID, shotID);
                    game.shots.put(shotID, shot);
                }

                // if a player shoots directly into a wall, they die immediately
                if (GameApplication.isRoomHost && game.grid.isWallCollision(shot)) {
                    Thread killBroadcaster = new Thread(new KillBroadcaster(game, playerID, shotID));
                    killBroadcaster.start();
                    killBroadcaster.join();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
