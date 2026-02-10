package app;

import ai.ExpectimaxPlayer;
import ai.Player;
import ai.eval.EmptyCellsEvaluator;
import ai.eval.Evaluator;
import ai.eval.MonotonicityEvaluator;
import ai.eval.WeightedEvaluator;
import game.runtime.GameConfig;
import game.runtime.Presets;

import java.util.List;

public final class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.exit(1);
        }

        String aiType = args[0].toLowerCase();
        int runs = parseRuns(args[1]);

        long baseSeed = 42L;

        GameConfig config = Presets.standard2048();
        Player player = getPlayer(aiType, config);

        ExperimentRunner runner =
                new ExperimentRunner(config, runs, baseSeed, player);

        runner.run();
    }

    private static int parseRuns(String s) {
        try {
            int runs = Integer.parseInt(s);
            if (runs <= 0) throw new IllegalArgumentException();
            return runs;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid run count: " + s);
        }
    }

    private static Player getPlayer(String aiType, GameConfig config) {
        Evaluator evaluator = new WeightedEvaluator(List.of(
                new WeightedEvaluator.Term(new MonotonicityEvaluator(), 2.0),
                new WeightedEvaluator.Term(new EmptyCellsEvaluator(), 3.0)
        ));

        return switch (aiType) {
            case "nocache" -> new ExpectimaxPlayer(config, evaluator);
            default -> throw new IllegalArgumentException("Unknown AI type: " + aiType);
        };
    }
}
