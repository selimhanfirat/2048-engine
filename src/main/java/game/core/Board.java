package game.core;

import game.util.Coordinate;

import java.util.Arrays;

public final class Board {

    private final int[][] grid;
    private final int n;
    private static long getGridCalls = 0;
    private int cachedHash;
    private boolean hashComputed = false;

    public Board() {
        this(4);
    }

    public Board(int dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension must be positive");
        }
        this.n = dimension;
        this.grid = new int[dimension][dimension];
    }

    public Board(int[][] grid) {
        if (grid == null || grid.length <= 0) {
            throw new IllegalArgumentException("cannot have a 0x0 array");
        }

        int size = grid.length;
        for (int i = 0; i < size; i++) {
            if (grid[i] == null || grid[i].length != size) {
                throw new IllegalArgumentException("Non-square grid");
            }
        }

        this.n = size;
        this.grid = copyGrid(grid); // defensive copy (public constructor)
    }

    // Internal constructor that assumes grid ownership (no defensive copy).
    private Board(int[][] trustedGrid, int dimension) {
        this.n = dimension;
        this.grid = trustedGrid;
    }

    public int getDimension() {
        return n;
    }

    // Fast accessor for hot paths (evaluators/search)
    public int get(int r, int c) {
        return grid[r][c];
    }

    // Defensive copy for external callers
    public int[][] getGrid() {
        getGridCalls++;
        return copyGrid(grid);
    }

    public int[] getEmptyCells() {
        int count = 0;
        for (int i = 0; i < n; i++) {
            int[] row = grid[i];
            for (int j = 0; j < n; j++) {
                if (row[j] == 0) count++;
            }
        }

        int[] emptyCells = new int[count];
        int idx = 0;

        for (int i = 0; i < n; i++) {
            int[] row = grid[i];
            int base = i * n;
            for (int j = 0; j < n; j++) {
                if (row[j] == 0) {
                    emptyCells[idx++] = base + j;
                }
            }
        }
        return emptyCells;
    }

    public Board placeTile(Coordinate coordinate, int value) {
        int[][] newGrid = copyGrid(grid);
        newGrid[coordinate.x()][coordinate.y()] = value;
        return new Board(newGrid); // keep defensive copy semantics via public ctor
    }

    @Override
    public int hashCode() {
        if (!hashComputed) {
            cachedHash = Arrays.deepHashCode(grid);
            hashComputed = true;
        }
        return cachedHash;
    }

    public int getMaxTile() {
        int maxTile = 0;
        for (int i = 0; i < n; i++) {
            int[] row = grid[i];
            for (int j = 0; j < n; j++) {
                int v = row[j];
                if (v > maxTile) maxTile = v;
            }
        }
        return maxTile;
    }

    public Board transpose() {
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            int[] row = grid[i];
            for (int j = 0; j < n; j++) {
                result[j][i] = row[j];
            }
        }
        return new Board(result, n); // trusted, no extra copy
    }

    public Board reverseRows() {
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            int[] row = grid[i];
            int[] out = result[i];
            for (int j = 0; j < n; j++) {
                out[j] = row[n - 1 - j];
            }
        }
        return new Board(result, n); // trusted, no extra copy
    }

    // These transformations are now single-pass (no intermediate Board allocations).
    public Board applyTransformation(Move move) {
        return switch (move) {
            case LEFT -> this;
            case RIGHT -> reverseRows(); // already optimized
            case UP -> transpose();      // already optimized
            case DOWN -> transformDown();
        };
    }

    public Board applyInverseTransformation(Move move) {
        return switch (move) {
            case LEFT -> this;
            case RIGHT -> reverseRows();
            case UP -> transpose();
            case DOWN -> inverseTransformDown();
        };
    }

    // applyTransformation(DOWN) == transpose().reverseRows()
    // result[i][j] = grid[n - 1 - j][i]
    private Board transformDown() {
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            int[] out = result[i];
            for (int j = 0; j < n; j++) {
                out[j] = grid[n - 1 - j][i];
            }
        }
        return new Board(result, n);
    }

    // applyInverseTransformation(DOWN) == reverseRows().transpose()
    // result[i][j] = grid[j][n - 1 - i]
    private Board inverseTransformDown() {
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            int[] out = result[i];
            for (int j = 0; j < n; j++) {
                out[j] = grid[j][n - 1 - i];
            }
        }
        return new Board(result, n);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Board board)) return false;
        return Arrays.deepEquals(this.grid, board.grid);
    }

    private static int[][] copyGrid(int[][] source) {
        int n = source.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(source[i], 0, result[i], 0, n);
        }
        return result;
    }

    public static void printGetGridStats() {
        System.out.println("Board.getGrid() calls = " + getGridCalls);
    }

    public static void resetGetGridStats() {
        getGridCalls = 0;
    }
}
