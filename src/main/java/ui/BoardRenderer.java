package ui;

import game.core.Board;

public final class BoardRenderer {

    private BoardRenderer() {} // no instances

    /** Pretty unicode box table. */
    public static String pretty(Board board) {
        int[][] g = board.getGrid();
        int n = g.length;

        int width = cellWidth(g);
        String top    = border('┌', '┬', '┐', n, width);
        String mid    = border('├', '┼', '┤', n, width);
        String bottom = border('└', '┴', '┘', n, width);

        StringBuilder sb = new StringBuilder();
        sb.append(top).append('\n');
        for (int i = 0; i < n; i++) {
            sb.append(rowLine(g, i, n, width)).append('\n');
            if (i != n - 1) sb.append(mid).append('\n');
        }
        sb.append(bottom);
        return sb.toString();
    }

    /** Convenience printing helper. */
    public static void print(Board board) {
        System.out.println(pretty(board));
    }

    /** Minimal renderer (no unicode), good for logs/tests. */
    public static String compact(Board board) {
        return java.util.Arrays.deepToString(board.getGrid());
    }

    private static int cellWidth(int[][] g) {
        int max = 0;
        for (int[] row : g) {
            for (int v : row) max = Math.max(max, v);
        }
        int digits = (max <= 0) ? 1 : String.valueOf(max).length();
        return Math.max(3, digits + 2); // looks nice: includes some breathing room
    }

    private static String border(char left, char mid, char right, int n, int width) {
        StringBuilder sb = new StringBuilder();
        sb.append(left);
        for (int j = 0; j < n; j++) {
            sb.append("─".repeat(width + 2)); // spaces around content
            sb.append(j == n - 1 ? right : mid);
        }
        return sb.toString();
    }

    private static String rowLine(int[][] g, int i, int n, int width) {
        StringBuilder sb = new StringBuilder();
        sb.append('│');
        for (int j = 0; j < n; j++) {
            int v = g[i][j];
            String content = (v == 0) ? "." : String.valueOf(v);
            sb.append(' ')
                    .append(padLeft(content, width))
                    .append(' ')
                    .append('│');
        }
        return sb.toString();
    }

    private static String padLeft(String s, int width) {
        if (s.length() >= width) return s;
        return " ".repeat(width - s.length()) + s;
    }
}
