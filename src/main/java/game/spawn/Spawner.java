package game.spawn;

import game.core.Board;

import java.util.Random;

public interface Spawner {

    // given a board, this method spawns a tile in the board and returns the board.
    public Board spawn(Board board, Random random);
}
