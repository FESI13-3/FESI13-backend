package com.fesi.deadlinemate.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OAuthCallbackRequest {

    @Schema(example = "auth-code-from-kakao")
    private String code;

    @Schema(example = "https://chukjibeob.store/callback")
    private String redirectUri;
}
