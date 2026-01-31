package game.spawn;

import game.core.Board;
import game.core.SpawnDecision;

import java.util.Random;

public interface Spawner {

    // given a board, this method should decide a position to create a tile, and return a new board with it
    public SpawnDecision pickTile(Board board, Random random);
}
