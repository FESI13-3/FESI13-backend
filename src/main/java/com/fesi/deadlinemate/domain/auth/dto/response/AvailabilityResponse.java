package com.fesi.deadlinemate.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailabilityResponse {

    private boolean available;

    public static AvailabilityResponse of(boolean available) {
        return new AvailabilityResponse(available);
    }
}
