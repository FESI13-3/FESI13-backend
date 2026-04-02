package com.fesi.deadlinemate.domain.gathering.dto.response;

import lombok.Builder;

@Builder
public record MyApplicationStatusResponse(
        String myApplicationStatus
) {
    public static MyApplicationStatusResponse of(String myApplicationStatus) {
        return MyApplicationStatusResponse.builder()
                .myApplicationStatus(myApplicationStatus)
                .build();
    }
}
