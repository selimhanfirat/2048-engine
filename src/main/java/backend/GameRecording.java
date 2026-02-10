package backend;

public record GameRecording(
        Meta meta,
        java.util.List<Step> steps
) {
    public record Meta(long seed, int finalScore, int stepCount, int maxTile, boolean reached2048) {}

    public record Step(
            int index,
            String move,
            int score,
            BoardSnapshot board
    ) {}}

