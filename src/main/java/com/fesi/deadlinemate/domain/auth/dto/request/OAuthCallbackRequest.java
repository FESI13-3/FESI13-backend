package com.fesi.deadlinemate.domain.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OAuthCallbackRequest {
    private String code;
    private String redirectUri;
}
