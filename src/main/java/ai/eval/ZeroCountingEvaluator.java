package ai.eval;

import game.core.Board;

public class ZeroCountingEvaluator implements Evaluator {

    @Override
    public double evaluate(Board board) {
        return 0.0;
    }

}
