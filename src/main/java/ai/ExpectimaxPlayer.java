package ai;

import ai.eval.Evaluator;
import ai.util.BoardLRUCache;
import game.core.Board;
import game.core.Move;
import game.rules.Rules;
import game.runtime.GameConfig;
import game.spawn.Spawner;

public class ExpectimaxPlayer implements Player {

    private final Evaluator eval;
    private final Rules rules;
    private final Spawner spawner;

    private static final int CACHE_SIZE = 200_000;

    private final BoardLRUCache<CacheKey, Double> tt;
    private final int depth;
    private final boolean useCache;

    // Key must include depth + turn
    private record CacheKey(Board board, int pliesLeft, boolean playerTurn) {}

    // ---- instrumentation ----
    private long nodes;
    private long evalCalls;
    private long chanceNodes;
    private long chanceOutcomes;
    private long searchNanos;
    private long cacheHits;
    private long cacheMisses;

    public record SearchStats(
            long nodes,
            long evalCalls,
            long chanceNodes,
            long chanceOutcomes,
            long searchNanos,
            long cacheHits,
            long cacheMisses
    ) {}

    public void resetStats() {
        nodes = 0;
        evalCalls = 0;
        chanceNodes = 0;
        chanceOutcomes = 0;
        searchNanos = 0;
        cacheHits = 0;
        cacheMisses = 0;
    }

    public SearchStats getStats() {
        return new SearchStats(nodes, evalCalls, chanceNodes, chanceOutcomes,
                searchNanos, cacheHits, cacheMisses);
    }

    // ---- Constructor WITH cache control ----
    public ExpectimaxPlayer(GameConfig config, Evaluator eval, int depth, boolean useCache) {
        this.eval = eval;
        this.rules = config.rules();
        this.spawner = config.spawner();
        this.depth = depth;
        this.useCache = useCache;
        this.tt = useCache ? new BoardLRUCache<>(CACHE_SIZE) : null;
    }

    // ---- Optional convenience constructor (cache ON by default) ----
    public ExpectimaxPlayer(GameConfig config, Evaluator eval, int depth) {
        this(config, eval, depth, true);
    }

    @Override
    public Move chooseMove(Board board) {
        long t0 = System.nanoTime();

        var moves = rules.getLegalMoves(board);
        if (moves.isEmpty()) {
            searchNanos += (System.nanoTime() - t0);
            return Move.LEFT;
        }

        double bestScore = Double.NEGATIVE_INFINITY;
        Move bestMove = Move.LEFT;

        int plies = depth * 2;

        for (Move m : moves) {
            Board afterMove = rules.makeMove(board, m).board();
            double score = value(afterMove, plies - 1, false);
            if (score >= bestScore) {
                bestScore = score;
                bestMove = m;
            }
        }

        searchNanos += (System.nanoTime() - t0);
        return bestMove;
    }

    private double value(Board board, int pliesLeft, boolean playerTurn) {
        nodes++;

        if (pliesLeft == 0) {
            evalCalls++;
            return eval.evaluate(board);
        }

        CacheKey key = null;

        if (useCache) {
            key = new CacheKey(board, pliesLeft, playerTurn);
            Double cached = tt.get(key);
            if (cached != null) {
                cacheHits++;
                return cached;
            }
            cacheMisses++;
        }

        final double result;

        if (playerTurn) {
            var moves = rules.getLegalMoves(board);
            if (moves.isEmpty()) {
                evalCalls++;
                result = Double.NEGATIVE_INFINITY;
            } else {
                double best = Double.NEGATIVE_INFINITY;
                for (Move m : moves) {
                    Board after = rules.makeMove(board, m).board();
                    best = Math.max(best, value(after, pliesLeft - 1, false));
                }
                result = best;
            }
        } else {
            chanceNodes++;

            int[] empties = board.getEmptyCells();
            chanceOutcomes += (long) empties.length * 2;

            if (empties.length == 0) {
                result = value(board, pliesLeft - 1, true);
            } else {
                double pCell = 1.0 / empties.length;
                double p2 = spawner.getP2();
                double p4 = 1.0 - p2;

                double expected = 0.0;
                for (int cell : empties) {
                    expected += pCell * p2 * value(board.placeTile(cell, 2), pliesLeft - 1, true);
                    expected += pCell * p4 * value(board.placeTile(cell, 4), pliesLeft - 1, true);
                }
                result = expected;
            }
        }

        if (useCache) {
            tt.put(key, result);
        }

        return result;
    }
}