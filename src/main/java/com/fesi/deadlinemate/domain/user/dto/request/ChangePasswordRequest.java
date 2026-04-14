package com.fesi.deadlinemate.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @Schema(example = "Test1234!")
    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    private String currentPassword;

    @Schema(example = "NewPass5678@")
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,20}$",
            message = "비밀번호는 8~20자의 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String newPassword;
}
