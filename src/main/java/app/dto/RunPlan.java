package app.dto;

public record RunPlan(
        int runs,
        long baseSeed,
        double warmupFraction,
        int checkpoints
) {
    public int warmupRuns() {
        return (int) Math.round(runs * warmupFraction);
    }

    public int checkpointEvery() {
        if (checkpoints <= 0) return Integer.MAX_VALUE; // effectively never
        return Math.max(1, runs / checkpoints);
    }
}