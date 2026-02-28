package app;

import ai.ExpectimaxPlayer;
import ai.Player;
import ai.eval.ClassicEvaluator;
import ai.eval.Evaluator;
import app.output.ConsoleSink;
import app.experiment.ExperimentRunner;
import app.dto.ExperimentSpec;
import app.dto.ExperimentCase;
import app.dto.RunPlan;
import app.output.MarkdownFileSink;
import app.output.OutputSink;
import game.rules.ClassicRules2048;
import game.rules.Rules;
import game.runtime.GameConfig;
import game.spawn.ClassicSpawner2048;
import game.spawn.Spawner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // ---- defaults (same spirit as before) ----
        String aiArg = "default";    // can be CSV list
        String depthArg = "3";       // can be CSV list
        String cacheArg = "true";    // can be CSV list

        int runs = 250;
        long seed = 42L;

        int checkpoints = 10;
        double warmupFraction = 0.08; // 8% (in your 5-10% range)

        // ---- parse flags ----
        for (int i = 0; i < args.length; i++) {
            String a = args[i];

            switch (a) {
                case "--help", "-h" -> usageAndExit();

                case "--ai" -> aiArg = requireValue(args, ++i, "--ai").toLowerCase();
                case "--depth" -> depthArg = requireValue(args, ++i, "--depth");
                case "--cache" -> cacheArg = requireValue(args, ++i, "--cache").toLowerCase();

                case "--runs" -> runs = parsePositiveInt(requireValue(args, ++i, "--runs"), "runs");
                case "--seed" -> seed = parseLong(requireValue(args, ++i, "--seed"), "seed");

                case "--checkpoints" -> checkpoints = parseNonNegativeInt(requireValue(args, ++i, "--checkpoints"), "checkpoints");
                case "--warmup" -> warmupFraction = parseWarmup(requireValue(args, ++i, "--warmup"));

                default -> throw new IllegalArgumentException("Unknown argument: " + a);
            }
        }

        // ---- game config ----
        int gridSize = 4;
        double p2 = 0.9;

        Rules rules = new ClassicRules2048();
        Spawner spawner = new ClassicSpawner2048(p2);
        GameConfig config = new GameConfig(gridSize, rules, spawner);

        // ---- parse list args (single value or CSV) ----
        List<String> ais = parseCsvStrings(aiArg);
        List<Integer> depths = parseCsvPositiveInts(depthArg, "depth");
        List<Boolean> caches = parseCsvBooleans(cacheArg, "cache");

        // ---- build cartesian product ----
        List<ExperimentCase> experiments = new ArrayList<>();
        for (String ai : ais) {
            for (int d : depths) {
                for (boolean c : caches) {
                    Player player = getPlayer(ai, config, d, c);
                    ExperimentSpec spec = new ExperimentSpec(d, c);
                    String label = ai + " d=" + d + " cache=" + (c ? "on" : "off");
                    experiments.add(new ExperimentCase(label, spec, player));
                }
            }
        }

        // ---- print config summary ----
        System.out.println("=== Configuration ===");
        System.out.println("Runs         : " + runs);
        System.out.println("Seed         : " + seed);
        System.out.println("Warmup       : " + warmupFraction + " (" + Math.round(warmupFraction * 100) + "%)");
        System.out.println("Checkpoints  : " + checkpoints);
        System.out.println("Grid Size    : " + gridSize + "x" + gridSize);
        System.out.println("P(2-tile)    : " + p2);
        System.out.println("Experiments  : " + experiments.size());
        System.out.println("AI(s)        : " + ais);
        System.out.println("Depth(s)     : " + depths);
        System.out.println("Cache mode(s): " + caches);
        System.out.println("=====================");

        // ---- run ----
        RunPlan plan = new RunPlan(runs, seed, warmupFraction, checkpoints);
        ExperimentRunner runner = new ExperimentRunner(config);
        String reportPath = buildMarkdownReportPath(aiArg, depthArg, cacheArg, runs, seed, warmupFraction, checkpoints);

        List<OutputSink> sinks = List.of(
                new ConsoleSink(),
                new MarkdownFileSink(reportPath)
        );

        runner.runExperiment(experiments, plan, sinks);
    }

    private static Player getPlayer(String aiType, GameConfig config, int depth, boolean useCache) {
        Evaluator evaluator = new ClassicEvaluator();

        return switch (aiType) {
            case "default" -> new ExpectimaxPlayer(config, evaluator, depth, useCache);
            default -> throw new IllegalArgumentException("Unknown AI type: " + aiType + " (expected: default)");
        };
    }

    // ---------- parsing helpers ----------

    private static List<String> parseCsvStrings(String s) {
        if (s == null || s.isBlank()) return List.of();
        String[] parts = s.split(",");
        List<String> out = new ArrayList<>(parts.length);
        for (String p : parts) {
            String x = p.trim();
            if (!x.isEmpty()) out.add(x);
        }
        return out;
    }

    private static List<Integer> parseCsvPositiveInts(String s, String name) {
        List<String> parts = parseCsvStrings(s);
        if (parts.isEmpty()) return List.of();
        List<Integer> out = new ArrayList<>(parts.size());
        for (String p : parts) out.add(parsePositiveInt(p, name));
        return out;
    }

    private static List<Boolean> parseCsvBooleans(String s, String name) {
        List<String> parts = parseCsvStrings(s);
        if (parts.isEmpty()) return List.of();
        List<Boolean> out = new ArrayList<>(parts.size());
        for (String p : parts) out.add(parseBoolean(p, name));
        return out;
    }

    private static boolean parseBoolean(String s, String name) {
        String x = s.trim().toLowerCase();
        return switch (x) {
            case "true", "1", "on", "enabled", "yes", "y" -> true;
            case "false", "0", "off", "disabled", "no", "n" -> false;
            default -> throw new IllegalArgumentException(name + " must be boolean (true/false/on/off/1/0). Got: " + s);
        };
    }

    // warmup accepts "0.08" or "8%" (clamped to [0, 0.95] just to prevent nonsense)
    private static double parseWarmup(String s) {
        String x = s.trim().toLowerCase();
        double v;
        if (x.endsWith("%")) {
            String num = x.substring(0, x.length() - 1).trim();
            v = Double.parseDouble(num) / 100.0;
        } else {
            v = Double.parseDouble(x);
            if (v > 1.0) v = v / 100.0; // allow "8" meaning 8%
        }
        if (v < 0.0) v = 0.0;
        if (v > 0.95) v = 0.95;
        return v;
    }

    private static String buildMarkdownReportPath(
            String aiArg,
            String depthArg,
            String cacheArg,
            int runs,
            long seed,
            double warmupFraction,
            int checkpoints
    ) {
        Path dir = Path.of("reports", "markdownFiles");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory: " + dir.toAbsolutePath(), e);
        }

        String fileName = "report_"
                + "ai-" + slug(aiArg)
                + "_depth-" + slug(depthArg)
                + "_cache-" + slug(cacheArg)
                + "_runs-" + runs
                + "_seed-" + seed
                + "_warmup-" + Math.round(warmupFraction * 100) + "pct"
                + "_cp-" + checkpoints
                + ".md";

        return dir.resolve(fileName).toString();
    }

    private static String slug(String s) {
        if (s == null || s.isBlank()) return "default";
        return s.trim().toLowerCase()
                .replace(",", "-")
                .replaceAll("\\s+", "")
                .replaceAll("[^a-z0-9_-]", "-")
                .replaceAll("-{2,}", "-");
    }

    private static String requireValue(String[] args, int idx, String flag) {
        if (idx >= args.length) {
            throw new IllegalArgumentException("Missing value after " + flag);
        }
        return args[idx];
    }

    private static int parsePositiveInt(String s, String name) {
        final int x;
        try {
            x = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be an integer. Got: " + s, e);
        }
        if (x <= 0) {
            throw new IllegalArgumentException(name + " must be > 0. Got: " + x);
        }
        return x;
    }

    private static int parseNonNegativeInt(String s, String name) {
        final int x;
        try {
            x = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be an integer. Got: " + s, e);
        }
        if (x < 0) {
            throw new IllegalArgumentException(name + " must be >= 0. Got: " + x);
        }
        return x;
    }

    private static long parseLong(String s, String name) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a long. Got: " + s, e);
        }
    }

    private static void usageAndExit() {
        System.out.println("Usage (all optional; CSV supported for ai/depth/cache):");
        System.out.println("  --ai default,otherAI        (default: default)");
        System.out.println("  --depth 2,3,4               (default: 3)");
        System.out.println("  --cache true,false          (default: true)");
        System.out.println("  --runs <n>                  (default: 250)");
        System.out.println("  --seed <n>                  (default: 42)");
        System.out.println("  --checkpoints <n>           (default: 10)");
        System.out.println("  --warmup <fraction|percent> (default: 0.08 or 8%)");
        System.exit(0);
    }
}