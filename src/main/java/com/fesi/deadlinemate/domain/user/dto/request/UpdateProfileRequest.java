package com.fesi.deadlinemate.domain.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 10, message = "닉네임은 2~10자여야 합니다.")
    private String nickname;

    private String profileImage;
}
