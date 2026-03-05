package backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.*;

public final class RecordingWriter {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void write(GameRecording rec, Path path) throws Exception {
        Files.createDirectories(path.getParent());
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), rec);
    }
}
