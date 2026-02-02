package ai;

import ai.eval.Evaluator;
import game.core.Board;
import game.core.Move;
import game.rules.Rules;
import game.runtime.Game;
import game.runtime.GameConfig;
import game.runtime.Presets;
import game.spawn.SpawnDistribution.Outcome;
import game.spawn.Spawner;

public class SearchingPlayer implements Player {

    private final GameConfig config;
    private final Evaluator eval;

    private final Rules rules;
    private final Spawner spawner;

    public SearchingPlayer(GameConfig config, Evaluator eval) {
        this.config = config;
        this.eval = eval;
        this.rules = config.rules();
        this.spawner = config.spawner();
    }

    @Override
    public Move chooseMove(Board board) {
        Move bestMove = Move.LEFT;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Move move : rules.getLegalMoves(board)) {
            Board after = rules.makeMove(board, move).board();

            calls = 0;
            double value = search(after, false, 2); // chance node next

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }
        return bestMove;
    }

    static long calls = 0;

    private double search(Board board, boolean player, int depth) {
        if (depth == 0 || rules.isGameOver(board)) {
            return eval.evaluate(board);
        }

        if (player) { // MAX
            double maxValue = Double.NEGATIVE_INFINITY;
            for (Move move : rules.getLegalMoves(board)) {
                Board next = rules.makeMove(board, move).board();
                maxValue = Math.max(maxValue, search(next, false, depth - 1));
            }
            return maxValue;

        } else { // CHANCE
            double expected = 0.0;
            for (Outcome outcome : spawner.distribution(board).outcomes()) {
                expected += outcome.probability() * search(outcome.board(), true, depth - 1);
            }
            return expected;
        }
    }
}
