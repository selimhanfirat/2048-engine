package app;

import ai.ExpectimaxPlayer;
import ai.Player;
import ai.eval.ClassicEvaluator;
import ai.eval.Evaluator;
import game.rules.ClassicRules2048;
import game.rules.Rules;
import game.runtime.GameConfig;
import game.runtime.SessionResult;
import game.spawn.ClassicSpawner2048;
import game.spawn.Spawner;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        // ---- defaults ----
        String aiType = "default";
        int runs = 250;
        int depth = 3;
        boolean useCache = true;
        long seed = 42L;
        boolean interactive = false;

        // ---- parse flags ----
        for (int i = 0; i < args.length; i++) {
            String a = args[i];

            switch (a) {
                case "--help", "-h" -> {
                    usageAndExit();
                }
                case "--interactive" -> interactive = true;
                case "--nocache" -> useCache = false;

                case "--ai" -> {
                    aiType = requireValue(args, ++i, "--ai").toLowerCase();
                }
                case "--runs" -> {
                    runs = parsePositiveInt(requireValue(args, ++i, "--runs"), "runs");
                }
                case "--depth" -> {
                    depth = parsePositiveInt(requireValue(args, ++i, "--depth"), "depth");
                }
                case "--seed" -> {
                    seed = parseLong(requireValue(args, ++i, "--seed"), "seed");
                }

                default -> throw new IllegalArgumentException("Unknown argument: " + a);
            }
        }

        if (interactive) {
            runs = 1;
        }

        // ---- game config ----
        int gridSize = 4;
        double p2 = 0.9;

        Rules rules = new ClassicRules2048();
        Spawner spawner = new ClassicSpawner2048(p2);
        GameConfig config = new GameConfig(gridSize, rules, spawner);

        // ---- player ----
        Player player = getPlayer(aiType, config, depth, useCache);

        // ---- print config ----
        System.out.println("=== Configuration ===");
        System.out.println("Mode      : " + (interactive ? "interactive" : "batch"));
        System.out.println("AI Type   : " + aiType);
        System.out.println("Runs      : " + runs);
        System.out.println("Depth     : " + depth);
        System.out.println("Cache     : " + (useCache ? "ENABLED" : "DISABLED"));
        System.out.println("Seed      : " + seed);
        System.out.println("Grid Size : " + gridSize + "x" + gridSize);
        System.out.println("P(2-tile) : " + p2);
        System.out.println("=====================");

        // ---- run ----
        ExperimentRunner runner = new ExperimentRunner(config, runs, seed, player);

        if (interactive) {
            runner.runInteractiveOnce(); // implement in ExperimentRunner
        } else {
            List<SessionResult> results = runner.run();
            runner.report(results);
        }
    }

    private static Player getPlayer(String aiType, GameConfig config, int depth, boolean useCache) {
        Evaluator evaluator = new ClassicEvaluator();

        return switch (aiType) {
            case "default" -> new ExpectimaxPlayer(config, evaluator, depth, useCache);
            default -> throw new IllegalArgumentException("Unknown AI type: " + aiType + " (expected: default)");
        };
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

    private static long parseLong(String s, String name) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a long. Got: " + s, e);
        }
    }

    private static void usageAndExit() {
        System.out.println("Usage (all optional):");
        System.out.println("  --ai <name>        (default: default)");
        System.out.println("  --runs <n>         (default: 250)");
        System.out.println("  --depth <n>        (default: 3)");
        System.out.println("  --seed <n>         (default: 42)");
        System.out.println("  --nocache          (default: cache enabled)");
        System.out.println("  --interactive      (forces runs=1)");
        System.exit(0);
    }
}