package app;

import ai.ExpectimaxPlayerDepth2;
import ai.ExpectimaxPlayerWithCache;
import ai.Player;
import ai.eval.EmptyCellsEvaluator;
import game.runtime.Presets;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: <ai-type> <runs>");
            System.err.println("Example: depth2 1000");
            System.err.println("AI types: depth2, cache");
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

        var config = Presets.standard2048();
        var evaluator = new EmptyCellsEvaluator();

        Player player = switch (aiType) {
            case "depth2" -> new ExpectimaxPlayerDepth2(config, evaluator);
            case "cache" -> new ExpectimaxPlayerWithCache(config, evaluator);
            default -> throw new IllegalArgumentException(
                    "Unknown AI type: " + aiType + " (expected: depth2 | cache)"
            );
        };

        ExperimentRunner runner = new ExperimentRunner(config, runs, baseSeed, player);
        var results = runner.run();
        runner.report(results);
    }
}
