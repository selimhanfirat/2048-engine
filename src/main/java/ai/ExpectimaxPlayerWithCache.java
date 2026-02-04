package ai;

import ai.eval.Evaluator;
import ai.util.BoardLRUCache;
import game.core.Board;
import game.core.Move;
import game.rules.Rules;
import game.runtime.GameConfig;
import game.spawn.SpawnDistribution.Outcome;
import game.spawn.Spawner;

public class ExpectimaxPlayerWithCache implements Player {

    private final Evaluator eval;
    private final Rules rules;
    private final Spawner spawner;

    private static final int DEPTH = 4;

    private record CacheKey(Board board, boolean player, int depth) {}

    private final BoardLRUCache<CacheKey, Double> cache = new BoardLRUCache<>(10000);

    private long cacheLookups = 0;
    private long cacheHits = 0;

    private int decisions = 0;
    private static final int PRINT_EVERY = 20;
    private static final boolean DEBUG_CACHE = false;

    public ExpectimaxPlayerWithCache(GameConfig config, Evaluator eval) {
        this.eval = eval;
        this.rules = config.rules();
        this.spawner = config.spawner();
    }

    @Override
    public Move chooseMove(Board board) {
        decisions++;

        Move bestMove = Move.LEFT;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Move move : rules.getLegalMoves(board)) {
            Board after = rules.makeMove(board, move).board();
            double value = search(after, false, DEPTH);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        if (DEBUG_CACHE && decisions % PRINT_EVERY == 0) {
            printCacheStats();
            resetCacheStats();
        }

        return bestMove;
    }

    private double search(Board board, boolean player, int depth) {
        cacheLookups++;

        CacheKey key = new CacheKey(board, player, depth);
        Double cached = cache.get(key);
        if (cached != null) {
            cacheHits++;
            return cached;
        }

        if (depth == 0 || rules.isGameOver(board)) {
            double leaf = eval.evaluate(board);
            cache.put(key, leaf);
            return leaf;
        }

        double result;
        if (player) { // MAX
            double maxValue = Double.NEGATIVE_INFINITY;
            for (Move move : rules.getLegalMoves(board)) {
                Board next = rules.makeMove(board, move).board();
                maxValue = Math.max(maxValue, search(next, false, depth - 1));
            }
            result = maxValue;
        } else { // CHANCE
            double expected = 0.0;
            for (Outcome outcome : spawner.distribution(board).outcomes()) {
                expected += outcome.probability() * search(outcome.board(), true, depth - 1);
            }
            result = expected;
        }

        cache.put(key, result);
        return result;
    }

    private void printCacheStats() {
        if (cacheLookups == 0) {
            System.out.println("Cache stats: no lookups");
            return;
        }

        double hitRate = 100.0 * cacheHits / cacheLookups;
        System.out.printf(
                "Cache stats: hits=%d, lookups=%d, hit rate=%.2f%%%n",
                cacheHits, cacheLookups, hitRate
        );
    }

    private void resetCacheStats() {
        cacheLookups = 0;
        cacheHits = 0;
    }
}
