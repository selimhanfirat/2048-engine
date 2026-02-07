package game.rules;

import game.core.Board;

/**
 * Package-private test utilities for ClassicRules2048 tests.
 * Centralizes all Board <-> matrix interaction so tests do NOT
 * depend on Board internals. If Board changes, only this file
 * should need edits.
 */
final class RulesTestHelper {

    private RulesTestHelper() {
        // no instances
    }

    /* =========================================================
       Board construction
       ========================================================= */

    static Board boardOf(int[][] values) {
        requireSquare(values);
        return new Board(values);
    }

    static int[][] toSquare(int[] flat, int n) {
        int[][] grid = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                grid[i][j] = flat[i * n + j];        // row-major mapping
            }
        }
        return grid;
    }

    /* =========================================================
       Board inspection
       ========================================================= */

    static int[][] toMatrix(Board board) {
        int n = board.getDimension();
        int[][] m = new int[n][n];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                m[r][c] = board.get(r, c);
            }
        }
        return m;
    }

    static boolean boardsEqual(Board a, Board b) {
        if (a.getDimension() != b.getDimension()) return false;
        int n = a.getDimension();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (a.get(r, c) != b.get(r, c)) return false;
            }
        }
        return true;
    }

    /* =========================================================
       Assertions helpers
       ========================================================= */

    static boolean matrixEquals(Board board, int[][] expected) {
        requireSquare(expected);
        int n = board.getDimension();
        if (n != expected.length) return false;

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (board.get(r, c) != expected[r][c]) return false;
            }
        }
        return true;
    }

    /* =========================================================
       Validation
       ========================================================= */

    static void requireSquare(int[][] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Grid must be non-null and non-empty");
        }
        int n = values.length;
        for (int i = 0; i < n; i++) {
            if (values[i] == null || values[i].length != n) {
                throw new IllegalArgumentException("Grid must be square");
            }
        }
    }
}
