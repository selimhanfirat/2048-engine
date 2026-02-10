package game.runtime;

import ai.Player;
import backend.GameListener;
import game.core.Move;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GameSession {

    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
    private static final boolean CPU_TIME_SUPPORTED = THREAD_MX_BEAN.isCurrentThreadCpuTimeSupported();

    static {
        if (CPU_TIME_SUPPORTED && !THREAD_MX_BEAN.isThreadCpuTimeEnabled()) {
            THREAD_MX_BEAN.setThreadCpuTimeEnabled(true);
        }
    }

    private final Game game;
    private final Player player;
    private final List<GameListener> listeners;


    public GameSession(Game game, Player player) {
        this(game, player, List.of());
    }

    public GameSession(Game game, Player player, List<GameListener> listeners) {
        this.game = game;
        this.player = player;
        this.listeners = (listeners == null) ? List.of() : List.copyOf(listeners);
    }

    public SessionResult runGame() {
        // timing start
        final long wallStart = System.nanoTime();
        final long cpuStart = (CPU_TIME_SUPPORTED && THREAD_MX_BEAN.isThreadCpuTimeEnabled())
                ? THREAD_MX_BEAN.getCurrentThreadCpuTime()
                : -1L;

        int steps = 0;
        int maxTile = 0;
        Map<Move, Integer> moveCounts = new HashMap<>();

        game.initialize();
        maxTile = game.getState().getMaxTile();

        // notify init (step 0)
        for (GameListener l : listeners) {
            l.onInit(game.getSeed(), game.getState(), game.getScore());
        }

        while (!game.isGameOver()) {
            Move move = player.chooseMove(game.getState());

            moveCounts.merge(move, 1, Integer::sum);

            game.step(move);
            steps++;

            maxTile = Math.max(maxTile, game.getState().getMaxTile());

            // notify step AFTER applying move
            for (GameListener l : listeners) {
                l.onStep(steps, move, game.getState(), game.getScore());
            }
        }

        // notify game over
        for (GameListener l : listeners) {
            l.onGameOver(game.getState(), game.getScore(), steps);
        }

        // timing end
        final long wallEnd = System.nanoTime();
        final long cpuEnd = (cpuStart != -1L)
                ? THREAD_MX_BEAN.getCurrentThreadCpuTime()
                : -1L;

        final long wallNanos = wallEnd - wallStart;
        final long cpuNanos = (cpuStart != -1L) ? (cpuEnd - cpuStart) : -1L;

        return new SessionResult(
                game.getSeed(),
                game.getScore(),
                steps,
                maxTile,
                maxTile >= 2048,
                Map.copyOf(moveCounts),
                wallNanos,
                cpuNanos
        );
    }
}
