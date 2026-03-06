package app;

import ai.ExpectimaxPlayer;
import app.dto.*;
import app.output.OutputSink;
import game.runtime.GameConfig;
import game.runtime.GameSession;
import game.runtime.SessionResult;

import java.util.ArrayList;
import java.util.List;

public class ExperimentRunner {
    private final GameConfig config;

    public ExperimentRunner(GameConfig config) {
        this.config = config;
    }

    private static final class Acc {
        int n = 0;

        long totalScore = 0;
        long totalSteps = 0;
        long totalMaxTile = 0;

        int reached2048 = 0;
        int bestScore = Integer.MIN_VALUE;
        int bestMaxTile = Integer.MIN_VALUE;

        long totalWallNanos = 0;

        long totalCpuNanos = 0;
        boolean cpuAvailableForAll = true;

        // optional search stats
        boolean hasSearchStats = false;
        long totalNodes = 0;
        long totalChanceNodes = 0;
        long totalChanceOutcomes = 0;
        long totalSearchNanos = 0;

        void add(SessionResult r) {
            n++;

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

        void addSearchStats(ExpectimaxPlayer.SearchStats s) {
            hasSearchStats = true;
            totalNodes += s.nodes();
            totalChanceNodes += s.chanceNodes();
            totalChanceOutcomes += s.chanceOutcomes();
            totalSearchNanos += s.searchNanos();
        }

        ExperimentResult toResult(String label, ExperimentSpec spec) {
            int runs = Math.max(1, n);

            double meanScore = totalScore / (double) runs;
            double meanSteps = totalSteps / (double) runs;
            double meanMaxTile = totalMaxTile / (double) runs;

            double reachedPct = 100.0 * reached2048 / runs;

            double totalWallSec = totalWallNanos / 1_000_000_000.0;
            double avgWallSec = totalWallSec / runs;

            double totalCpuSec = cpuAvailableForAll ? (totalCpuNanos / 1_000_000_000.0) : 0.0;
            double avgCpuSec = cpuAvailableForAll ? (totalCpuSec / runs) : 0.0;

            double nodesPerSec = 0.0;
            double avgOutcomes = 0.0;
            if (hasSearchStats && totalSearchNanos > 0) {
                double sec = totalSearchNanos / 1_000_000_000.0;
                nodesPerSec = totalNodes / sec;
                avgOutcomes = totalChanceNodes > 0
                        ? totalChanceOutcomes / (double) totalChanceNodes
                        : 0.0;
            }

            return new ExperimentResult(
                    label,
                    spec,
                    n,

                    meanScore,
                    bestScore == Integer.MIN_VALUE ? 0 : bestScore,
                    meanSteps,
                    meanMaxTile,
                    bestMaxTile == Integer.MIN_VALUE ? 0 : bestMaxTile,
                    reachedPct,
                    reached2048,

                    totalWallSec,
                    avgWallSec,
                    cpuAvailableForAll,
                    totalCpuSec,
                    avgCpuSec,

                    hasSearchStats,
                    nodesPerSec,
                    avgOutcomes
            );
        }
    }

    public void runExperiment(
            List<ExperimentCase> playerExperiments,
            RunPlan plan,
            List<OutputSink> sinks
    ) {
        int warmup = plan.warmupRuns();
        int numRuns = plan.runs();
        long baseSeed = plan.baseSeed();

        List<Acc> accs = new ArrayList<>(playerExperiments.size());
        for (int i = 0; i < playerExperiments.size(); i++) accs.add(new Acc());

        // Warmup (discard results). Also reset per-game stats for expectimax players.
        for (int i = 0; i < warmup; i++) {
            long seed = baseSeed + i;
            for (ExperimentCase e : playerExperiments) {
                if (e.player() instanceof ExpectimaxPlayer ep) ep.resetStats();
                new GameSession(config, seed).runGame(e.player());
                if (e.player() instanceof ExpectimaxPlayer ep) ep.resetStats();
            }
            System.out.printf("\rwarmup %d is complete", i + 1);
        }
        System.out.println();

        int checkpointEvery = plan.checkpointEvery();

        // Measured runs (interleaved, paired seeds)
        for (int i = 0; i < numRuns; i++) {
            long seed = baseSeed + warmup + i;

            for (int j = 0; j < playerExperiments.size(); j++) {
                ExperimentCase e = playerExperiments.get(j);

                // measure expectimax search stats per game
                if (e.player() instanceof ExpectimaxPlayer ep) ep.resetStats();

                SessionResult r = new GameSession(config, seed).runGame(e.player());
                accs.get(j).add(r);

                if (e.player() instanceof ExpectimaxPlayer ep) {
                    accs.get(j).addSearchStats(ep.getStats());
                    ep.resetStats();
                }
            }

            int done = i + 1;
            if (done % checkpointEvery == 0 || done == numRuns) {
                ExperimentSnapshot snap = snapshot(playerExperiments, accs, done, numRuns, seed);
                if (done == numRuns) {
                    for (OutputSink sink : sinks) sink.onFinal(snap);
                } else {
                    for (OutputSink sink : sinks) sink.onCheckpoint(snap);
                }
            }
        }
    }

    private static ExperimentSnapshot snapshot(
            List<ExperimentCase> experiments,
            List<Acc> accs,
            int done,
            int total,
            long seed
    ) {
        List<ExperimentResult> results = new ArrayList<>(experiments.size());
        for (int i = 0; i < experiments.size(); i++) {
            results.add(accs.get(i).toResult(
                    experiments.get(i).label(),
                    experiments.get(i).spec()
            ));
        }
        return new ExperimentSnapshot(done, total, seed, results);
    }
}