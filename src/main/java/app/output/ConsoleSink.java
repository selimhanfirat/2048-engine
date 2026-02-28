package app.output;

import app.dto.ExperimentSnapshot;
import app.dto.ExperimentResult;

import java.util.Comparator;
import java.util.Locale;

public final class ConsoleSink implements OutputSink {

    @Override
    public void onCheckpoint(ExperimentSnapshot s) {
        int done = s.measuredDone();
        int total = s.measuredTotal();
        int percent = (int) ((100.0 * done) / Math.max(1, total));

        System.out.printf(Locale.ROOT, "Progress: %d%% (%d / %d)%n", percent, done, total);

        if (s.results().size() == 1) {
            ExperimentResult r = s.results().getFirst();
            if (r.hasSearchStats()) {
                System.out.printf(Locale.ROOT,
                        "  Search so far: nodes/sec=%.0f, avgOutcomes=%.2f%n",
                        r.nodesPerSec(), r.avgOutcomes());
            }
            return;
        }

        s.results().stream()
                .sorted(Comparator.comparingDouble(ExperimentResult::meanScore).reversed())
                .limit(5)
                .forEach(r -> {
                    System.out.printf(Locale.ROOT,
                            "  %-24s mean=%.1f p2048=%.1f%%%n",
                            r.label(), r.meanScore(), r.reached2048Pct());
                    if (r.hasSearchStats()) {
                        System.out.printf(Locale.ROOT,
                                "    nodes/sec=%.0f avgOutcomes=%.2f%n",
                                r.nodesPerSec(), r.avgOutcomes());
                    }
                });
    }

    @Override
    public void onFinal(ExperimentSnapshot s) {
        System.out.println();
        System.out.println("=== Final Results ===");

        s.results().stream()
                .sorted(Comparator.comparingDouble(ExperimentResult::meanScore).reversed())
                .forEach(this::printPrettyReport);
    }

    private void printPrettyReport(ExperimentResult r) {
        System.out.println();
        System.out.println("Experiment report: " + r.label());
        System.out.println("-----------------");
        System.out.println("Runs              : " + r.n());

        System.out.println();
        System.out.println("Performance");
        System.out.printf(Locale.ROOT, "  Average score   : %.2f%n", r.meanScore());
        System.out.printf(Locale.ROOT, "  Best score      : %d%n", r.bestScore());
        System.out.printf(Locale.ROOT, "  Average steps   : %.2f%n", r.meanSteps());
        System.out.printf(Locale.ROOT, "  Average max tile: %.2f%n", r.meanMaxTile());
        System.out.printf(Locale.ROOT, "  Best max tile   : %d%n", r.bestMaxTile());
        System.out.printf(Locale.ROOT, "  Reached 2048    : %.2f %% (%d / %d)%n",
                r.reached2048Pct(), r.reached2048Count(), r.n());

        System.out.println();
        System.out.println("Timing (seconds)");
        System.out.printf(Locale.ROOT, "  Total wall time : %.3f s%n", r.totalWallSec());
        System.out.printf(Locale.ROOT, "  Avg wall per run: %.6f s%n", r.avgWallSec());

        if (r.cpuAvailableForAll()) {
            System.out.printf(Locale.ROOT, "  Total CPU time  : %.3f s%n", r.totalCpuSec());
            System.out.printf(Locale.ROOT, "  Avg CPU per run : %.6f s%n", r.avgCpuSec());
        } else {
            System.out.println("  CPU time        : unavailable");
        }

        if (r.hasSearchStats()) {
            System.out.println();
            System.out.println("Search (aggregated)");
            System.out.printf(Locale.ROOT, "  nodes/sec       : %.0f%n", r.nodesPerSec());
            System.out.printf(Locale.ROOT, "  avgOutcomes     : %.2f%n", r.avgOutcomes());
        }
    }
}