package application;

public class ShotBroadcaster implements Runnable {
    private Game game;
    private double x, y, rot;

    public ShotBroadcaster(Game game, double x, double y, double rot) {
        this.game = game;
        this.x = x;
        this.y = y;
        this.rot = rot;
    }

    @Override
    public void run() {
        try {
            for (int playerID : game.playerIDs)
                if (playerID != game.MY_PLAYER_ID)
                    game.gameSpace.put("new shot", playerID, x, y, rot);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
