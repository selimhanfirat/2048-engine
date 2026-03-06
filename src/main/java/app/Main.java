package app;

import ai.ExpectimaxPlayer;
import ai.Player;
import ai.SamplingExpectimaxPlayer;
import ai.eval.ClassicEvaluator;
import ai.eval.Evaluator;
import app.dto.ExperimentCase;
import app.dto.ExperimentSpec;
import app.dto.RunPlan;
import app.experiment.ExperimentRunner;
import app.output.ConsoleSink;
import app.output.MarkdownFileSink;
import app.output.OutputSink;
import game.rules.ClassicRules2048;
import game.rules.Rules;
import game.runtime.GameConfig;
import game.runtime.GameSession;
import game.spawn.ClassicSpawner2048;
import game.spawn.Spawner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final boolean DEFAULT_PLAY_MODE = true;

    private static final String DEFAULT_PLAY_AI = "default";
    private static final int DEFAULT_PLAY_DEPTH = 4;
    private static final boolean DEFAULT_PLAY_CACHE = true;
    private static final long DEFAULT_PLAY_SEED = 42L;
    private static final int DEFAULT_IGNORE4_THRESHOLD = 6;
    private static final String DEFAULT_PACE = "auto";
    private static final int DEFAULT_DELAY_MS = 300;

    private static final String DEFAULT_EXPERIMENT_AI = "default";
    private static final String DEFAULT_EXPERIMENT_DEPTH = "3";
    private static final String DEFAULT_EXPERIMENT_CACHE = "true";
    private static final int DEFAULT_RUNS = 250;
    private static final long DEFAULT_EXPERIMENT_SEED = 42L;
    private static final int DEFAULT_CHECKPOINTS = 10;
    private static final double DEFAULT_WARMUP_FRACTION = 0.02;

    private static final int DEFAULT_GRID_SIZE = 4;
    private static final double DEFAULT_P2 = 0.9;

    public static void main(String[] args) {

        boolean playMode = DEFAULT_PLAY_MODE;
        int startIdx = 0;

        if (args.length > 0) {
            String first = args[0].trim().toLowerCase();

            if (first.equals("play")) {
                playMode = true;
                startIdx = 1;
            } else if (first.equals("experiment") || first.equals("exp")) {
                playMode = false;
                startIdx = 1;
            } else if (first.equals("--help") || first.equals("-h")) {
                usageAndExit();
            }
        }

        if (playMode) {
            runPlay(args, startIdx);
        } else {
            runExperiment(args, startIdx);
        }
    }

    private static void runPlay(String[] args, int startIdx) {

        String aiType = DEFAULT_PLAY_AI;
        int depth = DEFAULT_PLAY_DEPTH;
        boolean useCache = DEFAULT_PLAY_CACHE;

        long seed = DEFAULT_PLAY_SEED;
        int ignore4Threshold = DEFAULT_IGNORE4_THRESHOLD;

        String pace = DEFAULT_PACE;
        int delayMs = DEFAULT_DELAY_MS;

        for (int i = startIdx; i < args.length; i++) {
            String a = args[i];

            switch (a) {
                case "--help", "-h" -> usageAndExit();

                case "--ai" -> aiType = requireValue(args, ++i, "--ai").toLowerCase();
                case "--depth" -> depth = parsePositiveInt(requireValue(args, ++i, "--depth"), "depth");
                case "--cache" -> useCache = parseBoolean(requireValue(args, ++i, "--cache"), "cache");

                case "--seed" -> seed = parseLong(requireValue(args, ++i, "--seed"), "seed");
                case "--ignore4" -> ignore4Threshold = parseNonNegativeInt(requireValue(args, ++i, "--ignore4"), "ignore4");

                case "--pace" -> pace = requireValue(args, ++i, "--pace").trim().toLowerCase();
                case "--delay-ms" -> delayMs = parseNonNegativeInt(requireValue(args, ++i, "--delay-ms"), "delay-ms");

                default -> throw new IllegalArgumentException("Unknown argument: " + a);
            }
        }

        if (!pace.equals("auto") && !pace.equals("step")) {
            throw new IllegalArgumentException("--pace must be 'auto' or 'step'. Got: " + pace);
        }

        int gridSize = DEFAULT_GRID_SIZE;
        double p2 = DEFAULT_P2;

        Rules rules = new ClassicRules2048();
        Spawner spawner = new ClassicSpawner2048(p2);
        GameConfig config = new GameConfig(gridSize, rules, spawner);

        Player player = getPlayer(aiType, config, depth, useCache, ignore4Threshold);

        GameSession session = new GameSession(config, seed);
        session.runGameInteractive(player, pace.equals("step"), delayMs, depth);
    }

    private static void runExperiment(String[] args, int startIdx) {

        String aiArg = DEFAULT_EXPERIMENT_AI;
        String depthArg = DEFAULT_EXPERIMENT_DEPTH;
        String cacheArg = DEFAULT_EXPERIMENT_CACHE;

        int runs = DEFAULT_RUNS;
        long seed = DEFAULT_EXPERIMENT_SEED;

        int checkpoints = DEFAULT_CHECKPOINTS;
        double warmupFraction = DEFAULT_WARMUP_FRACTION;
        int ignore4Threshold = DEFAULT_IGNORE4_THRESHOLD;

        for (int i = startIdx; i < args.length; i++) {
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

                case "--ignore4" -> ignore4Threshold = parseNonNegativeInt(requireValue(args, ++i, "--ignore4"), "ignore4");

                default -> throw new IllegalArgumentException("Unknown argument: " + a);
            }
        }

        int gridSize = DEFAULT_GRID_SIZE;
        double p2 = DEFAULT_P2;

        Rules rules = new ClassicRules2048();
        Spawner spawner = new ClassicSpawner2048(p2);
        GameConfig config = new GameConfig(gridSize, rules, spawner);

        List<String> ais = parseCsvStrings(aiArg);
        List<Integer> depths = parseCsvPositiveInts(depthArg, "depth");
        List<Boolean> caches = parseCsvBooleans(cacheArg, "cache");

        List<ExperimentCase> experiments = new ArrayList<>();
        for (String ai : ais) {
            for (int d : depths) {
                for (boolean c : caches) {

                    Player player = getPlayer(ai, config, d, c, ignore4Threshold);
                    ExperimentSpec spec = new ExperimentSpec(d, c);

                    String label = buildLabel(ai, d, c, ignore4Threshold);
                    experiments.add(new ExperimentCase(label, spec, player));
                }
            }
        }

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
        System.out.println("Ignore4 thr  : " + ignore4Threshold);
        System.out.println("=====================");

        RunPlan plan = new RunPlan(runs, seed, warmupFraction, checkpoints);
        ExperimentRunner runner = new ExperimentRunner(config);
        String reportPath = buildMarkdownReportPath(aiArg, depthArg, cacheArg, runs, seed, warmupFraction, checkpoints);

        List<OutputSink> sinks = List.of(
                new ConsoleSink(),
                new MarkdownFileSink(reportPath)
        );

        runner.runExperiment(experiments, plan, sinks);
    }

    private static String buildLabel(String aiType, int depth, boolean useCache, int ignore4Threshold) {
        String base = aiType + " d=" + depth + " cache=" + (useCache ? "on" : "off");
        if (aiType.equals("sample") || aiType.equals("sampling") || aiType.equals("ignore4")) {
            return base + " ignore4>" + ignore4Threshold;
        }
        return base;
    }

    private static Player getPlayer(String aiType, GameConfig config, int depth, boolean useCache, int ignore4Threshold) {
        Evaluator evaluator = new ClassicEvaluator();

        return switch (aiType) {
            case "default" -> new ExpectimaxPlayer(config, evaluator, depth, useCache);
            case "sample", "sampling", "ignore4" ->
                    new SamplingExpectimaxPlayer(config, evaluator, depth, useCache, ignore4Threshold);
            default -> throw new IllegalArgumentException(
                    "Unknown AI type: " + aiType + " (expected: default, sample)"
            );
        };
    }

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

    private static double parseWarmup(String s) {
        String x = s.trim().toLowerCase();
        double v;
        if (x.endsWith("%")) {
            String num = x.substring(0, x.length() - 1).trim();
            v = Double.parseDouble(num) / 100.0;
        } else {
            v = Double.parseDouble(x);
            if (v > 1.0) v = v / 100.0;
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
        System.out.println("Usage:");
        System.out.println("  java app.Main [play flags...]");
        System.out.println("  java app.Main play [play flags...]");
        System.out.println("  java app.Main experiment [experiment flags...]");
        System.out.println();
        System.out.println("Defaults:");
        System.out.println("  default mode: " + (DEFAULT_PLAY_MODE ? "play" : "experiment"));
        System.out.println();
        System.out.println("Play flags (single values):");
        System.out.println("  --ai default|sample       (default: " + DEFAULT_PLAY_AI + ")");
        System.out.println("  --depth <n>               (default: " + DEFAULT_PLAY_DEPTH + ")");
        System.out.println("  --cache <bool>            (default: " + DEFAULT_PLAY_CACHE + ")");
        System.out.println("  --ignore4 <n>             (default: " + DEFAULT_IGNORE4_THRESHOLD + ")");
        System.out.println("  --seed <n>                (default: " + DEFAULT_PLAY_SEED + ")");
        System.out.println("  --pace auto|step          (default: " + DEFAULT_PACE + ")");
        System.out.println("  --delay-ms <n>            (default: " + DEFAULT_DELAY_MS + ")");
        System.out.println();
        System.out.println("Experiment flags (CSV supported for ai/depth/cache):");
        System.out.println("  --ai default,sample         (default: " + DEFAULT_EXPERIMENT_AI + ")");
        System.out.println("  --depth 2,3,4               (default: " + DEFAULT_EXPERIMENT_DEPTH + ")");
        System.out.println("  --cache true,false          (default: " + DEFAULT_EXPERIMENT_CACHE + ")");
        System.out.println("  --ignore4 <n>               (default: " + DEFAULT_IGNORE4_THRESHOLD + ")");
        System.out.println("  --runs <n>                  (default: " + DEFAULT_RUNS + ")");
        System.out.println("  --seed <n>                  (default: " + DEFAULT_EXPERIMENT_SEED + ")");
        System.out.println("  --checkpoints <n>           (default: " + DEFAULT_CHECKPOINTS + ")");
        System.out.println("  --warmup <fraction|percent> (default: " + DEFAULT_WARMUP_FRACTION + " or " + Math.round(DEFAULT_WARMUP_FRACTION * 100) + "%)");
        System.exit(0);
    }
}