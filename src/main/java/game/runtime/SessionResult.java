package game.runtime;

import game.core.Move;

import java.util.HashMap;
import java.util.Map;

public record SessionResult(long seed, int finalScore, int steps, int maxTile, boolean reached2048, Map<Move, Integer> moveCounts) {
}
