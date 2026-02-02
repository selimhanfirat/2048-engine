package ai.eval;

import game.core.Board;

@FunctionalInterface
public interface Evaluator {
    double evaluate(Board board);
}
