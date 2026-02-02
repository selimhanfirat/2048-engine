package game.runtime;

import game.core.Board;
import game.core.Move;
import game.core.MoveResult;
import game.util.Rng;

public final class Game {

    private final GameConfig config;
    private final long seed;
    private final Rng rng;

    private Board state;
    private int score;

    public Game(GameConfig config, long seed) {
        this.config = config;
        this.seed = seed;
        this.rng = new Rng(seed);
        this.state = new Board(config.gridSize());
        this.score = 0;
    }

    public GameConfig getConfig() { return config; }
    public long getSeed() { return seed; }

    public void initialize() {
        state = config.spawner().spawn(state, rng);
        state = config.spawner().spawn(state, rng);
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
        state = config.spawner().spawn(state, rng);
    }
}
