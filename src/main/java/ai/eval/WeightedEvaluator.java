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
        if (terms.isEmpty()) return 0.0;

        double score = 0.0;
        double totalWeight = 0.0;

        for (Term t : terms) {
            double w = t.weight();
            if (w == 0.0) continue;
            score += w * t.evaluator().evaluate(board);
            totalWeight += w;
        }

        if (totalWeight == 0.0) return 0.0;
        return score / totalWeight;
    }
}
