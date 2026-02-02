package ai.eval;

import game.core.Board;

public class ZeroCountingEvaluator implements Evaluator {

    @Override
    public double evaluate(Board board) {
        return board.getEmptyCells().length * 10.0 + Math.log(board.getMaxTile());
    }

}
