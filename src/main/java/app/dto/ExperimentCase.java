package app.dto;

import ai.Player;

public record ExperimentCase(
        String label,
        ExperimentSpec spec,
        Player player
) {}