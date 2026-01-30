package game.runtime;

import game.spawn.Spawner;
import game.rules.Rules;

import java.util.Random;

public record GameConfig(
        int gridSize,
        Rules rules,
        Spawner spawner,
        Random random) {
}
