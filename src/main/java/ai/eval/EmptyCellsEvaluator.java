package ai.eval;

import game.core.Board;

public class EmptyCellsEvaluator implements Evaluator {

    private static final double MAX_SCORE = 170.0;
    // raw = emptyCells * 10 + log(maxTile)
    // ln(2048) ≈ 7.62, ln(4096) ≈ 8.32, ln(8192) ≈ 9.01

    @Override
    public double evaluate(Board board) {
        int empty = board.getEmptyCells().length;
        double maxTileLog = Math.log(board.getMaxTile());

        double raw = empty * 10.0 + maxTileLog;

        double normalized = raw / MAX_SCORE;

        return Math.max(0.0, Math.min(1.0, normalized));
    }
}
