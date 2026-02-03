package game.runtime;

import ai.Player;
import game.core.Move;

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

    private final Game game;
    private final Player player;

    public GameSession(Game game, Player player) {
        this.game = game;
        this.player = player;
    }

    public SessionResult runGame() {
        // timing start (doesn't affect logic)
        final long wallStart = System.nanoTime();
        final long cpuStart = (CPU_TIME_SUPPORTED && THREAD_MX_BEAN.isThreadCpuTimeEnabled())
                ? THREAD_MX_BEAN.getCurrentThreadCpuTime()
                : -1L;

        int steps = 0;
        int maxTile = 0;
        Map<Move, Integer> moveCounts = new HashMap<>();

        game.initialize();
        maxTile = game.getState().getMaxTile();

        while (!game.isGameOver()) {
            Move move = player.chooseMove(game.getState());

            moveCounts.merge(move, 1, Integer::sum);

            game.step(move);
            steps++;

            maxTile = Math.max(maxTile, game.getState().getMaxTile());
        }

        // timing end (doesn't affect logic)
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
