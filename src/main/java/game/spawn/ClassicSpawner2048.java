package game.spawn;

import game.core.Board;
import game.util.Coordinate;
import game.util.Rng;

import java.util.ArrayList;
import java.util.List;

public class ClassicSpawner2048 implements Spawner {

    private final double p2; // probability of spawning 2

    public ClassicSpawner2048(double p2) {
        if (p2 < 0.0 || p2 > 1.0) {
            throw new IllegalArgumentException("p2 must be in [0, 1]");
        }
        this.p2 = p2;
    }

    @Override
    public SpawnDistribution distribution(Board board) {
        int[] emptyCells = board.getEmptyCells();
        int n = emptyCells.length;

        if (n == 0) {
            throw new IllegalStateException("No empty cells");
        }

        List<SpawnDistribution.Outcome> outcomes = new ArrayList<>(2 * n);
        double pCell = 1.0 / n;

        int dim = board.getDimension();

        for (int cell : emptyCells) {
            int row = cell / dim;
            int col = cell % dim;
            Coordinate c = new Coordinate(row, col);

            outcomes.add(new SpawnDistribution.Outcome(
                    board.placeTile(c, 2),
                    pCell * p2
            ));

            outcomes.add(new SpawnDistribution.Outcome(
                    board.placeTile(c, 4),
                    pCell * (1.0 - p2)
            ));
        }

        return new SpawnDistribution(outcomes);
    }

    @Override
    public Board sample(Board board, Rng rng) {
        SpawnDistribution dist = distribution(board);

        double r = rng.nextDouble();
        double acc = 0.0;

        for (SpawnDistribution.Outcome o : dist.outcomes()) {
            acc += o.probability();
            if (r <= acc) {
                return o.board();
            }
        }

        // numerical / rounding fallback
        return dist.outcomes().get(dist.outcomes().size() - 1).board();
    }
}
