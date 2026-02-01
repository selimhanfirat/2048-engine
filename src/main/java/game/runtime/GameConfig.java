package game.runtime;

import game.rules.Rules;
import game.spawn.Spawner;

public record GameConfig(
        int gridSize,
        Rules rules,
        Spawner spawner,
        long seed
) {}
