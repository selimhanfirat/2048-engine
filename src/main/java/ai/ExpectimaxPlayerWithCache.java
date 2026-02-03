package ai;

import ai.eval.Evaluator;
import game.core.Board;
import game.core.Move;
import game.rules.Rules;
import game.runtime.GameConfig;
import game.spawn.SpawnDistribution.Outcome;
import game.spawn.Spawner;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ExpectimaxPlayerWithCache implements Player {

    private final GameConfig config;
    private final Evaluator eval;

    private final Rules rules;
    private final Spawner spawner;
    private static final int DEPTH = 3;

    public ExpectimaxPlayerWithCache(GameConfig config, Evaluator eval) {
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

            double value = search(after, false, DEPTH);

            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private record CacheEntry(Integer depth, Double eval){};
    BoardLRUCache<Board, CacheEntry> cache = new BoardLRUCache<Board, CacheEntry>(10000);
    private double search(Board board, boolean player, int depth) {
        if (cache.containsKey(board) && cache.get(board).depth == depth) {
            return cache.get(board).eval;
        }

        if (depth == 0 || rules.isGameOver(board)) {
            return eval.evaluate(board);
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
        cache.put(board, new CacheEntry(depth, result));
        return result;
    }
}
