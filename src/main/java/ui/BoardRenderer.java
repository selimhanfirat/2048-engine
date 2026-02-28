package ui;

import game.core.Board;

/**
 * Pretty console renderer for a 2048 board.
 * Intended for interactive/debug use (not performance-critical).
 */
public final class BoardRenderer {

    private BoardRenderer() {}

    /** Returns a pretty ASCII rendering of the board. */
    public static String render(Board board) {
        int n = board.getDimension();

        // Determine max width needed for cell contents
        int maxVal = 0;
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                maxVal = Math.max(maxVal, board.get(r, c));
            }
        }

        // Minimum width keeps small boards readable; scale for big tiles.
        int cellW = Math.max(4, String.valueOf(maxVal).length() + 1);

        String horizontal = horizontalLine(n, cellW);

        StringBuilder sb = new StringBuilder();
        sb.append(horizontal).append('\n');

        for (int r = 0; r < n; r++) {
            sb.append("|");
            for (int c = 0; c < n; c++) {
                int v = board.get(r, c);
                String s = (v == 0) ? "." : Integer.toString(v);
                sb.append(padLeft(s, cellW)).append("|");
            }
            sb.append('\n').append(horizontal).append('\n');
        }

        return sb.toString();
    }

    /** Convenience: prints directly to stdout. */
    public static void print(Board board) {
        System.out.print(render(board));
    }

    private static String horizontalLine(int n, int cellW) {
        StringBuilder sb = new StringBuilder();
        sb.append('+');
        for (int i = 0; i < n; i++) {
            sb.append("-".repeat(cellW)).append('+');
        }
        return sb.toString();
    }

    private static String padLeft(String s, int width) {
        if (s.length() >= width) return s;
        return " ".repeat(width - s.length()) + s;
    }
}