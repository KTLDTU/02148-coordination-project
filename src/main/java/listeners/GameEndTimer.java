package listeners;

import application.Game;

public class GameEndTimer implements Runnable {
    private final int GAME_END_DELAY_IN_MS = 2000;
    private Game game;

    public GameEndTimer(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(GAME_END_DELAY_IN_MS);

            for (int playerID : game.playersIdNameMap.keySet())
                game.gameSpace.put("game end", playerID);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
