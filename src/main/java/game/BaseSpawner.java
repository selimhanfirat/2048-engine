package game;

import java.util.Random;
import java.util.Set;

public class BaseSpawner implements Spawner {

    double p;

    public BaseSpawner(double p) {
        this.p = p;
    }

    @Override
    public SpawnDecision addRandomTile(Board board, Random random){
        int[] emptyCells = board.getEmptyCells();
        int n = emptyCells.length;

        int pick;

        // I need to pick 2 values
        // one value is 2 vs 4. 2 with probability p and 4 with probability 1-p
        // and another value between 0 and n
        if (random.nextDouble() < p) {
            pick = 2;
        } else {
            pick = 4;
        }

        int pickIndex = random.nextInt(n);
        int rowIndex = pickIndex / n;
        int columnIndex = pickIndex % n;

        return new SpawnDecision(rowIndex, columnIndex, pick);
    }
}
