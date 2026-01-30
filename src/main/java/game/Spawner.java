package game;

import java.util.Random;

public interface Spawner {

    // given a board, this method should decide a position to create a tile, and return a new board with it
    public SpawnDecision addRandomTile(Board board, Random random);
}
