package com.fesi.deadlinemate.domain.gatheringApplication.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fesi.deadlinemate.domain.gatheringApplication.command.CreateApplicationCommand;
import com.fesi.deadlinemate.domain.gatheringApplication.command.UpdateApplicationCommand;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.request.CreateApplicationRequest;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.request.UpdateApplicationRequest;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.response.ApplicationListResponse;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.response.CreateApplicationResponse;
import com.fesi.deadlinemate.domain.gatheringApplication.dto.response.UpdateApplicationResponse;
import com.fesi.deadlinemate.domain.gatheringApplication.entity.ApplicationStatus;
import com.fesi.deadlinemate.domain.gatheringApplication.service.GatheringApplicationService;
import com.fesi.deadlinemate.global.security.JwtTokenProvider;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GatheringApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private GatheringApplicationService gatheringApplicationService;

    private String token;
    private CreateApplicationRequest createRequest;
    private UpdateApplicationRequest updateRequest;

    @BeforeEach
    void setUp() {
        token = jwtTokenProvider.generateAccessToken(1L, "user1@test.com");
        createRequest = createApplicationRequest();
        updateRequest = updateApplicationRequest();
    }

    @Nested
    @DisplayName("POST /api/v1/gatherings/{gatheringId}/applications")
    class Apply {

        @Test
        @DisplayName("인증된 사용자는 모임에 신청할 수 있다")
        void apply_success() throws Exception {
            given(gatheringApplicationService.create(any(CreateApplicationCommand.class)))
                    .willReturn(createApplicationResponse());

            mockMvc.perform(post("/api/v1/gatherings/{gatheringId}/applications", 1L)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(10))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/gatherings/{gatheringId}/applications")
    class GetApplications {

        @Test
        @DisplayName("인증된 사용자는 신청 목록을 조회할 수 있다")
        void getApplications_success() throws Exception {
            given(gatheringApplicationService.getApplications(1L, 1L))
                    .willReturn(applicationListResponse());

            mockMvc.perform(get("/api/v1/gatherings/{gatheringId}/applications", 1L)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.applications[0].id").value(10))
                    .andExpect(jsonPath("$.data.applications[0].applicant.nickname").value("신청자"))
                    .andExpect(jsonPath("$.data.applications[0].status").value("PENDING"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/gatherings/{gatheringId}/applications/{applicationId}")
    class UpdateApplication {

        @Test
        @DisplayName("인증된 사용자는 신청 상태를 변경할 수 있다")
        void updateApplication_success() throws Exception {
            given(gatheringApplicationService.updateApplication(any(UpdateApplicationCommand.class)))
                    .willReturn(updateApplicationResponse());

            mockMvc.perform(patch("/api/v1/gatherings/{gatheringId}/applications/{applicationId}", 1L, 10L)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(10))
                    .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/me/applications")
    class GetMyApplications {

        @Test
        @DisplayName("인증 없이 내 신청 목록 조회 시 403을 반환한다")
        void getMyApplications_withoutAuth_forbidden() throws Exception {
            mockMvc.perform(get("/api/v1/users/me/applications"))
                    .andExpect(status().isForbidden());
        }
    }

    private CreateApplicationRequest createApplicationRequest() {
        return CreateApplicationRequest.builder()
                .personalGoal("React 기초 완벽 이해")
                .selfIntroduction("프론트엔드 3개월차입니다.")
                .build();
    }

    private UpdateApplicationRequest updateApplicationRequest() {
        return UpdateApplicationRequest.builder()
                .status(ApplicationStatus.ACCEPTED)
                .build();
    }

    private CreateApplicationResponse createApplicationResponse() {
        return CreateApplicationResponse.builder()
                .id(10L)
                .status(ApplicationStatus.PENDING)
                .createdAt(LocalDateTime.of(2026, 4, 10, 12, 0))
                .build();
    }

    private UpdateApplicationResponse updateApplicationResponse() {
        return UpdateApplicationResponse.builder()
                .id(10L)
                .status(ApplicationStatus.ACCEPTED)
                .build();
    }

    private ApplicationListResponse applicationListResponse() {
        return ApplicationListResponse.builder()
                .applications(List.of(
                        ApplicationListResponse.ApplicationItemResponse.builder()
                                .id(10L)
                                .applicant(ApplicationListResponse.ApplicantResponse.builder()
                                        .id(2L)
                                        .nickname("신청자")
                                        .profileImage("https://profile.test/2.png")
                                        .reputationScore(BigDecimal.valueOf(36.5))
                                        .reviewSummary(
                                                ApplicationListResponse.ReviewSummaryResponse.builder()
                                                        .reviewCount(3)
                                                        .topTags(List.of("성실해요", "소통이 좋아요"))
                                                        .build()
                                        )
                                        .recentReviews(List.of(
                                                ApplicationListResponse.RecentReviewResponse.builder()
                                                        .id(100L)
                                                        .comment("좋은 팀원이었어요")
                                                        .tags(List.of("성실해요"))
                                                        .build()
                                        ))
                                        .build())
                                .personalGoal("React 기초 완벽 이해")
                                .selfIntroduction("프론트엔드 3개월차입니다.")
                                .status(ApplicationStatus.PENDING)
                                .createdAt(LocalDateTime.of(2026, 4, 10, 12, 0))
                                .build()
                ))
                .build();
    }
}