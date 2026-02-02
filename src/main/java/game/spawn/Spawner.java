package game.spawn;

import game.core.Board;
import game.util.Rng;

public interface Spawner {
    Board spawn(Board board, Rng rng);
}
