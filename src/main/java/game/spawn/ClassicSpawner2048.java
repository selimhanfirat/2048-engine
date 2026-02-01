package game.spawn;

import game.core.Board;
import game.util.Coordinate;

import java.util.Random;

public class ClassicSpawner2048 implements Spawner {

    private final double p;

    public ClassicSpawner2048(double p) {
        this.p = p;
    }

    @Override
    public Board spawn(Board board, Random random) {
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

        return board.placeTile(new Coordinate(row, col), value);
    }

}
