package app.output;

import app.dto.ExperimentResult;
import app.dto.ExperimentSnapshot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;

public final class MarkdownFileSink implements OutputSink {

    private final Path file;

    public MarkdownFileSink(String fileName) {
        this.file = Path.of(fileName);
    }

    @Override
    public void onCheckpoint(ExperimentSnapshot snapshot) {
        writeMarkdown(snapshot, "CHECKPOINT");
    }

    @Override
    public void onFinal(ExperimentSnapshot snapshot) {
        writeMarkdown(snapshot, "FINAL");
    }

    private void writeMarkdown(ExperimentSnapshot s, String kind) {
        // overwrite file each time => no duplicates
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file.toFile(), false))) {

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            int done = s.measuredDone();
            int total = s.measuredTotal();
            int percent = (int) ((100.0 * done) / Math.max(1, total));

            w.write("# 2048 Experiment Report\n\n");
            w.write("- Generated at: " + timestamp + "\n");
            w.write("- Type: " + kind + "\n");
            w.write(String.format(Locale.ROOT, "- Progress: %d%% (%d / %d)\n", percent, done, total));
            w.write("- Last seed: " + s.currentSeed() + "\n\n");

            w.write("## Results (sorted by mean score)\n\n");

            s.results().stream()
                    .sorted(Comparator.comparingDouble(ExperimentResult::meanScore).reversed())
                    .forEach(r -> writeExperimentBlock(w, r));

        } catch (IOException e) {
            throw new RuntimeException("Failed to write markdown report to " + file, e);
        }
    }

    private void writeExperimentBlock(BufferedWriter w, ExperimentResult r) {
        try {
            w.write("### " + r.label() + "\n\n");

            w.write("- Runs: " + r.n() + "\n");
            w.write(String.format(Locale.ROOT, "- Mean score: %.2f\n", r.meanScore()));
            w.write("- Best score: " + r.bestScore() + "\n");
            w.write(String.format(Locale.ROOT, "- Mean steps: %.2f\n", r.meanSteps()));
            w.write(String.format(Locale.ROOT, "- Mean max tile: %.2f\n", r.meanMaxTile()));
            w.write("- Best max tile: " + r.bestMaxTile() + "\n");
            w.write(String.format(Locale.ROOT,
                    "- Reached 2048: %.2f%% (%d / %d)\n",
                    r.reached2048Pct(), r.reached2048Count(), r.n()));

            w.write("\n**Timing (seconds)**\n\n");
            w.write(String.format(Locale.ROOT, "- Total wall time: %.3f\n", r.totalWallSec()));
            w.write(String.format(Locale.ROOT, "- Avg wall per run: %.6f\n", r.avgWallSec()));

            if (r.cpuAvailableForAll()) {
                w.write(String.format(Locale.ROOT, "- Total CPU time: %.3f\n", r.totalCpuSec()));
                w.write(String.format(Locale.ROOT, "- Avg CPU per run: %.6f\n", r.avgCpuSec()));
            } else {
                w.write("- CPU time: unavailable\n");
            }

            if (r.hasSearchStats()) {
                w.write("\n**Search (aggregated)**\n\n");
                w.write(String.format(Locale.ROOT, "- Nodes/sec: %.0f\n", r.nodesPerSec()));
                w.write(String.format(Locale.ROOT, "- Avg outcomes: %.2f\n", r.avgOutcomes()));
            }

            w.write("\n---\n\n");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}