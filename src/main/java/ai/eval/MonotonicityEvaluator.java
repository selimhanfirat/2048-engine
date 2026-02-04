package ai.eval;

import game.core.Board;

public class MonotonicityEvaluator implements Evaluator {

    private static final double MAX_PENALTY = 264.0; // 24 * ~11

    @Override
    public double evaluate(Board board) {
        int[][] grid = board.getGrid();
        int n = grid.length;
        int m = grid[0].length;

        int penalty = 0;

        // rows: decreasing left -> right
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m - 1; j++) {
                int left  = log2Tile(grid[i][j]);
                int right = log2Tile(grid[i][j + 1]);
                penalty += Math.max(0, right - left);
            }
        }

        // columns: decreasing top -> bottom
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n - 1; i++) {
                int top    = log2Tile(grid[i][j]);
                int bottom = log2Tile(grid[i + 1][j]);
                penalty += Math.max(0, bottom - top);
            }
        }

        // raw score is in [-MAX_PENALTY, 0]
        double raw = -penalty;

        // normalize to [0, 1]
        double normalized = (raw + MAX_PENALTY) / MAX_PENALTY;

        // clamp for safety
        return Math.max(0.0, Math.min(1.0, normalized));
    }

    private int log2Tile(int v) {
        if (v <= 0) return 0;
        return 31 - Integer.numberOfLeadingZeros(v);
    }
}
