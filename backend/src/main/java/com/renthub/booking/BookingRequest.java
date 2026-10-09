package com.renthub.booking;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BookingRequest(
    @NotNull Long userId,
    @NotBlank String assetType,
    @NotNull Long assetId,
    @NotNull @FutureOrPresent LocalDate startDate,
    @NotNull LocalDate endDate
) {}
