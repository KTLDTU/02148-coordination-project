package broadcasters;

import application.Game;

public class ShotBroadcaster implements Runnable {
    private Game game;
    private int fromPlayerID, shotID;
    private double x, y, rot;

    public ShotBroadcaster(Game game, int fromPlayerID, int shotID, double x, double y, double rot) {
        this.game = game;
        this.fromPlayerID = fromPlayerID;
        this.shotID = shotID;
        this.x = x;
        this.y = y;
        this.rot = rot;
    }

    @Override
    public void run() {
        try {
            for (int toPlayerID : game.playersIdNameMap.keySet())
                game.gameSpace.put("new shot", fromPlayerID, toPlayerID, shotID, x, y, rot);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
