package game.core;

import game.runtime.GameConfig;

import java.util.Random;

public final class Game {

    private final GameConfig config;
    private final Random random;

    private Board state;
    private int score;

    public Game(GameConfig config) {
        this.config = config;
        this.random = new Random(config.seed());
        this.state = new Board(config.gridSize());
        this.score = 0;
    }

    public GameConfig getConfig() {
        return config;
    }

    public void initialize() {
        state = config.spawner().spawn(state, random);
        state = config.spawner().spawn(state, random);
    }

    public void step(Move move) {
        MoveResult result = config.rules().makeMove(state, move);
        state = result.board();
        score += result.scoreGained();

        state = config.spawner().spawn(state, random);
    }

    public Board getState() {
        return state;
    }

    public boolean isGameOver() {
        return config.rules().isGameOver(state);
    }

    public int getScore() {
        return score;
    }
}
