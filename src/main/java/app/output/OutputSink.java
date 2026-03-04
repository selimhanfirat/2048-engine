package app.output;

import app.dto.ExperimentSnapshot;

public interface OutputSink {
    void onCheckpoint(ExperimentSnapshot snapshot);
    void onFinal(ExperimentSnapshot snapshot);
}