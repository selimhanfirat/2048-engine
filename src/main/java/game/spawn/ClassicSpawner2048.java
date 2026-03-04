package game.spawn;

import game.core.Board;
import game.util.Rng;

import java.util.ArrayList;
import java.util.List;

public class ClassicSpawner2048 implements Spawner {

    private final double p2;

    public ClassicSpawner2048(double p2) {
        if (p2 < 0.0 || p2 > 1.0) {
            throw new IllegalArgumentException("p2 must be in [0, 1]");
        }
        this.p2 = p2;
    }

    public double getP2() {
        return p2;
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

        double p4 = 1.0 - p2;

        for (int cell : emptyCells) {
            outcomes.add(new SpawnDistribution.Outcome(
                    board.placeTile(cell, 2),
                    pCell * p2
            ));

            outcomes.add(new SpawnDistribution.Outcome(
                    board.placeTile(cell, 4),
                    pCell * p4
            ));
        }

        return new SpawnDistribution(outcomes);
    }

    @Override
    public Board sample(Board board, Rng rng) {
        int[] emptyCells = board.getEmptyCells();
        int n = emptyCells.length;

        if (n == 0) {
            throw new IllegalStateException("No empty cells");
        }

        int cell = emptyCells[rng.nextInt(n)];
        int value = (rng.nextDouble() < p2) ? 2 : 4;

        return board.placeTile(cell, value);
    }
}