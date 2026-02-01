package game.runtime;

import game.core.Board;
import game.core.Move;
import game.core.MoveResult;

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

    public boolean isGameOver() {
        return config.rules().isGameOver(state);
    }

    public int getScore() {
        return score;
    }
}
