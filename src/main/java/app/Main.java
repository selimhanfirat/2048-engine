package app;

import ai.DumbGreedyPlayer;
import game.runtime.Presets;

public class Main {
    public static void main(String[] args) {
        int runs = Integer.parseInt(args[0]);     // e.g. 1000
        long baseSeed = 42L;

        var config = Presets.standard2048();

        ExperimentRunner runner = new ExperimentRunner(
                config,
                runs,
                baseSeed,
                DumbGreedyPlayer::new // factory: Rules -> Player
        );

        var results = runner.run();

        runner.report(results);
    }
}
