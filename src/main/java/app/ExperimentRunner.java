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
    private long lastTotalNodes = 0;
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

    public List<SessionResult> run() {
        List<SessionResult> results = new ArrayList<>(runs);

        lastHasSearchStats = (player instanceof ExpectimaxPlayer);
        lastTotalNodes = 0;
        lastTotalEvalCalls = 0;
        lastTotalChanceNodes = 0;
        lastTotalChanceOutcomes = 0;
        lastTotalSearchNanos = 0;

        if (player instanceof ExpectimaxPlayer e) {
            e.resetStats();
        }

        for (int i = 0; i < runs; i++) {
            long seed = baseSeed + i;

            GameSession session = new GameSession(config, seed);
            results.add(session.runGame(player));

            // pull per-game stats from expectimax and aggregate
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
            if (count % 5 == 0) {
                System.out.println("Run " + count + " of " + runs + " is complete");

                if (lastHasSearchStats) {
                    double sec = lastTotalSearchNanos / 1_000_000_000.0;
                    double nps = sec > 0 ? lastTotalNodes / sec : 0.0;
                    double avgOutcomes = lastTotalChanceNodes > 0
                            ? lastTotalChanceOutcomes / (double) lastTotalChanceNodes
                            : 0.0;

                    System.out.printf("  Search so far: nodes/sec=%.0f, avgOutcomes=%.2f%n", nps, avgOutcomes);
                }
            }
        }

        return results;
    }

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
        System.out.printf("  Avg wall per run  : %.6f s%n", avgWallSec);

        if (cpuAvailableForAll) {
            double totalCpuSec = totalCpuNanos / 1_000_000_000.0;
            double avgCpuSec = totalCpuSec / n;
            System.out.printf("  Total CPU time  : %.3f s%n", totalCpuSec);
            System.out.printf("  Avg CPU per run   : %.6f s%n", avgCpuSec);
        } else {
            System.out.println("  CPU time        : unavailable");
        }

        if (lastHasSearchStats) {
            double searchSec = lastTotalSearchNanos / 1_000_000_000.0;
            double nodesPerSec = searchSec > 0 ? lastTotalNodes / searchSec : 0.0;
            double avgOutcomes = lastTotalChanceNodes > 0
                    ? lastTotalChanceOutcomes / (double) lastTotalChanceNodes
                    : 0.0;

            double avgNodesPerMove = totalSteps > 0
                    ? lastTotalNodes / (double) totalSteps
                    : 0.0;

            double avgMsPerMove = totalSteps > 0
                    ? (lastTotalSearchNanos / 1_000_000.0) / totalSteps
                    : 0.0;

            System.out.println();
            System.out.println("Search");
            System.out.printf("  Total search time   : %.3f s%n", searchSec);
            System.out.printf("  Nodes/sec           : %.0f%n", nodesPerSec);
            System.out.printf("  Avg nodes per move  : %.0f%n", avgNodesPerMove);
            System.out.printf("  Avg ms per move     : %.3f ms%n", avgMsPerMove);
            System.out.printf("  Avg outcomes/chance : %.2f%n", avgOutcomes);
            System.out.printf("  Eval calls          : %d%n", lastTotalEvalCalls);
        }
    }
}