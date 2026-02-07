package ai.eval;

import game.core.Board;

public class MonotonicityEvaluator implements Evaluator {

    private static final double MAX_PENALTY = 264.0;

    @Override
    public double evaluate(Board board) {
        int n = board.getDimension();

        int penalty = 0;

        // rows: decreasing left -> right
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n - 1; j++) {
                int left  = log2Tile(board.get(i, j));
                int right = log2Tile(board.get(i, j + 1));
                penalty += Math.max(0, right - left);
            }
        }

        // columns: decreasing top -> bottom
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n - 1; i++) {
                int top    = log2Tile(board.get(i, j));
                int bottom = log2Tile(board.get(i + 1, j));
                penalty += Math.max(0, bottom - top);
            }
        }

        double raw = -penalty; // [-MAX_PENALTY, 0]
        double normalized = (raw + MAX_PENALTY) / MAX_PENALTY;

        // clamp
        if (normalized < 0.0) return 0.0;
        if (normalized > 1.0) return 1.0;
        return normalized;
    }

    private int log2Tile(int v) {
        if (v <= 0) return 0;
        return 31 - Integer.numberOfLeadingZeros(v);
    }
}
