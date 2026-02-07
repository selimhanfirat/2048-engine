package ai;

import ai.eval.Evaluator;
import ai.util.BoardLRUCache;
import game.core.Board;
import game.core.Move;
import game.rules.Rules;
import game.runtime.GameConfig;
import game.spawn.SpawnDistribution.Outcome;
import game.spawn.Spawner;

public class ExpectimaxPlayer implements Player {

    private final Evaluator eval;
    private final Rules rules;
    private final Spawner spawner;

    private static final int DEPTH = 2;
    private record SearchResult(double eval, Move move){};

    public ExpectimaxPlayer(GameConfig config, Evaluator eval) {
        this.eval = eval;
        this.rules = config.rules();
        this.spawner = config.spawner();
    }

    @Override
    public Move chooseMove(Board board) {
        return playerNode(board, DEPTH).move();
    }

    private SearchResult playerNode(Board board, int depth) {
        if (depth == 0) {
            return new SearchResult(eval.evaluate(board), null);
        }

        var moves = rules.getLegalMoves(board);
        if (rules.isGameOver(board)) {
            return new SearchResult(eval.evaluate(board), null);
        }

        double bestScore = Double.NEGATIVE_INFINITY;
        Move bestMove = null;

        for (Move m : moves) {
            Board afterMove = rules.makeMove(board, m).board();

            // Spend one player choice, THEN force a spawn, THEN recurse.
            double score = chanceNode(afterMove, depth - 1).eval;

            if (score > bestScore) {
                bestScore = score;
                bestMove = m;
            }
        }
        return new SearchResult(bestScore, bestMove);
    }

    private SearchResult chanceNode(Board boardAfterMove, int depth) {

        var outcomes = spawner.distribution(boardAfterMove).outcomes();

        // If there are no possible spawns, treat it like "no random event" and continue.
        if (outcomes.isEmpty()) {
            return playerNode(boardAfterMove, depth);
        }

        double expected = 0.0;
        for (Outcome o : outcomes) {
            expected += o.probability() * playerNode(o.board(), depth).eval;
        }
        return new SearchResult(expected, null);
    }


}
