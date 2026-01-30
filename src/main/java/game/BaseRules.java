package game;

public class BaseRules implements Rules {

    public BaseRules() {}

    // given a board, detect if game over
    public boolean isGameOver(Board board) {
        for (Move move : Move.values()) {
            if (isMovePossible(board, move)) {
                return false;
            }
        }
        return true;
    }

    // given a move and a board check if it is possible
    public boolean isMovePossible(Board board, Move move) {
        board = board.applyTransformation(move); // transform the board according to the board

        // [2, 0, 0, 2]

        /*
        A move is only possible if there exists a row in which where
        there exists a zero that is to the left of a nonzero tile
        or there exists a merge
         */
        for (int i = 0; i < board.getDimension(); i++) {
            Tile[] row = board.getGrid()[i];
            boolean mergingExists = false; // flag if we can merge any two tiles at any row

            int lastMergingValue = 0; // value of the last nonZero tile we saw so we can merge
            boolean sawZero = false;
            boolean containsFillableZero = false;

            for (int j = 0; j < board.getDimension(); j++) {
                Tile tile = row[j];
                if (tile.isEmpty()) {
                    sawZero = true;
                } else if (tile.getValue() == lastMergingValue) {
                    mergingExists = true;
                    containsFillableZero = sawZero;
                } else {
                    lastMergingValue = tile.getValue();
                }
                if (containsFillableZero || mergingExists) {
                    return true;
                }
            }

        }
        return false;
    }

    // make a move and return the new board
    public MoveResult makeMove(Board board, Move move) {

        // depending on the move type we either/or reverse, transpose the array so that any move is equivalent to a left move
        board = board.applyTransformation(move);
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
                if (!src[j].isEmpty()) {

                    // if target slot is empty, just place
                    if (dst[movableIndex].isEmpty()) {
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

        newBoard = newBoard.applyTransformation(move, true);


        return new MoveResult(newBoard, scoreGained);

    }

}
