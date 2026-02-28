package app.dto;

public record ExperimentResult(
        String label,
        ExperimentSpec spec,
        int n,

        // Performance
        double meanScore,
        int bestScore,
        double meanSteps,
        double meanMaxTile,
        int bestMaxTile,
        double reached2048Pct,
        int reached2048Count,

        // Timing (seconds)
        double totalWallSec,
        double avgWallSec,
        boolean cpuAvailableForAll,
        double totalCpuSec,
        double avgCpuSec,

        // Search stats (optional)
        boolean hasSearchStats,
        double nodesPerSec,
        double avgOutcomes
) {}