package ai.eval;

import game.core.Board;

import java.util.List;

public class WeightedEvaluator implements Evaluator {

    public record Term(Evaluator evaluator, double weight) {}

    private final List<Term> terms;

    public WeightedEvaluator(List<Term> terms) {
        this.terms = terms;
    }

    @Override
    public double evaluate(Board board) {
        double score = 0.0;
        for (Term t : terms) {
            score += t.weight() * t.evaluator().evaluate(board);
        }
        return score / terms.size();
    }
}
