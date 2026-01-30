package game;

public interface Rules {

    // given a board, detect if game over
    boolean isGameOver(Board board);

    // apply a move and return the result
    MoveResult makeMove(Board board, Move move);
}
