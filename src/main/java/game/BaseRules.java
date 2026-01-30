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

        /*
        A move is only possible if there exists a row in which where
        there exists a zero that is to the left of a nonzero tile
        or there exists a merge
        we return early when we see a merge, or when we see a nonzero value after we see a zero value
         */
        int[][] grid = board.getGrid();
        for (int i = 0; i < board.getDimension(); i++) {
            int[] row = board.getGrid()[i];
            boolean mergingExists = false; // flag if we can merge any two tiles at any row

            int lastMergingValue = 0; // value of the last nonZero tile we saw so we can merge
            boolean sawZero = false;
            boolean containsFillableZero = false;

            for (int j = 0; j < board.getDimension(); j++) {
                int value = row[j];
                if (value == 0) {
                    sawZero = true;
                } else if (value == lastMergingValue) {
                    mergingExists = true;
                    containsFillableZero = sawZero;
                } else {
                    lastMergingValue = value;
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

        Board newBoard = new Board(newGrid).applyTransformation(move, true);
        return new MoveResult(newBoard, scoreGained);

    }

}
