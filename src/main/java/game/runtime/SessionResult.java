package game.runtime;

import game.core.Move;

import java.util.Map;

public record SessionResult(long seed, int finalScore, int moveCount, boolean reached2048, Map<Move, Integer> moveCounts) {
}
