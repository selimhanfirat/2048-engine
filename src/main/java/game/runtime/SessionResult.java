package game.runtime;

public record SessionResult(
        long seed,
        int finalScore,
        int steps,
        int maxTile,
        boolean reached2048,
        long wallTimeNanos,
        long cpuTimeNanos
) {}
