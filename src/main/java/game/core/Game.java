package game.core;

import game.runtime.GameConfig;

import java.util.Random;

public final class Game {

    private final GameConfig config;
    private final long seed;
    private final Random random;

    private Board state;
    private int score;

    public Game(GameConfig config, long seed) {
        this.config = config;
        this.seed = seed;
        this.random = new Random(seed);
        this.state = new Board(config.gridSize());
        this.score = 0;
    }

    public GameConfig getConfig() { return config; }
    public long getSeed() { return seed; }

    public void initialize() {
        state = config.spawner().spawn(state, random);
        state = config.spawner().spawn(state, random);
    }

    public Board getState() { return state; }
    public int getScore() { return score; }

    public boolean isGameOver() {
        return config.rules().isGameOver(state);
    }

    public void step(Move move) {
        MoveResult result = config.rules().makeMove(state, move);
        state = result.board();
        score += result.scoreGained();
        state = config.spawner().spawn(state, random);
    }
}
