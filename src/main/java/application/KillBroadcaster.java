package application;

public class KillBroadcaster implements Runnable {
    private Game game;
    private int playerID, shotID;

    public KillBroadcaster(Game game, int playerID, int shotID) {
        this.game = game;
        this.playerID = playerID;
        this.shotID = shotID;
    }

    @Override
    public void run() {
        try {
            for (int toPlayerID : game.playersIdNameMap.keySet())
                game.gameSpace.put("kill", toPlayerID, playerID, shotID);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
