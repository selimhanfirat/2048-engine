package app;

import ai.Player;
import ai.ExpectimaxPlayer;
import game.runtime.GameConfig;
import game.runtime.GameSession;
import game.runtime.SessionResult;

import java.util.ArrayList;
import java.util.List;

public final class ExperimentRunner {

    private final GameConfig config;
    private final int runs;
    private final long baseSeed;
    private final Player player;

    // ---- aggregated search stats from the last run() ----
    private boolean lastHasSearchStats = false;
    private long lastTotalEvalCalls = 0;
    private long lastTotalChanceNodes = 0;
    private long lastTotalChanceOutcomes = 0;
    private long lastTotalSearchNanos = 0;

    public ExperimentRunner(
            GameConfig config,
            int runs,
            long baseSeed,
            Player player
    ) {
        this.config = config;
        this.runs = runs;
        this.baseSeed = baseSeed;
        this.player = player;
    }

    /* =========================
       Batch mode
       ========================= */

    public List<SessionResult> run() {
        List<SessionResult> results = new ArrayList<>(runs);

        lastHasSearchStats = (player instanceof ExpectimaxPlayer);
        long lastTotalNodes = 0;
        lastTotalEvalCalls = 0;
        lastTotalChanceNodes = 0;
        lastTotalChanceOutcomes = 0;
        lastTotalSearchNanos = 0;

        if (player instanceof ExpectimaxPlayer e) {
            e.resetStats();
        }

        int progressUpdateFrequency = 5;
        int progressStep = Math.max(1, runs / progressUpdateFrequency);

        for (int i = 0; i < runs; i++) {
            long seed = baseSeed + i;

            GameSession session = new GameSession(config, seed);
            results.add(session.runGame(player));

            if (player instanceof ExpectimaxPlayer e) {
                ExpectimaxPlayer.SearchStats s = e.getStats();

                lastTotalNodes += s.nodes();
                lastTotalEvalCalls += s.evalCalls();
                lastTotalChanceNodes += s.chanceNodes();
                lastTotalChanceOutcomes += s.chanceOutcomes();
                lastTotalSearchNanos += s.searchNanos();

                e.resetStats();
            }

            int count = i + 1;

            if (count % progressStep == 0 || count == runs) {
                int percent = (int) ((100.0 * count) / runs);
                System.out.println("Progress: " + percent + "% (" + count + " / " + runs + ")");
                if (lastHasSearchStats) {
                    double sec = lastTotalSearchNanos / 1_000_000_000.0;
                    double nps = sec > 0 ? lastTotalNodes / sec : 0.0;
                    double avgOutcomes = lastTotalChanceNodes > 0
                            ? lastTotalChanceOutcomes / (double) lastTotalChanceNodes
                            : 0.0;

                    System.out.printf("  Search so far: nodes/sec=%.0f, avgOutcomes=%.2f%n",
                            nps, avgOutcomes);
                }
            }
        }

        return results;
    }

    /* =========================
       Interactive mode
       ========================= */

    public void runInteractiveOnce() {
        System.out.println();
        System.out.println("=== Interactive Run ===");

        GameSession session = new GameSession(config, baseSeed);
        SessionResult result = session.runGameInteractive(player);

        System.out.println();
        System.out.println("=== Final Result ===");
        System.out.println("Score     : " + result.finalScore());
        System.out.println("Steps     : " + result.steps());
        System.out.println("Max tile  : " + result.maxTile());
        System.out.println("Reached 2048: " + result.reached2048());
        System.out.printf("Wall time : %.3f s%n",
                result.wallTimeNanos() / 1_000_000_000.0);

        if (result.cpuTimeNanos() >= 0) {
            System.out.printf("CPU time  : %.3f s%n",
                    result.cpuTimeNanos() / 1_000_000_000.0);
        }
    }

    /* =========================
       Report (batch only)
       ========================= */

    public void report(List<SessionResult> results) {
        String playerType = player.getClass().getSimpleName();
        int n = results.size();

        long totalScore = 0;
        long totalSteps = 0;
        long totalMaxTile = 0;

        int reached2048 = 0;
        int bestScore = Integer.MIN_VALUE;
        int bestMaxTile = Integer.MIN_VALUE;

        long totalWallNanos = 0;
        long totalCpuNanos = 0;
        boolean cpuAvailableForAll = true;

        for (SessionResult r : results) {
            totalScore += r.finalScore();
            totalSteps += r.steps();
            totalMaxTile += r.maxTile();

            if (r.reached2048()) reached2048++;

            bestScore = Math.max(bestScore, r.finalScore());
            bestMaxTile = Math.max(bestMaxTile, r.maxTile());

            totalWallNanos += r.wallTimeNanos();

            if (r.cpuTimeNanos() >= 0) {
                totalCpuNanos += r.cpuTimeNanos();
            } else {
                cpuAvailableForAll = false;
            }
        }

        double avgScore = totalScore / (double) n;
        double avgSteps = totalSteps / (double) n;
        double avgMaxTile = totalMaxTile / (double) n;
        double reachRate = 100.0 * reached2048 / n;

        double totalWallSec = totalWallNanos / 1_000_000_000.0;
        double avgWallSec = totalWallSec / n;

        System.out.println("Experiment report");
        System.out.println("-----------------");
        System.out.println("Runs              : " + n);
        System.out.println("Player            : " + playerType);

        System.out.println();
        System.out.println("Performance");
        System.out.printf("  Average score   : %.2f%n", avgScore);
        System.out.printf("  Best score      : %d%n", bestScore);
        System.out.printf("  Average steps   : %.2f%n", avgSteps);
        System.out.printf("  Average max tile: %.2f%n", avgMaxTile);
        System.out.printf("  Best max tile   : %d%n", bestMaxTile);
        System.out.printf("  Reached 2048    : %.2f %% (%d / %d)%n",
                reachRate, reached2048, n);

        System.out.println();
        System.out.println("Timing (seconds)");
        System.out.printf("  Total wall time : %.3f s%n", totalWallSec);
        System.out.printf("  Avg wall per run: %.6f s%n", avgWallSec);

        if (cpuAvailableForAll) {
            double totalCpuSec = totalCpuNanos / 1_000_000_000.0;
            double avgCpuSec = totalCpuSec / n;
            System.out.printf("  Total CPU time  : %.3f s%n", totalCpuSec);
            System.out.printf("  Avg CPU per run : %.6f s%n", avgCpuSec);
        } else {
            System.out.println("  CPU time        : unavailable");
        }
    }
}