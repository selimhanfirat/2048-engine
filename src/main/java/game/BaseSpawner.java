package game;

import java.util.Random;
import java.util.Set;

public class BaseSpawner implements Spawner {

    double p;

    public BaseSpawner(double p) {
        this.p = p;
    }

    @Override
    public SpawnDecision pickRandomTile(Board board, Random random) {
        int[] emptyCells = board.getEmptyCells();
        int count = emptyCells.length;

        if (count == 0) {
            throw new IllegalStateException("No empty cells to spawn");
        }

        int value = (random.nextDouble() < p) ? 2 : 4;

        int cell = emptyCells[random.nextInt(count)];

        int dim = board.getDimension();
        int row = cell / dim;
        int col = cell % dim;

        return new SpawnDecision(row, col, value);
    }

}
