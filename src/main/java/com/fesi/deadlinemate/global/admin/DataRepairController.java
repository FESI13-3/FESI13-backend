package com.fesi.deadlinemate.global.admin;

import com.fesi.deadlinemate.global.common.ApiResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/data-repair")
@RequiredArgsConstructor
public class DataRepairController {

    private final DataIntegrityService dataIntegrityService;

    @Value("${admin.repair-secret:}")
    private String repairSecret;

    /** 전체 정합성 문제 조회 (삭제 없음) */
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> check(
            @RequestParam("secret") String secret) {
        validateSecret(secret);
        return ResponseEntity.ok(ApiResponse.success(dataIntegrityService.checkAll()));
    }

    /** 전체 정합성 복구 실행 */
    @PostMapping("/repair")
    public ResponseEntity<ApiResponse<Map<String, Object>>> repair(
            @RequestParam("secret") String secret) {
        validateSecret(secret);
        return ResponseEntity.ok(ApiResponse.success(dataIntegrityService.repairAll()));
    }

    private void validateSecret(String secret) {
        if (repairSecret.isBlank() || !repairSecret.equals(secret)) {
            throw new IllegalArgumentException("유효하지 않은 시크릿입니다.");
        }
    }
}
