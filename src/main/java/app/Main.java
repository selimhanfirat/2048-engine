package app;

import ai.ExpectimaxPlayer;
import ai.Player;
import ai.eval.Evaluator;
import ai.eval.ClassicEvaluator;
import game.rules.ClassicRules2048;
import game.rules.Rules;
import game.runtime.GameConfig;
import game.spawn.ClassicSpawner2048;
import game.spawn.Spawner;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <ai-type> <runs>");
            System.err.println("Example: nocache 1000");
            // System.err.println("AI types: nocache, cache");
            System.exit(1);
        }

        String aiType = args[0].toLowerCase();
        int runs;
        try {
            runs = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Second argument must be an integer run count. Got: " + args[1], e);
        }

        long baseSeed = 42L;

        int gridSize = 4;
        double p = 0.9;
        Rules rules = new ClassicRules2048();
        Spawner spawner = new ClassicSpawner2048(p);

        GameConfig config = new GameConfig(gridSize, rules, spawner);
        Player player = getPlayer(aiType, config);

        ExperimentRunner runner = new ExperimentRunner(config, runs, baseSeed, player);
        var results = runner.run();
        runner.report(results);
    }

    private static Player getPlayer(String aiType, GameConfig config) {
        Evaluator evaluator = new ClassicEvaluator();

        return switch (aiType) {
            case "nocache" -> new ExpectimaxPlayer(config, evaluator);
            // case "cache" -> new ExpectimaxPlayerWithCache(config, evaluator);
            default -> throw new IllegalArgumentException(
                    "Unknown AI type: " + aiType + " (expected: depth2 | cache)"
            );
        };
    }
}
