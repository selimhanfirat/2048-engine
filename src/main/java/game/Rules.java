package game;

public interface Rules {

    // given a board, detect if game over
    boolean isGameOver(Board board);

    // given a move and a board check if it is possible
    boolean isMovePossible(Board board, Move move);

    // apply a move and return the result
    MoveResult makeMove(Board board, Move move);
}
