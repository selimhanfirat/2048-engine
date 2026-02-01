package game.runtime;

import ai.Player;
import game.core.Board;
import game.core.Game;
import game.core.Move;
import game.rules.Rules;

import java.util.EnumSet;
import java.util.HashMap;

public class GameSession {

    Game game;
    Player player;

    public GameSession(Game game, Player player) {
        this.game = game;
        this.player = player;
    }


    public SessionResult runGame() {
        Rules rules = game.getConfig().rules();
        long seed = game.getConfig().seed();

        int steps = 0;
        int maxTile = 0;
        HashMap<Move, Integer> moveCounts = new HashMap<>();

        game.initialize();
        maxTile = game.getState().getMaxTile();

        while (!game.isGameOver()) {
            Board state = game.getState();
            var possibleMoves = rules.getLegalMoves(state);

            // Not a termination check. This is a "ruleset is inconsistent" check.
            if (possibleMoves.isEmpty()) {
                throw new IllegalStateException(
                        "Ruleset returned isGameOver=false but provided zero legal moves."
                );
            }

            Move decision = player.chooseMove(state, possibleMoves);
            moveCounts.merge(decision, 1, Integer::sum);

            game.step(decision);
            steps++;

            maxTile = Math.max(maxTile, game.getState().getMaxTile());
        }

        int finalScore = game.getScore();
        boolean reached2048 = maxTile >= 2048;

        return new SessionResult(seed, finalScore, steps, reached2048, new HashMap<>(moveCounts));
    }


}
