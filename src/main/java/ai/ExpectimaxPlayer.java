package ai;

import ai.eval.Evaluator;
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

    private record SearchResult(double eval, Move move) {}

    // ---- instrumentation ----
    private long nodes;
    private long evalCalls;
    private long chanceNodes;
    private long chanceOutcomes;
    private long searchNanos;

    public record SearchStats(
            long nodes,
            long evalCalls,
            long chanceNodes,
            long chanceOutcomes,
            long searchNanos
    ) {
    }

    public void resetStats() {
        nodes = 0;
        evalCalls = 0;
        chanceNodes = 0;
        chanceOutcomes = 0;
        searchNanos = 0;
    }

    public SearchStats getStats() {
        return new SearchStats(nodes, evalCalls, chanceNodes, chanceOutcomes, searchNanos);
    }

    public ExpectimaxPlayer(GameConfig config, Evaluator eval) {
        this.eval = eval;
        this.rules = config.rules();
        this.spawner = config.spawner();
    }

    @Override
    public Move chooseMove(Board board) {
        long t0 = System.nanoTime();
        SearchResult result = search(board, DEPTH * 2, true);
        searchNanos += (System.nanoTime() - t0);
        return result.move();
    }

    private SearchResult search(Board board, int pliesLeft, boolean playerTurn) {
        nodes++;

        if (pliesLeft == 0) {
            return leaf(board);
        }

        if (playerTurn) {
            var moves = rules.getLegalMoves(board);
            if (moves.isEmpty()) {
                return leaf(board);
            }

            double bestScore = Double.NEGATIVE_INFINITY;
            Move bestMove = null;

            for (Move m : moves) {
                Board afterMove = rules.makeMove(board, m).board();
                double score = search(afterMove, pliesLeft - 1, false).eval;

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = m;
                }
            }
            return new SearchResult(bestScore, bestMove);

        } else {
            chanceNodes++;

            var outcomes = spawner.distribution(board).outcomes();
            chanceOutcomes += outcomes.size();

            if (outcomes.isEmpty()) {
                return search(board, pliesLeft - 1, true);
            }

            int[] empties = board.getEmptyCells();
            double pCell = 1.0 / empties.length;
            double p2 = spawner.getP2();
            double p4 = 1.0 - p2;

            double expected = 0.0;
            for (int cell : empties) {
                expected += pCell * p2 * search(board.placeTile(cell, 2), pliesLeft - 1, true).eval;
                expected += pCell * p4 * search(board.placeTile(cell, 4), pliesLeft - 1, true).eval;
            }
            return new SearchResult(expected, null);
        }
    }

    private SearchResult leaf(Board board) {
        evalCalls++;
        return new SearchResult(eval.evaluate(board), null);
    }
}