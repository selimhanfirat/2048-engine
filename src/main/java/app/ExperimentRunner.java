package app;

import ai.Player;
import game.runtime.Game;
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

        int count = 1;
        for (int i = 0; i < runs; i++) {
            long seed = baseSeed + i;

            Game game = new Game(config, seed);

            GameSession session = new GameSession(game, player);
            results.add(session.runGame());
            if (count % 5 == 0) {
                System.out.println("Run " + count + " of " + runs + " is complete");
            }
            count++;
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
    }
}
