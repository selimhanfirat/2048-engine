package game;

public class BaseRules implements Rules {

    public BaseRules() {}

    // given a board, detect if game over
    public boolean gameOver(Board board) {
        return false;
    }

    // given a move and a board check if it is possible
    public boolean isMovePossible(Board board, Move move) {
        return true;
    }

    // make a move and return the new board
    public MoveResult makeMove(Board board, Move move) {
        if (!isMovePossible(board, move)) {
            throw new IllegalArgumentException("Move Not Possible");
        }

        // depending on the move type we either/or reverse, transpose the array so that any move is equivalent to a left move
        if (move.equals(Move.UP)) {
            board = board.reverseRows().transpose();
        } else if (move.equals(Move.DOWN)) {
            board = board.transpose();
        } else if (move.equals(Move.RIGHT)) {
            board = board.reverseRows();
        }

        int n = board.getDimension();

        // create a new board so we are immutable
        Board newBoard = new Board(n);

        int scoreGained = 0;

        // every row is independent, we loop over each of them.
        for (int i = 0; i < n; i++) {
            Tile[] src = board.getGrid()[i];
            Tile[] dst = newBoard.getGrid()[i];

            int movableIndex = 0;

            for (int j = 0; j < n; j++) {
                if (src[j].getValue() > 0) {

                    // if target slot is empty, just place
                    if (dst[movableIndex].getValue() == 0) {
                        dst[movableIndex] = src[j];
                    }
                    // if mergeable, merge into dst
                    else if (dst[movableIndex].getValue() == src[j].getValue()) {
                        dst[movableIndex] = src[j].merge(dst[movableIndex]);
                        scoreGained += dst[movableIndex].getValue();
                        movableIndex++; // move past merged tile
                    }
                    // otherwise move to next slot
                    else {
                        movableIndex++;
                        dst[movableIndex] = src[j];
                    }
                }
            }
        }


        // finally we revert back the array
        if (move.equals(Move.UP)) {
            newBoard = newBoard.transpose().reverseRows(); // reverse reversing rows and transposing
        } else if (move.equals(Move.DOWN)) {
            newBoard = newBoard.transpose(); // reverse transposing
        } else if (move.equals(Move.RIGHT)) {
            newBoard = newBoard.reverseRows(); // reverse reversing rows
        }

        return new MoveResult(newBoard, scoreGained);

    }

}
