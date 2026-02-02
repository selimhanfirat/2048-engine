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

        // print something minimal for now
        long sum = 0;
        int best = Integer.MIN_VALUE;
        for (var r : results) {
            sum += r.finalScore();
            best = Math.max(best, r.finalScore());
        }
        System.out.println("Runs: " + results.size());
        System.out.println("Avg score: " + (sum / (double) results.size()));
        System.out.println("Best score: " + best);
    }
}
