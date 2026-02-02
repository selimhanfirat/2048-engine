package app;

import ai.DumbGreedyPlayer;
import ai.SearchingPlayer;
import ai.eval.ZeroCountingEvaluator;
import game.runtime.Presets;

public class Main {
    public static void main(String[] args) {
        int runs = Integer.parseInt(args[0]);     // e.g. 1000
        runs = 20;
        long baseSeed = 42L;

        var config = Presets.standard2048();

        ExperimentRunner runner = new ExperimentRunner(
                config,
                runs,
                baseSeed,
                new SearchingPlayer(config, new ZeroCountingEvaluator())
        );

        var results = runner.run();

        runner.report(results);
    }
}
