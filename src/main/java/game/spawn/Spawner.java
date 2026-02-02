package game.spawn;

import game.core.Board;
import game.util.Rng;

public interface Spawner {
    SpawnDistribution distribution(Board board); // for AI/search
    Board sample(Board board, Rng rng);          // runtime only
}
