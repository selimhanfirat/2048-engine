package game;

import java.util.Arrays;

public final class Board {

    private final int[][] grid;
    private final int n;

    public Board() {
        this(4);
    }

    public Board(int dimension) {
        this(new int[dimension][dimension]);
    }

    public Board(int[][] grid) {
        this.n = grid.length;
        this.grid = copyGrid(grid);
    }

    public int getDimension() {
        return n;
    }

    public int[][] getGrid() {
        return copyGrid(grid);
    }

    public int[] getEmptyCells() {
        int count = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 0) count++;
            }
        }

        int[] emptyCells = new int[count];
        int index = 0;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (grid[i][j] == 0) {
                    emptyCells[index++] = i * n + j;
                }
            }
        }
        return emptyCells;
    }

    public Board placeTile(SpawnDecision decision) {
        int[][] newGrid = copyGrid(grid);
        newGrid[decision.x()][decision.y()] = decision.value();
        return new Board(newGrid);
    }

    public Board transpose() {
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[j][i] = grid[i][j];
            }
        }
        return new Board(result);
    }

    public Board reverseRows() {
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = grid[i][n - j - 1];
            }
        }
        return new Board(result);
    }

    public Board applyTransformation(Move move) {
        return switch (move) {
            case LEFT -> this;
            case RIGHT -> reverseRows();
            case UP -> transpose();
            case DOWN -> transpose().reverseRows();
        };
    }

    public Board applyInverseTransformation(Move move) {
        return switch (move) {
            case LEFT -> this;
            case RIGHT -> reverseRows();
            case UP -> transpose();
            case DOWN -> reverseRows().transpose();
        };
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Board board)) return false;
        return Arrays.deepEquals(this.grid, board.grid);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(grid);
    }

    private int[][] copyGrid(int[][] source) {
        int[][] result = new int[source.length][source.length];
        int n = source.length;
        for (int i = 0; i < source.length; i++) {
            if (n != source[i].length) {
                throw new IllegalArgumentException("Non-square grid");
            }
            System.arraycopy(source[i], 0, result[i], 0, source.length);
        }
        return result;
    }
}
