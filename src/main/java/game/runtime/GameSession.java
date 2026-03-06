package game.runtime;

import ai.ExpectimaxPlayer;
import ai.Player;
import game.core.Board;
import game.core.Move;
import game.core.MoveResult;
import game.util.Rng;
import ui.BoardRenderer;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

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

    public SessionResult runGameInteractive(Player player, boolean stepMode, int delayMs, int depth) {

        final long wallStart = System.nanoTime();
        final long cpuStart = (CPU_TIME_SUPPORTED && THREAD_MX_BEAN.isThreadCpuTimeEnabled())
                ? THREAD_MX_BEAN.getCurrentThreadCpuTime()
                : -1L;

        int steps = 0;
        int maxTile;

        initialize();
        maxTile = state.getMaxTile();

        org.jline.terminal.Terminal terminal;
        try {
            terminal = org.jline.terminal.TerminalBuilder.builder()
                    .system(true)
                    .jna(false)
                    .jansi(true)
                    .build();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to create terminal", e);
        }

        org.jline.terminal.Attributes saved = terminal.getAttributes();

        Thread shutdownHook = new Thread(() -> {
            try { terminal.setAttributes(saved); } catch (Exception ignored) {}
            try {
                terminal.puts(org.jline.utils.InfoCmp.Capability.cursor_visible);
                terminal.puts(org.jline.utils.InfoCmp.Capability.exit_ca_mode);
                terminal.puts(org.jline.utils.InfoCmp.Capability.keypad_local);
                terminal.flush();
            } catch (Exception ignored) {}
            try { terminal.close(); } catch (Exception ignored) {}
        });

        Runtime.getRuntime().addShutdownHook(shutdownHook);

        try {
            terminal.enterRawMode();
            terminal.puts(org.jline.utils.InfoCmp.Capability.enter_ca_mode);
            terminal.puts(org.jline.utils.InfoCmp.Capability.cursor_invisible);
            terminal.flush();

            String modeLine = stepMode
                    ? "Mode: STEP  (SPACE/ENTER = next, q = quit)"
                    : ("Mode: AUTO  (minimum delay " + delayMs + " ms)");

            String headerBase = "2048 AI  |  Seed: " + seed + "  |  Depth: " + depth;

            BoardRenderer.render(terminal, state, headerBase, modeLine);

            while (!isGameOver()) {

                if (stepMode) {
                    int ch = readKeyBlocking(terminal);
                    if (ch == 'q' || ch == 'Q') break;
                    if (!(ch == ' ' || ch == '\r' || ch == '\n')) continue;
                } else {
                    if (delayMs > 0) {
                        try {
                            Thread.sleep(delayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    if (terminal.reader().ready()) {
                        int ch = terminal.reader().read();
                        if (ch == 'q' || ch == 'Q') break;
                    }
                }

                long decisionStart = System.nanoTime();
                Move move = player.chooseMove(state);
                long decisionEnd = System.nanoTime();
                double decisionMs = (decisionEnd - decisionStart) / 1_000_000.0;

                if (move == null) break;

                MoveResult result = config.rules().makeMove(state, move);
                boolean changed = !result.board().equals(state);

                state = result.board();
                score += result.scoreGained();

                if (changed) {
                    state = config.spawner().sample(state, rng);
                }

                steps++;
                maxTile = Math.max(maxTile, state.getMaxTile());

                String stats1 = String.format(
                        "Step %d | Move %s | +%d | Score %d | Max %d | AI %.3f ms",
                        steps, moveSymbol(move), result.scoreGained(), score, maxTile, decisionMs
                );

                String stats2 = "";
                if (player instanceof ExpectimaxPlayer ep) {
                    var st = ep.getStats();
                    double hitRate = (st.cacheHits() + st.cacheMisses()) == 0
                            ? 0.0
                            : 100.0 * st.cacheHits() / (st.cacheHits() + st.cacheMisses());

                    stats2 = String.format("Nodes %d | Eval %d | Cache hit %.1f%%",
                            st.nodes(), st.evalCalls(), hitRate);

                    ep.resetStats();
                }

                String headerNow = headerBase + "  |  " + stats1;
                String footerNow = modeLine + (stats2.isBlank() ? "" : ("  |  " + stats2));

                BoardRenderer.render(terminal, state, headerNow, footerNow);
            }

        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException ignored) {}

            try { terminal.setAttributes(saved); } catch (Exception ignored) {}

            try {
                terminal.puts(org.jline.utils.InfoCmp.Capability.cursor_visible);
                terminal.puts(org.jline.utils.InfoCmp.Capability.exit_ca_mode);
                terminal.puts(org.jline.utils.InfoCmp.Capability.keypad_local);
                terminal.flush();
            } catch (Exception ignored) {}

            try { terminal.close(); } catch (Exception ignored) {}
            System.out.println();
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

    private static int readKeyBlocking(org.jline.terminal.Terminal terminal) throws java.io.IOException {
        while (true) {
            int ch = terminal.reader().read();
            if (ch != -1) return ch;
        }
    }

    private static String moveSymbol(Move move) {
        if (move == null) {
            return "?";
        }

        return switch (move) {
            case LEFT -> "←";
            case RIGHT -> "→";
            case UP -> "↑";
            case DOWN -> "↓";
        };
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