package game.core;

import game.util.Coordinate;

import java.util.Arrays;

/**
 * Immutable 2D board.*
 * Notes:
 * - Public constructors defensively copy input grids.
 * - wrapTrustedGrid(...) is an escape hatch for performance: it takes ownership of the provided grid.
 */
public final class Board {

    private static long getGridCalls = 0;

    private final int[][] grid;
    private final int n;

    // Hash is cached because Board is immutable
    private int cachedHash;
    private boolean hashComputed = false;

    /* =========================
       Constructors / Factories
       ========================= */

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

    /**
     * Public constructor: defensively copies the provided grid.
     */
    public Board(int[][] grid) {
        validateSquareGrid(grid);
        this.n = grid.length;
        this.grid = copyGrid(grid);
    }

    /**
     * Performance escape hatch: takes ownership of the given grid (no defensive copy).
     * Use ONLY when the caller guarantees the array will never be mutated again.
     */
    public static Board wrapTrustedGrid(int[][] trustedGrid) {
        validateSquareGrid(trustedGrid);
        return new Board(trustedGrid, trustedGrid.length);
    }

    /**
     * Internal constructor: assumes grid ownership (no defensive copy).
     */
    private Board(int[][] trustedGrid, int dimension) {
        this.n = dimension;
        this.grid = trustedGrid;
    }

    /* =========================
       Basic accessors
       ========================= */

    public int getDimension() {
        return n;
    }

    /** Fast accessor for hot paths (evaluators/search). */
    public int get(int r, int c) {
        return grid[r][c];
    }

    /** Defensive copy for external callers. */
    public int[][] getGrid() {
        getGridCalls++;
        return copyGrid(grid);
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

    /**
     * Returns empty cells as flattened indices (r * n + c).
     */
    public int[] getEmptyCells() {
        int count = 0;
        for (int i = 0; i < n; i++) {
            int[] row = grid[i];
            for (int j = 0; j < n; j++) {
                if (row[j] == 0) count++;
            }
        }

        int[] empty = new int[count];
        int idx = 0;

        for (int i = 0; i < n; i++) {
            int[] row = grid[i];
            int base = i * n;
            for (int j = 0; j < n; j++) {
                if (row[j] == 0) {
                    empty[idx++] = base + j;
                }
            }
        }
        return empty;
    }

    public Board placeTile(Coordinate coordinate, int value) {
        int[][] newGrid = copyGrid(grid);
        newGrid[coordinate.x()][coordinate.y()] = value;
        return new Board(newGrid, n);
    }

    /* =========================
       equals / hashCode
       ========================= */

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Board board)) return false;
        return Arrays.deepEquals(this.grid, board.grid);
    }

    @Override
    public int hashCode() {
        if (!hashComputed) {
            cachedHash = Arrays.deepHashCode(grid);
            hashComputed = true;
        }
        return cachedHash;
    }

    /* =========================
       Helpers
       ========================= */

    private static void validateSquareGrid(int[][] grid) {
        if (grid == null || grid.length == 0) {
            throw new IllegalArgumentException("cannot have a 0x0 array");
        }
        int n = grid.length;
        for (int i = 0; i < n; i++) {
            if (grid[i] == null || grid[i].length != n) {
                throw new IllegalArgumentException("Non-square grid");
            }
        }
    }

    private static int[][] copyGrid(int[][] source) {
        int n = source.length;
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(source[i], 0, result[i], 0, n);
        }
        return result;
    }
}
