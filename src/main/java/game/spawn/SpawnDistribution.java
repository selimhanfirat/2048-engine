package game.spawn;

import game.core.Board;
import java.util.List;

public record SpawnDistribution(List<Outcome> outcomes) {

    public record Outcome(Board board, double probability) {}

    public double totalProbability() {
        return outcomes.stream().mapToDouble(Outcome::probability).sum();
    }
}
