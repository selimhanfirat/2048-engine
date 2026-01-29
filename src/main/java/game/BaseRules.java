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

            // track the leftmost index where we can move our tile
            int movableIndex = 0;

            if (src[0].getValue() > 0) movableIndex = 1;

            // we loop over each tile in the column
            for (int j = 0; j < n; j++) {

                // if we find a nonempty tile
                if (src[j].getValue() > 0) {

                    // but it is mergeable
                    if (src[movableIndex].getValue() == src[j].getValue()) {
                        src[movableIndex] = src[j].merge(src[movableIndex]); // merge it
                        scoreGained += src[movableIndex].getValue(); // get the score gained value
                    } else {
                        src[movableIndex] = src[j]; // put the value into the empty tile
                    }
                    src[j] = new Tile(0); // empty the old tile regardless we merge or not
                    movableIndex = j; // set the moveable index so that we know the next empty spot
                }
            }
        }

        return new MoveResult(newBoard, scoreGained);

    }

}
