package game.rules;

import game.core.Board;
import game.core.Move;
import game.core.MoveResult;

import java.util.EnumSet;

public class ClassicRules2048 implements Rules {

    public ClassicRules2048() {}

    // Read board as if transformed into LEFT-space
    private int getLeftSpace(Board board, Move move, int i, int j) {
        int n = board.getDimension();
        return switch (move) {
            case LEFT  -> board.get(i, j);
            case RIGHT -> board.get(i, n - 1 - j);
            case UP    -> board.get(j, i);
            case DOWN  -> board.get(n - 1 - j, i);
        };
    }

    // Write a LEFT-space cell back into final board orientation
    private void setFromLeftSpace(int[][] out, Move move, int i, int j, int value) {
        int n = out.length;
        switch (move) {
            case LEFT -> out[i][j] = value;
            case RIGHT -> out[i][n - 1 - j] = value;
            case UP -> out[j][i] = value;
            case DOWN -> out[j][n - 1 - i] = value;
        }
    }
    // Rules implementation
    @Override
    public boolean isGameOver(Board board) {
        return getLegalMoves(board).isEmpty();
    }

    @Override
    public MoveResult makeMove(Board board, Move move) {
        int n = board.getDimension();
        int scoreGained = 0;

        // Final board grid (already in correct orientation)
        int[][] trustedOut = new int[n][n];

        // Scratch row in LEFT-space
        int[] rowOut = new int[n];

        for (int i = 0; i < n; i++) {
            // reset scratch row
            for (int k = 0; k < n; k++) rowOut[k] = 0;

            int write = 0;
            int lastMergedAt = -1;

            for (int j = 0; j < n; j++) {
                int v = getLeftSpace(board, move, i, j);
                if (v == 0) continue;

                if (rowOut[write] == 0) {
                    rowOut[write] = v;
                } else if (rowOut[write] == v && lastMergedAt != write) {
                    rowOut[write] = v * 2;
                    scoreGained += rowOut[write];
                    lastMergedAt = write;
                    write++;
                } else {
                    write++;
                    rowOut[write] = v;
                }
            }

            // write LEFT-space row into final board orientation
            for (int j = 0; j < n; j++) {
                int v = rowOut[j];
                if (v != 0) {
                    setFromLeftSpace(trustedOut, move, i, j, v);
                }
            }
        }
        return new MoveResult(Board.wrapTrustedGrid(trustedOut), scoreGained);
    }

    @Override
    public EnumSet<Move> getLegalMoves(Board board) {
        EnumSet<Move> legalMoves = EnumSet.noneOf(Move.class);
        for (Move move : Move.values()) {
            if (canMove(board, move)) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    @Override
    public boolean canMove(Board board, Move move) {
        int n = board.getDimension();

        for (int i = 0; i < n; i++) {
            int lastNonZero = 0;
            boolean hasLast = false;
            int targetIndex = 0;

            for (int j = 0; j < n; j++) {
                int v = getLeftSpace(board, move, i, j);
                if (v == 0) continue;

                if (j != targetIndex) return true;
                if (hasLast && lastNonZero == v) return true;

                hasLast = true;
                lastNonZero = v;
                targetIndex++;
            }
        }

        return false;
    }
}
