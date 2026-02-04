package game.rules;

import game.core.Board;
import game.core.Move;
import game.core.MoveResult;

import java.util.EnumSet;

public interface Rules {

    // given a board, detect if game over
    boolean isGameOver(Board board);

    // apply a move and return the result
    MoveResult makeMove(Board board, Move move);

    EnumSet<Move> getLegalMoves(Board board);

    boolean canMove(Board board, Move move);
}
