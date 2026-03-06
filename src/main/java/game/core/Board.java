package game.core;


import java.util.Arrays;

/**
 * Immutable NxN board, stored as a flat array for performance.
 * Index: cells[r * n + c]
 */
public final class Board {

    private static long getGridCalls = 0;

    private final int n;
    private final int[] cells; // length = n*n

    // cached hash
    private int cachedHash;
    private boolean hashComputed = false;

    /* =========================
       Constructors / Factories
       ========================= */

    public Board() {
        this(4);
    }

    public Board(int dimension) {
        if (dimension <= 0) throw new IllegalArgumentException("Dimension must be positive");
        this.n = dimension;
        this.cells = new int[n * n];
    }

    /**
     * Escape hatch: takes ownership of a trusted flat array (no copy).
     * Caller must guarantee it will never be mutated again.
     */
    public static Board wrapTrustedCells(int n, int[] trustedCells) {
        if (n <= 0) throw new IllegalArgumentException("n must be positive");
        if (trustedCells == null || trustedCells.length != n * n) {
            throw new IllegalArgumentException("trustedCells length must be n*n");
        }
        return new Board(n, trustedCells);
    }

    private Board(int n, int[] trustedCells) {
        this.n = n;
        this.cells = trustedCells;
    }

    public int getDimension() {
        return n;
    }

    public int get(int r, int c) {
        return cells[r * n + c];
    }

    /** Mainly for UI/testing; creates a defensive 2D copy. */
    public int[][] getGrid() {
        getGridCalls++;
        int[][] out = new int[n][n];
        for (int r = 0; r < n; r++) {
            System.arraycopy(cells, r * n, out[r], 0, n);
        }
        return out;
    }

    public int getMaxTile() {
        int max = 0;
        for (int v : cells) {
            if (v > max) max = v;
        }
        return max;
    }

    /** Returns empty cells as flattened indices (r * n + c). */
    public int[] getEmptyCells() {
        int count = 0;
        for (int v : cells) {
            if (v == 0) count++;
        }

        int[] empty = new int[count];
        int idx = 0;
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] == 0) empty[idx++] = i;
        }
        return empty;
    }

    public Board placeTile(int cellIndex, int value) {
        if (cellIndex < 0 || cellIndex >= n * n) {
            throw new IndexOutOfBoundsException("cellIndex out of range: " + cellIndex);
        }
        int[] copy = Arrays.copyOf(cells, cells.length);
        copy[cellIndex] = value;
        return Board.wrapTrustedCells(n, copy);
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Board b)) return false;
        return this.n == b.n && Arrays.equals(this.cells, b.cells);
    }

    @Override
    public int hashCode() {
        if (!hashComputed) {
            cachedHash = 31 * n + Arrays.hashCode(cells);
            hashComputed = true;
        }
        return cachedHash;
    }
}