package com.fesi.deadlinemate.domain.gathering.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fesi.deadlinemate.domain.gathering.command.CreateGatheringCommand;
import com.fesi.deadlinemate.domain.gathering.command.UpdateGatheringCommand;
import com.fesi.deadlinemate.domain.gathering.dto.request.CreateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.request.GatheringSearchCondition;
import com.fesi.deadlinemate.domain.gathering.dto.request.UpdateGatheringRequest;
import com.fesi.deadlinemate.domain.gathering.dto.response.CreateGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringListItemResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.GatheringListResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.MemberListResponse;
import com.fesi.deadlinemate.domain.gathering.dto.response.UpdateGatheringResponse;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringRole;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringStatus;
import com.fesi.deadlinemate.domain.gathering.entity.GatheringType;
import com.fesi.deadlinemate.domain.gathering.service.GatheringQueryService;
import com.fesi.deadlinemate.domain.gathering.service.GatheringService;
import com.fesi.deadlinemate.domain.gathering.service.MembershipCommandService;
import com.fesi.deadlinemate.domain.gathering.service.MembershipQueryService;
import com.fesi.deadlinemate.global.common.ImageStorageService;
import com.fesi.deadlinemate.global.security.JwtTokenProvider;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GatheringControllerTest {
    private static final String APPLICATION_JSON_VALUE = "application/json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private GatheringService gatheringService;

    @MockitoBean
    private GatheringQueryService gatheringQueryService;

    @MockitoBean
    private MembershipCommandService membershipCommandService;

    @MockitoBean
    private MembershipQueryService membershipQueryService;

    @MockitoBean
    private ImageStorageService imageStorageService;

    private String token;
    private CreateGatheringRequest createRequest;
    private UpdateGatheringRequest updateRequest;

    @BeforeEach
    void setUp() {
        token = jwtTokenProvider.generateAccessToken(1L, "user1@test.com");
        createRequest = createGatheringRequest();
        updateRequest = updateGatheringRequest();
    }

    @Nested
    @DisplayName("POST /api/v1/gatherings")
    class Create {

        @Test
        @DisplayName("인증된 사용자는 multipart/form-data로 모임을 생성할 수 있다")
        void create_success() throws Exception {
            given(imageStorageService.uploadAll(any(), eq("gatherings")))
                    .willReturn(List.of("https://image.test/gatherings/1.png"));
            given(gatheringService.create(any(CreateGatheringCommand.class)))
                    .willReturn(createGatheringResponse());

            mockMvc.perform(multipart("/api/v1/gatherings")
                            .file(requestPart(createRequest))
                            .file(imagePart())
                            .header("Authorization", "Bearer " + token)
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(100))
                    .andExpect(jsonPath("$.data.title").value("React 완전 정복 스터디"))
                    .andExpect(jsonPath("$.data.type").value("스터디"));
        }

        @Test
        @DisplayName("인증 없이 모임 생성 시 403을 반환한다")
        void create_withoutAuth_forbidden() throws Exception {
            mockMvc.perform(multipart("/api/v1/gatherings")
                            .file(requestPart(createRequest))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/gatherings")
    class GetGatherings {

        @Test
        @DisplayName("모임 목록을 조회할 수 있다")
        void getGatherings_success() throws Exception {
            given(gatheringQueryService.getGatherings(any(GatheringSearchCondition.class), eq(1), eq(12)))
                    .willReturn(gatheringListResponse());

            mockMvc.perform(get("/api/v1/gatherings")
                            .param("type", "STUDY")
                            .param("categoryIds", "1", "2")
                            .param("sort", "latest")
                            .param("status", "recruiting")
                            .param("query", "React")
                            .param("page", "1")
                            .param("limit", "12"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.gatherings[0].id").value(1))
                    .andExpect(jsonPath("$.data.gatherings[0].title").value("React 스터디"))
                    .andExpect(jsonPath("$.data.totalCount").value(1))
                    .andExpect(jsonPath("$.data.currentPage").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/gatherings/{gatheringId}/members")
    class GetMembers {

        @Test
        @DisplayName("인증된 사용자는 모임 멤버 목록을 조회할 수 있다")
        void getMembers_success() throws Exception {
            given(membershipQueryService.getMembers(1L, 1L))
                    .willReturn(memberListResponse());

            mockMvc.perform(get("/api/v1/gatherings/{gatheringId}/members", 1L)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.members[0].userId").value(1))
                    .andExpect(jsonPath("$.data.members[0].nickname").value("모임장"));
        }

        @Test
        @DisplayName("인증 없이 멤버 목록 조회 시 403을 반환한다")
        void getMembers_withoutAuth_forbidden() throws Exception {
            mockMvc.perform(get("/api/v1/gatherings/{gatheringId}/members", 1L))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/gatherings/{gatheringId}")
    class Update {
        @Test
        @DisplayName("인증된 사용자는 모임을 수정할 수 있다")
        void update_success() throws Exception {
            given(gatheringService.update(eq(1L), any(UpdateGatheringCommand.class)))
                    .willReturn(updateGatheringResponse());

            mockMvc.perform(put("/api/v1/gatherings/{gatheringId}", 1L)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.title").value("수정된 스터디"));
        }
    }

    private MockMultipartFile requestPart(Object request) throws Exception {
        return new MockMultipartFile(
                "request",
                "request",
                APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );
    }

    private MockMultipartFile imagePart() {
        return new MockMultipartFile(
                "images",
                "sample.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image".getBytes(StandardCharsets.UTF_8)
        );
    }

    private CreateGatheringRequest createGatheringRequest() {
        return CreateGatheringRequest.builder()
                .type(GatheringType.STUDY)
                .categoryIds(List.of(1L, 2L))
                .title("React 완전 정복 스터디")
                .shortDescription("리액트 공식문서를 같이 읽어요")
                .description("매주 공식문서 1챕터씩 읽고 블로그를 작성합니다.")
                .goal("React 공식문서 완독")
                .tags(List.of("React", "프론트엔드"))
                .maxMembers(6)
                .recruitDeadline(LocalDate.of(2026, 4, 20))
                .startDate(LocalDate.of(2026, 4, 21))
                .endDate(LocalDate.of(2026, 5, 20))
                .weeklyGuides(List.of(
                        new CreateGatheringRequest.WeeklyGuideRequest(
                                1,
                                "1주차",
                                List.of("공식문서 읽기", "예제 실습")
                        )
                ))
                .build();
    }

    private UpdateGatheringRequest updateGatheringRequest() {
        return UpdateGatheringRequest.builder()
                .type(GatheringType.STUDY)
                .categoryIds(List.of(1L, 2L))
                .title("수정된 스터디")
                .shortDescription("수정된 한 줄 소개")
                .description("수정된 상세 설명입니다.")
                .goal("수정된 목표")
                .tags(List.of("React", "수정"))
                .maxMembers(6)
                .recruitDeadline(LocalDate.of(2026, 4, 20))
                .startDate(LocalDate.of(2026, 4, 21))
                .endDate(LocalDate.of(2026, 5, 30))
                .weeklyGuides(List.of(
                        new UpdateGatheringRequest.WeeklyGuideRequest(
                                1,
                                "1주차 수정",
                                List.of("수정된 계획 1", "수정된 계획 2")
                        )
                ))
                .build();
    }

    private CreateGatheringResponse createGatheringResponse() {
        return CreateGatheringResponse.builder()
                .id(100L)
                .type("스터디")
                .categories(List.of("개발", "웹"))
                .title("React 완전 정복 스터디")
                .shortDescription("리액트 공식문서를 같이 읽어요")
                .description("매주 공식문서 1챕터씩 읽고 블로그를 작성합니다.")
                .tags(List.of("React", "프론트엔드"))
                .goal("React 공식문서 완독")
                .maxMembers(6)
                .currentMembers(1)
                .recruitDeadline(LocalDate.of(2026, 4, 20))
                .startDate(LocalDate.of(2026, 4, 21))
                .endDate(LocalDate.of(2026, 5, 20))
                .totalWeeks(5)
                .status(GatheringStatus.RECRUITING)
                .imageUrls(List.of("https://image.test/gatherings/1.png"))
                .build();
    }

    private GatheringListResponse gatheringListResponse() {
        return GatheringListResponse.builder()
                .gatherings(List.of(
                        GatheringListItemResponse.builder()
                                .id(1L)
                                .type("스터디")
                                .categories(List.of("개발", "웹"))
                                .title("React 스터디")
                                .shortDescription("리액트 같이 공부해요")
                                .tags(List.of("React", "프론트엔드"))
                                .maxMembers(6)
                                .currentMembers(3)
                                .recruitDeadline(LocalDate.of(2026, 4, 20))
                                .startDate(LocalDate.of(2026, 4, 21))
                                .endDate(LocalDate.of(2026, 5, 20))
                                .status(GatheringStatus.RECRUITING)
                                .leader(GatheringListItemResponse.LeaderSummary.builder()
                                        .id(10L)
                                        .nickname("모임장")
                                        .profileImage("https://profile.test/10.png")
                                        .build())
                                .build()
                ))
                .totalCount(1)
                .totalPages(1)
                .currentPage(1)
                .build();
    }

    private MemberListResponse memberListResponse() {
        return MemberListResponse.builder()
                .members(List.of(
                        MemberListResponse.MemberItem.builder()
                                .userId(1L)
                                .nickname("모임장")
                                .profileImage("https://profile.test/1.png")
                                .role(GatheringRole.LEADER)
                                .overallAchievementRate(BigDecimal.valueOf(80.0))
                                .isActive(true)
                                .build()
                ))
                .build();
    }

    private UpdateGatheringResponse updateGatheringResponse() {
        return UpdateGatheringResponse.builder()
                .id(1L)
                .type("스터디")
                .categories(List.of("개발", "웹"))
                .title("수정된 스터디")
                .shortDescription("수정된 한 줄 소개")
                .description("수정된 상세 설명입니다.")
                .goal("수정된 목표")
                .maxMembers(6)
                .currentMembers(3)
                .recruitDeadline(LocalDate.of(2026, 4, 20))
                .startDate(LocalDate.of(2026, 4, 21))
                .endDate(LocalDate.of(2026, 5, 30))
                .totalWeeks(6)
                .status(GatheringStatus.RECRUITING)
                .tags(List.of("React", "수정"))
                .imageUrls(List.of())
                .build();
    }
}