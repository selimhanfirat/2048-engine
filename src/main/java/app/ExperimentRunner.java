package app;

import ai.Player;
import game.runtime.Game;
import game.runtime.GameConfig;
import game.runtime.GameSession;
import game.runtime.SessionResult;
import game.rules.Rules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ExperimentRunner {

    private final GameConfig config;
    private final int runs;
    private final long baseSeed;
    private final Function<Rules, Player> playerFactory;

    public ExperimentRunner(
            GameConfig config,
            int runs,
            long baseSeed,
            Function<Rules, Player> playerFactory
    ) {
        this.config = config;
        this.runs = runs;
        this.baseSeed = baseSeed;
        this.playerFactory = playerFactory;
    }

    public List<SessionResult> run() {
        List<SessionResult> results = new ArrayList<>(runs);

        for (int i = 0; i < runs; i++) {
            long seed = baseSeed + i;

            Game game = new Game(config, seed);
            Player player = playerFactory.apply(config.rules());

            GameSession session = new GameSession(game, player);
            results.add(session.runGame());
        }

        return results;
    }

    public void report(List<SessionResult> results) {
        int n = results.size();

        long totalScore = 0;
        long totalSteps = 0;
        long totalMaxTile = 0;

        int reached2048 = 0;
        int bestScore = Integer.MIN_VALUE;
        int bestMaxTile = Integer.MIN_VALUE;

        for (SessionResult r : results) {
            totalScore += r.finalScore();
            totalSteps += r.steps();
            totalMaxTile += r.maxTile();

            if (r.reached2048()) reached2048++;

            bestScore = Math.max(bestScore, r.finalScore());
            bestMaxTile = Math.max(bestMaxTile, r.maxTile());
        }

        System.out.println("Runs: " + n);
        System.out.printf("Average score: %.2f%n", totalScore / (double) n);
        System.out.printf("Average max tile: %.2f%n", totalMaxTile / (double) n);
        System.out.printf("Average steps: %.2f%n", totalSteps / (double) n);
        System.out.printf("Reached 2048 rate: %.2f%%%n", 100.0 * reached2048 / n);
        System.out.println("Best score: " + bestScore);
        System.out.println("Best max tile: " + bestMaxTile);
    }
}
