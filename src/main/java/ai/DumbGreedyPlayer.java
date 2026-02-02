package ai;

import game.core.Board;
import game.core.Move;
import game.core.MoveResult;
import game.rules.Rules;
import game.runtime.Game;
import game.runtime.GameConfig;
import game.spawn.Spawner;

public class DumbGreedyPlayer implements Player {

    private final GameConfig config;
    private final Rules rules;
    private final Spawner spawner;

    public DumbGreedyPlayer(GameConfig config) {
        this.config = config;
        this.rules = config.rules();
        this.spawner = config.spawner();
    }

    @Override
    public Move chooseMove(Board board) {
        var possibleMoves = rules.getLegalMoves(board);

        int maxScoreGained = Integer.MIN_VALUE / 2;
        Move bestMove = null;

        for (Move move : possibleMoves) {
            MoveResult mr = rules.makeMove(board, move);
            if (mr.scoreGained() > maxScoreGained) {
                maxScoreGained = mr.scoreGained();
                bestMove = move;
            }
        }

        return bestMove;
    }
}
