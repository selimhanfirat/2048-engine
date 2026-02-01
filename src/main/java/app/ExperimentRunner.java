package app;

import game.core.Game;

// experiment runner, will be given a player, and a preset, it will run an experiment, reporting its stats
public class ExperimentRunner {

    Game game;

    public ExperimentRunner(Game game) {
        this.game = game;
    }

    public int runExperiment() {
        return 1;
    }

}
