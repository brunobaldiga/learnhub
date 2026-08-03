package org.example.learnhub.enrollment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProgressRequest(
        @NotNull
        @Min(0)  @Max(86400)
        Integer lastPositionInSeconds
) {}
