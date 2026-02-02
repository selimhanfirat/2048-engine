package game.runtime;

import ai.Player;
import game.core.Game;
import game.core.Move;

import java.util.HashMap;
import java.util.Map;

public final class GameSession {

    private final Game game;
    private final Player player;

    public GameSession(Game game, Player player) {
        this.game = game;
        this.player = player;
    }

    public SessionResult runGame() {
        int steps = 0;
        int maxTile = 0;
        Map<Move, Integer> moveCounts = new HashMap<>();

        game.initialize();
        maxTile = game.getState().getMaxTile();

        while (!game.isGameOver()) {
            Move move = player.chooseMove(game.getState());

            moveCounts.merge(move, 1, Integer::sum);

            game.step(move);
            steps++;

            maxTile = Math.max(maxTile, game.getState().getMaxTile());
        }

        return new SessionResult(
                game.getSeed(),
                game.getScore(),
                steps,
                maxTile,
                maxTile >= 2048,
                Map.copyOf(moveCounts)
        );
    }
}
