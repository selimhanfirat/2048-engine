package ai.eval;

import game.core.Board;

public final class ClassicEvaluator implements Evaluator {

    private static final double W_EMPTY = 2000.0;
    private static final double W_MAX_TILE = 1.0;
    private static final double W_MAX_IN_CORNER = 50.0;
    private static final double W_MONOTONICITY = 10.0;
    private static final double W_SMOOTHNESS = 5.0;

    @Override
    public double evaluate(Board board) {
        BoardStats stats = scanBoard(board);

        double monotonicity = monotonicity(board);
        boolean maxInCorner = isCorner(board.getDimension(), stats.maxRow, stats.maxCol);

        return
                W_EMPTY * stats.emptyCount +
                        W_MAX_TILE * stats.maxTile +
                        W_MAX_IN_CORNER * (maxInCorner ? 1.0 : 0.0) +
                        W_MONOTONICITY * monotonicity +
                        W_SMOOTHNESS * stats.smoothness;
    }

    private static BoardStats scanBoard(Board board) {
        int n = board.getDimension();

        int emptyCount = 0;
        int maxTile = 0;
        int maxRow = 0;
        int maxCol = 0;

        double smoothness = 0.0;

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                int v = board.get(r, c);

                if (v == 0) {
                    emptyCount++;
                    continue;
                }

                if (v > maxTile) {
                    maxTile = v;
                    maxRow = r;
                    maxCol = c;
                }

                // Penalize differences between adjacent non-empty tiles.
                // Only check right and down to avoid double counting.
                smoothness += smoothnessPenaltyToRight(board, r, c, v);
                smoothness += smoothnessPenaltyDown(board, r, c, v);
            }
        }

        return new BoardStats(emptyCount, maxTile, maxRow, maxCol, smoothness);
    }

    private static double smoothnessPenaltyToRight(Board board, int r, int c, int v) {
        int n = board.getDimension();
        if (c + 1 >= n) return 0.0;

        int right = board.get(r, c + 1);
        if (right == 0) return 0.0;

        return -Math.abs(exp(v) - exp(right));
    }

    private static double smoothnessPenaltyDown(Board board, int r, int c, int v) {
        int n = board.getDimension();
        if (r + 1 >= n) return 0.0;

        int down = board.get(r + 1, c);
        if (down == 0) return 0.0;

        return -Math.abs(exp(v) - exp(down));
    }

    private static double monotonicity(Board board) {
        return monotonicityRows(board) + monotonicityCols(board);
    }

    private static double monotonicityRows(Board board) {
        int n = board.getDimension();
        double score = 0.0;

        for (int r = 0; r < n; r++) {
            double inc = 0.0;
            double dec = 0.0;

            for (int c = 0; c + 1 < n; c++) {
                int a = expOrZero(board.get(r, c));
                int b = expOrZero(board.get(r, c + 1));

                if (a > b) inc += (a - b);
                else dec += (b - a);
            }

            score -= Math.min(inc, dec);
        }

        return score;
    }

    private static double monotonicityCols(Board board) {
        int n = board.getDimension();
        double score = 0.0;

        for (int c = 0; c < n; c++) {
            double inc = 0.0;
            double dec = 0.0;

            for (int r = 0; r + 1 < n; r++) {
                int a = expOrZero(board.get(r, c));
                int b = expOrZero(board.get(r + 1, c));

                if (a > b) inc += (a - b);
                else dec += (b - a);
            }

            score -= Math.min(inc, dec);
        }

        return score;
    }

    private static boolean isCorner(int n, int r, int c) {
        boolean topOrBottom = (r == 0) || (r == n - 1);
        boolean leftOrRight = (c == 0) || (c == n - 1);
        return topOrBottom && leftOrRight;
    }

    private static int expOrZero(int tileValue) {
        return (tileValue == 0) ? 0 : exp(tileValue);
    }

    private static int exp(int tileValue) {
        // 2048 tiles are powers of two, so log2 is safe and fast.
        return 31 - Integer.numberOfLeadingZeros(tileValue);
    }

    private record BoardStats(int emptyCount, int maxTile, int maxRow, int maxCol, double smoothness) {}
}