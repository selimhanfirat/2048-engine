package game.runtime;

import ai.Player;
import ai.ExpectimaxPlayer;
import game.core.Board;
import game.core.Move;
import game.core.MoveResult;
import ui.BoardRenderer;
import game.util.Rng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

public final class GameSession {

    private static final ThreadMXBean THREAD_MX_BEAN =
            ManagementFactory.getThreadMXBean();
    private static final boolean CPU_TIME_SUPPORTED =
            THREAD_MX_BEAN.isCurrentThreadCpuTimeSupported();

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

    /* =========================
       Normal batch run (unchanged)
       ========================= */

    public SessionResult runGame(Player player) {
        final long wallStart = System.nanoTime();
        final long cpuStart = (CPU_TIME_SUPPORTED && THREAD_MX_BEAN.isThreadCpuTimeEnabled())
                ? THREAD_MX_BEAN.getCurrentThreadCpuTime()
                : -1L;

        int steps = 0;
        int maxTile;

        initialize();
        maxTile = state.getMaxTile();

        while (!isGameOver()) {
            Move move = player.chooseMove(state);

            step(move);
            steps++;

            maxTile = Math.max(maxTile, state.getMaxTile());
        }

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
                wallNanos,
                cpuNanos
        );
    }

    /* =========================
       Interactive mode
       ========================= */

    public SessionResult runGameInteractive(Player player) {

        final long wallStart = System.nanoTime();
        final long cpuStart = (CPU_TIME_SUPPORTED && THREAD_MX_BEAN.isThreadCpuTimeEnabled())
                ? THREAD_MX_BEAN.getCurrentThreadCpuTime()
                : -1L;

        int steps = 0;
        int maxTile;

        initialize();
        maxTile = state.getMaxTile();

        BufferedReader in =
                new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Interactive mode");
        System.out.println("Seed: " + seed);
        System.out.println("Press ENTER for next move, 'q' + ENTER to quit.\n");

        System.out.println("Initial board:");
        System.out.print(BoardRenderer.render(state));

        while (!isGameOver()) {

            System.out.print("> ");
            String line;
            try {
                line = in.readLine();
            } catch (IOException e) {
                break;
            }

            if (line != null && line.trim().equalsIgnoreCase("q")) {
                break;
            }

            // ---- measure AI decision time ----
            long decisionStart = System.nanoTime();
            Move move = player.chooseMove(state);
            long decisionEnd = System.nanoTime();
            double decisionMs =
                    (decisionEnd - decisionStart) / 1_000_000.0;

            if (move == null) {
                System.out.println("AI returned null move. Stopping.");
                break;
            }


            MoveResult result =
                    config.rules().makeMove(state, move);

            boolean changed =
                    !result.board().equals(state);

            state = result.board();
            score += result.scoreGained();

            if (changed) {
                state = config.spawner().sample(state, rng);
            }

            steps++;
            maxTile = Math.max(maxTile, state.getMaxTile());

            System.out.printf(
                    "Step %d | Move: %s | +%d | Score: %d | Max: %d | AI time: %.3f ms%n",
                    steps,
                    move,
                    result.scoreGained(),
                    score,
                    maxTile,
                    decisionMs
            );

            // Optional: print search stats if Expectimax
            if (player instanceof ExpectimaxPlayer ep) {
                var stats = ep.getStats();

                double hitRate = (stats.cacheHits() + stats.cacheMisses()) == 0
                        ? 0.0
                        : 100.0 * stats.cacheHits()
                        / (stats.cacheHits() + stats.cacheMisses());

                System.out.printf(
                        "   Nodes: %d | Eval: %d | Cache hit: %.1f%%%n",
                        stats.nodes(),
                        stats.evalCalls(),
                        hitRate
                );

                ep.resetStats();
            }

            System.out.print(BoardRenderer.render(state));
        }

        final long wallEnd = System.nanoTime();
        final long cpuEnd = (cpuStart != -1L)
                ? THREAD_MX_BEAN.getCurrentThreadCpuTime()
                : -1L;

        final long wallNanos = wallEnd - wallStart;
        final long cpuNanos =
                (cpuStart != -1L) ? (cpuEnd - cpuStart) : -1L;

        return new SessionResult(
                seed,
                score,
                steps,
                maxTile,
                maxTile >= 2048,
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
        MoveResult result =
                config.rules().makeMove(state, move);
        state = result.board();
        score += result.scoreGained();
        state = config.spawner().sample(state, rng);
    }
}