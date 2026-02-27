package game.runtime;

import ai.Player;
import game.core.Board;
import game.core.Move;
import game.core.MoveResult;
import game.rules.Rules;
import game.spawn.Spawner;
import game.util.Rng;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

public final class GameSession {

    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
    private static final boolean CPU_TIME_SUPPORTED = THREAD_MX_BEAN.isCurrentThreadCpuTimeSupported();

    static {
        if (CPU_TIME_SUPPORTED && !THREAD_MX_BEAN.isThreadCpuTimeEnabled()) {
            THREAD_MX_BEAN.setThreadCpuTimeEnabled(true);
        }
    }

    private final GameConfig config;
    private final long seed;

    private final Rng rng;

    private Board state;
    private int score;

    public GameSession(GameConfig config, long seed) {
        this.config = config;
        this.seed = seed;
        this.rng = new Rng(seed);
        this.state = new Board(config.gridSize());
        this.score = 0;
    }

    public SessionResult runGame(Player player) {
        // timing start
        final long wallStart = System.nanoTime();
        final long cpuStart = (CPU_TIME_SUPPORTED && THREAD_MX_BEAN.isThreadCpuTimeEnabled())
                ? THREAD_MX_BEAN.getCurrentThreadCpuTime()
                : -1L;

        int steps = 0;
        int maxTile;
        Map<Move, Integer> moveCounts = new HashMap<>();

        initialize();
        maxTile = state.getMaxTile();

        while (!isGameOver()) {
            Move move = player.chooseMove(state);
            moveCounts.merge(move, 1, Integer::sum);

            step(move);
            steps++;

            maxTile = Math.max(maxTile, state.getMaxTile());
        }

        // timing end
        final long wallEnd = System.nanoTime();
        final long cpuEnd = (cpuStart != -1L)
                ? THREAD_MX_BEAN.getCurrentThreadCpuTime()
                : -1L;

        final long wallNanos = wallEnd - wallStart;
        final long cpuNanos = (cpuStart != -1L) ? (cpuEnd - cpuStart) : -1L;

        return new SessionResult(
                seed,
                score,
                steps,
                maxTile,
                maxTile >= 2048,
                Map.copyOf(moveCounts),
                wallNanos,
                cpuNanos
        );
    }

    private void initialize() {
        state = config.spawner().sample(state, rng);
        state = config.spawner().sample(state, rng);
    }

    private boolean isGameOver() {
        return config.rules().isGameOver(state);
    }

    private void step(Move move) {
        MoveResult result = config.rules().makeMove(state, move);
        state = result.board();
        score += result.scoreGained();
        state = config.spawner().sample(state, rng);
    }
}