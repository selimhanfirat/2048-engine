package game.rules;

import game.core.Board;
import game.core.Move;
import game.core.MoveResult;

import java.util.EnumSet;

public class ClassicRules2048 implements Rules {

    public ClassicRules2048() {}

    // given a board, detect if game over
    public boolean isGameOver(Board board) {
        return getLegalMoves(board).isEmpty();
    }

    // make a move and return the new board
    public MoveResult makeMove(Board board, Move move) {
        // depending on the move type we either/or reverse, transpose the array so that any move is equivalent to a left move
        board = board.applyTransformation(move);
        int n = board.getDimension();


        int scoreGained = 0;
        int[][] grid = board.getGrid();
        int[][] newGrid = new int[n][n];
        // every row is independent, we loop over each of them.
        for (int i = 0; i < n; i++) {
            int movableIndex = 0;
            for (int j = 0; j < n; j++) {
                if (grid[i][j] != 0) {

                    // if target slot is empty, just place
                    if (newGrid[i][movableIndex] == 0) {
                        newGrid[i][movableIndex] = grid[i][j];
                    }
                    // if mergeable, merge into dst
                    else if (newGrid[i][movableIndex] == grid[i][j]) {
                        newGrid[i][movableIndex] = grid[i][j] * 2;
                        grid[i][j] = 0;
                        scoreGained += newGrid[i][movableIndex];
                        movableIndex++; // move past merged tile
                    }
                    // otherwise move to next slot
                    else {
                        movableIndex++;
                        newGrid[i][movableIndex] = grid[i][j];
                    }
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
            if (!this.makeMove(board, move).board().equals(board)){
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

}
