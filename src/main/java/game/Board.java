package game;

import java.util.Arrays;

public class Board {

    private Tile[][] grid;
    private double bitMask;

    public Board() {
        this(4);
    }

    public Board(int n) {
        this(new int[n][n]);
    }

    public Board(Tile[][] grid) {
        this.grid = grid;
    }

    public Board(int[][] grid) {
        Tile[][] newGrid = new Tile[grid.length][grid[0].length];
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                newGrid[i][j] = new Tile(grid[i][j]);
            }
        }
        this.grid = newGrid;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Board otherBoard) {
            if (this.getDimension() != otherBoard.getDimension()) {
                return false;
            }
            for (int i = 0; i < this.getDimension(); i++) {
                for (int j = 0; j < this.getDimension(); j++) {
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
        Tile[][] newGrid = new Tile[this.getDimension()][this.getDimension()];
        for (int i = 0; i < this.getDimension(); i++) {
            for (int j = 0; j < this.getDimension(); j++) {
                newGrid[j][i] = this.grid[i][j];
            }
        }
        return new Board(newGrid);
    }

    public Board reverseRows() {
        Tile[][] newGrid = new Tile[this.getDimension()][this.getDimension()];
        for (int i = 0; i < this.getDimension(); i++) {
            for (int j = 0; j < this.getDimension(); j++) {
                newGrid[i][j] = this.grid[i][this.getDimension() - j - 1];
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
        return grid.length;
    }

    public double getBitMask() {
        return bitMask;
    }

    public void setBitMask(double bitMask) {
        this.bitMask = bitMask;
    }

    public Tile[][] getGrid() {
        return grid;
    }

    public void setGrid(Tile[][] grid) {
        this.grid = grid;
    }

}
