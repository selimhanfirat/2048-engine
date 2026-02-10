package app;

import ai.Player;
import backend.GameRecording;
import backend.JsonRecorder;
import backend.RecordingWriter;
import game.runtime.Game;
import game.runtime.GameConfig;
import game.runtime.GameSession;
import game.runtime.SessionResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ExperimentRunner {

    private final GameConfig config;
    private final int runs;
    private final long baseSeed;
    private final Player player;

    public ExperimentRunner(GameConfig config, int runs, long baseSeed, Player player) {
        this.config = config;
        this.runs = runs;
        this.baseSeed = baseSeed;
        this.player = player;
    }

    public List<SessionResult> run() {
        List<SessionResult> results = new ArrayList<>(runs);

        for (int i = 0; i < runs; i++) {
            long seed = baseSeed + i;

            Game game = new Game(config, seed);

            JsonRecorder recorder = new JsonRecorder();
            GameSession session = new GameSession(game, player, List.of(recorder));

            SessionResult res = session.runGame();
            results.add(res);

            GameRecording rec = recorder.build();
            try {
                RecordingWriter.write(rec, Path.of("replays", seed + ".json"));
            } catch (Exception e) {
                throw new RuntimeException("There was an I/O error", e);
            }
        }

        return results;
    }

}
