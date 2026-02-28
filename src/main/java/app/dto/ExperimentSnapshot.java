package app.dto;

import java.util.List;

public record ExperimentSnapshot(
        int measuredDone,
        int measuredTotal,
        long currentSeed,
        List<ExperimentResult> results
) {
}
