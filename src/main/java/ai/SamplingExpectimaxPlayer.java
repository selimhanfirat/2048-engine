package ai;

import ai.eval.Evaluator;
import game.core.Board;
import game.runtime.GameConfig;

public class SamplingExpectimaxPlayer extends ExpectimaxPlayer {

    private final int ignore4Threshold;

    public SamplingExpectimaxPlayer(GameConfig config, Evaluator eval, int depth, boolean useCache, int ignore4Threshold) {
        super(config, eval, depth, useCache);
        this.ignore4Threshold = ignore4Threshold;
    }

    public SamplingExpectimaxPlayer(GameConfig config, Evaluator eval, int depth) {
        this(config, eval, depth, true, 6);
    }

    @Override
    protected boolean shouldCacheChance(Board board, int pliesLeft, int[] empties) {
        // Don't cache approximated chance nodes (empties > threshold)
        return empties.length <= ignore4Threshold;
    }

    @Override
    protected double chanceValue(Board board, int pliesLeft, int[] empties) {
        if (empties.length > ignore4Threshold) {
            // instrumentation: only 1 outcome per empty (2-only approximation)
            chanceOutcomes += empties.length;

            double pCell = 1.0 / empties.length;
            double expected = 0.0;
            for (int cell : empties) {
                expected += pCell * value(board.placeTile(cell, 2), pliesLeft - 1, true);
            }
            return expected;
        }

        // Fall back to exact model
        return super.chanceValue(board, pliesLeft, empties);
    }
}