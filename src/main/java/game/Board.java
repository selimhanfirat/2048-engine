package game;

import java.util.*;

public class Board {

    private final Tile[][] grid;
    private final int[][] intGrid;
    int n;
    private double bitMask;

    public Board() {
        this(4);
    }

    public Board(int n) {
        this(new int[n][n]);
    }

    public Board(int[][] grid) {
        Tile[][] newGrid = new Tile[grid.length][grid[0].length];
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                newGrid[i][j] = new Tile(grid[i][j]);
            }
        }
        this.n = grid.length;
        this.intGrid = grid;
        this.grid = newGrid;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Board otherBoard) {
            if (this.n != otherBoard.n) {
                return false;
            }
            for (int i = 0; i < this.n; i++) {
                for (int j = 0; j < this.n; j++) {
                    if (!grid[i][j].equals(otherBoard.grid[i][j])) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(grid);
    }

    public Board transpose() {
        int[][] newGrid = new int[this.n][this.n];
        for (int i = 0; i < this.n; i++) {
            for (int j = 0; j < this.n; j++) {
                newGrid[j][i] = this.intGrid[j][i];
            }
        }
        return new Board(newGrid);
    }

    public Board reverseRows() {
        int[][] newGrid = new int[this.n][this.n];
        for (int i = 0; i < this.n; i++) {
            for (int j = 0; j < this.n; j++) {
                newGrid[i][j] = this.intGrid[i][this.n - j - 1];
            }
        }
        return new Board(newGrid);
    }

    public Board applyTransformation(Move move, boolean inverse) {
        if (!inverse) {
            switch (move) {
                case LEFT -> { return this; }
                case RIGHT -> { return this.reverseRows(); }
                case UP -> { return this.transpose(); }
                case DOWN -> { return this.transpose().reverseRows(); }
            }
        } else {
            switch (move) {
                case LEFT -> { return this; }
                case RIGHT -> { return this.reverseRows(); }
                case UP -> { return this.transpose(); }
                case DOWN -> { return this.reverseRows().transpose(); }
            }
        }
        throw new IllegalStateException("Unhandled move: " + move);
    }
    public Board applyTransformation(Move move) {
        return applyTransformation(move, false);
    }


    public int getDimension() {
        return this.n;
    }

    public double getBitMask() {
        return bitMask;
    }

    public int[][] getGrid() {
        int[][] newGrid = new int[this.n][this.n];
        for (int i = 0; i < this.n; i++) {
            System.arraycopy(this.intGrid[i], 0, newGrid[i], 0, this.n);
        }
        return newGrid;
    }

    public int[] getEmptyCells() {
        List<Integer> emptyCells = new ArrayList<Integer>();
        for (int i = 0; i < this.n; i++) {
            for (int j = 0; j < this.n; j++) {
                if (this.intGrid[i][j] == 0) {
                    emptyCells.add(i * n + 1);
                }
            }
        }
        return emptyCells.stream().mapToInt(i -> i).toArray();
    }
    

}
