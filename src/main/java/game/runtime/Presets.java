package game.runtime;

import game.spawn.ClassicSpawner2048;
import game.spawn.Spawner;
import game.rules.ClassicRules2048;
import game.rules.Rules;

import java.util.Random;

public final class Presets {

    private Presets() {
        // utility class — no instances
    }

    /**
     * Standard / Classic 2048.
     * - 4x4 grid
     * - 90% chance of spawning a 2, 10% a 4
     * - classic rules
     * - random spawner
     */
    public static GameConfig standard2048() {
        return standard2048(new Random());
    }

    /**
     * Deterministic Standard / Classic 2048.
     * Same as standard2048(), but reproducible via seed.
     */
    public static GameConfig standard2048(long seed) {
        return standard2048(new Random(seed));
    }

    private static GameConfig standard2048(Random rng) {
        int gridSize = 4;
        double p = 0.9;

        Rules rules = new ClassicRules2048();
        Spawner spawner = new ClassicSpawner2048(p);

        return new GameConfig(
                gridSize,
                rules,
                spawner,
                rng
        );
    }
}
