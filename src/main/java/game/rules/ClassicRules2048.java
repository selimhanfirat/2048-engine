package game.rules;

import game.core.Board;
import game.core.Move;
import game.core.MoveResult;

import java.util.EnumSet;

public class ClassicRules2048 implements Rules {

    public ClassicRules2048() {}

    @Override
    public boolean isGameOver(Board board) {
        return getLegalMoves(board).isEmpty();
    }

    @Override
    public MoveResult makeMove(Board board, Move move) {
        // Work in "LEFT space"
        Board t = board.applyTransformation(move);
        int n = t.getDimension();

        int scoreGained = 0;
        int[][] newGrid = new int[n][n];

        for (int i = 0; i < n; i++) {
            int write = 0;
            int lastMergedAt = -1;

            for (int j = 0; j < n; j++) {
                int v = t.get(i, j);
                if (v == 0) continue;

                if (newGrid[i][write] == 0) {
                    newGrid[i][write] = v;
                } else if (newGrid[i][write] == v && lastMergedAt != write) {
                    newGrid[i][write] = v * 2;
                    scoreGained += newGrid[i][write];
                    lastMergedAt = write;
                    write++;
                } else {
                    write++;
                    newGrid[i][write] = v;
                }
            }
        }

        Board newBoard = new Board(newGrid).applyInverseTransformation(move);
        return new MoveResult(newBoard, scoreGained);
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

    // Checks if applying the move would change the board, with no allocations.
    @Override
    public boolean canMove(Board board, Move move) {
        Board t = board.applyTransformation(move);
        int n = t.getDimension();

        for (int i = 0; i < n; i++) {
            int lastNonZero = 0;
            boolean hasLast = false;
            int targetIndex = 0;

            for (int j = 0; j < n; j++) {
                int v = t.get(i, j);
                if (v == 0) continue;

                // If tile is not already packed left, it can move.
                if (j != targetIndex) return true;

                // If it can merge with previous tile, it can move.
                if (hasLast && lastNonZero == v) return true;

                // Simulate placing this tile without merging
                hasLast = true;
                lastNonZero = v;
                targetIndex++;
            }
        }

        return false;
    }
}
